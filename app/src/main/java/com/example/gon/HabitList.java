package com.example.gon;

import android.content.Intent;
import android.media.MediaPlayer;
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
import java.util.*;

public class HabitList extends AppCompatActivity {
    private ArrayList<Habit> habits;
    private HabitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_list);

        habits = new ArrayList<>();
        adapter = new HabitAdapter(habits);

        RecyclerView rv = findViewById(R.id.recyclerViewHabits);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback swipe = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder v, @NonNull RecyclerView.ViewHolder t) { return false; }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int pos = viewHolder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && habits.get(pos).isCompletedToday()) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                Habit h = habits.get(pos);
                if (h.isCompletedToday()) {
                    adapter.notifyItemChanged(pos); // Snap back
                    Toast.makeText(HabitList.this, "Already completed today", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Optimistic UI: Update locally first
                h.setCompletedToday(true);
                adapter.notifyItemChanged(pos);
                completeHabitOnServer(h.getId(), pos);
            }
        };
        new ItemTouchHelper(swipe).attachToRecyclerView(rv);

        findViewById(R.id.fabAddHabit).setOnClickListener(v -> startActivity(new Intent(this, AddEditHabit.class)));
        setupNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchHabits();
    }

    public void fetchHabits() {
        String uuid = PreferenceManager.getUUID(this);
        Map<String, String> params = new HashMap<>();
        params.put("uuid", uuid);

        PreferenceManager.post("get_habit.php", params, response -> {
            try {
                JSONObject json = new JSONObject(response);
                JSONArray array = json.getJSONArray("habits");
                ArrayList<Habit> newList = new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    // Manual boolean check for Postgres "t"/"f"
                    Object comp = obj.opt("completed_today");
                    boolean isComp = false;
                    if (comp instanceof Boolean) isComp = (Boolean) comp;
                    else if (comp instanceof String) isComp = ((String) comp).equalsIgnoreCase("t") || comp.equals("1");

                    newList.add(new Habit(obj.getString("name"), obj.optString("description", ""),
                            String.valueOf(obj.get("id")), isComp));
                }

                runOnUiThread(() -> {
                    habits.clear();
                    habits.addAll(newList);
                    adapter.notifyDataSetChanged();
                });
            } catch (JSONException e) {
                Log.e("GON", "Parse error", e);
            }
        });
    }

    private void completeHabitOnServer(String habitId, int pos) {
        Map<String, String> params = new HashMap<>();
        params.put("habit_id", habitId);
        params.put("uuid", PreferenceManager.getUUID(this));

        PreferenceManager.post("complete_habit.php", params, response -> {
            runOnUiThread(() -> {
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.getString("status").equals("success")) {
                        MediaPlayer.create(this, R.raw.goal_complete).start();
                        fetchHabits(); // Re-sort and refresh
                    } else {
                        revertHabit(pos);
                    }
                } catch (Exception e) { revertHabit(pos); }
            });
        });
    }

    private void revertHabit(int pos) {
        if (pos < habits.size()) {
            habits.get(pos).setCompletedToday(false);
            adapter.notifyItemChanged(pos);
            Toast.makeText(this, "Failed to update server", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setSelectedItemId(R.id.nav_habits);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) startActivity(new Intent(this, GoalList.class));
            else if (item.getItemId() == R.id.nav_profile) startActivity(new Intent(this, Profile.class));
            else return true;
            finish();
            return true;
        });
    }
}