package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

public class StockViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "StockViewsFactory";
    private Cursor mCursor;
    private static String[] symbols;
    private int appWidgetId;
    private Context mContext;
    public StockViewsFactory(Context context, Intent intent){
        mContext = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
    }
    @Override
    public void onCreate() {
       //do nothing
        Log.d(TAG, "onCreate: called");
    }

    @Override
    public void onDataSetChanged() {
        Log.d(TAG, "onDataSetChanged: called");
        if(mCursor!=null)
            mCursor.close();

        mCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.SYMBOL},
                null,
                null,
                null);
        symbols = new String[mCursor.getCount()];
        Log.d(TAG, "onDataSetChanged: Cursor Count " + String.valueOf(symbols.length));
        try{
            while(mCursor.moveToNext()){
                Log.d(TAG, "onDataSetChanged: cursor parsing " + String.valueOf(mCursor.getPosition()));
                symbols[mCursor.getPosition()] = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL));
            }
        }
        finally {
            if(mCursor!=null)
                mCursor.close();
        }


    }

    @Override
    public void onDestroy() {
      //do nothing
        Log.d(TAG, "onDestroy: called ");
    }

    @Override
    public int getCount() {
        return symbols.length;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews  = new RemoteViews(mContext.getPackageName(), R.layout.widget_collection_item);

        remoteViews.setTextViewText(R.id.stock_symbol_row,symbols[position]);

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
