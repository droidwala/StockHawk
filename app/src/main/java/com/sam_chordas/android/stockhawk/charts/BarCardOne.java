package com.sam_chordas.android.stockhawk.charts;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.BarSet;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.BarChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.db.chart.view.animation.easing.LinearEase;
import com.sam_chordas.android.stockhawk.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

        min_value = (int) getMin_value(values);
        max_value = (int) getMax_value(values);

        //Min logic
        if(min_value > 0 && min_value < 10){
            MIN = 0;
        }
        else if(min_value > 9 && min_value < 99){
            MIN = min_value - (min_value % 10);//2 digit
        }
        else if(min_value > 99 && min_value < 1000){
            MIN = min_value - (min_value % 100);//3 digit
        }
        else if(min_value > 999 && min_value < 10000){
            MIN = min_value - (min_value % 1000);//4 digit
        }
        else if(min_value > 9999 && max_value < 100000){
            MIN = min_value - (min_value % 10000);// 5 digit
        }
        else if(min_value==0){
            MIN = 0;
        }

        //Max logic
        if(max_value > 0 && max_value < 9){
            MAX = 10;
        }
        else if(max_value > 9 && max_value < 99){
            MAX = (max_value - (max_value % 10)) + 10;//2 digit
        }
        else if(max_value > 99 && max_value < 1000){
            MAX = (max_value - (max_value % 100)) + 100;//3 digit
        }
        else if(max_value > 999 && max_value < 10000){
            MAX = (max_value - (max_value % 1000)) + 1000;//4 digit
        }
        else if(max_value > 9999 && max_value < 100000){
            MAX = (max_value - (max_value % 10000)) + 10000;// 5 digit
        }
        else if(max_value == 0){
            MAX = 0;
        }

        //STEP logic

        STEP = GCD(MAX,MIN);

        Log.d(TAG, "BarcardOne: initialize " + " MIN " + String.valueOf(MIN) +
        " MAX " + String.valueOf(MAX) + " STEP " + String.valueOf(STEP));
    }

    private int GCD(int a,int b){
        if(b==0) return a;
        return GCD(b,a%b);
    }

    private float getMin_value(float[] inputArray){

        float minvalue = inputArray[0];

        for(int i=1;i<=inputArray.length - 1;i++){
            if(inputArray[i] !=0 && inputArray[i] < minvalue){
                minvalue = inputArray[i];
            }
        }

        return minvalue;
    }

    private float getMax_value(float[] inputArray){

        float maxvalue = inputArray[0];

        for (int i = 1; i <= inputArray.length - 1 ; i++) {
            if(inputArray[i] > maxvalue){
                maxvalue = inputArray[i];
            }
        }

        //Log.d(TAG, "getMin_value: " + String.valueOf(inputArray.length));
        return maxvalue;
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

            //mTip.setPivotX(Tools.fromDpToPx(65) / 2);
            //mTip.setPivotY(Tools.fromDpToPx(25));

        }

        mTip.setMargins(0,0,0, (int) Tools.fromDpToPx(10));
        mTip.prepare(mChart.getEntriesArea(0).get(0),values[0]);
        mChart.showTooltip(mTip,true);
        mChart.setTooltips(mTip);

    }

}
