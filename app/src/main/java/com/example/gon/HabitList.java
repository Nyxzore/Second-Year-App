package com.example.gon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HabitList extends AppCompatActivity {

    private ArrayList<Habit> habits;
    private HabitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_list);

        habits = new ArrayList<>();
        adapter = new HabitAdapter(habits);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewHabits);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        registerForContextMenu(recyclerView);

        FloatingActionButton fab = findViewById(R.id.fabAddHabit);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditHabit.class)));

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                Habit h = habits.get(pos);
                if (h.isCompletedToday()) {
                    adapter.notifyItemChanged(pos);
                    Toast.makeText(HabitList.this, "Already completed today", Toast.LENGTH_SHORT).show();
                    return;
                }
                completeHabit(h.getId(), pos);
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);

        setupNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchHabits();
    }

    public void fetchHabits() {
        habits.clear();
        Map<String, String> params = new HashMap<>();
        String uuid = PreferenceManager.getUUID(this);
        Log.d("GON_DEBUG", "Fetching habits for UUID: " + uuid);
        params.put("uuid", uuid);

        PreferenceManager.post("get_habit.php", params, responseData -> {
            try {
                JSONObject jsonResponse = new JSONObject(responseData);
                JSONArray habitArray = jsonResponse.getJSONArray("habits");
                for (int i = 0; i < habitArray.length(); i++) {
                    JSONObject habit = habitArray.getJSONObject(i);
                    String name = habit.getString("name");
                    String id = String.valueOf(habit.get("id")); // int or string from PG
                    String description = habit.optString("description", "");
                    boolean completedToday = habit.optBoolean("completed_today", false);
                    Habit newHabit = new Habit(name, description, id, completedToday);
                    habits.add(newHabit);
                    Log.d("GON_DEBUG", "Fetched habit: " + newHabit.toString());
                }
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e("GON_DEBUG", "fetchHabits JSON", e);
            }
        });
    }

    public void completeHabit(String habitId, int listPosition) {
        Map<String, String> params = new HashMap<>();
        params.put("habit_id", habitId);
        params.put("uuid", PreferenceManager.getUUID(this));

        PreferenceManager.post("complete_habit.php", params, responseData -> runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(responseData);
                String status = json.getString("status");
                String message = json.optString("message", "");

                if ("success".equals(status)) {
                    List<String> affirmations = List.of("Habit Crushed", "1 More Down", "Keep Going", "Completed");
                    String randomAffirmation = affirmations.get(new Random().nextInt(affirmations.size()));
                    Toast.makeText(HabitList.this, randomAffirmation, Toast.LENGTH_LONG).show();
                    fetchHabits();
                } else if ("already_completed_today".equals(status)) {
                    Toast.makeText(HabitList.this, message.isEmpty() ? "Already completed today" : message, Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(listPosition);
                } else {
                    Toast.makeText(HabitList.this, "Server: " + message, Toast.LENGTH_LONG).show();
                    adapter.notifyItemChanged(listPosition);
                }
            } catch (JSONException e) {
                Log.e("GON_DEBUG", "completeHabit JSON", e);
                Toast.makeText(HabitList.this, "Response Error: " + responseData, Toast.LENGTH_LONG).show();
                adapter.notifyItemChanged(listPosition);
            }
        }));
    }

    @Override
    public boolean onContextItemSelected(@NonNull android.view.MenuItem item) {
        int position = item.getGroupId();
        if (position < 0 || position >= habits.size()) return false;
        Habit h = habits.get(position);

        if (item.getItemId() == 101) {
            Intent intent = new Intent(this, AddEditHabit.class);
            intent.putExtra("EDIT_MODE", true);
            intent.putExtra("habit_id", h.getId());
            intent.putExtra("name", h.getName());
            intent.putExtra("description", h.getDescription());
            startActivity(intent);
            return true;
        } else if (item.getItemId() == 102) {
            deleteHabit(h.getId(), position);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void deleteHabit(String habitId, int position) {
        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.getUUID(this));
        params.put("mode", "delete");
        params.put("habit_id", habitId);

        PreferenceManager.post("mutate_habit.php", params, responseData -> runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(responseData);
                if ("success".equals(json.getString("status"))) {
                    habits.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, json.optString("message", "Deleted"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, json.optString("message", "Error"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(this, "Response: " + responseData, Toast.LENGTH_LONG).show();
            }
        }));
    }

    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setItemIconTintList(null);
        nav.setSelectedItemId(R.id.nav_habits);
        PreferenceManager.updateNavIcon(this, nav);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, GoalList.class));
                finish();
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, Profile.class));
                finish();
                return true;
            }
            if (id == R.id.nav_friends) {
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return id == R.id.nav_habits;
        });
    }
}