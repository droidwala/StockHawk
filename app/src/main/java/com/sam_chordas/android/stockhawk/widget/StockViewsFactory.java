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
    private  String[] symbols;
    private  String[] percent_changes;
    private  String[] bid_prices;
    private  int[] is_up;
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
                new String[]{QuoteColumns.SYMBOL,QuoteColumns.BIDPRICE,QuoteColumns.PERCENT_CHANGE,
                        QuoteColumns.ISUP},
                null,
                null,
                null);

        int count = mCursor.getCount();
        symbols = new String[count];
        bid_prices = new String[count];
        percent_changes = new String[count];
        is_up = new int[count];

        Log.d(TAG, "onDataSetChanged: Cursor Count " + String.valueOf(symbols.length));
        try{
            while(mCursor.moveToNext()){
                Log.d(TAG, "onDataSetChanged: cursor parsing " + String.valueOf(mCursor.getPosition()));
                int pos = mCursor.getPosition();
                symbols[pos] = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL));
                bid_prices[pos] = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE));
                percent_changes[pos] = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
                is_up[pos] = mCursor.getInt(mCursor.getColumnIndex(QuoteColumns.ISUP));
            }
        }
        finally {
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

        remoteViews.setTextViewText(R.id.stock_symbol_row_widget,symbols[position]);
        remoteViews.setTextViewText(R.id.bid_price_row_widget,bid_prices[position]);
        remoteViews.setTextViewText(R.id.change_row_widget,percent_changes[position]);
        if(is_up[position] == 1)
        remoteViews.setInt(R.id.change_row_widget,"setBackgroundResource",R.drawable.percent_change_pill_green);
        else
        remoteViews.setInt(R.id.change_row_widget,"setBackgroundResource",R.drawable.percent_change_pill_red);


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
