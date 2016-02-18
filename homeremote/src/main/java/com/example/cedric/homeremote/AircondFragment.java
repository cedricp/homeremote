package com.example.cedric.homeremote;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

/**
 * Created by cedric on 2/18/16.
 */
public class AircondFragment extends Fragment implements HttpRequest.onHttpRequestComplete {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_aircond, container, false);

        NumberPicker tempPicker = (NumberPicker)view.findViewById(R.id.temperature);
        tempPicker.setMinValue(16);
        tempPicker.setMaxValue(32);
        tempPicker.setValue(20);
        return view;
    }

    @Override
    public void onHttpRequestComplete(String s){

    }
}
