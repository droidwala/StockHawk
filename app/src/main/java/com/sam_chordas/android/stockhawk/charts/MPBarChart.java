package com.sam_chordas.android.stockhawk.charts;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.sam_chordas.android.stockhawk.R;

import java.util.ArrayList;

public class MPBarChart {

    BarChart barChart;
    public MPBarChart(CardView card,ArrayList<String> amounts){
        barChart = (BarChart) card.findViewById(R.id.weekly_bar_chart);
        barChart.setVisibility(View.VISIBLE);
        SettingUpBarChart();
        DrawingChart(amounts);
    }


    private void SettingUpBarChart(){
        //Settings
        barChart.setDrawValueAboveBar(true);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);
        barChart.setDescription("");
        barChart.setNoDataText("");
        barChart.setNoDataTextDescription("");
        barChart.getLegend().setEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setHighlightPerDragEnabled(false);

        Object xObject = barChart.getXAxis();
        ((XAxis)xObject).setPosition(XAxis.XAxisPosition.BOTTOM);
        ((XAxis)xObject).setDrawGridLines(false);
        ((XAxis)xObject).setSpaceBetweenLabels(5);
        ((XAxis)xObject).setLabelsToSkip(0);
        ((XAxis)xObject).setDrawAxisLine(false);
        ((XAxis)xObject).setAxisLineColor(Color.parseColor("#FFFFFF"));
        ((XAxis)xObject).setTextSize(12.0F);
        ((XAxis)xObject).setTextColor(Color.parseColor("#FFFFFF"));



        Object yObject1 = barChart.getAxisLeft();
        ((YAxis)yObject1).setDrawGridLines(false);
        ((YAxis)yObject1).setDrawAxisLine(false);
        ((YAxis)yObject1).setDrawLabels(false);


        Object yObject2 = barChart.getAxisRight();
        ((YAxis)yObject2).setDrawGridLines(false);
        ((YAxis)yObject2).setDrawAxisLine(false);
        ((YAxis)yObject2).setDrawLabels(false);


        barChart.animateY(1500);

        barChart.setBackgroundColor(Color.parseColor("#343f57"));
        barChart.invalidate();
    }


    public MPBarChart(CardView card){
        barChart = (BarChart) card.findViewById(R.id.weekly_bar_chart);
        barChart.setDescription("");
        barChart.setNoDataText("");
        barChart.setNoDataTextDescription("");
        barChart.invalidate();
    }


    private void DrawingChart(ArrayList<String> amounts){
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < amounts.size(); i++) {
            entries.add(new BarEntry(Float.parseFloat(amounts.get(i)),i));
        }

        BarDataSet dataSet = new BarDataSet(entries,"");
        dataSet.setColor(Color.parseColor("#FFFFFF"));
        dataSet.setValueTextColor(Color.parseColor("#FFFFFF"));

        ArrayList<String> labels = new ArrayList<>();
        labels.add("Mon");
        labels.add("Tues");
        labels.add("Wed");
        labels.add("Thurs");
        labels.add("Fri");

        dataSet.setBarSpacePercent(35.0F);

        BarData data = new BarData(labels,dataSet);

        barChart.setData(data);
    }
}
