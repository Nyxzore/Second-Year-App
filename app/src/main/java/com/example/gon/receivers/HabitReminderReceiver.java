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
        
        NotificationHelper.create_notification_channel(context);
        NotificationHelper.show_habit_reminder(context);
        
        ReminderScheduler.schedule_next_reminder(context);
    }
}
