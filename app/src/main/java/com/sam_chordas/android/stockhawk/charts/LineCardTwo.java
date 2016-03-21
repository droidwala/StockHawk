package com.sam_chordas.android.stockhawk.charts;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.view.View;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.XController;
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


    public LineCardTwo(CardView card,Context context,ArrayList<String> amounts){
        super();
        mChart = (LineChartView) card.findViewById(R.id.monthly_line_chart);
        mContext = context;
        values = new float[amounts.size()];
        labels = new String[amounts.size()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = "";
        }
        for (int j = 0; j < values.length; j++) {
            values[j] = Float.parseFloat(amounts.get(j));
        }
    }

    public void show(){
        LineSet dataSet = new LineSet(labels,values);
        dataSet.setColor(Color.parseColor("#53c1bd"))
                .setFill(Color.parseColor("#3d6c73"))
                .setGradientFill(new int[]{Color.parseColor("#364d5a"), Color.parseColor("#3f7178")}, null);

        mChart.addData(dataSet);

        mChart.setBorderSpacing(1)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setYLabels(AxisController.LabelPosition.NONE)
                .setXAxis(false)
                .setYAxis(false)
                .setBorderSpacing(Tools.fromDpToPx(5));

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

