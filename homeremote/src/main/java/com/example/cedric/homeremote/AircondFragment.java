package com.example.cedric.homeremote;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


/**
 * Created by cedric on 2/18/16.
 */
public class AircondFragment extends Fragment implements HttpRequest.onHttpRequestComplete {
    private String[] arraySpinner;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_aircond, container, false);

        arraySpinner = new String[] { "Airton", "Fujitsu", "All" };

        NumberPicker tempPicker = (NumberPicker)view.findViewById(R.id.temperature);
        tempPicker.setMinValue(16);
        tempPicker.setMaxValue(32);
        tempPicker.setValue(20);

        Spinner model_spinner = (Spinner)view.findViewById(R.id.model_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.custom_spinner_item, arraySpinner);
        model_spinner.setAdapter(adapter);

        renderGraph();

        return view;
    }

    private void renderGraph() {
        GraphView graph = (GraphView) view.findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);
    }

    @Override
    public void onHttpRequestComplete(String s){

    }
}
