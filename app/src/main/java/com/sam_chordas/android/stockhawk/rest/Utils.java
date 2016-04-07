package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static final String TAG = "Utils";
  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    Log.i(LOG_TAG, "GET FB: " + JSON);
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.optJSONObject("query");
        if(jsonObject !=null) {
            int count = Integer.parseInt(jsonObject.optString("count"));
            if (count == 1) {
                jsonObject = jsonObject.optJSONObject("results")
                        .optJSONObject("quote");
                if(!checkForNulls(jsonObject)){
                    batchOperations.add(buildBatchOperation(jsonObject));
                }
            } else {
                resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                if (resultsArray != null && resultsArray.length() != 0) {
                    for (int i = 0; i < resultsArray.length(); i++) {
                        jsonObject = resultsArray.getJSONObject(i);
                        if(!checkForNulls(jsonObject)) {
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
        }
          else{
            //Server down or network error edge case handled
            Log.d(TAG, "error result : called " );
            return null;
        }
      }
    } catch (JSONException e){
       Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format(Locale.ENGLISH,"%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format(Locale.ENGLISH,"%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
            QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString("Change");
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol").toUpperCase());
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
          jsonObject.getString("ChangeinPercent"), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      //builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-'){
        builder.withValue(QuoteColumns.ISUP, 0);
      }else{
        builder.withValue(QuoteColumns.ISUP, 1);
      }

      //newly added columns
        Log.d(TAG, "buildBatchOperation: COMPANY NAME " + jsonObject.getString("Name"));
        Log.d(TAG, "buildBatchOperation: YEAR LOW " + jsonObject.getString("YearLow"));
        Log.d(TAG, "buildBatchOperation: YEAR HIGH " + jsonObject.getString("YearHigh"));
        Log.d(TAG, "buildBatchOperation: MARKET VALUE " + jsonObject.getString("MarketCapitalization"));
      builder.withValue(QuoteColumns.COMPANY_NAME,jsonObject.getString("Name"));
      builder.withValue(QuoteColumns.YEAR_LOW,jsonObject.getString("YearLow"));
      builder.withValue(QuoteColumns.YEAR_HIGH,jsonObject.getString("YearHigh"));
      builder.withValue(QuoteColumns.MARKET_VALUE,jsonObject.getString("MarketCapitalization"));

    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }

    private static boolean checkForNulls(JSONObject jsonObject){
        String bid_price = null;
        String change_in_percent = null;
        if(jsonObject.has("Bid") && jsonObject.has("ChangeinPercent")) {
            try {
                bid_price = jsonObject.getString("Bid");
                change_in_percent = jsonObject.getString("ChangeinPercent");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (!(bid_price.equals("null") || change_in_percent.equals("null"))) {
            Log.d(TAG, "Utils : This is Not null!!");
            return false;
        } else {
            //No Stock found edge case handled
            Log.d(TAG, "Utils : This is null");
            return true;
        }
    }
}
