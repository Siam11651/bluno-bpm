package com.siam11651.bluno_bpm.Utils;

import android.app.Activity;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Stack;
import java.util.TimerTask;
import java.util.Vector;

import kotlin.Pair;

public class DataUpdateTimerTask extends TimerTask
{
    private final Activity activity;
    private final Vector<Entry> systoleEntries;
    private final SignalData signalData;
    private final LineData lineData;
    private final LineDataSet systoleDataSet;
    private final LineChart chart;
    private final TextView systoleTextView;
    private int count;
    private float signalValueBaseline, signalSum;
    private final ArrayDeque<Float> peakValue, peakTime, throughValue, throughTime;
    private boolean recording;
    private final Vector<Pair<Float, Float>> signalValueTimeRecord;

    public DataUpdateTimerTask(Activity activity, Vector<Entry> systoleEntries, LineData lineData, LineDataSet systoleDataSet, LineChart chart, TextView systoleTextView)
    {
        signalData = SignalData.GetSignalData();
        this.activity = activity;
        this.systoleEntries = systoleEntries;
        this.lineData = lineData;
        this.systoleDataSet = systoleDataSet;
        this.chart = chart;
        this.systoleTextView = systoleTextView;
        count = 0;
        peakValue = new ArrayDeque<>();
        peakTime = new ArrayDeque<>();
        throughValue = new ArrayDeque<>();
        throughTime = new ArrayDeque<>();
        signalValueBaseline = 0;
        signalSum = 0;
        signalValueTimeRecord = new Vector<>();
    }

    public void ToggleRecording()
    {
        recording = !recording;

        if(recording)
        {
            signalValueTimeRecord.clear();
        }
    }

    public boolean IsRecording()
    {
        return recording;
    }

    public Vector<Pair<Float, Float>> GetSignalValueTimeRecord()
    {
        return signalValueTimeRecord;
    }

    @Override
    public void run()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                String data = signalData.GetSignalDataValue();

                if(systoleEntries.size() > 50)
                {
                    systoleEntries.remove(0);
                }

                float dataFloat = Float.parseFloat(data);

                systoleEntries.add(new Entry((float)count / 10, dataFloat));

                ++count;
                signalSum += dataFloat;
                signalValueBaseline = signalSum / count;

                if(recording)
                {
                    Pair<Float, Float> signalValueTime = new Pair<>(dataFloat, (float)count / 10);

                    signalValueTimeRecord.add(signalValueTime);
                }

                systoleDataSet.notifyDataSetChanged();
                lineData.notifyDataChanged();
                chart.notifyDataSetChanged();
                chart.invalidate();
                systoleTextView.setText(data);
            }
        });
    }
}
