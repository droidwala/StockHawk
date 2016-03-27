package com.sam_chordas.android.stockhawk.widget;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.sam_chordas.android.stockhawk.R;

public class IntervalSettingsActivity extends AppCompatActivity {

    public static final String PREF_NAME = "INTERVAL_PREF";
    private static final String TAG = "IntervalSettings";
    static long[] intervals ={5,15,30,60};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.interval_settings);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        new MaterialDialog.Builder(this)
                .title("Choose Refresh Interval")
                .items(R.array.intervals)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        finish();
                    }
                })
                .itemsCallbackSingleChoice((int) preferences.getLong(PREF_NAME, 1), new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        Intent intent = new Intent(IntervalSettingsActivity.this, QuoteWidgetProvider.class);
                        intent.setAction(QuoteWidgetProvider.INTERVAL_CHANGE);
                        Bundle b = new Bundle();
                        b.putLong("Interval", intervals[which]);
                        intent.putExtras(b);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putLong(PREF_NAME, which);
                        editor.apply();
                        Log.d(TAG, String.valueOf(intervals[which]));
                        sendBroadcast(intent);
                        finish();
                        return true;
                    }
                })
                .positiveText("OK")
                .show();
    }
}
