package com.example.gon.ui.activities;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

        // Start by showing normal friends
        friendAdaptor = new FriendAdapter(acceptedFriendsList);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));
        rvFriends.setAdapter(friendAdaptor);

        setup_navigation();

        btnToggleRequests.setOnClickListener(v -> {
            showReq = !showReq;
            TextView title = findViewById(R.id.textViewFriendsTitle);

            if (showReq) {
                title.setText("Requests");
                btnToggleRequests.setText("Show Friends");
                friendAdaptor = new FriendAdapter(pendingRequestsList);
                rvFriends.setAdapter(friendAdaptor);
            } else {
                title.setText("Community");
                btnToggleRequests.setText("Requests");
                friendAdaptor = new FriendAdapter(acceptedFriendsList);
                rvFriends.setAdapter(friendAdaptor);
            }
        });

        btnAddFriend.setOnClickListener(v -> {
            String friendUsername = etFriendUsername.getText().toString().trim();
            if (friendUsername.isEmpty()) {
                Toast.makeText(this, "Enter a username", Toast.LENGTH_SHORT).show();
                return;
            }
            addFriend(friendUsername);
        });

        loadFriends();
        loadFriendRequests();

        if (getIntent().getBooleanExtra("FOCUS_SEARCH", false)) {
            etFriendUsername.post(() -> {
                etFriendUsername.requestFocus();
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etFriendUsername, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }
    }

    private void setup_navigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        PreferenceManager.update_nav_icon(this, bottomNav);
        bottomNav.setItemIconTintList(null);
        bottomNav.setSelectedItemId(R.id.nav_friends);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, GoalList.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_friends) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, Profile.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_habits) {
                startActivity(new Intent(this, HabitList.class));
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.update_nav_icon(this, findViewById(R.id.bottomNavigationView));
        loadFriends();
        loadFriendRequests();

        if (getIntent().getBooleanExtra("FOCUS_SEARCH", false)) {
            etFriendUsername.post(() -> {
                etFriendUsername.requestFocus();
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etFriendUsername, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }
    }

    private void addFriend(String friendUsername) {
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

    public void loadFriends() {
        String currentUserId = PreferenceManager.get_uuid(this);
        Map<String, String> params = new HashMap<>();
        params.put("uuid", currentUserId);
        PreferenceManager.post("get_friends.php", params, responseData -> {
            runOnUiThread(() -> {
                try {
                    acceptedFriendsList.clear();
                    JSONObject json = new JSONObject(responseData);
                    JSONArray array = json.getJSONArray("friends");
                    for (int i = 0; i < array.length(); i++){
                        JSONObject obj = array.getJSONObject(i);
                        acceptedFriendsList.add(new Friend(
                                obj.getString("status"),
                                obj.getString("id"),
                                obj.getString("username"),
                                obj.optInt("profile_pic", 0)
                        ));
                    }
                    if (!showReq) {
                        friendAdaptor.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error loading friends", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    public void loadFriendRequests() {
        String currentUserId = PreferenceManager.get_uuid(this);
        Map<String, String> params = new HashMap<>();
        params.put("uuid", currentUserId);
        PreferenceManager.post("get_friend_requests.php", params, responseData -> {
            runOnUiThread(() -> {
                try {
                    pendingRequestsList.clear();
                    JSONObject json = new JSONObject(responseData);
                    JSONArray array = json.getJSONArray("requests");
                    for (int i = 0; i < array.length(); i++){
                        JSONObject obj = array.getJSONObject(i);
                        pendingRequestsList.add(new Friend(
                                "pending",
                                obj.getString("id"),
                                obj.getString("username"),
                                obj.optInt("profile_pic", 0)
                        ));
                    }
                    if (showReq) {
                        friendAdaptor.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error loading friend requests", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }




}



