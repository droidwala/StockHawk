package com.sam_chordas.android.stockhawk.widget;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.sam_chordas.android.stockhawk.R;

/**
 * Used to set up refresh interval timing for widget as per user choice using the dialog
 */
public class IntervalSettingsActivity extends AppCompatActivity {

    public static final String PREF_DIALOG_NUM = "DIALOG_PREF_NUM";
    public static final String PREF_TIME ="INTERVAL_PREF_TIME";
    private static final String TAG = "IntervalSettings";
    static long[] intervals ={5,15,30,60};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        new MaterialDialog.Builder(this)
                .title("Choose Refresh Interval")
                .items(R.array.intervals)
                .theme(Theme.LIGHT)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        finish();
                    }
                })
                .itemsCallbackSingleChoice((int) preferences.getLong(PREF_DIALOG_NUM, 1), new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        //We save the interval value selected by user in Shared Prefs.
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putLong(PREF_TIME, intervals[which]);
                        editor.putLong(PREF_DIALOG_NUM,which);
                        editor.apply();

                        //We send the interval timings back to MyStockActivity for it to update period of periodtask service.
                        Intent task_update_intent = new Intent();
                        Bundle b = new Bundle();
                        b.putLong("Interval", intervals[which]);
                        task_update_intent.putExtras(b);
                        setResult(Activity.RESULT_OK,task_update_intent);
                        finish();
                        return true;
                    }
                })
                .positiveText("OK")
                .show();
    }
}
