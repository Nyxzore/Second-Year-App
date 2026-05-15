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

    public static void scheduleNextReminder(Context context) {
        if (!PreferenceManager.isReminderEnabled(context)) {
            cancelReminder(context);
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        boolean canScheduleExact = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            canScheduleExact = alarmManager.canScheduleExactAlarms();
            if (!canScheduleExact) {
                Log.w("ReminderScheduler", "Exact alarm permission not granted; using inexact alarm");
            }
        }

        Intent intent = new Intent(context, HabitReminderReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags);

        int hour = PreferenceManager.getReminderHour(context);
        int minute = PreferenceManager.getReminderMinute(context);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // If time is in the past, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        long triggerAt = calendar.getTimeInMillis();
        if (canScheduleExact) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                }
            } catch (SecurityException e) {
                Log.w("ReminderScheduler", "Exact alarm denied; falling back to inexact alarm", e);
                scheduleInexactAlarm(alarmManager, triggerAt, pendingIntent);
            }
        } else {
            scheduleInexactAlarm(alarmManager, triggerAt, pendingIntent);
        }

        Log.d("ReminderScheduler", "Scheduled reminder for: " + calendar.getTime().toString());
    }

    private static void scheduleInexactAlarm(AlarmManager alarmManager, long triggerAt, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    public static void cancelReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(context, HabitReminderReceiver.class);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags);
            alarmManager.cancel(pendingIntent);
            Log.d("ReminderScheduler", "Cancelled reminder");
        }
    }
}
