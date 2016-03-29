package com.sam_chordas.android.stockhawk.charts;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
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

public class LineCardThree {


    private final LineChartView mChart;

    private final String[] labels;

    private final float[] values;
    private Context mContext;
    private Tooltip mTip;
    int min_value,max_value;
    int MIN,MAX,STEP;
    private static final String TAG = "LineCardThree";

    public LineCardThree(CardView card,Context context,ArrayList<String> amounts,ArrayList<String> dates){
        super();
        mChart = (LineChartView) card.findViewById(R.id.sixty_days_line_chart);
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

        min_value = (int) MinMaxHelper.getMin_value(values);
        max_value = (int) MinMaxHelper.getMax_value(values);

        MIN = MinMaxHelper.getMinGraphValue(min_value);
        MAX = MinMaxHelper.getMaxGraphValue(max_value);
        STEP = MinMaxHelper.GCD(MAX,MIN);

        Log.d(TAG, "LineCardTwo: " + String.valueOf(MIN));
    }
    public void show(){
        LineSet dataSet = new LineSet(labels,values);
        dataSet.setColor(Color.parseColor("#ad4b4b"))
                .setFill(Color.parseColor("#ffcc6a"))
                .setGradientFill(new int[]{Color.parseColor("#8c3838"), Color.parseColor("#a71717")}, null);

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

