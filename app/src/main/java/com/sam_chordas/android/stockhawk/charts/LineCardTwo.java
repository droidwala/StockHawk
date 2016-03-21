package com.sam_chordas.android.stockhawk.charts;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.sam_chordas.android.stockhawk.R;



import java.util.ArrayList;

public class LineCardTwo {


    private final LineChartView mChart;

    private final String[] labels;

    private final float[] values;
    private Context mContext;
    private Tooltip mTip;
    int min_value,max_value;
    int MIN,MAX,STEP;
    private static final String TAG = "LineCardTwo";

    public LineCardTwo(CardView card,Context context,ArrayList<String> amounts,ArrayList<String> dates){
        super();
        mChart = (LineChartView) card.findViewById(R.id.monthly_line_chart);
        mContext = context;
        values = new float[amounts.size()];
        labels = new String[amounts.size()];
        for (int i = 0; i < dates.size(); i++) {
            labels[i] = dates.get(i);
        }
        for (int j = 0; j < values.length; j++) {
            values[j] = Float.parseFloat(amounts.get(j));
        }
        MinMaxAndStepLogic();

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

        Log.d(TAG, "LineCardTwo: " + String.valueOf(MIN) );
    }

    private int GCD(int a,int b){
        if(b==0) return a;
        return GCD(b,a%b);
    }

    private float getMin_value(float[] inputArray){

        float minvalue = inputArray[0];

        for(int i=1;i<=inputArray.length - 1;i++){
            if(inputArray[i] < minvalue){
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

    public void show(){
        LineSet dataSet = new LineSet(labels,values);
        dataSet.setColor(Color.parseColor("#53c1bd"))
                .setFill(Color.parseColor("#3d6c73"))
                .setGradientFill(new int[]{Color.parseColor("#364d5a"), Color.parseColor("#3f7178")}, null);

        mChart.addData(dataSet);

        mChart.setXLabels(AxisController.LabelPosition.OUTSIDE)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setAxisLabelsSpacing(Tools.fromDpToPx(3))
                .setLabelsColor(Color.parseColor("#FFFFFF"))
                .setXAxis(true)
                .setYAxis(true);
        Log.d(TAG, "show: " + "MIN " + String.valueOf(MIN) +
                " \nMAX " + String.valueOf(MAX) +
                " \nSTEP " + String.valueOf(STEP));

        if(MIN > 0 && MAX > 0) //For cases where stock price is less than 1.
        mChart.setAxisBorderValues(MIN, MAX, STEP);

        mTip = new Tooltip(mContext,R.layout.linechart_tooltip,R.id.value);

        mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        mTip.setDimensions((int) Tools.fromDpToPx(80), (int) Tools.fromDpToPx(25));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

            mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

            mTip.setPivotX(Tools.fromDpToPx(65) / 2);
            mTip.setPivotY(Tools.fromDpToPx(25));
        }


        mChart.setTooltips(mTip);

        Runnable chartAction = new Runnable() {
            @Override
            public void run() {
                mTip.prepare(mChart.getEntriesArea(0).get(3),values[3]);
                mChart.showTooltip(mTip,true);
            }
        };


        Animation anim = new Animation().setEasing(new BounceEase()).setEndAction(chartAction);

        mChart.show(anim);
    }
}

