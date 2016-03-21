package com.sam_chordas.android.stockhawk.charts;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;

import java.util.ArrayList;

public class MPLineChart {

    private LineChart mChart;

    private ArrayList<Entry> entries = new ArrayList<>();

    private ArrayList<String> labels = new ArrayList<>();

    public MPLineChart(CardView card, Context context, ArrayList<String> amounts) {
        super();
        mChart = (LineChart) card.findViewById(R.id.monthly_line_chart);
        for (int i = 0; i < amounts.size(); i++) {
            entries.add(new Entry(Float.parseFloat(amounts.get(i)), i));
        }

        for (int j = 0; j < amounts.size(); j++) {
            labels.add("");
        }

    }

    public void show() {

        LineDataSet dataset = new LineDataSet(entries, "");
        LineData data = new LineData(labels, dataset);

        dataset.setDrawCubic(true);
        dataset.setDrawFilled(true);

        //dataset.setDrawFilled(true);

        mChart.getXAxis().setDrawGridLines(false);
        mChart.setDescription("");
        mChart.setData(data);
        mChart.invalidate();



    }

}







