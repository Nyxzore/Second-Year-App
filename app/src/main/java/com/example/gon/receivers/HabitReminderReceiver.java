package com.example.gon.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.gon.utils.NotificationHelper;
import com.example.gon.utils.ReminderScheduler;

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
