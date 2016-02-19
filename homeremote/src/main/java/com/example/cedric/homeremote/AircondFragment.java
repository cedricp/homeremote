package com.example.cedric.homeremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by cedric on 2/18/16.
 */
public class AircondFragment extends Fragment implements HttpRequest.onHttpRequestComplete {
    private HttpRequest httpReq = null;
    static String httpServer, port, key, currentAircond;

    private String[]        arraySpinner;
    private View            view;
    private Spinner         model_spinner;
    private NumberPicker    tempPicker;
    private Switch          power_switch;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_aircond, container, false);

        currentAircond = "airton";

        arraySpinner = new String[] { "Airton", "Fujitsu" };

        tempPicker = (NumberPicker)view.findViewById(R.id.temperature);
        tempPicker.setMinValue(16);
        tempPicker.setMaxValue(32);
        tempPicker.setValue(20);

        tempPicker.setOnScrollListener(new NumberPicker.OnScrollListener() {

            private int oldValue;  //You need to init this value.

            @Override
            public void onScrollStateChange(NumberPicker numberPicker, int scrollState) {
                if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                    sendHttp("aircond_settemp=" + Integer.valueOf(tempPicker.getValue()) + "&aircond_model=" + currentAircond);
                    Log.i(">>>", "val " + Integer.valueOf(tempPicker.getValue()));
                }
            }
        });

        model_spinner = (Spinner)view.findViewById(R.id.model_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.custom_spinner_item, arraySpinner);
        model_spinner.setAdapter(adapter);
        model_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                    currentAircond = "airton";
                else
                    currentAircond = "fujitsu";
                sendHttp("aircond_status=" + currentAircond);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        power_switch = (Switch)view.findViewById(R.id.power_switch);

        power_switch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String state = ((Switch) view).isChecked() ? "on" : "off";
                sendHttp("aircond_setpower=" + state + "&aircond_model=" + currentAircond);
            }
        });
        //renderGraphHumidity();
        //renderGraphTemperature();
        loadPrefs();
        sendHttp("aircond_status="+currentAircond);
        return view;
    }

    @Override
    public void onHttpRequestComplete(String s){
        JSONObject jsonOb;

        if (s.equals("NOK")){
            showMessage("Cannot connect to home server !");
            httpReq = null;
            return;
        }

        if (s.toUpperCase().equals("OK")) {
            httpReq = null;
            return;
        }

        try {
            jsonOb = new JSONObject(s);
            String temperature = jsonOb.getString("ac_temperature");
            tempPicker.setValue(Integer.valueOf(temperature));
            String power = jsonOb.getString("ac_power_state");
            if (power.equals("ON"))
                power_switch.setChecked(true);
            else
                power_switch.setChecked(false);
        }
        catch (JSONException e){
            e.printStackTrace();
            showMessage("Unknown server response...");
        }
        httpReq = null;
    }

    private void showMessage(String message){
        Context context = getActivity().getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        return;
    }

    private void renderGraphHumidity(float[] temp_data) {
        GraphView graph = (GraphView) view.findViewById(R.id.graph_hum);
        graph.setTitle("Humidity history");
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);
    }

    private void renderGraphTemperature() {
        GraphView graph = (GraphView) view.findViewById(R.id.graph_temp);
        graph.setTitle("Temperature history");
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);
    }

    private boolean sendHttp(String s){
        if (httpServer.isEmpty() || port.isEmpty()){
            return false;
        }
        String data = "/control.py?key=" + key + "&" + s;
        String request = "http://" + httpServer + ":" + port + data;
        httpReq = new HttpRequest(this);
        httpReq.execute(request);
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (httpReq != null) httpReq.abort();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (httpReq != null) httpReq.abort();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (httpReq != null) httpReq.abort();
    }

    @Override
    public void onResume() {
        super.onResume();


        // Send a request to initialize all buttons status from server
//        timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                updateFromServer();
//            }
//        };
//        timer = new Timer();
//        timer.schedule(timerTask, 10, 10000);
        SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
                SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                          String k) {
                        if (k.equals("host")){
                            httpServer = sharedPreferences.getString("host", "");
                        }
                        if (k.equals("port")){
                            httpServer = sharedPreferences.getString("port", "");
                        }
                        if (k.equals("key")){
                            key = sharedPreferences.getString("key", "");
                        }
                        Log.d(">>>", "Preference changed : " + k);
                    }
                };

    }

    private void loadPrefs(){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getContext());
        httpServer = SP.getString("host", "");
        port = SP.getString("port", "");
        key = SP.getString("key", "");
    }
}
