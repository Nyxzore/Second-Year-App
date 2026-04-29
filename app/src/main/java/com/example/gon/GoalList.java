package com.example.gon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GoalList extends AppCompatActivity {

    private GoalAdapter adapter;
    private ArrayList<Goal> myGoals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_list);

        myGoals = new ArrayList<>();
        String userUuid = getIntent().getStringExtra("USER_UUID");
        new Thread(() -> {
            try {
                final String TAG = "GoalTrackerDebug";
                Log.d(TAG, "Attempting to fetch goals for UUID: " + userUuid);
                OkHttpClient client = new OkHttpClient();

                RequestBody formBody = new FormBody.Builder()
                        .add("UUID", userUuid)
                        .build();

                Request request = new Request.Builder()
                        .url("http://192.168.68.131:8000/get_goals.php")
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
                            myGoals.add(new Goal(title, description, due_date));
                        }

                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();


        RecyclerView recyclerView = findViewById(R.id.recyclerViewGoals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GoalAdapter(myGoals);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            //myGoals.add("New Goal #" + (myGoals.size() + 1));
            //adapter.notifyItemInserted(myGoals.size() - 1);
            //recyclerView.scrollToPosition(myGoals.size() - 1);
        });
    }
}