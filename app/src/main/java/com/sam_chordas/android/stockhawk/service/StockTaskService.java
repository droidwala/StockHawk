package com.sam_chordas.android.stockhawk.service;

import android.appwidget.AppWidgetProvider;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.widget.QuoteWidgetProvider;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService{

  private static final String TAG = "StockTaskService";
  private static final int NO_STOCK_FOUND = 5;
  private static final int SERVER_ISSUE = 6;
  private static final int NEW_STOCK_ADDED = 7;
  private String LOG_TAG = StockTaskService.class.getSimpleName();

  private OkHttpClient client = new OkHttpClient();
  private Context mContext;
  private StringBuilder mStoredSymbols = new StringBuilder();
  private boolean isUpdate;
  private boolean isSuccessful;

  public StockTaskService(){}

  public StockTaskService(Context context){
    mContext = context;
  }
  String fetchData(String url) throws IOException{
    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = client.newCall(request).execute();
    isSuccessful = response.isSuccessful();
    return response.body().string();
  }

  @Override
  public int onRunTask(TaskParams params){
    Log.d(TAG, "onRunTask: called ");
    Cursor initQueryCursor;
    if (mContext == null){
      mContext = this;
    }
    StringBuilder urlStringBuilder = new StringBuilder();
    try{
      // Base URL for the Yahoo query
      urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
      urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
        + "in (", "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (params.getTag().equals("init") || params.getTag().equals("periodic")){
      isUpdate = true;
      initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
          new String[] { "Distinct " + QuoteColumns.SYMBOL }, null,
          null, null);
      if (initQueryCursor.getCount() == 0 || initQueryCursor == null){
        // Init task. Populates DB with quotes for the symbols seen below
        try {
          urlStringBuilder.append(
              URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      }
      else if (initQueryCursor != null){
        DatabaseUtils.dumpCursor(initQueryCursor);
        initQueryCursor.moveToFirst();
        for (int i = 0; i < initQueryCursor.getCount(); i++){
          mStoredSymbols.append("\""+
              initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol"))+"\",");
          initQueryCursor.moveToNext();
        }
        mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
        try {
          urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
        finally {
            if(initQueryCursor!=null)
            initQueryCursor.close();
        }
      }
    }
    else if (params.getTag().equals("add")){
      isUpdate = false;
      // get symbol from params.getExtra and build query
      String stockInput = params.getExtras().getString("symbol");
      try {
        urlStringBuilder.append(URLEncoder.encode("\""+stockInput+"\")", "UTF-8"));
      } catch (UnsupportedEncodingException e){
        e.printStackTrace();
      }
    }
    // finalize the URL for the API query.
    urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
        + "org%2Falltableswithkeys&callback=");

    String urlString;
    String getResponse;
    int result = GcmNetworkManager.RESULT_FAILURE;

    if (urlStringBuilder != null){
      urlString = urlStringBuilder.toString();
      try{
        getResponse = fetchData(urlString);

        Log.d(TAG, "onRunTask: URL " + urlString);
        Log.d(TAG, "onRunTask: Response " + getResponse);

        try {
          ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
          batchOperations = Utils.quoteJsonToContentVals(getResponse);
          if(batchOperations!=null && batchOperations.size()>0) {
            Log.d(TAG, "Response Result : Not null");
            // No more is_Current column logic
            if (isUpdate) {
                Log.d(TAG, "Deleting Existing Stock to avoid duplicates");
                result = GcmNetworkManager.RESULT_SUCCESS;
                mContext.getContentResolver().delete(QuoteProvider.Quotes.CONTENT_URI,null,null);
            }
            else{
                result = NEW_STOCK_ADDED;
            }

              mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                      batchOperations);

          }
          else{
            if(isSuccessful) {
              result = NO_STOCK_FOUND;
            }
            else{
              result = SERVER_ISSUE;
            }

          }

        }
        catch (RemoteException | OperationApplicationException e) {
            Log.e(LOG_TAG, "Error applying batch insert", e);
        }
      } catch (IOException e){
        e.printStackTrace();
      }
    }

      if(isUpdate && params.getTag().equals("periodic")){
          //send broadcast to update our stock hawk widget
          Intent i = new Intent(QuoteWidgetProvider.STOCK_UPDATED_INTENT);
          mContext.sendBroadcast(i);
      }
    Log.d(TAG, "Result returned : " + String.valueOf(result));
    return result;
  }

}
