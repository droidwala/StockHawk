package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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
import java.util.concurrent.TimeoutException;

public class StockDetailActivity extends AppCompatActivity{

    private static final String TAG = "StockDetailActivity";
    private final OkHttpClient client = new OkHttpClient();
    private String BASE_URL = "https://query.yahooapis.com/v1/public/yql?q=";
    private String startDate = "",endDate = "",today_date="",past_thirty="",past_sixty="";
    private String stock_name;
    private Toolbar toolbar;
    private TextView no_weekly_data,no_monthly_data,no_sixty_data;
    private TextView company_name,year_low,year_high,market_value;
    private ProgressBar weekly_progressBar,monthly_progressBar,sixty_progressBar;

    ArrayList<String> weekly_close_amt = new ArrayList<String>();//Used to store weekly data

    ArrayList<String> monthly_close_amt = new ArrayList<String>();//Used to store monthly data
    ArrayList<String> monthly_dates = new ArrayList<String>();

    ArrayList<String> sixty_close_amt = new ArrayList<String>();//Used to store Past sixty days data
    ArrayList<String> sixty_dates = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        no_weekly_data = (TextView) findViewById(R.id.no_weekly_data);
        no_monthly_data = (TextView) findViewById(R.id.no_monthly_data);
        no_sixty_data = (TextView) findViewById(R.id.no_sixty_data);

        company_name = (TextView) toolbar.findViewById(R.id.company_name);
        year_low = (TextView) toolbar.findViewById(R.id.year_low_value);
        year_high = (TextView) toolbar.findViewById(R.id.year_high_value);
        market_value = (TextView) toolbar.findViewById(R.id.market_value);

        weekly_progressBar = (ProgressBar) findViewById(R.id.weekly_progressbar);
        monthly_progressBar = (ProgressBar) findViewById(R.id.monthly_progressbar);
        sixty_progressBar = (ProgressBar) findViewById(R.id.sixty_progressbar);



        //Fetching all extras passed in Intent
        Bundle b = getIntent().getExtras();
        stock_name = b.getString("STOCK");
        company_name.setText(b.getString("Name"));
        year_low.setText(b.getString("YearLow"));
        year_high.setText(b.getString("YearHigh"));
        market_value.setText("$" + b.getString("MarketValue"));
        Log.d(TAG, "onCreate: called " + b.getString("STOCK"));

        //Initializing all date variables.
        InitializingDates();

        //Create weekly api query for the given stock name
        try {
            weekly_progressBar.setVisibility(View.VISIBLE);
            FetchWeeklyData(GenerateUrl(startDate, endDate));
            monthly_progressBar.setVisibility(View.VISIBLE);
            FetchMonthlyData(GenerateUrl(past_thirty,today_date));
            sixty_progressBar.setVisibility(View.VISIBLE);
            FetchSixtyData(GenerateUrl(past_sixty,today_date));

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
                //We need to handle timeout exception..
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String result = response.body().string();
                int result_count = getResultCount(result);

                if (result_count > 1) {
                    Gson gson = new Gson();
                    History history = gson.fromJson(result, new TypeToken<History>() {
                    }.getType());
                    if (history.getQuery().getResults() != null) {
                        ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity> quotes = (ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity>) history.getQuery().getResults().getQuote();
                        for (int i = 0; i < quotes.size(); i++) {
                            weekly_close_amt.add(quotes.get(i).getAdj_Close());

                        }
                        Collections.reverse(weekly_close_amt);//need to reverse the data as it is received in desc order
                        if (weekly_close_amt.size() < 5) {
                            for (int j = weekly_close_amt.size(); j < 5; j++) {
                                weekly_close_amt.add("0");
                            }
                        }
                    } else {
                        Log.d(TAG, "onResponse: No data!!");
                    }
                }
                else if (result_count == 1) {
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
                }
                else if(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            no_weekly_data.setText("Sorry,the week has just started.\nNo data for current week!");
                            Log.d(TAG, "NO DATA FOR CURRENT WEEK BRO!!");
                        }
                    });

                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            no_weekly_data.setText("Sorry,looks like we don't have\nhistorical data available for this stock!");
                        }
                    });

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        weekly_progressBar.setVisibility(View.INVISIBLE);
                        if(weekly_close_amt.size()>0)
                            new BarCardOne((CardView) findViewById(R.id.weekly_card), StockDetailActivity.this, weekly_close_amt).show();
                        else
                            no_weekly_data.setVisibility(View.VISIBLE);
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
                String result = response.body().string();
                int result_count = getResultCount(result);

                if(result_count>1) {
                    Gson gson = new Gson();
                    History history = gson.fromJson(result, new TypeToken<History>() {
                    }.getType());
                    if (history.getQuery().getResults() != null) {
                        ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity> quotes = (ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity>) history.getQuery().getResults().getQuote();
                        for (int i = 0; i < quotes.size(); i++) {
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

                    } else {
                        Log.d(TAG, "onResponse: No data!!");
                    }
                }
                else{
                    no_monthly_data.setText("Sorry,looks like we don't have\nhistorical data available for this stock!");
                    Log.d(TAG, "onResponse: We should have atleast more than one result count to plot line chart properly");

                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        monthly_progressBar.setVisibility(View.INVISIBLE);
                        if (monthly_close_amt.size() > 0)
                            new LineCardTwo((CardView) findViewById(R.id.monthly_card), StockDetailActivity.this, monthly_close_amt, monthly_dates).show();
                        else
                            no_monthly_data.setVisibility(View.VISIBLE);
                    }
                });
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
                String result = response.body().string();
                int result_count = getResultCount(result);

                if(result_count>1) {
                    Gson gson = new Gson();
                    History history = gson.fromJson(result, new TypeToken<History>() {
                    }.getType());
                    if (history.getQuery().getResults() != null) {
                        ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity> quotes = (ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity>) history.getQuery().getResults().getQuote();
                        for (int i = 0; i < quotes.size(); i++) {
                            if (i == 0) {
                                sixty_dates.add(quotes.get(i).getDate());
                            } else if (i == quotes.size() - 1) {
                                sixty_dates.add(quotes.get(i).getDate());
                            } else {
                                sixty_dates.add("");
                            }
                            sixty_close_amt.add(quotes.get(i).getAdj_Close());
                        }
                        Collections.reverse(sixty_close_amt);//since the data comes in descending order
                        Collections.reverse(sixty_dates);
                    } else {
                        Log.d(TAG, "onResponse: No data!!");
                    }
                }
                else{
                    no_sixty_data.setText("Sorry,looks like we don't have\nhistorical data available for this stock!");
                    Log.d(TAG, "onResponse: Sixty days data should have result count at least more than one to plot LC properly");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sixty_progressBar.setVisibility(View.INVISIBLE);
                        if (sixty_close_amt.size() > 0)
                            new LineCardThree((CardView) findViewById(R.id.sixty_days_card), StockDetailActivity.this, sixty_close_amt, sixty_dates).show();
                        else
                            no_sixty_data.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private int getResultCount(String response){
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(response).getAsJsonObject();
        JsonObject query = object.getAsJsonObject("query");
        JsonPrimitive count = query.getAsJsonPrimitive("count");
        return count.getAsInt();
    };
    private void InitializingDates(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        startDate = df.format(c.getTime());
        c.add(Calendar.DATE, 4);
        endDate = df.format(c.getTime());
        c.add(Calendar.DATE,-4);
        today_date = df.format(c.getTime());
        c.add(Calendar.DATE,-30);
        past_thirty = df.format(c.getTime());
        c.add(Calendar.DATE,-30);
        past_sixty = df.format(c.getTime());

    }

    private String GenerateUrl(String d1,String d2){
        StringBuilder urlString = new StringBuilder();
        urlString.append(BASE_URL);
        try {
            urlString.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = ","UTF-8"));
            urlString.append(URLEncoder.encode("\"" + stock_name + "\"","UTF-8"));
            urlString.append(URLEncoder.encode(" and startDate = " + "\"" + d1 + "\"","UTF-8"));
            urlString.append(URLEncoder.encode(" and endDate = " + "\"" + d2 + "\"","UTF-8"));
            urlString.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return urlString.toString();
    }



}

