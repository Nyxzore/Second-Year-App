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

    final String hosted_server = "https://wmc.ms.wits.ac.za/students/sgroup2689/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_list);

        myGoals = new ArrayList<>(); //Users fetched goals
        String userUuid = getIntent().getStringExtra("USER_UUID"); //UUID from login page
        new Thread(() -> {
            try {
                //Making an https request to fetch goals
                OkHttpClient client = new OkHttpClient();

                RequestBody formBody = new FormBody.Builder()
                        .add("UUID", userUuid)
                        .build();

                Request request = new Request.Builder()
                        .url(hosted_server + "get_goals.php") 
                        .post(formBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        //parse the JSON sent by the php containing rows of goal object related to this user
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONArray goalsArray = jsonResponse.getJSONArray("goals");

                        for (int i = 0; i < goalsArray.length(); i++) {
                            JSONObject goal = goalsArray.getJSONObject(i);
                            String title = goal.getString("title");
                            String description = goal.getString("description");
                            String due_date = goal.getString("due_date");
                            //any other attributes added to Goal object will follow here
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

        adapter = new GoalAdapter(myGoals); //instantiate the GoalAdapter with goals
        recyclerView.setAdapter(adapter); //The reason we coded GoalAdapter fr

        FloatingActionButton btn_add_goal = findViewById(R.id.fab);
        btn_add_goal.setOnClickListener(view -> {
            Intent intent = new Intent(GoalList.this, AddGoal.class); //changing to the AddGoal menu
            intent.putExtra("USER_UUID", userUuid);
            startActivity(intent);
        });
    }
}