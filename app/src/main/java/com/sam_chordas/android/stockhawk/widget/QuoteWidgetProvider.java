package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
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

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            Intent svcIntent = new Intent(context,QuoteWidgetRemoteViewsService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget = new RemoteViews(context.getPackageName(),R.layout.widget_collection);

            widget.setRemoteAdapter(R.id.widget_list,svcIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i],widget);

            //Not handling row clicks currently

        }
        super.onUpdate(context,appWidgetManager,appWidgetIds);
       // ComponentName componentName = new ComponentName(context,QuoteWidgetProvider.class);
        //appWidgetManager.updateAppWidget(componentName,buildUpdate(context,appWidgetIds));
    }

    /*
    private RemoteViews buildUpdate(Context context,int[] appWidgetIds){
         RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_large);
         Intent i = new Intent(context,QuoteWidgetProvider.class);
         i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
         i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        PendingIntent pi = PendingIntent.getBroadcast(context,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setTextViewText(R.id.stock_symbol,"GOOG");
        remoteViews.setTextViewText(R.id.bid_price,"134.5");
        remoteViews.setTextViewText(R.id.widget_change,"+3.2%");
        remoteViews.setOnClickPendingIntent(R.id.stock_symbol,pi);
        return remoteViews;
    }
    */

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "App Widget Created " );
        super.onEnabled(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        Log.d(TAG, "onAppWidgetOptionsChanged: ");
    }
}
