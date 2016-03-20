package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sam_chordas.android.stockhawk.R;
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
import java.util.Date;
import java.util.Locale;

public class StockDetailActivity extends AppCompatActivity{

    private static final String TAG = "StockDetailActivity";
    ListView lv;
    private final OkHttpClient client = new OkHttpClient();
    private String BASE_URL = "https://query.yahooapis.com/v1/public/yql?q=";
    private String startDate = "",endDate = "";
    ArrayAdapter<String> adapter;

    Date date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_detail);
        lv = (ListView) findViewById(R.id.current_week);
        Bundle b = getIntent().getExtras();
        String stock_name = b.getString("STOCK");
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
                ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity> quotes = (ArrayList<History.QueryEntity.ResultsEntity.QuoteEntity>) history.getQuery().getResults().getQuote();

                Log.d(TAG, "onResponse: after parsing " + String.valueOf(quotes.size())) ;
                ArrayList<String> close_amt = new ArrayList<String>();
                for (int i = 0; i < quotes.size(); i++) {
                    close_amt.add(quotes.get(i).getAdj_Close());
                }
                adapter = new ArrayAdapter<String>(StockDetailActivity.this,android.R.layout.simple_list_item_1,close_amt);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lv.setAdapter(adapter);
                    }
                });
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
}

