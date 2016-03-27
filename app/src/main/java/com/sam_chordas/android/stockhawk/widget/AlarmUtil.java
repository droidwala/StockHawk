package com.sam_chordas.android.stockhawk.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmUtil {


    public static void scheduleUpdate(Context context){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long intervalMillis = 5 * 60 * 1000; // 5 mins update interval

        PendingIntent pi = getAlarmIntent(context);
        am.cancel(pi);

        am.setInexactRepeating(AlarmManager.RTC,System.currentTimeMillis() + intervalMillis,intervalMillis,pi);

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
