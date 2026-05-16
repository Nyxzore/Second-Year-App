package com.example.gon.ui.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;

import com.example.gon.R;
import com.example.gon.Entities.Habit;
import com.example.gon.Entities.Category;
import com.example.gon.ui.adapters.HabitAdapter;
import com.example.gon.ui.helpers.CategoryUiHelper;
import com.example.gon.utils.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HabitList extends AppCompatActivity {
    private ArrayList<Habit> habits;
    private HabitAdapter adapter;
    private final ArrayList<Category> user_categories = new ArrayList<>();
    private String selected_filter_category_id = null;
    private ChipGroup chip_group_habit_filters;
    private int habits_fetch_generation = 0;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.activity_habit_list);
        Log.d("GON_DEBUG : HABIT_LIST", "Activity created");

        habits = new ArrayList<>();
        adapter = new HabitAdapter(habits);

        RecyclerView rv = findViewById(R.id.recyclerViewHabits);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        chip_group_habit_filters = findViewById(R.id.chipGroupHabitFilters);
        findViewById(R.id.btnAddHabitCategory).setOnClickListener(v -> {
            Log.d("GON_DEBUG : HABIT_LIST", "Add category button clicked");
            CategoryUiHelper.show_add_category_simple_dialog(this, this::fetch_categories);
        });

        ItemTouchHelper.SimpleCallback swipe = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder v, @NonNull RecyclerView.ViewHolder t) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recycler_view, @NonNull RecyclerView.ViewHolder view_holder) {
                int pos = view_holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && habits.get(pos).is_completed_today()) {
                    return 0;
                }
                return super.getSwipeDirs(recycler_view, view_holder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                Habit h = habits.get(pos);
                Log.d("GON_DEBUG : HABIT_LIST", "Swiped habit: " + h.get_name());
                if (h.is_completed_today()) {
                    adapter.notifyItemChanged(pos);
                    Toast.makeText(HabitList.this, "Already completed today", Toast.LENGTH_SHORT).show();
                    return;
                }

                h.set_completed_today(true);
                adapter.notifyItemChanged(pos);
                complete_habit_on_server(h.get_id(), pos);
            }
        };
        new ItemTouchHelper(swipe).attachToRecyclerView(rv);

        findViewById(R.id.fabAddHabit).setOnClickListener(v -> {
            Log.d("GON_DEBUG : HABIT_LIST", "Add habit FAB clicked");
            startActivity(new Intent(this, AddEditHabit.class));
        });
        setup_navigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("GON_DEBUG : HABIT_LIST", "Activity started");
        fetch_categories();
        fetch_habits();
    }

    private void bind_filter_chips() {
        CategoryUiHelper.bind_filter_chips(this, chip_group_habit_filters, user_categories,
                selected_filter_category_id, category_id -> {
                    Log.d("GON_DEBUG : HABIT_LIST", "Filter changed to category_id: " + category_id);
                    selected_filter_category_id = category_id;
                    fetch_habits();
                }, this::fetch_categories);
    }

    public void fetch_categories() {
        Log.d("GON_DEBUG : HABIT_LIST", "Fetching categories...");
        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.get_uuid(this));

        PreferenceManager.post("get_categories.php", params, response -> runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(response);
                if (!"success".equals(json.optString("status"))) {
                    Log.d("GON_DEBUG : HABIT_LIST", "Categories fetch failed: " + json.optString("message"));
                    return;
                }
                user_categories.clear();
                user_categories.addAll(Category.list_from_json_array(json.getJSONArray("categories")));
                Log.d("GON_DEBUG : HABIT_LIST", "Categories fetched: " + user_categories.size());
                bind_filter_chips();
            } catch (JSONException e) {
                Log.e("GON", "fetch_categories", e);
            }
        }));
    }

    public void fetch_habits() {
        final int generation = ++habits_fetch_generation;
        Log.d("GON_DEBUG : HABIT_LIST", "Fetching habits (gen " + generation + ")...");
        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.get_uuid(this));
        if (selected_filter_category_id != null) {
            params.put("category_id", selected_filter_category_id);
        }

        PreferenceManager.post("get_habit.php", params, response -> {
            if (generation != habits_fetch_generation) {
                Log.d("GON_DEBUG : HABIT_LIST", "Discarding stale response (gen " + generation + ")");
                return;
            }
            try {
                JSONObject json = new JSONObject(response);
                if (!"success".equals(json.optString("status"))) {
                    Log.d("GON_DEBUG : HABIT_LIST", "Habits fetch failed: " + json.optString("message"));
                    return;
                }

                JSONArray array = json.getJSONArray("habits");
                ArrayList<Habit> new_list = new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Object comp = obj.opt("completed_today");
                    boolean is_comp = false;
                    if (comp instanceof Boolean) is_comp = (Boolean) comp;
                    else if (comp instanceof String) is_comp = ((String) comp).equalsIgnoreCase("t") || comp.equals("1");

                    Habit h = new Habit(obj.getString("name"), obj.optString("description", ""),
                            String.valueOf(obj.get("id")), is_comp);
                    h.set_categories(Category.list_from_item_json(obj));
                    new_list.add(h);
                }

                Log.d("GON_DEBUG : HABIT_LIST", "Habits fetched: " + new_list.size());

                runOnUiThread(() -> {
                    habits.clear();
                    habits.addAll(new_list);
                    adapter.notifyDataSetChanged();
                });
            } catch (JSONException e) {
                Log.e("GON", "Parse error", e);
            }
        });
    }

    private void complete_habit_on_server(String habit_id, int pos) {
        Log.d("GON_DEBUG : HABIT_LIST", "Completing habit id: " + habit_id);
        Map<String, String> params = new HashMap<>();
        params.put("habit_id", habit_id);
        params.put("uuid", PreferenceManager.get_uuid(this));

        PreferenceManager.post("complete_habit.php", params, response -> runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(response);
                String status = json.getString("status");
                Log.d("GON_DEBUG : HABIT_LIST", "Complete habit response: " + status);
                if (status.equals("success")) {
                    MediaPlayer.create(this, R.raw.goal_complete).start();
                    fetch_habits();
                } else {
                    revert_habit(pos);
                }
            } catch (Exception e) {
                revert_habit(pos);
            }
        }));
    }

    private void revert_habit(int pos) {
        Log.d("GON_DEBUG : HABIT_LIST", "Reverting habit at position: " + pos);
        if (pos < habits.size()) {
            habits.get(pos).set_completed_today(false);
            adapter.notifyItemChanged(pos);
            Toast.makeText(this, "Failed to update server", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = item.getGroupId();
        if (position < 0 || position >= habits.size()) return false;

        Habit selected_habit = habits.get(position);
        Log.d("GON_DEBUG : HABIT_LIST", "Context menu item " + item.getItemId() + " for habit " + selected_habit.get_name());

        if (item.getItemId() == 101) {
            Intent intent = new Intent(HabitList.this, AddEditHabit.class);
            intent.putExtra("EDIT_MODE", true);
            intent.putExtra("habit_id", selected_habit.get_id());
            intent.putExtra("name", selected_habit.get_name());
            intent.putExtra("description", selected_habit.get_description());
            intent.putExtra("category_ids", Category.join_ids(selected_habit.get_categories()));
            startActivity(intent);
            return true;
        } else if (item.getItemId() == 102) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Habit")
                    .setMessage("Are you sure you want to delete this habit?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        habits.remove(position);
                        adapter.notifyItemRemoved(position);
                        delete_habit_on_server(selected_habit.get_id());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void delete_habit_on_server(String habit_id) {
        Log.d("GON_DEBUG : HABIT_LIST", "Deleting habit id: " + habit_id);
        Map<String, String> params = new HashMap<>();
        params.put("habit_id", habit_id);
        params.put("mode", "delete");
        params.put("uuid", PreferenceManager.get_uuid(this));

        PreferenceManager.post("mutate_habit.php", params, response_data -> runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(response_data);
                String status = json.getString("status");
                String message = json.optString("message", "");
                Log.d("GON_DEBUG : HABIT_LIST", "Delete habit response: " + status + ". Message: " + message);
                if ("success".equals(status)) {
                    Toast.makeText(HabitList.this, message.isEmpty() ? "Deleted" : message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HabitList.this, "Server: " + message, Toast.LENGTH_LONG).show();
                    fetch_habits();
                }
            } catch (JSONException e) {
                Log.e("GON", "Delete habit parse error", e);
                Toast.makeText(HabitList.this, "Response error", Toast.LENGTH_SHORT).show();
                fetch_habits();
            }
        }));
    }

    private void setup_navigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        PreferenceManager.update_nav_icon(this, nav);
        nav.setItemIconTintList(null);
        nav.setSelectedItemId(R.id.nav_habits);
        nav.setOnItemSelectedListener(item -> {
            Log.d("GON_DEBUG : HABIT_LIST", "Nav item clicked: " + item.getItemId());
            if (item.getItemId() == R.id.nav_home) startActivity(new Intent(this, GoalList.class));
            else if (item.getItemId() == R.id.nav_profile) startActivity(new Intent(this, Profile.class));
            else if (item.getItemId() == R.id.nav_friends) startActivity(new Intent(this, FriendsList.class));
            else if (item.getItemId() == R.id.nav_habits) return true;
            else return true;
            finish();
            return true;
        });
    }
}
