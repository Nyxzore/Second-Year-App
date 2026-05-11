package com.example.gon;
import android.content.Intent;
import androidx.core.graphics.Insets;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.*;
public class FriendsList extends AppCompatActivity {

private RecyclerView rvFriends;
private FriendAdaptor friendAdaptor;
private ArrayList<Friend> friendsList;


@Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_friends2);

    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        return insets;
    });

    rvFriends = findViewById(R.id.rvFriends);

    friendsList = new ArrayList<>();


    friendsList.add(new Friend("1", "RayTest"));
    friendsList.add(new Friend("2", "DamoTest"));
    friendsList.add(new Friend("3", "NicksonTest"));

    friendAdaptor = new FriendAdaptor(friendsList);

    rvFriends.setLayoutManager(new LinearLayoutManager(this));
    rvFriends.setAdapter(friendAdaptor);
}

    @Override
    protected void onStart() {
        super.onStart();



        // Setup Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        PreferenceManager.updateNavIcon(this, bottomNav);
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
            }
            return false;
        });
    }

}



