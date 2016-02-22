package com.example.cedric.homeremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by cedric on 2/18/16.
 */
public class AircondFragment extends Fragment implements HttpRequest.onHttpRequestComplete {
    static String httpServer, port, key, currentAircond;

    private String[]        arraySpinner, arrayFanSpinner, arrayModeSpinner;
    private View            view;
    private Spinner         modelSpinner;
    private SaneSpinner     fanSpinner, modeSpinner;
    private NumberPicker    tempPicker;
    private Switch          power_switch;
    private ArrayAdapter<String> fanAdapter, modeAdapter;

    private Boolean         can_treat_event = false;
    private ArrayList<HttpRequest> httpReq  = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_aircond, container, false);

        httpReq = new ArrayList<>();

        currentAircond = "airton";

        arraySpinner = new String[] { "Airton", "Fujitsu" };
        arrayFanSpinner = new String[] {"AUTO", "HIGH", "MEDIUM", "LOW", "QUIET"};
        arrayModeSpinner = new String[] {"AUTO", "COOL", "HEAT", "DRY", "FAN"};

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
                }
            }
        });

        modelSpinner = (SaneSpinner)view.findViewById(R.id.model_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.custom_spinner_item, arraySpinner);
        modelSpinner.setAdapter(adapter);

        fanSpinner = (SaneSpinner)view.findViewById(R.id.fan_spinner);
        fanAdapter = new ArrayAdapter<>(getContext(), R.layout.custom_spinner_item, arrayFanSpinner);
        fanSpinner.setAdapter(fanAdapter);


        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!can_treat_event)
                    return;

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

        fanSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!can_treat_event)
                    return;
                Log.i(">>>>", "onMode : " + Integer.valueOf(position));
                String mode = "auto";
                switch (fanAdapter.getItem(position)) {
                    case "AUTO":
                        mode = "auto";
                        break;
                    case "HIGH":
                        mode = "high";
                        break;
                    case "MEDIUM":
                        mode = "mid";
                        break;
                    case "LOW":
                        mode = "low";
                        break;
                    case "QUIET":
                        mode = "quiet";
                        break;
                    default:
                        break;
                }
                sendHttp("aircond_model=" + currentAircond + "&aircond_setfanmode=" + mode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        modeSpinner = (SaneSpinner)view.findViewById(R.id.func_spinner);
        modeAdapter = new ArrayAdapter<>(getContext(), R.layout.custom_spinner_item, arrayModeSpinner);
        modeSpinner.setAdapter(modeAdapter);
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!can_treat_event)
                    return;
                String mode = "auto";
                switch (modeAdapter.getItem(position)) {
                    case "AUTO":
                        mode = "auto";
                        break;
                    case "COOL":
                        mode = "cool";
                        break;
                    case "HEAT":
                        mode = "heat";
                        break;
                    case "FAN":
                        mode = "fan";
                        break;
                    case "DRY":
                        mode = "DRY";
                        break;
                    default:
                        break;
                }
                sendHttp("aircond_model=" + currentAircond + "&aircond_setmode=" + mode);
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
            return;
        }

        if (s.toUpperCase().equals("OK")) {
            return;
        }

        try {
            jsonOb = new JSONObject(s);
            String temperature = jsonOb.getString("ac_temperature");
            String mode = jsonOb.getString("ac_mode");
            String power = jsonOb.getString("ac_power_state");
            String fanmode = jsonOb.getString("ac_fan_mode");

            tempPicker.setValue(Integer.valueOf(temperature));

            if (power.equals("ON"))
                power_switch.setChecked(true);
            else
                power_switch.setChecked(false);

            switch (fanmode){
                case "HIGH":
                    fanSpinner.setSelection(fanAdapter.getPosition("HIGH"), false, true);
                    break;
                case "MEDIUM":
                    fanSpinner.setSelection(fanAdapter.getPosition("MEDIUM"), false, true);
                    break;
                case "LOW":
                    fanSpinner.setSelection(fanAdapter.getPosition("LOW"), false, true);
                    break;
                case "QUIET":
                    fanSpinner.setSelection(fanAdapter.getPosition("QUIET"), false, true);
                    break;
                default:
                case "AUTO":
                    fanSpinner.setSelection(fanAdapter.getPosition("AUTO"), false, true);
                    break;
            }

            switch (mode){
                case "AUTO":
                    modeSpinner.setSelection(modeAdapter.getPosition("AUTO"), false, true);
                    break;
                case "COOL":
                    modeSpinner.setSelection(modeAdapter.getPosition("COOL"), false, true);
                    break;
                case "HEAT":
                    modeSpinner.setSelection(modeAdapter.getPosition("HEAT"), false, true);
                    break;
                case "FAN":
                    modeSpinner.setSelection(modeAdapter.getPosition("FAN"), false, true);
                    break;
                case "DRY":
                    modeSpinner.setSelection(modeAdapter.getPosition("DRY"), false, true);
                    break;
                default:
                    break;
            }
        }
        catch (JSONException e){
            e.printStackTrace();
            showMessage("Unknown server response... {" + s + "}");
        }

        can_treat_event = true;
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
        HttpRequest req = new HttpRequest(this);
        httpReq.add(req);
        req.execute(request);
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        int j = 0;
        while(j < httpReq.size()){
            httpReq.get(j).abort();
            j++;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        int j = 0;
        while(j < httpReq.size()){
            httpReq.get(j).abort();
            j++;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        int j = 0;
        while(j < httpReq.size()){
            httpReq.get(j).abort();
            j++;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

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
