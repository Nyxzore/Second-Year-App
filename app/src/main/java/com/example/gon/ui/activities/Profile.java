package com.example.gon.ui.activities;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.gon.R;
import com.example.gon.utils.PreferenceManager;
import com.example.gon.utils.NotificationHelper;
import com.example.gon.utils.ReminderScheduler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private static final int notification_permission_code = 101;
    private ImageView img_profile;
    private final int[] profile_photos = {
            R.drawable.pp0, R.drawable.pp1, R.drawable.pp2,
            R.drawable.pp3, R.drawable.pp4, R.drawable.pp5,
            R.drawable.pp6, R.drawable.pp7, R.drawable.pp8
    };

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("GON_DEBUG : PROFILE", "Activity started");
        BottomNavigationView bottom_nav = findViewById(R.id.bottomNavigationView);
        bottom_nav.setSelectedItemId(R.id.nav_profile);

        PreferenceManager.update_nav_icon(this, bottom_nav);
        update_stats_from_server();
    }

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.activity_profile);
        Log.d("GON_DEBUG : PROFILE", "Activity created");

        img_profile = findViewById(R.id.imgProfilePicture);
        TextView txt_username = findViewById(R.id.txtUsername);

        int current_pic_index = PreferenceManager.get_profile_pic(this);
        img_profile.setImageResource(profile_photos[current_pic_index]);

        String saved_username = PreferenceManager.get_username(this);
        if (saved_username != null) {
            txt_username.setText(saved_username);
        }

        img_profile.setOnClickListener(v -> {
            Log.d("GON_DEBUG : PROFILE", "Profile picture clicked");
            show_profile_picker();
        });

        BottomNavigationView bottom_nav = findViewById(R.id.bottomNavigationView);
        bottom_nav.setItemIconTintList(null);
        bottom_nav.setSelectedItemId(R.id.nav_profile);
        bottom_nav.setOnItemSelectedListener(item -> {
            int item_id = item.getItemId();
            Log.d("GON_DEBUG : PROFILE", "Nav item clicked: " + item_id);
            if (item_id == R.id.nav_home) {
                Intent intent = new Intent(this, GoalList.class);
                startActivity(intent);
                finish();
                return true;
            } else if (item_id == R.id.nav_friends) {
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item_id == R.id.nav_habits) {
                Intent intent = new Intent(this, HabitList.class);
                startActivity(intent);
                finish();
                return true;
            } else if (item_id == R.id.nav_profile) {
                return true;
            }
            return false;
        });

        TextView txt_completed = findViewById(R.id.txtGoalsCompleted);
        TextView txt_active = findViewById(R.id.txtActiveGoals);
        txt_completed.setText(String.valueOf(PreferenceManager.get_completed_goal_count(this)));
        txt_active.setText(String.valueOf(PreferenceManager.get_active_goal_count(this)));

        setup_reminder_ui();
    }

    private void setup_reminder_ui() {
        SwitchCompat switch_reminder = findViewById(R.id.switchReminder);
        TextView txt_reminder_time = findViewById(R.id.txtReminderTime);
        View layout_reminder_time = findViewById(R.id.layoutReminderTime);

        boolean is_enabled = PreferenceManager.is_reminder_enabled(this);
        switch_reminder.setChecked(is_enabled);
        update_reminder_time_text(txt_reminder_time);

        switch_reminder.setOnCheckedChangeListener((button_view, is_checked) -> {
            Log.d("GON_DEBUG : PROFILE", "Reminder switch toggled: " + is_checked);
            if (is_checked) {
                if (check_notification_permission()) {
                    enable_reminders(true);
                } else {
                    switch_reminder.setChecked(false);
                }
            } else {
                enable_reminders(false);
            }
        });

        layout_reminder_time.setOnClickListener(v -> {
            Log.d("GON_DEBUG : PROFILE", "Reminder time layout clicked");
            show_time_picker(txt_reminder_time);
        });
    }

    private void update_reminder_time_text(TextView txt_reminder_time) {
        int hour = PreferenceManager.get_reminder_hour(this);
        int minute = PreferenceManager.get_reminder_minute(this);
        txt_reminder_time.setText(String.format(Locale.getDefault(), getString(R.string.reminder_time_format), hour, minute));
    }

    private void enable_reminders(boolean enable) {
        Log.d("GON_DEBUG : PROFILE", "Enabling reminders: " + enable);
        PreferenceManager.set_reminder_enabled(this, enable);
        if (enable) {
            NotificationHelper.create_notification_channel(this);
            ReminderScheduler.schedule_next_reminder(this);
            Toast.makeText(this, "Reminders enabled", Toast.LENGTH_SHORT).show();
        } else {
            ReminderScheduler.cancel_reminder(this);
            Toast.makeText(this, "Reminders disabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void show_time_picker(TextView txt_reminder_time) {
        int hour = PreferenceManager.get_reminder_hour(this);
        int minute = PreferenceManager.get_reminder_minute(this);

        TimePickerDialog time_picker_dialog = new TimePickerDialog(this, (view, hour_of_day, minute_of_hour) -> {
            Log.d("GON_DEBUG : PROFILE", "Time selected: " + hour_of_day + ":" + minute_of_hour);
            PreferenceManager.save_reminder_time(this, hour_of_day, minute_of_hour);
            update_reminder_time_text(txt_reminder_time);
            if (PreferenceManager.is_reminder_enabled(this)) {
                ReminderScheduler.schedule_next_reminder(this);
            }
        }, hour, minute, true);

        time_picker_dialog.show();
    }

    private boolean check_notification_permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("GON_DEBUG : PROFILE", "Requesting notification permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, notification_permission_code);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int request_code, @NonNull String[] permissions, @NonNull int[] grant_results) {
        super.onRequestPermissionsResult(request_code, permissions, grant_results);
        if (request_code == notification_permission_code) {
            boolean granted = grant_results.length > 0 && grant_results[0] == PackageManager.PERMISSION_GRANTED;
            Log.d("GON_DEBUG : PROFILE", "Permission result: " + granted);
            if (granted) {
                ((SwitchCompat) findViewById(R.id.switchReminder)).setChecked(true);
                enable_reminders(true);
            } else {
                Toast.makeText(this, R.string.permission_rationale, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void show_profile_picker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Profile Picture");

        View dialog_view = LayoutInflater.from(this).inflate(R.layout.dialog_profile_picker, null);
        GridView grid_view = dialog_view.findViewById(R.id.gridViewProfilePhotos);
        
        AlertDialog dialog = builder.setView(dialog_view).create();

        grid_view.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() { return profile_photos.length; }
            @Override
            public Object getItem(int position) { return profile_photos[position]; }
            @Override
            public long getItemId(int position) { return position; }

            @Override
            public View getView(int position, View convert_view, ViewGroup parent) {
                if (convert_view == null) {
                    convert_view = LayoutInflater.from(Profile.this).inflate(R.layout.item_profile_photo, parent, false);
                }
                ImageView img = (ImageView) convert_view;
                img.setImageResource(profile_photos[position]);
                return convert_view;
            }
        });

        grid_view.setOnItemClickListener((parent, view, position, id) -> {
            Log.d("GON_DEBUG : PROFILE", "Selected profile pic index: " + position);
            img_profile.setImageResource(profile_photos[position]);
            PreferenceManager.save_profile_pic(this, position);

            BottomNavigationView bottom_nav = findViewById(R.id.bottomNavigationView);
            bottom_nav.setSelectedItemId(R.id.nav_profile);
            PreferenceManager.update_nav_icon(this, bottom_nav);
            
            update_profile_pic_on_server(position);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void update_profile_pic_on_server(int index) {
        Log.d("GON_DEBUG : PROFILE", "Updating profile pic on server: " + index);
        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.get_uuid(this));
        params.put("profile_pic", String.valueOf(index));
        params.put("mode", "update_profile_pic");

        PreferenceManager.post("mutate_user.php", params, response_data -> {
            Log.d("GON_DEBUG : PROFILE", "Profile pic update server response: " + response_data);
        });
    }

    public void update_stats_from_server() {
        Log.d("GON_DEBUG : PROFILE", "Fetching stats from server...");
        TextView txt_completed = findViewById(R.id.txtGoalsCompleted);
        TextView txt_active = findViewById(R.id.txtActiveGoals);

        Map<String, String> params = new HashMap<>();
        params.put("uuid", PreferenceManager.get_uuid(this));
        PreferenceManager.post("get_stats.php", params, response_data -> {
            try {
                JSONObject json = new JSONObject(response_data);
                int completed = json.getInt("completed_count");
                int active = json.getInt("active_count");
                Log.d("GON_DEBUG : PROFILE", "Stats fetched: completed=" + completed + ", active=" + active);

                runOnUiThread(() -> {
                    txt_completed.setText(String.valueOf(completed));
                    txt_active.setText(String.valueOf(active));
                    PreferenceManager.save_stats(this, completed, active);
                });
            } catch (JSONException e) {
                Log.e("GON_DEBUG : PROFILE", "update_stats_from_server error", e);
            }
        });
    }
}
