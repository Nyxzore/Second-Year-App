package com.example.gon;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private ArrayList<Goal> myGoals;
    private final ArrayList<Category> userCategories = new ArrayList<>();
    private String selectedFilterCategoryId = null;
    private int goalsFetchGeneration = 0;

    @Override
    protected void onStart() {
        super.onStart();
        fetchCategories();
        fetchGoalsFromServer();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_home);
        PreferenceManager.updateNavIcon(this, bottomNav);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_list);
        myGoals = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewGoals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GoalAdapter(myGoals);
        adapter.setHeaderBindListener(this::bindGoalListHeader);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == 0 || position > myGoals.size()) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Goal selectedGoal = myGoals.get(position - 1);

                myGoals.remove(position - 1);
                adapter.notifyItemRemoved(position);
                complete_goal_post(selectedGoal.getId());

                MediaPlayer mediaPlayer = MediaPlayer.create(GoalList.this, R.raw.goal_complete);
                mediaPlayer.start();

                updateHeaderStats();
                Log.e("GON_DEBUG : goal_list", "goal completed post succesful");
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);

        FloatingActionButton btn_add_goal = findViewById(R.id.fab);
        btn_add_goal.setOnClickListener(view -> {
            Intent intent = new Intent(GoalList.this, AddEditGoal.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setItemIconTintList(null);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_friends) {
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(GoalList.this, Profile.class));
                return true;
            } else if (itemId == R.id.nav_habits) {
                startActivity(new Intent(GoalList.this, HabitList.class));
                return true;
            }
            return false;
        });
    }

    private void bindGoalListHeader(GoalAdapter.HeaderViewHolder header) {
        header.lblActiveGoals.setText(String.valueOf(myGoals.size()));
        header.txtUserName.setText(PreferenceManager.getUsername(this) + " 🌱");
        header.btnAddCategory.setOnClickListener(view -> CategoryUiHelper.showAddCategoryDialog(this, newCategory -> fetchCategories()));
        bindFilterChips(header);
    }

    private void bindFilterChips(GoalAdapter.HeaderViewHolder header) {
        CategoryUiHelper.bindFilterChips(this, header.chipGroupGoalFilters, userCategories,
                selectedFilterCategoryId, categoryId -> {
                    selectedFilterCategoryId = categoryId;
                    fetchGoalsFromServer();
                });
    }

    private void updateHeaderStats() {
        GoalAdapter.HeaderViewHolder header = adapter.getHeaderViewHolder();
        if (header != null) {
            bindGoalListHeader(header);
        }
    }

    public void fetchCategories() {
        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.getUUID(this));

        PreferenceManager.post("get_categories.php", params, responseData -> {
            try {
                JSONObject jsonResponse = new JSONObject(responseData);
                if (!"success".equals(jsonResponse.optString("status"))) {
                    return;
                }
                userCategories.clear();
                userCategories.addAll(Category.listFromJsonArray(jsonResponse.getJSONArray("categories")));
                runOnUiThread(this::updateHeaderStats);
            } catch (JSONException e) {
                Log.e("GON_DEBUG", "fetchCategories", e);
            }
        });
    }

    public void fetchGoalsFromServer() {
        final int generation = ++goalsFetchGeneration;
        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.getUUID(this));
        if (selectedFilterCategoryId != null) {
            params.put("category_id", selectedFilterCategoryId);
        }

        PreferenceManager.post("get_goals.php", params, responseData -> {
            if (generation != goalsFetchGeneration) return;
            try {
                JSONObject jsonResponse = new JSONObject(responseData);
                if (!"success".equals(jsonResponse.optString("status"))) return;

                JSONArray goalsArray = jsonResponse.getJSONArray("goals");
                ArrayList<Goal> newGoals = new ArrayList<>();

                for (int i = 0; i < goalsArray.length(); i++) {
                    JSONObject goal = goalsArray.getJSONObject(i);
                    Goal g = new Goal(
                            goal.getString("title"),
                            goal.optString("description", ""),
                            goal.getString("due_date"),
                            String.valueOf(goal.get("id"))
                    );
                    if (goal.has("categories")) {
                        g.setCategories(Category.listFromJsonArray(goal.getJSONArray("categories")));
                    }
                    newGoals.add(g);
                }

                runOnUiThread(() -> {
                    myGoals.clear();
                    myGoals.addAll(newGoals);
                    adapter.notifyDataSetChanged();
                    updateHeaderStats();
                });
            } catch (JSONException e) {
                Log.e("GON_DEBUG", "fetchGoals", e);
            }
        });
    }

    public void delete_goal_post(String goal_id) {
        Map<String, String> params = new HashMap<>();
        params.put("goal_id", goal_id);
        params.put("mode", "delete");
        params.put("uuid", PreferenceManager.getUUID(this));

        PreferenceManager.post("mutate_goal.php", params, responseData -> {
            try {
                JSONObject json = new JSONObject(responseData);
                String status = json.getString("status");
                String message = json.getString("message");
                Toast.makeText(GoalList.this,
                        status.equals("success") ? message : "Server: " + message,
                        Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                Log.e("GON_DEBUG", "JSON Error: " + e.getMessage());
                Toast.makeText(GoalList.this, "Response Error: " + responseData, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void complete_goal_post(String goal_id) {
        Map<String, String> params = new HashMap<>();
        params.put("goal_id", goal_id);

        PreferenceManager.post("complete_goal.php", params, responseData -> {
            try {
                JSONObject json = new JSONObject(responseData);
                String status = json.getString("status");
                String message = json.getString("message");

                if (status.equals("success")) {
                    List<String> affirmations = List.of("Goal Crushed", "1 More Down", "Keep Going", "Completed");
                    String randomAffirmation = affirmations.get(new Random().nextInt(affirmations.size()));
                    Toast.makeText(GoalList.this, randomAffirmation, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(GoalList.this, "Server: " + message, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Log.e("GON_DEBUG", "JSON Error: " + e.getMessage());
                Toast.makeText(GoalList.this, "Response Error: " + responseData, Toast.LENGTH_LONG).show();
            }
        });
    }
}
