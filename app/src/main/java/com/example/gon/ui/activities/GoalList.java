package com.example.gon.ui.activities;

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

import com.example.gon.R;
import com.example.gon.Entities.Goal;
import com.example.gon.Entities.Category;
import com.example.gon.ui.adapters.GoalAdapter;
import com.example.gon.ui.helpers.CategoryUiHelper;
import com.example.gon.utils.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GoalList extends AppCompatActivity {

    private GoalAdapter adapter;
    private ArrayList<Goal> my_goals;
    private final ArrayList<Category> user_categories = new ArrayList<>();
    private String selected_filter_category_id = null;
    private int goals_fetch_generation = 0;

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("GON_DEBUG : GOAL_LIST", "Activity started");
        fetch_categories();
        fetch_goals_from_server();
        BottomNavigationView bottom_nav = findViewById(R.id.bottomNavigationView);
        bottom_nav.setSelectedItemId(R.id.nav_home);
        PreferenceManager.update_nav_icon(this, bottom_nav);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_list);
        my_goals = new ArrayList<>();

        RecyclerView recycler_view = findViewById(R.id.recyclerViewGoals);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GoalAdapter(my_goals);
        adapter.set_header_bind_listener(this::bind_goal_list_header);
        recycler_view.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback swipe_callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recycler_view, @NonNull RecyclerView.ViewHolder view_holder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recycler_view, @NonNull RecyclerView.ViewHolder view_holder) {
                int position = view_holder.getBindingAdapterPosition();
                if (position == 0 || position > my_goals.size()) return 0;
                return super.getSwipeDirs(recycler_view, view_holder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder view_holder, int direction) {
                int position = view_holder.getBindingAdapterPosition();
                Goal selected_goal = my_goals.get(position - 1);

                Log.d("GON_DEBUG : GOAL_LIST", "Swiped goal: " + selected_goal.get_title());

                my_goals.remove(position - 1);
                adapter.notifyItemRemoved(position);
                complete_goal_post(selected_goal.get_id());

                MediaPlayer media_player = MediaPlayer.create(GoalList.this, R.raw.goal_complete);
                media_player.start();

                update_header_stats();
            }
        };
        new ItemTouchHelper(swipe_callback).attachToRecyclerView(recycler_view);

        FloatingActionButton btn_add_goal = findViewById(R.id.fab);
        btn_add_goal.setOnClickListener(view -> {
            Log.d("GON_DEBUG : GOAL_LIST", "Add goal FAB clicked");
            Intent intent = new Intent(GoalList.this, AddEditGoal.class);
            startActivity(intent);
        });

        BottomNavigationView bottom_nav = findViewById(R.id.bottomNavigationView);
        bottom_nav.setItemIconTintList(null);
        bottom_nav.setSelectedItemId(R.id.nav_home);
        bottom_nav.setOnItemSelectedListener(item -> {
            int item_id = item.getItemId();
            Log.d("GON_DEBUG : GOAL_LIST", "Nav item clicked: " + item_id);
            if (item_id == R.id.nav_home) {
                return true;
            } else if (item_id == R.id.nav_friends) {
                Toast.makeText(this, "friends", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(GoalList.this, FriendsList.class);
                startActivity(intent);
                return true;
            } else if (item_id == R.id.nav_profile) {
                startActivity(new Intent(GoalList.this, Profile.class));
                return true;
            } else if (item_id == R.id.nav_habits) {
                startActivity(new Intent(GoalList.this, HabitList.class));
                return true;
            }
            return false;
        });
    }

    private void bind_goal_list_header(GoalAdapter.HeaderViewHolder header) {
        header.lblActiveGoals.setText(String.valueOf(my_goals.size()));
        header.txtUserName.setText(PreferenceManager.get_username(this) + " 🌱");
        header.btnAddCategory.setOnClickListener(view -> {
            Log.d("GON_DEBUG : GOAL_LIST", "Header add category clicked");
            CategoryUiHelper.show_add_category_simple_dialog(this, this::fetch_categories);
        });
        bind_filter_chips(header);
    }

    private void bind_filter_chips(GoalAdapter.HeaderViewHolder header) {
        CategoryUiHelper.bind_filter_chips(this, header.chipGroupGoalFilters, user_categories,
                selected_filter_category_id, category_id -> {
                    Log.d("GON_DEBUG : GOAL_LIST", "Filter changed to category_id: " + category_id);
                    selected_filter_category_id = category_id;
                    fetch_goals_from_server();
                }, this::fetch_categories);
    }

    private void update_header_stats() {
        GoalAdapter.HeaderViewHolder header = adapter.get_header_view_holder();
        if (header != null) {
            bind_goal_list_header(header);
        }
    }

    public void fetch_categories() {
        Log.d("GON_DEBUG : GOAL_LIST", "Fetching categories...");
        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.get_uuid(this));

        PreferenceManager.post("get_categories.php", params, response_data -> {
            try {
                JSONObject json_response = new JSONObject(response_data);
                if (!"success".equals(json_response.optString("status"))) {
                    Log.d("GON_DEBUG : GOAL_LIST", "Categories fetch failed: " + json_response.optString("message"));
                    return;
                }
                user_categories.clear();
                user_categories.addAll(Category.list_from_json_array(json_response.getJSONArray("categories")));
                Log.d("GON_DEBUG : GOAL_LIST", "Categories fetched: " + user_categories.size());
                runOnUiThread(this::update_header_stats);
            } catch (JSONException e) {
                Log.e("GON_DEBUG : GOAL_LIST", "fetch_categories error", e);
            }
        });
    }

    public void fetch_goals_from_server() {
        final int generation = ++goals_fetch_generation;
        Log.d("GON_DEBUG : GOAL_LIST", "Fetching goals (gen " + generation + ")...");
        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.get_uuid(this));
        if (selected_filter_category_id != null) {
            params.put("category_id", selected_filter_category_id);
        }

        PreferenceManager.post("get_goals.php", params, response_data -> {
            if (generation != goals_fetch_generation) {
                Log.d("GON_DEBUG : GOAL_LIST", "Discarding stale response (gen " + generation + ")");
                return;
            }
            try {
                JSONObject json_response = new JSONObject(response_data);
                if (!"success".equals(json_response.optString("status"))) {
                    Log.d("GON_DEBUG : GOAL_LIST", "Goals fetch failed: " + json_response.optString("message"));
                    return;
                }

                JSONArray goals_array = json_response.getJSONArray("goals");
                ArrayList<Goal> new_goals = new ArrayList<>();

                for (int i = 0; i < goals_array.length(); i++) {
                    JSONObject goal = goals_array.getJSONObject(i);
                    Goal g = new Goal(
                            goal.getString("title"),
                            goal.optString("description", ""),
                            goal.getString("due_date"),
                            String.valueOf(goal.get("id"))
                    );
                    g.set_categories(Category.list_from_item_json(goal));
                    new_goals.add(g);
                }

                Log.d("GON_DEBUG : GOAL_LIST", "Goals fetched: " + new_goals.size());

                runOnUiThread(() -> {
                    my_goals.clear();
                    my_goals.addAll(new_goals);
                    adapter.notifyDataSetChanged();
                    update_header_stats();
                });
            } catch (JSONException e) {
                Log.e("GON_DEBUG : GOAL_LIST", "fetch_goals error", e);
            }
        });
    }

    @Override
    public boolean onContextItemSelected(@NonNull android.view.MenuItem item) {
        int position = item.getGroupId();
        if (position == 0 || position > my_goals.size()) return false;

        Goal selected_goal = my_goals.get(position - 1);
        Log.d("GON_DEBUG : GOAL_LIST", "Context menu item " + item.getItemId() + " for goal " + selected_goal.get_title());

        if (item.getItemId() == 101) {
            Intent intent = new Intent(GoalList.this, AddEditGoal.class);
            intent.putExtra("EDIT_MODE", true);
            intent.putExtra("goal_id", selected_goal.get_id());
            intent.putExtra("title", selected_goal.get_title());
            intent.putExtra("description", selected_goal.get_description());
            intent.putExtra("due_date", selected_goal.get_due_date());
            intent.putExtra("category_ids", Category.join_ids(selected_goal.get_categories()));
            startActivity(intent);
            return true;
        } else if (item.getItemId() == 102) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Goal")
                    .setMessage("Are you sure you want to delete this goal?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        my_goals.remove(position - 1);
                        adapter.notifyItemRemoved(position);
                        delete_goal_post(selected_goal.get_id());
                        update_header_stats();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    public void delete_goal_post(String goal_id) {
        Log.d("GON_DEBUG : GOAL_LIST", "Deleting goal id: " + goal_id);
        Map<String, String> params = new HashMap<>();
        params.put("goal_id", goal_id);
        params.put("mode", "delete");
        params.put("uuid", PreferenceManager.get_uuid(this));

        PreferenceManager.post("mutate_goal.php", params, response_data -> {
            try {
                JSONObject json = new JSONObject(response_data);
                String status = json.getString("status");
                String message = json.getString("message");
                Log.d("GON_DEBUG : GOAL_LIST", "Delete goal response: " + status + ". Message: " + message);
                Toast.makeText(GoalList.this,
                        status.equals("success") ? message : "Server: " + message,
                        Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                Log.e("GON_DEBUG : GOAL_LIST", "JSON Error: " + e.getMessage());
            }
        });
    }

    public void complete_goal_post(String goal_id) {
        Log.d("GON_DEBUG : GOAL_LIST", "Completing goal id: " + goal_id);
        Map<String, String> params = new HashMap<>();
        params.put("goal_id", goal_id);

        PreferenceManager.post("complete_goal.php", params, response_data -> {
            try {
                JSONObject json = new JSONObject(response_data);
                String status = json.getString("status");
                String message = json.getString("message");
                Log.d("GON_DEBUG : GOAL_LIST", "Complete goal response: " + status + ". Message: " + message);

                if (status.equals("success")) {
                    List<String> affirmations = List.of("Goal Crushed", "1 More Down", "Keep Going", "Completed");
                    String random_affirmation = affirmations.get(new Random().nextInt(affirmations.size()));
                    Toast.makeText(GoalList.this, random_affirmation, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(GoalList.this, "Server: " + message, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Log.e("GON_DEBUG : GOAL_LIST", "JSON Error: " + e.getMessage());
            }
        });
    }
}
