package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;
import com.sam_chordas.android.stockhawk.widget.IntervalSettingsActivity;
import com.sam_chordas.android.stockhawk.widget.QuoteWidgetProvider;

/**
 * MainActivity to display stocks either added by user or pre-defined stocks.
 */
public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,View.OnClickListener{

    private static final String TAG = "MyStocksActivity";
    private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;
    private ProgressBarReceiver receiver;
    private static Toast progress_toast;


    private static final int UPDATE_INTERVAL_REQ_CODE = 1000;
    private static final int DEFAULT_RESULT_CODE = 99;

    private static final int NO_STOCK_FOUND = 5;
    private static final int SERVER_ISSUE = 6;
    private static final int NEW_STOCK_ADDED = 7;
    private static final int FETCH_ERROR = 2;
    private static final int STOCKS_UPDATED = 0;
    private static final int STOCK_REMOVED = 299;


    RecyclerView recyclerView;
    ProgressBar progressBar;
    TextView error_txt,no_stocks_txt;
    Button retry_connection;
    FloatingActionButton fab;
    ConnectivityManager cm;
    NetworkInfo activeNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_my_stocks);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        error_txt = (TextView) findViewById(R.id.error_txt);
        no_stocks_txt = (TextView) findViewById(R.id.no_stock_txt);
        retry_connection = (Button) findViewById(R.id.retry_connection);
        retry_connection.setOnClickListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent(this, StockIntentService.class);

        //Registering for ProgressBar Receiver to receive respond to events received from StockTaskService
        IntentFilter filter = new IntentFilter(ProgressBarReceiver.RECEIVER_NAME);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ProgressBarReceiver();
        registerReceiver(receiver, filter);

        if (savedInstanceState == null){
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "init");
            FetchData();
        }

        //Setting up layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Setting up loader manager to load listview with data from db..
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(this, null);

        //Handling recyclerview row item clicks
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        Log.d(TAG, "onItemClick: called " + String.valueOf(position));
                        Intent i = new Intent(MyStocksActivity.this,StockDetailActivity.class);
                        Bundle b = new Bundle();
                        Cursor c = mCursorAdapter.getCursor();
                        c.moveToPosition(position);
                        b.putString("STOCK", c.getString(c.getColumnIndex(QuoteColumns.SYMBOL)));
                        b.putString("Name", c.getString(c.getColumnIndex(QuoteColumns.COMPANY_NAME)));
                        b.putString("YearLow",c.getString(c.getColumnIndex(QuoteColumns.YEAR_LOW)));
                        b.putString("YearHigh", c.getString(c.getColumnIndex(QuoteColumns.YEAR_HIGH)));
                        b.putString("MarketValue", c.getString(c.getColumnIndex(QuoteColumns.MARKET_VALUE)));

                        i.putExtras(b);
                        startActivity(i);
                    }
                }));
        recyclerView.setAdapter(mCursorAdapter);

        //Searching Stocks by Name
        fab.attachToRecyclerView(recyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            if (CheckConnection()){
              new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                  .content(R.string.content_test)
                  .inputType(InputType.TYPE_CLASS_TEXT)
                  .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                    @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                      // On FAB click, receive user input. Make sure the stock doesn't already exist
                      // in the DB and proceed accordingly
                      Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                          new String[] { QuoteColumns.SYMBOL }, QuoteColumns.SYMBOL + "= ?",
                          new String[] { input.toString().toUpperCase() }, null);
                      if (c.getCount() != 0) {
                        Toast toast =
                            Toast.makeText(MyStocksActivity.this,getResources().getString(R.string.stock_already_saved),
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                        toast.show();
                        return;
                      } else {
                        // Adding the stock to DB using StockIntentService
                        mServiceIntent.putExtra("tag", "add");
                        mServiceIntent.putExtra("symbol", input.toString().toUpperCase());
                        progress_toast = Toast.makeText(MyStocksActivity.this,"Fetching...Please Wait",Toast.LENGTH_SHORT);
                        progress_toast.show();
                        startService(mServiceIntent);
                      }
                        if(c!=null)
                            c.close();
                    }
                  })
                  .show();
            } else {
              networkToast();
            }

          }
        });

        //Used for handling swipe events
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        mTitle = getTitle();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long period = preferences.getLong(IntervalSettingsActivity.PREF_TIME,15L) * 60;
        long flex = 10L;
        String periodicTag = "periodic";

        // creates a periodic task to pull stocks as per user defined interval settings saved in preferences
        // so widget data stays up to date.
        PeriodicTask periodicTask = new PeriodicTask.Builder()
                .setService(StockTaskService.class)
                .setPeriod(period)
                .setFlex(flex)
                .setPersisted(true)
                .setTag(periodicTag)
                .setUpdateCurrent(true)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setRequiresCharging(false)
                .build();
        // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
        // are updated.
        GcmNetworkManager.getInstance(this).schedule(periodicTask);




    }

    //Checks for network connectivity of the device
    private boolean CheckConnection(){
        cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());
    }
    //Starts StockIntentService to fetch stock's data to be displayed in Recyclerview
    private void FetchData(){
        if (CheckConnection()){
            Log.d(TAG, "FetchData: ");
            retry_connection.setVisibility(View.INVISIBLE);
            error_txt.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            fab.setVisibility(View.INVISIBLE);
            startService(mServiceIntent);
        }
        else{
            recyclerView.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            retry_connection.setVisibility(View.VISIBLE);
            error_txt.setVisibility(View.VISIBLE);
            error_txt.setText(getResources().getString(R.string.not_connected));
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(receiver!=null)
            unregisterReceiver(receiver);
    }

    public void networkToast(){
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    public void restoreActionBar() {
      ActionBar actionBar = getSupportActionBar();
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
      actionBar.setDisplayShowTitleEnabled(true);
      actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();

      //Used to set up interval settings to refresh widget..
      if (id == R.id.action_settings) {
        Intent i = new Intent(MyStocksActivity.this, IntervalSettingsActivity.class);
        startActivityForResult(i,UPDATE_INTERVAL_REQ_CODE);
        return true;
      }

      if (id == R.id.action_change_units){
        // this is for changing stock changes from percent value to dollar value
        Utils.showPercent = !Utils.showPercent;
        this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
      }

      return super.onOptionsItemSelected(item);
    }


    //Update Periodtask period as set by user in Refresh Interval dialog..
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        long flex = 10L;
        String periodicTag = "periodic";
        if(requestCode==UPDATE_INTERVAL_REQ_CODE){
            if(resultCode==RESULT_OK){
                long period = data.getExtras().getLong("Interval") * 60;
                Log.d(TAG, "onActivityResult: " + String.valueOf(period));
                PeriodicTask periodicTask = new PeriodicTask.Builder()
                        .setService(StockTaskService.class)
                        .setPeriod(period)
                        .setFlex(flex)
                        .setPersisted(true)
                        .setTag(periodicTag)
                        .setUpdateCurrent(true)
                        .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                        .setRequiresCharging(false)
                        .build();
                GcmNetworkManager.getInstance(this).schedule(periodicTask);
            }
        }
    }

      @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
      // Since we aren't building history data ourself locally but relying on historical table of yahoo api
      // we will only keep single records  of all the stocks in the table(so removing is_Current column from table)
      // Also few more columns added for storing additional information related to stock
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
          new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
              QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP,
              QuoteColumns.COMPANY_NAME,QuoteColumns.YEAR_LOW,QuoteColumns.YEAR_HIGH,
              QuoteColumns.MARKET_VALUE},
          null,
          null,
          null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
      mCursorAdapter.swapCursor(data);
      mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
      mCursorAdapter.swapCursor(null);
    }

    //Retry fetching data during bad network conditions
    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.retry_connection){
            Log.d(TAG, "onClick: called " );
            FetchData();
        }
    }

    //Listens to all responses received from StockIntentService class
    public class ProgressBarReceiver extends BroadcastReceiver{
        public static final String RECEIVER_NAME ="com.example.intent.action.PROGRESS_BAR";
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.INVISIBLE);
            if(error_txt.getVisibility() == View.VISIBLE)
                error_txt.setVisibility(View.INVISIBLE);

            //Triggered when response comes with no stock with name searched
            if(intent.getIntExtra("RESULT", DEFAULT_RESULT_CODE) == NO_STOCK_FOUND) {
                Toast.makeText(context,getResources().getString(R.string.invalid_stock_name), Toast.LENGTH_SHORT).show();
            }
            //Triggered when error occurs parsing response
            else if(intent.getIntExtra("RESULT",DEFAULT_RESULT_CODE) == FETCH_ERROR){
                Toast.makeText(context,getResources().getString(R.string.fetching_error),Toast.LENGTH_SHORT).show();
                retry_connection.setVisibility(View.VISIBLE);
            }
            //Triggered when response isn't received due to client-server connection issue
            else if(intent.getIntExtra("RESULT",DEFAULT_RESULT_CODE) == SERVER_ISSUE){
                Toast.makeText(context,getResources().getString(R.string.server_busy),Toast.LENGTH_SHORT).show();
            }
            //Triggered when new stock is added to list
            else if(intent.getIntExtra("RESULT",DEFAULT_RESULT_CODE) == NEW_STOCK_ADDED){
                if(no_stocks_txt.getVisibility()==View.VISIBLE)
                    no_stocks_txt.setVisibility(View.INVISIBLE);
                if(progress_toast!=null)
                    progress_toast.cancel();
                if(!fab.isVisible())
                    fab.show();
                recyclerView.smoothScrollToPosition(mCursorAdapter.getItemCount());
                Intent i = new Intent(QuoteWidgetProvider.STOCK_ADDED_INTENT);
                sendBroadcast(i);
            }
            //Triggered when user opens up the activity and list needs to be updated with new stock values
            else if(intent.getIntExtra("RESULT",DEFAULT_RESULT_CODE) == STOCKS_UPDATED){
                if(no_stocks_txt.getVisibility()==View.VISIBLE)
                    no_stocks_txt.setVisibility(View.INVISIBLE);
                fab.setVisibility(View.VISIBLE);
                Intent i = new Intent(QuoteWidgetProvider.STOCK_UPDATED_INTENT);
                sendBroadcast(i);
            }
            //Triggered when stock is removed from the list
            else if(intent.getIntExtra("RESULT",DEFAULT_RESULT_CODE) == STOCK_REMOVED){
                //Receives intent when items are removed from recyclerview to overcome existing issue in melnykov's FAB library.
                if(intent.getBooleanExtra("EMPTY",false)){
                    no_stocks_txt.setVisibility(View.VISIBLE);
                }
                else {
                    no_stocks_txt.setVisibility(View.INVISIBLE);
                }
                if(!fab.isVisible())
                    fab.show();
                Toast.makeText(context,"Stock Removed from list",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
