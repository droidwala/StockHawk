package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;
import com.sam_chordas.android.stockhawk.widget.QuoteWidgetProvider;

public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
    implements ItemTouchHelperAdapter{

  private static final String TAG = "QuoteCursorAdapter";
  private static Context mContext;
  private static Typeface robotoLight;
  private boolean isTalkBackOn;
  public QuoteCursorAdapter(Context context, Cursor cursor){
    super(context, cursor);
    mContext = context;
    if(isAccessibilityOn())
       isTalkBackOn = true;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_quote, parent, false);
    ViewHolder vh = new ViewHolder(itemView);
    return vh;
  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor){
    viewHolder.symbol.setText(cursor.getString(cursor.getColumnIndex("symbol")));
    viewHolder.bidPrice.setText(cursor.getString(cursor.getColumnIndex("bid_price")));

    if(isTalkBackOn) {
      String company_name = cursor.getString(cursor.getColumnIndex("company_name"));
      String bid_price = cursor.getString(cursor.getColumnIndex("bid_price"));
      String percent_change = cursor.getString(cursor.getColumnIndex("percent_change"));
      String change = cursor.getString(cursor.getColumnIndex("change"));
      viewHolder.symbol.setContentDescription("Stock name is " + company_name + "Move right to know bid price");
      viewHolder.bidPrice.setContentDescription("Bid price of" + company_name + "stock is" +  bid_price);


      if (Utils.showPercent){
        viewHolder.change.setContentDescription("Percentage Change in " + company_name + "stock is " +  percent_change);
      } else{
        viewHolder.change.setContentDescription("Change in stock price is " + company_name + "stock is " + change);
      }

    }
      int sdk = Build.VERSION.SDK_INT;
    if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1){
      if (sdk < Build.VERSION_CODES.JELLY_BEAN){
        viewHolder.change.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
      }else {
        viewHolder.change.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
      }
    }
    else{
      if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
        viewHolder.change.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
      } else{
        viewHolder.change.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
      }
    }
    if (Utils.showPercent){
      viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("percent_change")));
     }
    else{
      viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("change")));
     }


  }

  @Override
  public void onItemDismiss(int position) {
    Cursor c = getCursor();
    c.moveToPosition(position);
    String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
    Log.d("SwipeRemove", "Removing Stock : " + symbol);
    mContext.getContentResolver().delete(QuoteProvider.Quotes.CONTENT_URI,QuoteColumns.SYMBOL + "=?",new String[]{symbol});
    notifyItemRemoved(position);
    //Below broadcast needs to be sent to MyStocksActivity due to existing(below) issue with melynkov's fab button implementation.
    //This is because when FAB is hidden and we are swiping away stocks from the list to the point when scroll bar disappears,
    //it becomes impossible to recover it without switching orientation or restarting the app.
    //Please refer Issue #168 --> https://github.com.makovkastar/FloatingActionButton/issues/186 for more information

    Intent intent = new Intent(MyStocksActivity.ProgressBarReceiver.RECEIVER_NAME);
    intent.putExtra("RESULT", 299);
    if(c.getCount()==1)
        intent.putExtra("EMPTY",true);
    mContext.sendBroadcast(intent);
  }

  @Override
  public int getItemCount() {
    return super.getItemCount();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder
      implements ItemTouchHelperViewHolder{
    public final TextView symbol;
    public final TextView bidPrice;
    public final TextView change;
    public ViewHolder(View itemView){
      super(itemView);
      symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
      symbol.setTypeface(robotoLight);
      bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
      change = (TextView) itemView.findViewById(R.id.change);
    }

    @Override
    public void onItemSelected() {
      //do nothing
    }

    @Override
    public void onItemClear(){
      itemView.setBackgroundColor(0);
      Intent swipe_intent = new Intent(QuoteWidgetProvider.STOCK_REMOVED_INTENT);
      mContext.sendBroadcast(swipe_intent);
    }

  }

  private boolean isAccessibilityOn(){
    AccessibilityManager am = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
    return am.isEnabled() && am.isTouchExplorationEnabled();
  }

}
