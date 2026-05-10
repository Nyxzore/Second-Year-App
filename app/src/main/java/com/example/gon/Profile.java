package com.example.gon;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class Profile extends AppCompatActivity {

    private ImageView imgProfile;
    private final int[] profilePhotos = {
            R.drawable.pp0, R.drawable.pp1, R.drawable.pp2,
            R.drawable.pp3, R.drawable.pp4, R.drawable.pp5,
            R.drawable.pp6, R.drawable.pp7, R.drawable.pp8
    };

    @Override
    protected void onStart(){
        super.onStart();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        PreferenceManager.updateNavIcon(this, bottomNav);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        imgProfile = findViewById(R.id.imgProfilePicture);
        TextView txtUsername = findViewById(R.id.txtUsername);

        int currentPicIndex = PreferenceManager.getProfilePic(this);
        imgProfile.setImageResource(profilePhotos[currentPicIndex]);

        String savedUsername = PreferenceManager.getUsername(this);
        if (savedUsername != null) {
            txtUsername.setText(savedUsername);
        }

        imgProfile.setOnClickListener(v -> showProfilePicker());


        Button btnFriends = findViewById(R.id.btnFriends);
        btnFriends.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this,FriendsList.class);
            startActivity(intent);
        });

        // Setup Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setItemIconTintList(null); // Fixes the white/green square issue
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, GoalList.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_friends) {
                Intent intent = new Intent(Profile.this,FriendsList.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });


        //preupdate
        TextView txtCompleted = findViewById(R.id.txtGoalsCompleted);
        TextView txtActive = findViewById(R.id.txtActiveGoals);
        txtCompleted.setText(String.valueOf(PreferenceManager.getCompletedGoalCount(this)));
        txtActive.setText(String.valueOf(PreferenceManager.getActiveGoalCount(this)));
        updateStatsFromServer();
    }

    private void showProfilePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Profile Picture");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_profile_picker, null);
        GridView gridView = dialogView.findViewById(R.id.gridViewProfilePhotos);
        
        AlertDialog dialog = builder.setView(dialogView).create();

        gridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() { return profilePhotos.length; }
            @Override
            public Object getItem(int position) { return profilePhotos[position]; }
            @Override
            public long getItemId(int position) { return position; }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(Profile.this).inflate(R.layout.item_profile_photo, parent, false);
                }
                ImageView img = (ImageView) convertView;
                img.setImageResource(profilePhotos[position]);
                return convertView;
            }
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            imgProfile.setImageResource(profilePhotos[position]);
            PreferenceManager.saveProfilePic(this, position);

            BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
            bottomNav.setSelectedItemId(R.id.nav_profile);
            PreferenceManager.updateNavIcon(this, bottomNav);
            
            updateProfilePicOnServer(position);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void updateProfilePicOnServer(int index) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormBody.Builder()
                        .add("uuid", PreferenceManager.getUUID(this))
                        .add("profile_pic", String.valueOf(index))
                        .add("mode", "update_profile_pic")
                        .build();

                Request request = new Request.Builder()
                        .url(PreferenceManager.HOSTED_SERVER + "mutate_user.php")
                        .post(formBody)
                        .build();

                client.newCall(request).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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

                            PreferenceManager.save_stats(this, completed,active);
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
