package com.example.gon.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.gon.receivers.HabitReminderReceiver;

import java.util.Calendar;

public class ReminderScheduler {

    public static void schedule_next_reminder(Context context) {
        if (!PreferenceManager.is_reminder_enabled(context)) {
            cancel_reminder(context);
            return;
        }

        AlarmManager alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm_manager == null) return;

        boolean can_schedule_exact = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            can_schedule_exact = alarm_manager.canScheduleExactAlarms();
            if (!can_schedule_exact) {
                Log.w("ReminderScheduler", "Exact alarm permission not granted; using inexact alarm");
            }
        }

        Intent intent = new Intent(context, HabitReminderReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pending_intent = PendingIntent.getBroadcast(context, 0, intent, flags);

        int hour = PreferenceManager.get_reminder_hour(context);
        int minute = PreferenceManager.get_reminder_minute(context);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        long trigger_at = calendar.getTimeInMillis();
        if (can_schedule_exact) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarm_manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger_at, pending_intent);
                } else {
                    alarm_manager.setExact(AlarmManager.RTC_WAKEUP, trigger_at, pending_intent);
                }
            } catch (SecurityException e) {
                Log.w("ReminderScheduler", "Exact alarm denied; falling back to inexact alarm", e);
                schedule_inexact_alarm(alarm_manager, trigger_at, pending_intent);
            }
        } else {
            schedule_inexact_alarm(alarm_manager, trigger_at, pending_intent);
        }

        Log.d("ReminderScheduler", "Scheduled reminder for: " + calendar.getTime().toString());
    }

    private static void schedule_inexact_alarm(AlarmManager alarm_manager, long trigger_at, PendingIntent pending_intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm_manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger_at, pending_intent);
        } else {
            alarm_manager.set(AlarmManager.RTC_WAKEUP, trigger_at, pending_intent);
        }
    }

    public static void cancel_reminder(Context context) {
        AlarmManager alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm_manager != null) {
            Intent intent = new Intent(context, HabitReminderReceiver.class);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
            PendingIntent pending_intent = PendingIntent.getBroadcast(context, 0, intent, flags);
            alarm_manager.cancel(pending_intent);
            Log.d("ReminderScheduler", "Cancelled reminder");
        }
    }
}
