package com.example.gon.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.gon.R;
import com.example.gon.ui.activities.HabitList;

public class NotificationHelper {
    public static final String channel_id = "habit_reminders";

    public static void create_notification_channel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_channel_name);
            String description = context.getString(R.string.notification_channel_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
            channel.setDescription(description);
            
            NotificationManager notification_manager = context.getSystemService(NotificationManager.class);
            if (notification_manager != null) {
                notification_manager.createNotificationChannel(channel);
            }
        }
    }

    public static void show_habit_reminder(Context context) {
        Intent intent = new Intent(context, HabitList.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pending_intent = PendingIntent.getActivity(context, 0, intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel_id)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.reminder_title))
                .setContentText(context.getString(R.string.reminder_content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pending_intent)
                .setAutoCancel(true);

        NotificationManagerCompat notification_manager = NotificationManagerCompat.from(context);
        try {
            notification_manager.notify(1, builder.build());
        } catch (SecurityException e) {
            Log.e("GON_DEBUG : notification", "does not have permission");
        }
    }
}
