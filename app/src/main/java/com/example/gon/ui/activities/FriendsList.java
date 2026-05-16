package com.example.gon.ui.activities;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gon.Entities.Friend;
import com.example.gon.R;
import com.example.gon.ui.adapters.FriendAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.gon.utils.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
public class FriendsList extends AppCompatActivity {
private boolean showReq = false;                //FOR MODE OF RECYCLER VIEW STARTS WITH ALL FRIENDS
private RecyclerView rvFriends;
private FriendAdapter friendAdaptor;
private ArrayList<Friend> friendsList;   //general friend list

private ArrayList<Friend> pendingRequestsList;   //pending requests

private ArrayList<Friend> acceptedFriendsList;  //accepted req i.e actuall friends  could probably do more efficient
private Button btnToggleRequests;

private EditText etFriendUsername;
     Button btnAddFriend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends2);

        rvFriends = findViewById(R.id.rvFriends);
    btnToggleRequests = findViewById(R.id.btnToggleRequests);
    etFriendUsername = findViewById(R.id.etFriendUsername);
    btnAddFriend = findViewById(R.id.btnAddFriend);
    friendsList = new ArrayList<>();
    acceptedFriendsList = new ArrayList<>();
    pendingRequestsList = new ArrayList<>();


    friendAdaptor = new FriendAdapter(friendsList);

    rvFriends.setLayoutManager(new LinearLayoutManager(this));
    rvFriends.setAdapter(friendAdaptor);


    // Fake test data
    //friendsList.add(new Friend("Accepted", "1", "RayTest"));
    //friendsList.add(new Friend("Pending", "2", "DamoTest"));
    //friendsList.add(new Friend("Pending", "3", "NicksonTest"));

    // Split into accepted friends and pending requests
    /*for (Friend friend : friendsList) {
        if (friend.getStatus().equalsIgnoreCase("Accepted")) {
            acceptedFriendsList.add(friend);
        } else if (friend.getStatus().equalsIgnoreCase("Pending")) {
            pendingRequestsList.add(friend);
        }
    }*/

    // Start by showing normal friends
    friendAdaptor = new FriendAdapter(acceptedFriendsList);

    rvFriends.setLayoutManager(new LinearLayoutManager(this));
    rvFriends.setAdapter(friendAdaptor);

    btnToggleRequests.setOnClickListener(v -> {
        showReq = !showReq;

        if (showReq) {
            // Show friend requests

            btnToggleRequests.setText("Friends");

            friendAdaptor = new FriendAdapter(pendingRequestsList);
            rvFriends.setAdapter(friendAdaptor);

        } else {
            // Show normal friends

            btnToggleRequests.setText("Requests");

            friendAdaptor = new FriendAdapter(acceptedFriendsList);
            rvFriends.setAdapter(friendAdaptor);
        }
    });

    btnAddFriend.setOnClickListener(v -> { //MAKING FRIENDS
        String friendUsername = etFriendUsername.getText().toString().trim();

        if (friendUsername.isEmpty()) {
            Toast.makeText(this, "Enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        addFriend(friendUsername);
    });

    loadFriends();
    loadFriendRequests();
}


    private void addFriend(String friendUsername) {   //FUNCTION TO ADD A FRIEND  TO DO
        String currentUserId = PreferenceManager.get_uuid(this);

        Map<String, String> params = new HashMap<>();
        params.put("uuid", currentUserId);
        params.put("friend_username", friendUsername);

        PreferenceManager.post("add_friend.php", params, responseData -> {
            runOnUiThread(() -> {
                Toast.makeText(this, responseData, Toast.LENGTH_LONG).show();
                etFriendUsername.setText("");
            });
        });
    }

    private void loadFriends() {   // FUNCTION TO LOAD ACCEPTED FRIENDS - TO DO
        String currentUserId = PreferenceManager.get_uuid(this);

        Map<String, String> params = new HashMap<>();
        params.put("uuid", currentUserId);

        PreferenceManager.post("get_friends.php", params, responseData -> {
            runOnUiThread(() -> {
                try {
                    acceptedFriendsList.clear();

                    // Parse JSON response from get_friends.php
                    JSONObject json = new JSONObject(responseData);
                    JSONArray array = json.getJSONArray("friends");

                    for (int i = 0; i < array.length(); i++){
                        JSONObject obj = array.getJSONObject(i);
                        acceptedFriendsList.add(new Friend(
                                obj.getString("status"),
                                obj.getString("id"),
                                obj.getString("username")
                        ));
                    }
                    // After adding friends:
                    if (!showReq) {
                        friendAdaptor.notifyDataSetChanged();
                    }

                } catch (Exception e) {
                    Toast.makeText(this, "Error loading friends", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadFriendRequests() {   // FUNCTION TO LOAD FRIEND REQUESTS - TO DO
        String currentUserId = PreferenceManager.get_uuid(this);

        Map<String, String> params = new HashMap<>();
        params.put("uuid", currentUserId);

        PreferenceManager.post("get_friend_requests.php", params, responseData -> {
            runOnUiThread(() -> {
                try {
                    pendingRequestsList.clear();

                    // Parse JSON response from get_friend_requests.php
                    JSONObject json = new JSONObject(responseData);
                    JSONArray array = json.getJSONArray("requests");

                    for (int i = 0; i < array.length(); i++){
                        JSONObject obj = array.getJSONObject(i);
                        pendingRequestsList.add(new Friend(
                                "pending",
                                obj.getString("id"),
                                obj.getString("username")
                        ));
                    }
                    // After adding pending requests:
                    if (showReq) {
                        friendAdaptor.notifyDataSetChanged();
                    }

                } catch (Exception e) {
                    Toast.makeText(this, "Error loading friend requests", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();



        // Setup Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        PreferenceManager.update_nav_icon(this, bottomNav);
        bottomNav.setItemIconTintList(null);
        bottomNav.setSelectedItemId(R.id.nav_friends);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(FriendsList.this, GoalList.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_friends) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(FriendsList.this, Profile.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_habits) {
                Intent intent = new Intent(FriendsList.this, HabitList.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }




}



