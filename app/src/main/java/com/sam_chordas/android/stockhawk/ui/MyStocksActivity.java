package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.sam_chordas.android.stockhawk.widget.QuoteWidgetProvider;

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
    RecyclerView recyclerView;
    ProgressBar progressBar;
    TextView error_txt;
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
      retry_connection = (Button) findViewById(R.id.retry_connection);
      retry_connection.setOnClickListener(this);
      recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
      fab = (FloatingActionButton) findViewById(R.id.fab);

    // The intent service is for executing immediate pulls from the Yahoo API
    // GCMTaskService can only schedule tasks, they cannot execute immediately
    mServiceIntent = new Intent(this, StockIntentService.class);
      //Registering for ProgressBar Receiver

      IntentFilter filter = new IntentFilter(ProgressBarReceiver.RECEIVER_NAME);
      filter.addCategory(Intent.CATEGORY_DEFAULT);
      receiver = new ProgressBarReceiver();
      registerReceiver(receiver, filter);

    if (savedInstanceState == null){
      // Run the initialize task service so that some stocks appear upon an empty database
      mServiceIntent.putExtra("tag", "init");
      FetchData();

    }

    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

    mCursorAdapter = new QuoteCursorAdapter(this, null);
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
                    // Add the stock to DB
                    mServiceIntent.putExtra("tag", "add");
                    mServiceIntent.putExtra("symbol", input.toString().toUpperCase());
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

    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
    mItemTouchHelper = new ItemTouchHelper(callback);
    mItemTouchHelper.attachToRecyclerView(recyclerView);

    mTitle = getTitle();
    if (CheckConnection()){
      long period = 1800L;
      long flex = 10L;
      String periodicTag = "periodic";

      // create a periodic task to pull stocks once every hour after the app has been opened. This
      // is so Widget data stays up to date.
      PeriodicTask periodicTask = new PeriodicTask.Builder()
          .setService(StockTaskService.class)
          .setPeriod(period)
          .setFlex(flex)
          .setTag(periodicTag)
          .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
          .setRequiresCharging(false)
          .build();
      // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
      // are updated.
      GcmNetworkManager.getInstance(this).schedule(periodicTask);
    }


  }

    private boolean CheckConnection(){
        cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());
    }

    private void FetchData(){
        if (CheckConnection()){
            Log.d(TAG, "FetchData: ");
            retry_connection.setVisibility(View.INVISIBLE);
            error_txt.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
            startService(mServiceIntent);
        }
        else{
            recyclerView.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            retry_connection.setVisibility(View.VISIBLE);
            error_txt.setVisibility(View.VISIBLE);
            error_txt.setText("Seems like you are not connected to internet.\n Please check your network settings!");
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

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    if (id == R.id.action_change_units){
      // this is for changing stock changes from percent value to dollar value
      Utils.showPercent = !Utils.showPercent;
      this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args){
    // This narrows the return to only the stocks that are most current.
      //Since we aren't building history data ourself locally but relying on historical table
      // we will only keep single records  of all the stocks in the table(so removing is_Current column from table)
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


    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.retry_connection){
            Log.d(TAG, "onClick: called " );
            FetchData();
        }
    }

    public class ProgressBarReceiver extends BroadcastReceiver{

        public static final String RECEIVER_NAME ="com.example.intent.action.PROGRESS_BAR";
        @Override
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.INVISIBLE);
            if(error_txt.getVisibility() == View.VISIBLE)
                error_txt.setVisibility(View.INVISIBLE);

            if(intent.getIntExtra("RESULT", 99) == 5) {
                Toast.makeText(context,getResources().getString(R.string.invalid_stock_name), Toast.LENGTH_SHORT).show();
            }
            else if(intent.getIntExtra("RESULT",99) == 6){
                Toast.makeText(context,getResources().getString(R.string.server_busy),Toast.LENGTH_SHORT).show();
            }
            else if(intent.getIntExtra("RESULT",99) == 7){
                recyclerView.smoothScrollToPosition(mCursorAdapter.getItemCount());
                Intent i = new Intent(QuoteWidgetProvider.STOCK_ADDED_INTENT);
                sendBroadcast(i);
            }
            else if(intent.getIntExtra("RESULT",99) == 0){
                Log.d(TAG, "Inside onReceive CAPTAIN!!");
                Intent i = new Intent(QuoteWidgetProvider.STOCK_UPDATED_INTENT);
                sendBroadcast(i);
            }
            else if(intent.getIntExtra("RESULT",99) == 299){
                //Receives intent when items are removed from recyclerview to overcome existing issue in melnykov's FAB library.
                if(!fab.isVisible())
                    fab.show();
                Toast.makeText(context,"Stock Removed from list",Toast.LENGTH_SHORT).show();
            }

        }
    }
}
