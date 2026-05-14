package com.example.gon;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PreferenceManager {

    public static final String HOSTED_SERVER = "https://wmc.ms.wits.ac.za/students/sgroup2689/";
    private static final String PREF_NAME = "my_prefs";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_HASH = "hash";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PROFILE_PIC = "profile_pic";

    private static final String KEY_COMPLETED_COUNT = "completed_goal_count";
    private static final String KEY_ACTIVE_COUNT = "active_goal_count";

    // Save UUID
    public static void saveUUID(Context context, String uuid) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        prefs.edit()
                .putString(KEY_UUID, uuid)
                .apply();
    }

    public static String getUUID(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return prefs.getString(KEY_UUID, null);
    }

    public static void saveProfilePic(Context context, int index) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        prefs.edit()
                .putInt(KEY_PROFILE_PIC, index)
                .apply();
    }
    public static int getProfilePic(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return prefs.getInt(KEY_PROFILE_PIC, 0); // Default to pp0
    }

    // Save hash
    public static void saveHash(Context context, String hash) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        prefs.edit()
                .putString(KEY_HASH, hash)
                .apply();
    }

    // Read hash
    public static String getHashString(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return prefs.getString(KEY_HASH, null);
    }

    public static void saveUsername(Context context, String username) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        prefs.edit()
                .putString(KEY_USERNAME, username)
                .apply();
    }

    // Read hash
    public static String getUsername(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return prefs.getString(KEY_USERNAME, null);
    }

    public static int getCompletedGoalCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_COMPLETED_COUNT, 0);
    }

    public static int getActiveGoalCount(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_ACTIVE_COUNT, 0);
    }

    public static void save_stats(Context context, int completed, int active){
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        prefs.edit()
                .putInt(KEY_ACTIVE_COUNT, active)
                .putInt(KEY_COMPLETED_COUNT, completed)
                .apply();
    }

    public static final int[] PROFILE_PHOTOS = {
            R.drawable.pp0, R.drawable.pp1, R.drawable.pp2,
            R.drawable.pp3, R.drawable.pp4, R.drawable.pp5,
            R.drawable.pp6, R.drawable.pp7, R.drawable.pp8
    };
    public static void updateNavIcon(Context context, BottomNavigationView navView) {
        int currentPicIndex = getProfilePic(context);
        navView.getMenu().findItem(R.id.nav_profile).setIcon(PROFILE_PHOTOS[currentPicIndex]);
    }

    public interface NetworkCallback {
        void onResponse(String response);
    }

    public static void post(String phpFile, Map<String, String> params, NetworkCallback callback) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                FormBody.Builder builder = new FormBody.Builder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    builder.add(entry.getKey(), entry.getValue());
                }

                Request request = new Request.Builder()
                        .url(HOSTED_SERVER + phpFile)
                        .post(builder.build())
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        new Handler(Looper.getMainLooper()).post(() -> callback.onResponse(responseData));
                    } else {
                        Log.e("GON_DEBUG", "HTTP ERROR:"  + response.code() +  " for "  + phpFile);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}