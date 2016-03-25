package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

public class QuoteWidgetRemoteViewsService extends RemoteViewsService {

    private static final String TAG = "RemoteViewsService";
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d(TAG, "Inside Remote Views Service");
        return (new StockViewsFactory(this.getApplicationContext(),intent));
    }
}
