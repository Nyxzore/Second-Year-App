package com.example.gon.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.gon.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PreferenceManager {

    public static final String hosted_server = "https://wmc.ms.wits.ac.za/students/sgroup2689/";
    private static final String pref_name = "my_prefs";
    private static final String key_uuid = "uuid";
    private static final String key_hash = "hash";
    private static final String key_username = "username";
    private static final String key_profile_pic = "profile_pic";

    private static final String key_completed_count = "completed_goal_count";
    private static final String key_active_count = "active_goal_count";

    private static final String key_reminder_enabled = "reminder_enabled";
    private static final String key_reminder_hour = "reminder_hour";
    private static final String key_reminder_minute = "reminder_minute";

    public static void save_uuid(Context context, String uuid) {
        SharedPreferences prefs = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        prefs.edit().putString(key_uuid, uuid).apply();
    }

    public static String get_uuid(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        return prefs.getString(key_uuid, null);
    }

    public static void save_profile_pic(Context context, int index) {
        SharedPreferences prefs = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        prefs.edit().putInt(key_profile_pic, index).apply();
    }

    public static int get_profile_pic(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        return prefs.getInt(key_profile_pic, 0);
    }

    public static void save_hash(Context context, String hash) {
        SharedPreferences prefs = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        prefs.edit().putString(key_hash, hash).apply();
    }

    public static String get_hash_string(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        return prefs.getString(key_hash, null);
    }

    public static void save_username(Context context, String username) {
        SharedPreferences prefs = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        prefs.edit().putString(key_username, username).apply();
    }

    public static String get_username(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        return prefs.getString(key_username, null);
    }

    public static int get_completed_goal_count(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        return prefs.getInt(key_completed_count, 0);
    }

    public static int get_active_goal_count(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        return prefs.getInt(key_active_count, 0);
    }

    public static void save_stats(Context context, int completed, int active) {
        SharedPreferences prefs = context.getSharedPreferences(pref_name, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(key_active_count, active)
                .putInt(key_completed_count, completed)
                .apply();
    }

    public static void set_reminder_enabled(Context context, boolean enabled) {
        context.getSharedPreferences(pref_name, Context.MODE_PRIVATE)
                .edit().putBoolean(key_reminder_enabled, enabled).apply();
    }

    public static boolean is_reminder_enabled(Context context) {
        return context.getSharedPreferences(pref_name, Context.MODE_PRIVATE)
                .getBoolean(key_reminder_enabled, false);
    }

    public static void save_reminder_time(Context context, int hour, int minute) {
        context.getSharedPreferences(pref_name, Context.MODE_PRIVATE)
                .edit()
                .putInt(key_reminder_hour, hour)
                .putInt(key_reminder_minute, minute)
                .apply();
    }

    public static int get_reminder_hour(Context context) {
        return context.getSharedPreferences(pref_name, Context.MODE_PRIVATE)
                .getInt(key_reminder_hour, 20);
    }

    public static int get_reminder_minute(Context context) {
        return context.getSharedPreferences(pref_name, Context.MODE_PRIVATE)
                .getInt(key_reminder_minute, 0);
    }

    public static final int[] profile_photos = {
            R.drawable.pp0, R.drawable.pp1, R.drawable.pp2,
            R.drawable.pp3, R.drawable.pp4, R.drawable.pp5,
            R.drawable.pp6, R.drawable.pp7, R.drawable.pp8
    };

    public static void update_nav_icon(Context context, BottomNavigationView nav_view) {
        int current_pic_index = get_profile_pic(context);
        nav_view.getMenu().findItem(R.id.nav_profile).setIcon(profile_photos[current_pic_index]);
    }

    public interface NetworkCallback {
        void on_response(String response);
    }

    public static void post(String php_file, Map<String, String> params, NetworkCallback callback) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                FormBody.Builder builder = new FormBody.Builder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    builder.add(entry.getKey(), entry.getValue());
                }

                Request request = new Request.Builder()
                        .url(hosted_server + php_file)
                        .post(builder.build())
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String response_data = response.body().string();
                        new Handler(Looper.getMainLooper()).post(() -> callback.on_response(response_data));
                    } else {
                        Log.e("GON_DEBUG", "HTTP ERROR:" + response.code() + " for " + php_file);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
