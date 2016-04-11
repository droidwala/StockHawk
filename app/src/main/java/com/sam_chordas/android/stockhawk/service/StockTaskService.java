package com.sam_chordas.android.stockhawk.service;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;

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
 *
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService{

  private static final String TAG = "StockTaskService";
  //Constants indicating various conditions we could get while parsing response received
  private static final int NO_STOCK_FOUND = 5;
  private static final int SERVER_ISSUE = 6;
  private static final int NEW_STOCK_ADDED = 7;
  private String LOG_TAG = StockTaskService.class.getSimpleName();

  private OkHttpClient client = new OkHttpClient();
  private Context mContext;
  private StringBuilder mStoredSymbols = new StringBuilder();
  private boolean isUpdate; // indicating whether we are updating values of existing stocks or not
  private boolean isSuccessful;//indicated whether Json response was received or not

  public StockTaskService(){}

  public StockTaskService(Context context){
    mContext = context;
  }
  //Synchronous REST API call
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
        // Init task. Populates DB with quotes for the symbols seen below in event db/list is empty
        try {
          urlStringBuilder.append(
              URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      }
      else if (initQueryCursor != null){
        // If values are already present in db..Then we extract symbol names of the stock
        // And generate rest api url with those stocks include in where clause
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
      //Called when user is try to add stocks to the list.
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
          ArrayList<ContentProviderOperation> batchOperations;
          batchOperations = Utils.quoteJsonToContentVals(getResponse);
          if(batchOperations!=null && batchOperations.size()>0) {
            //Indicates response was parsed properly and the contentprovideroperations arraylist
            //received is legit
            if (isUpdate) {
                Log.d(TAG, "Deleting Existing Stock to avoid duplicates");
                result = GcmNetworkManager.RESULT_SUCCESS;
                mContext.getContentResolver().delete(QuoteProvider.Quotes.CONTENT_URI,null,null);
            }
            else{
                result = NEW_STOCK_ADDED;
            }
            //Adds stocks to db in batches
            mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                      batchOperations);
          }
          else{
            //when batchoperations arraylist is null indicating error is response received..
            if(isSuccessful) {
              if(isUpdate){
                result = GcmNetworkManager.RESULT_FAILURE ;
              }
              else {
                result = NO_STOCK_FOUND;
              }
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
          //whenever DB is updated by periodic task service with latest values
          Intent i = new Intent(QuoteWidgetProvider.STOCK_UPDATED_INTENT);
          mContext.sendBroadcast(i);
      }
    Log.d(TAG, "Result returned : " + String.valueOf(result));
    return result;
  }

}
