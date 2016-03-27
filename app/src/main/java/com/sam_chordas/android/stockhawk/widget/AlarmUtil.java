package com.sam_chordas.android.stockhawk.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmUtil {

    private static final String TAG = "AlarmUtil";
    public static void scheduleUpdate(Context context){
        Log.d(TAG, "scheduleUpdate: without interval");
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long intervalMillis = 15 * 60 * 1000; // 5 mins update interval

        PendingIntent pi = getAlarmIntent(context);
        am.cancel(pi);

        am.setInexactRepeating(AlarmManager.RTC,System.currentTimeMillis() + intervalMillis,intervalMillis,pi);

    }

    public static void scheduleUpdate(Context context,long interval){
        Log.d(TAG, "scheduleUpdate: with interval");
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long intervalMillis = interval * 60 * 1000;

        Log.d(TAG, "scheduleUpdate: with interval" + String.valueOf(intervalMillis));
        PendingIntent pi = getAlarmIntent(context);
        am.cancel(pi);

        am.setInexactRepeating(AlarmManager.RTC,System.currentTimeMillis(),intervalMillis,pi);
    }

    private static PendingIntent getAlarmIntent(Context context){
        Intent intent = new Intent(context,QuoteWidgetProvider.class);
        intent.setAction(QuoteWidgetProvider.ALARM_UPDATE);
        PendingIntent pi = PendingIntent.getBroadcast(context,0,intent,0);
        return pi;
    }

    public static void clearUpdate(Context context){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getAlarmIntent(context));
    }



}
