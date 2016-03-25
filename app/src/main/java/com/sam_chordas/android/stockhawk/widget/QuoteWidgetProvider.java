package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;

public class QuoteWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "QuoteWidgetProvider";

    public static final String STOCK_ADDED_INTENT = "com.sam_chordas.android.stockhawk.stock_added";
    public static final String STOCK_UPDATED_INTENT = "com.sam_chordas.android.stockhawk.stock_updated";
    public static final String STOCK_REMOVED_INTENT = "com.sam_chordas.android.stockhawk.stock_removed";
    public static final String ALARM_UPDATE ="com.sam_chordas.android.stockhawk.alarm_update";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == STOCK_ADDED_INTENT || intent.getAction() == STOCK_UPDATED_INTENT
                || intent.getAction() == STOCK_REMOVED_INTENT || intent.getAction() == ALARM_UPDATE){
            Log.d(TAG, "onReceive: called " + "YUP I HEARD THAT!");
            onUpdate(context);
        }
        else{
            super.onReceive(context, intent);
        }
    }

    private void onUpdate(Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context.getPackageName(),getClass().getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        onUpdate(context,appWidgetManager,appWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "Widget is getting updated.Please take your seats!!");

        for (int i = 0; i < appWidgetIds.length; i++) {
            Intent svcIntent = new Intent(context,QuoteWidgetRemoteViewsService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget = new RemoteViews(context.getPackageName(),R.layout.widget_collection);

            widget.setRemoteAdapter(R.id.widget_list,svcIntent);

            Log.d(TAG, "Before UpdateAppWidget Call ");
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[i],R.id.widget_list);
            appWidgetManager.updateAppWidget(appWidgetIds[i], widget);
            Log.d(TAG, "After UpdateAppWidget Call ");

            //Not handling row clicks currently

        }
       // super.onUpdate(context,appWidgetManager,appWidgetIds);
       // ComponentName componentName = new ComponentName(context,QuoteWidgetProvider.class);
        //appWidgetManager.updateAppWidget(componentName,buildUpdate(context,appWidgetIds));
    }



    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "App Widget Created " );
        AlarmUtil.scheduleUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        AlarmUtil.clearUpdate(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        Log.d(TAG, "onAppWidgetOptionsChanged: ");
    }
}
