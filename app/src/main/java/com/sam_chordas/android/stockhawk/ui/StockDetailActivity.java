package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.charts.LineCardOne;
import com.sam_chordas.android.stockhawk.charts.LineCardTwo;
import com.sam_chordas.android.stockhawk.charts.MPLineChart;
import com.sam_chordas.android.stockhawk.pojo.History;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_detail);
        Bundle b = getIntent().getExtras();
        stock_name = b.getString("STOCK");
        Log.d(TAG, "onCreate: called " + b.getString("STOCK"));

        InitializeStartAndEndDate();//Get start and end dates.

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
            FetchData(urlString.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void FetchData(String URL) throws IOException{
         Request request = new Request.Builder()
                  .url(URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                //Log.d(TAG, "onResponse: called " + response.body().string());
                Gson gson = new Gson();
                History history = gson.fromJson(response.body().string(),new TypeToken<History>(){}.getType());
                if(history.getQuery().getResults()!= null) {
                    ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity> quotes = (ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity>) history.getQuery().getResults().getQuote();

                    Log.d(TAG, "onResponse: after parsing " + String.valueOf(quotes.size()));
                    final ArrayList<String> close_amt = new ArrayList<String>();
                    for (int i = 0; i < quotes.size(); i++) {
                        close_amt.add(quotes.get(i).getAdj_Close());
                    }
                }
                else{
                    Log.d(TAG, "onResponse: No data!!" );
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //(new LineCardOne((CardView)findViewById(R.id.weekly_card),StockDetailActivity.this,close_amt)).show();
                        PastThirty();
                        StringBuilder urlString = new StringBuilder();
                        urlString.append(BASE_URL);
                        try {
                            urlString.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = ","UTF-8"));
                            urlString.append(URLEncoder.encode("\"" + stock_name + "\"","UTF-8"));
                            urlString.append(URLEncoder.encode(" and startDate = " + "\"" + past_thirty + "\"","UTF-8"));
                            urlString.append(URLEncoder.encode(" and endDate = " + "\"" + today_date + "\"","UTF-8"));
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
                History history = gson.fromJson(response.body().string(),new TypeToken<History>(){}.getType());
                if(history.getQuery().getResults()!= null) {
                    ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity> quotes = (ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity>) history.getQuery().getResults().getQuote();

                    Log.d(TAG, "onResponse: after parsing " + String.valueOf(quotes.size()));

                    final ArrayList<String> monthly_close_amt = new ArrayList<String>();
                    final ArrayList<String> dates = new ArrayList<String>();
                    for (int i = 0; i < quotes.size(); i++) {
                        Log.d(TAG, "Data: " +  quotes.get(i).getDate() + " " + quotes.get(i).getAdj_Close());
                        if(i==0){
                            dates.add(quotes.get(i).getDate());
                        }
                        else if(i == quotes.size()-1){
                            dates.add(quotes.get(i).getDate());
                        }
                        else{
                            dates.add("");
                        }
                        monthly_close_amt.add(quotes.get(i).getAdj_Close());
                    }

                    Collections.reverse(monthly_close_amt);
                    Collections.reverse(dates);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new LineCardTwo((CardView) findViewById(R.id.monthly_card), StockDetailActivity.this, monthly_close_amt,dates).show();
                            // new MPLineChart((CardView)findViewById(R.id.monthly_card),StockDetailActivity.this,monthly_close_amt).show();
                        }
                    });
                }
                else{
                    Log.d(TAG, "onResponse: No data!!" );
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
        Log.d(TAG, "Start Date : " + startDate + "    " + "End Date : " + endDate );
    }

    private void PastThirty(){
        Calendar c = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        today_date = df.format(c.getTime());
        c.add(Calendar.DATE,-30);
        past_thirty = df.format(c.getTime());
        Log.d(TAG, "PastThirty: Today's date " + today_date + " thirty days ago " + past_thirty);
    }
}

