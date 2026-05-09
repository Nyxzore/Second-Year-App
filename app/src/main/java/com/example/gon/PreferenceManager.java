package com.example.gon;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    public static final String HOSTED_SERVER = "https://wmc.ms.wits.ac.za/students/sgroup2689/";
    private static final String PREF_NAME = "my_prefs";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_HASH = "hash";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PROFILE_PIC = "profile_pic";

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
}