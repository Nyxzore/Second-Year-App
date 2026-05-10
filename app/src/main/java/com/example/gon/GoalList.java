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
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GoalList extends AppCompatActivity {

    private GoalAdapter adapter;
    private ArrayList<Goal> myGoals;

    @Override
    protected void onStart() {
        super.onStart();
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
        recyclerView.setAdapter(adapter);

        // Swipe left to complete
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Goal selectedGoal = myGoals.get(position);

                myGoals.remove(position);
                adapter.notifyItemRemoved(position);
                complete_goal_post(selectedGoal.getId());

                MediaPlayer mediaPlayer = MediaPlayer.create(GoalList.this, R.raw.goal_complete);
                mediaPlayer.start();
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);

        FloatingActionButton btn_add_goal = findViewById(R.id.fab);
        btn_add_goal.setOnClickListener(view -> {
            Intent intent = new Intent(GoalList.this, AddEditGoal.class);
            startActivity(intent);
        });

        // Setup Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setItemIconTintList(null);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_friends) {
                Intent intent = new Intent(GoalList.this, FriendsList.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(GoalList.this, Profile.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    public void fetchGoalsFromServer() {
        myGoals.clear();
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                RequestBody formBody = new FormBody.Builder()
                        .add("uuid", PreferenceManager.getUUID(this))
                        .build();

                Request request = new Request.Builder()
                        .url(PreferenceManager.HOSTED_SERVER + "get_goals.php")
                        .post(formBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONArray goalsArray = jsonResponse.getJSONArray("goals");

                        for (int i = 0; i < goalsArray.length(); i++) {
                            JSONObject goal = goalsArray.getJSONObject(i);
                            String title = goal.getString("title");
                            String description = goal.getString("description");
                            String due_date = goal.getString("due_date");
                            String id = goal.getString("id");
                            myGoals.add(new Goal(title, description, due_date, id));
                        }

                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public boolean onContextItemSelected(@NonNull android.view.MenuItem item) {
        int position = item.getGroupId();
        Goal selectedGoal = myGoals.get(position);

        if (item.getItemId() == 101) { // Edit
            Intent intent = new Intent(GoalList.this, AddEditGoal.class);
            intent.putExtra("EDIT_MODE", true);
            intent.putExtra("goal_id", selectedGoal.getId());
            intent.putExtra("title", selectedGoal.getTitle());
            intent.putExtra("description", selectedGoal.getDescription());
            intent.putExtra("due_date", selectedGoal.getDueDate());
            startActivity(intent);
            return true;
        } else if (item.getItemId() == 102) { // Delete
            myGoals.remove(position);
            adapter.notifyItemRemoved(position);
            delete_goal_post(selectedGoal.getId());
            return true;
        }
        return super.onContextItemSelected(item);
    }

    public void delete_goal_post(String goal_id) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("goal_id", goal_id)
                    .add("mode", "delete")
                    .build();

            Request request = new Request.Builder()
                    .url(PreferenceManager.HOSTED_SERVER + "mutate_goal.php")
                    .post(formBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                final String responseData = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(responseData);
                        String status = json.getString("status");
                        String message = json.getString("message");
                        Toast.makeText(GoalList.this,
                                status.equals("success") ? message : "Server: " + message,
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("GON_DEBUG", "JSON Error: " + e.getMessage());
                        Toast.makeText(GoalList.this, "Response Error: " + responseData, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                e.printStackTrace();
            }
        }).start();
    }

    public void complete_goal_post(String goal_id) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("goal_id", goal_id)
                    .build();

            Request request = new Request.Builder()
                    .url(PreferenceManager.HOSTED_SERVER + "complete_goal.php")
                    .post(formBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                final String responseData = response.body().string();
                runOnUiThread(() -> {
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
                    } catch (Exception e) {
                        Log.e("GON_DEBUG", "JSON Error: " + e.getMessage());
                        Toast.makeText(GoalList.this, "Response Error: " + responseData, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                e.printStackTrace();
            }
        }).start();
    }
}