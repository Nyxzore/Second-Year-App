package com.example.gon;

import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendGoalsActivity extends AppCompatActivity{
    private GoalAdapter adapter;
    private ArrayList<Goal> friendGoalsList;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //links java code to xml
        setContentView(R.layout.activity_friend_goals);

        Intent intent = getIntent();

        String friendID = intent.getStringExtra("FRIEND_ID");
        String friendName = intent.getStringExtra("FRIEND_NAME");
        //get intent and strings
        if (friendName != null){
            TextView tvTitle = findViewById(R.id.tvFriendGoalsTitle);
            tvTitle.setText(friendName + "'s Goals");
        }

        recyclerView = findViewById(R.id.rvFriendGoals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendGoalsList = new ArrayList<>();
        adapter = new GoalAdapter(friendGoalsList);
        recyclerView.setAdapter(adapter);

        fetchFriendGoals(friendID);
    }

    public void fetchFriendGoals(String fID){
        friendGoalsList.clear();

        Map<String, String> params = new HashMap<>();
        params.put("uuid", fID);

        PreferenceManager.post("get_goals.php", params, responseData ->{
            try{
                JSONObject jsonResponse = new JSONObject(responseData);
                JSONArray goalsArray = jsonResponse.getJSONArray("goals");

                for (int i = 0; i < goalsArray.length(); i++) {
                    JSONObject goal = goalsArray.getJSONObject(i);

                    String title = goal.getString("title");
                    String description = goal.getString("description");
                    String due_date = goal.getString("due_date");
                    String id = goal.getString("id");

                    friendGoalsList.add(new Goal(title, description, due_date, id));
                }
                adapter.notifyDataSetChanged();
                }
            catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
}
