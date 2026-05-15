package com.example.gon.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.gon.utils.PreferenceManager;
import com.example.gon.utils.ReminderScheduler;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Boot completed, rescheduling reminders");
            if (PreferenceManager.is_reminder_enabled(context)) {
                ReminderScheduler.schedule_next_reminder(context);
            }
        }
    }
}
