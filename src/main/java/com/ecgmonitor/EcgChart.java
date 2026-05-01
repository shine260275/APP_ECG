package com.ecgmonitor;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class EcgChart {

    private LineChart chart;
    private List<Entry> entries;
    private LineDataSet dataSet;
    private LineData lineData;
    private long startTime;

    public EcgChart(Context context) {
        chart = new LineChart(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        chart.setLayoutParams(params);
        
        entries = new ArrayList<>();
        dataSet = new LineDataSet(entries, "ECG");
        dataSet.setColor(0xFF0000FF);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        
        lineData = new LineData(dataSet);
        chart.setData(lineData);
        
        // 配置X轴
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(5);
        
        // 配置Y轴
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMinimum(-2f);
        yAxis.setAxisMaximum(2f);
        yAxis.setDrawGridLines(true);
        
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        
        startTime = System.currentTimeMillis();
    }

    public LineChart getChart() {
        return chart;
    }

    public void addDataPoint(double voltage, long timestamp) {
        float x = (timestamp - startTime) / 1000f; // 转换为秒
        float y = (float) voltage;
        
        entries.add(new Entry(x, y));
        
        // 限制显示最近10秒的数据
        if (entries.size() > 10 * Config.ECG_SAMPLE_RATE_HZ) {
            entries.remove(0);
        }
        
        dataSet.notifyDataSetChanged();
        lineData.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
        
        // 自动滚动到最新数据
        chart.moveViewToX(x);
    }

    public void clear() {
        entries.clear();
        dataSet.notifyDataSetChanged();
        lineData.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
        startTime = System.currentTimeMillis();
    }
}