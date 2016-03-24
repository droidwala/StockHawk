package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;

public class StockViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String[] items = {"GOOG","FB","TWTR","ARP","MCD"};
    private int appWidgetId;
    private Context mContext;
    public StockViewsFactory(Context context, Intent intent){
        mContext = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
    }
    @Override
    public void onCreate() {
       //do nothing
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {
      //do nothing
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews  = new RemoteViews(mContext.getPackageName(), R.layout.widget_collection_item);

        remoteViews.setTextViewText(R.id.stock_symbol_row,items[position]);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
