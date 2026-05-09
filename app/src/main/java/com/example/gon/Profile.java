package com.example.gon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import com.example.gon.GoalList;
public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        TextView txtUsername = findViewById(R.id.txtUsername);
        String savedUsername = PreferenceManager.getUsername(this);
        if (savedUsername != null) {
            txtUsername.setText(savedUsername);
        }

        // Setup Friends button
        Button btnFriends = findViewById(R.id.btnFriends);
        btnFriends.setOnClickListener(v -> {
            // Placeholder for Friends list navigation
            Toast.makeText(this, "Friends List coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Setup Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, GoalList.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_friends) {
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    public void updateStatsFromServer() {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                RequestBody formBody = new FormBody.Builder()
                        .add("uuid", PreferenceManager.getUUID(this))
                        .build();

                Request request = new Request.Builder()
                        .url(PreferenceManager.HOSTED_SERVER + "get_stats.php")
                        .post(formBody)
                        .build();

                try (okhttp3.Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        org.json.JSONObject json = new org.json.JSONObject(responseData);

                        int completed = json.getInt("completed_count");
                        int active = json.getInt("active_count");

                        runOnUiThread(() -> {
                            TextView txtCompleted = findViewById(R.id.txtGoalsCompleted);
                            TextView txtActive = findViewById(R.id.txtActiveGoals);
                            txtCompleted.setText(String.valueOf(completed));
                            txtActive.setText(String.valueOf(active));
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
