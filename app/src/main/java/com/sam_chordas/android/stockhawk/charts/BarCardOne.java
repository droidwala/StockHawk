package com.sam_chordas.android.stockhawk.charts;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;

import com.db.chart.Tools;
import com.db.chart.model.BarSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.BarChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.XController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.LinearEase;
import com.sam_chordas.android.stockhawk.R;

import java.util.ArrayList;

public class BarCardOne {

    private final Context mContext;

    private final BarChartView mChart;
    private Tooltip mTip;

    private final String[] labels = {"Mon","Tue","Wed","Thu","Fri"};

    float[] values;

    int min_value,max_value;
    int MIN,MAX,STEP;
    private static final String TAG = "BarCardOne";



    public BarCardOne(CardView card, Context context, ArrayList<String> amounts){
        super();
        mChart = (BarChartView) card.findViewById(R.id.weekly_bar_chart);
        mContext = context;
        values = new float[amounts.size()];
        for (int i = 0; i < amounts.size(); i++) {
            values[i] = Float.parseFloat(amounts.get(i));
        }

        MinMaxAndStepLogic();

    }

    public void show(){
        BarSet barSet = new BarSet(labels,values);
        barSet.setColor(Color.parseColor("#FFFFFF"));

        mChart.addData(barSet);

        mChart.setBarSpacing(Tools.fromDpToPx(40));
        mChart.setBackgroundColor(Color.parseColor("#343f57"));


        mChart.setXAxis(false)
                .setYAxis(true)
                .setXLabels(AxisController.LabelPosition.OUTSIDE)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setLabelsColor(Color.parseColor("#FFFFFF"));


        Log.d(TAG, "BarcardOne: Inside method show " + " MIN " + String.valueOf(MIN) +
                " MAX " + String.valueOf(MAX) + " STEP " + String.valueOf(STEP));


        if(MIN > 0 && MAX > 0) //For cases where stock price is less than 1 don't set up axisbordervalues instead use default ones.
            mChart.setAxisBorderValues(MIN, MAX, STEP);


        int[] order = {1,0,2,3,4};

        Runnable chartAction = new Runnable() {
            @Override
            public void run() {
                showTooltip();
            }
        };

        mChart.show(new Animation()
                .setOverlap(.7f, order)
                .setEndAction(chartAction)
                .setEasing(new LinearEase()));

    }


    private void MinMaxAndStepLogic(){

        min_value = (int) MinMaxHelper.getMin_value(values);
        max_value = (int) MinMaxHelper.getMax_value(values);

        MIN = MinMaxHelper.getMinGraphValue(min_value);
        MAX = MinMaxHelper.getMaxGraphValue(max_value);
        STEP = MinMaxHelper.GCD(MAX, MIN);

        Log.d(TAG, "BarcardOne: initialize " + " MIN " + String.valueOf(MIN) +
        " MAX " + String.valueOf(MAX) + " STEP " + String.valueOf(STEP));
    }


    private void showTooltip(){

        mTip = new Tooltip(mContext,R.layout.barchart_tooltip,R.id.value);

        mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        mTip.setDimensions((int) Tools.fromDpToPx(80), (int) Tools.fromDpToPx(25));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

            mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);
        }

        mTip.setMargins(0,0,0, (int) Tools.fromDpToPx(10));
        mTip.prepare(mChart.getEntriesArea(0).get(0),values[0]);
        mChart.showTooltip(mTip,true);
        mChart.setTooltips(mTip);

    }

}
