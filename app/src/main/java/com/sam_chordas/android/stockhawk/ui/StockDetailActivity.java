package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.charts.BarCardOne;
import com.sam_chordas.android.stockhawk.charts.LineCardThree;
import com.sam_chordas.android.stockhawk.charts.LineCardTwo;
import com.sam_chordas.android.stockhawk.pojo.History;
import com.sam_chordas.android.stockhawk.pojo.OneDayHistory;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class StockDetailActivity extends AppCompatActivity{

    private static final String TAG = "StockDetailActivity";
    private final OkHttpClient client = new OkHttpClient();
    private String BASE_URL = "https://query.yahooapis.com/v1/public/yql?q=";
    private String startDate = "",endDate = "",today_date="",past_thirty="",past_sixty="";
    private String stock_name;
    private Toolbar toolbar;

    ArrayList<String> weekly_close_amt = new ArrayList<String>();
     ArrayList<String> monthly_close_amt = new ArrayList<String>();
     ArrayList<String> monthly_dates = new ArrayList<String>();

    ArrayList<String> sixty_close_amt = new ArrayList<String>();
    ArrayList<String> sixty_dates = new ArrayList<String>();

    TextView no_weekly_data;
    TextView company_name,year_low,year_high,market_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        no_weekly_data = (TextView) findViewById(R.id.no_weekly_data);
        company_name = (TextView) toolbar.findViewById(R.id.company_name);
        year_low = (TextView) toolbar.findViewById(R.id.year_low_value);
        year_high = (TextView) toolbar.findViewById(R.id.year_high_value);
        market_value = (TextView) toolbar.findViewById(R.id.market_value);
        setSupportActionBar(toolbar);
        Bundle b = getIntent().getExtras();
        stock_name = b.getString("STOCK");
        company_name.setText(b.getString("Name"));
        year_low.setText(b.getString("YearLow"));
        year_high.setText(b.getString("YearHigh"));
        market_value.setText("$" + b.getString("MarketValue"));
        Log.d(TAG, "onCreate: called " + b.getString("STOCK"));


        InitializeStartAndEndDate();//Get start and end dates for weekly data
        Thiry_And_Sixty();//for 30 days and 60 days

        //Create weekly api query for the given stock name
        StringBuilder urlString = new StringBuilder();
        urlString.append(BASE_URL);
        try {
            urlString.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = ","UTF-8"));
            urlString.append(URLEncoder.encode("\"" + stock_name + "\"","UTF-8"));
            urlString.append(URLEncoder.encode(" and startDate = " + "\"" + startDate + "\"","UTF-8"));
            urlString.append(URLEncoder.encode(" and endDate = " + "\"" + endDate + "\"","UTF-8"));
            urlString.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            FetchWeeklyData(urlString.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void FetchWeeklyData(String URL) throws IOException{
         Request request = new Request.Builder()
                  .url(URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d(TAG, "Dates : " + " Start Date " + startDate + " End Date " + endDate);
                //    Log.d(TAG, "Weekly Data " + response.body().string());
                String result = response.body().string();

                JsonParser parser = new JsonParser();
                JsonObject object = parser.parse(result).getAsJsonObject();
                JsonObject query = object.getAsJsonObject("query");
                JsonPrimitive count = query.getAsJsonPrimitive("count");
                int result_count = count.getAsInt();
                Log.d(TAG, "Weekly count value is : " + String.valueOf(result_count));


                if (result_count > 1) {
                    Gson gson = new Gson();
                    History history = gson.fromJson(result, new TypeToken<History>() {
                    }.getType());
                    if (history.getQuery().getResults() != null) {
                        ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity> quotes = (ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity>) history.getQuery().getResults().getQuote();

                        Log.d(TAG, "onResponse: after parsing " + String.valueOf(quotes.size()));

                        for (int i = 0; i < quotes.size(); i++) {
                            weekly_close_amt.add(quotes.get(i).getAdj_Close());

                        }
                        Collections.reverse(weekly_close_amt);
                        Log.d(TAG, "Weekly Amt Size : before for loop " + String.valueOf(weekly_close_amt.size()));
                        if (weekly_close_amt.size() < 5) {
                            for (int j = weekly_close_amt.size(); j < 5; j++) {
                                weekly_close_amt.add("0");
                            }
                        }
                        Log.d(TAG, "Weekly Amt Size : after for loop " + String.valueOf(weekly_close_amt.size()));
                    } else {
                        Log.d(TAG, "onResponse: No data!!");
                    }
                }
                else if (result_count == 1) {
                    Log.d(TAG, "onResponse: one result case!");
                    Gson gson = new Gson();
                    OneDayHistory oneDayHistory = gson.fromJson(result, new TypeToken<OneDayHistory>() {
                    }.getType());
                    if (oneDayHistory.getQuery().getResults() != null) {
                        OneDayHistory.QueryEntity.ResultsEntity.QuoteEntity quote = oneDayHistory.getQuery().getResults().getQuote();
                        weekly_close_amt.add(quote.getAdj_Close());

                    }
                    Collections.reverse(weekly_close_amt);
                    if (weekly_close_amt.size() < 5) {
                        for (int j = weekly_close_amt.size(); j < 5; j++) {
                            weekly_close_amt.add("0");
                        }
                    }

                    Log.d(TAG, "weekly close array size : " + String.valueOf(weekly_close_amt.size()));
                }
                else {
                    Log.d(TAG, "NO DATA FOR CURRENT WEEK BRO!!");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder urlString = new StringBuilder();
                        urlString.append(BASE_URL);
                        try {
                            urlString.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = ", "UTF-8"));
                            urlString.append(URLEncoder.encode("\"" + stock_name + "\"", "UTF-8"));
                            urlString.append(URLEncoder.encode(" and startDate = " + "\"" + past_thirty + "\"", "UTF-8"));
                            urlString.append(URLEncoder.encode(" and endDate = " + "\"" + today_date + "\"", "UTF-8"));
                            urlString.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        try {
                            FetchMonthlyData(urlString.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });

            }
        });

    }

    
    private void FetchMonthlyData(String url) throws IOException{

        Request request = new Request.Builder()
                .url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                //Log.d(TAG, "onResponse: called " + response.body().string());
                Gson gson = new Gson();
                History history = gson.fromJson(response.body().string(), new TypeToken<History>() {
                }.getType());
                if (history.getQuery().getResults() != null) {
                    ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity> quotes = (ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity>) history.getQuery().getResults().getQuote();

                    Log.d(TAG, "onResponse: after parsing " + String.valueOf(quotes.size()));


                    for (int i = 0; i < quotes.size(); i++) {
                        Log.d(TAG, "Data: " + quotes.get(i).getDate() + " " + quotes.get(i).getAdj_Close());
                        if (i == 0) {
                            monthly_dates.add(quotes.get(i).getDate());
                        } else if (i == quotes.size() - 1) {
                            monthly_dates.add(quotes.get(i).getDate());
                        } else {
                            monthly_dates.add("");
                        }
                        monthly_close_amt.add(quotes.get(i).getAdj_Close());
                    }

                    Collections.reverse(monthly_close_amt);
                    Collections.reverse(monthly_dates);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                          //  new LineCardTwo((CardView) findViewById(R.id.monthly_card), StockDetailActivity.this, monthly_close_amt, monthly_dates).show();
                            // new MPLineChart((CardView)findViewById(R.id.monthly_card),StockDetailActivity.this,monthly_close_amt).show();
                            StringBuilder urlString = new StringBuilder();
                            urlString.append(BASE_URL);
                            try {
                                urlString.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = ","UTF-8"));
                                urlString.append(URLEncoder.encode("\"" + stock_name + "\"","UTF-8"));
                                urlString.append(URLEncoder.encode(" and startDate = " + "\"" + past_sixty + "\"","UTF-8"));
                                urlString.append(URLEncoder.encode(" and endDate = " + "\"" + today_date + "\"","UTF-8"));
                                urlString.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");

                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            try {
                                FetchSixtyData(urlString.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "onResponse: No data!!");
                }


            }
        });
    }



    private void FetchSixtyData(String url) throws IOException{

        Request request = new Request.Builder()
                .url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                //Log.d(TAG, "onResponse: called " + response.body().string());
                Gson gson = new Gson();
                History history = gson.fromJson(response.body().string(), new TypeToken<History>() {
                }.getType());
                if (history.getQuery().getResults() != null) {
                    ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity> quotes = (ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity>) history.getQuery().getResults().getQuote();

                    Log.d(TAG, "onResponse: after parsing " + String.valueOf(quotes.size()));


                    for (int i = 0; i < quotes.size(); i++) {
                        Log.d(TAG, "Data: " + quotes.get(i).getDate() + " " + quotes.get(i).getAdj_Close());
                        if (i == 0) {
                            sixty_dates.add(quotes.get(i).getDate());
                        } else if (i == quotes.size() - 1) {
                            sixty_dates.add(quotes.get(i).getDate());
                        } else {
                            sixty_dates.add("");
                        }
                        sixty_close_amt.add(quotes.get(i).getAdj_Close());
                    }

                    Collections.reverse(sixty_close_amt);
                    Collections.reverse(sixty_dates);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(weekly_close_amt.size()>0) {
                                new BarCardOne((CardView) findViewById(R.id.weekly_card), StockDetailActivity.this, weekly_close_amt).show();
                            }else{
                                no_weekly_data.setVisibility(View.VISIBLE);
                            }

                            new LineCardTwo((CardView) findViewById(R.id.monthly_card), StockDetailActivity.this, monthly_close_amt, monthly_dates).show();
                            new LineCardThree((CardView) findViewById(R.id.sixty_days_card), StockDetailActivity.this, sixty_close_amt, sixty_dates).show();
                        }
                    });
                } else {
                    Log.d(TAG, "onResponse: No data!!");
                }


            }
        });
    }
    private void InitializeStartAndEndDate(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        startDate = df.format(c.getTime());
        c.add(Calendar.DATE,4);
        endDate = df.format(c.getTime());
        Log.d(TAG, "Start Date : " + startDate + "    " + "End Date : " + endDate);
    }

    private void Thiry_And_Sixty(){
        Calendar c = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        today_date = df.format(c.getTime());
        c.add(Calendar.DATE,-30);
        past_thirty = df.format(c.getTime());
        c.add(Calendar.DATE,-30);
        past_sixty = df.format(c.getTime());
        Log.d(TAG, "PastThirty: Today's date " + today_date + " thirty days ago " + past_thirty +
        " sixty days ago " + past_sixty);
    }



}

