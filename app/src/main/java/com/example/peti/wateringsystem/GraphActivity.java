package com.example.peti.wateringsystem;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphActivity extends AppCompatActivity {

    private MeasurementViewModel mMeasurementViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        mMeasurementViewModel = ViewModelProviders.of(this).get(MeasurementViewModel.class);
        GraphView graph = (GraphView) findViewById(R.id.graph);
        List<Measurement> measurements=mMeasurementViewModel.getAllMeasurementsStatic();
        Set<String> differentDaysOfMeasurements = new HashSet<>();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        for (Measurement measurement:measurements)
        {
            String tmpDate=new SimpleDateFormat("yyyy-MM-dd").format(measurement.getDate());
            if(!differentDaysOfMeasurements.contains(tmpDate))
            {
                series.appendData(new DataPoint(measurement.getDate(),measurement.getWaterPercentage()),true,1000);
                differentDaysOfMeasurements.add(tmpDate);
            }

        }

        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        Date d1=calendar.getTime();

        series.appendData(new DataPoint(d1,10),true,1000);

        graph.getViewport().setScrollable(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(series.getLowestValueX());
        graph.getViewport().setMaxX(series.getHighestValueX());
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);
        graph.getGridLabelRenderer().setNumHorizontalLabels(2);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        graph.getGridLabelRenderer().setHumanRounding(false);






    }
}
