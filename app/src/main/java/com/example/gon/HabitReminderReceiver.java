package com.example.gon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HabitReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("HabitReminderReceiver", "Alarm received");
        
        // Show the notification
        NotificationHelper.createNotificationChannel(context);
        NotificationHelper.showHabitReminder(context);
        
        // Schedule the next reminder for tomorrow
        ReminderScheduler.scheduleNextReminder(context);
    }
}
