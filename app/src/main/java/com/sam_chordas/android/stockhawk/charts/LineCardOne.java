package com.sam_chordas.android.stockhawk.charts;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.CardView;
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
import com.sam_chordas.android.stockhawk.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LineCardOne {

    private final Context mContext;

    private final LineChartView mChart;
    private Tooltip mTip;

    private final String[] labels = {"Mon","Tue","Wed","Thu","Fri"};

    float[] values;
    int min,max;

    public LineCardOne(CardView card, Context context, ArrayList<String> amounts){
        super();
        mChart = (LineChartView) card.findViewById(R.id.weekly_bar_chart);
        mContext = context;
        values = new float[amounts.size()];
        for (int i = 0; i < amounts.size(); i++) {
            values[i] = Float.parseFloat(amounts.get(i));
        }



    }

    public void show(){
       LineSet dataset = new LineSet(labels,values);
       dataset.setColor(Color.parseColor("#758cbb"))
               .setFill(Color.parseColor("#2d374c"))
               .setThickness(4);

       mChart.addData(dataset);

       mChart.setBorderSpacing(Tools.fromDpToPx(15))
               .setYLabels(AxisController.LabelPosition.NONE)
               .setXLabels(AxisController.LabelPosition.OUTSIDE)
               .setFontSize(25)
               .setLabelsColor(Color.parseColor("#FFFFFF"))
               .setXAxis(false).setYAxis(false);


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
        Animation anim = new Animation()
                .setEasing(new BounceEase())
                .setEndAction(chartAction);

        mChart.show(anim);

    }



}
