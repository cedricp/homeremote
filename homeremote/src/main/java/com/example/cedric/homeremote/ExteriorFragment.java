package com.example.cedric.homeremote;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by cedric on 1/25/16.
 */
public class ExteriorFragment extends Fragment implements HttpRequest.onHttpRequestComplete {
    private ToggleButton[] toggles;
    private Timer timer;
    private TimerTask timerTask;
    private HttpRequest httpReq = null;
    static String httpServer, port, key;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_remote, container, false);

        toggles = new ToggleButton[] {
                (ToggleButton) view.findViewById(R.id.wallLight1),
                (ToggleButton) view.findViewById(R.id.wallLight2),
                (ToggleButton) view.findViewById(R.id.backYardLight),
                (ToggleButton) view.findViewById(R.id.swimToggle),
                (ToggleButton) view.findViewById(R.id.counterCurrentButton),
                (ToggleButton) view.findViewById(R.id.pumpAuto),
                (ToggleButton) view.findViewById(R.id.pumpForceOn)
        };

        toggles[0].setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean isChecked = ((ToggleButton)view).isChecked();
                String cmd = "setrelay=0&status=" + Integer.valueOf(isChecked == true ? 1 : 0);
                sendHttp(cmd);
            }
        });

        toggles[1].setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean isChecked = ((ToggleButton)view).isChecked();
                String cmd = "setrelay=1&status=" + Integer.valueOf(isChecked == true ? 1 : 0);
                sendHttp(cmd);
            }
        });

        toggles[2].setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean isChecked = ((ToggleButton)view).isChecked();
                String cmd = "setrelay=2&status=" + Integer.valueOf(isChecked == true ? 1 : 0);
                sendHttp(cmd);
            }
        });

        toggles[3].setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean isChecked = ((ToggleButton) view).isChecked();
                String cmd = "setrelay=3&status=" + Integer.valueOf(isChecked == true ? 1 : 0);
                sendHttp(cmd);
            }
        });

        toggles[4].setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean isChecked = ((ToggleButton) view).isChecked();
                String cmd = "setrelay=4&status=" + Integer.valueOf(isChecked == true ? 1 : 0);
                sendHttp(cmd);
            }
        });

        toggles[5].setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean isChecked = ((ToggleButton) view).isChecked();
                String cmd = "setpump=auto&status=" + Integer.valueOf(isChecked == true ? 1 : 0);
                sendHttp(cmd);
            }
        });

        toggles[6].setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean isChecked = ((ToggleButton) view).isChecked();
                String cmd = "setpump=on&status=" + Integer.valueOf(isChecked == true ? 1 : 0);
                sendHttp(cmd);
            }
        });


        ((Button) view.findViewById(R.id.allOnButton)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String cmd = String.format("setrelay=0,1,2,3&status=1");
                sendHttp(cmd);
            }
        });

        ((Button) view.findViewById(R.id.allOffButton)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String cmd = String.format("setrelay=0,1,2,3&status=0");
                sendHttp(cmd);
            }
        });
        return view;
    }

    @Override
    public void onHttpRequestComplete(String s){
        JSONObject jsonOb;

        if (s.equals("NOK")){
            showMessage("Cannot connect to home server !");
            return;
        }

        try {
            jsonOb = new JSONObject(s);
            JSONArray relays = jsonOb.getJSONArray("RELAYSSTAUS");

            for (int i = 0; i < relays.length(); ++i){
                boolean checked = relays.get(i).toString().equals("1") ? true : false;
                this.toggles[i].setChecked(checked);
            }

            JSONArray pump = jsonOb.getJSONArray("PUMP");

            if (pump.getString(0).equals("auto")){
                this.toggles[5].setChecked(true);
                this.toggles[6].setChecked(false);
            }
            if (pump.getString(0).equals("on")){
                this.toggles[5].setChecked(false);
                this.toggles[6].setChecked(true);
            }
            if (pump.getString(0).equals("off")){
                this.toggles[5].setChecked(false);
                this.toggles[6].setChecked(false);
            }

            String water_temp = jsonOb.getString("WATERTEMP");
            String air_temp = jsonOb.getString("AIRTEMP");
            TextView airview = (TextView) getView().findViewById(R.id.AirTemp);
            airview.setText("Air : " + air_temp);
            TextView waterview = (TextView) getView().findViewById(R.id.WaterTemp);
            waterview.setText("Water : " + water_temp);
        }
        catch (JSONException e){
            e.printStackTrace();
            showMessage("Bad server response !");
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

    private boolean sendHttp(String s){
        if (httpServer.isEmpty() || port.isEmpty()){
            Log.d(">>>", "no sendHttp");
            return false;
        }
        String data = "/control.py?key=" + key + "&" + s;
        String request = "http://" + httpServer + ":" + port + data;
        Log.d(">>>", "Request : " + request);
        httpReq = new HttpRequest(this);
        httpReq.execute(request);
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (httpReq != null) httpReq.abort();
        timer.cancel();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (httpReq != null) httpReq.abort();
        timer.cancel();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (httpReq != null) httpReq.abort();
        timer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getContext());
        httpServer = SP.getString("host", "");
        port = SP.getString("port", "");
        key = SP.getString("key", "");
        Log.d(">>>", "Preference host : " + httpServer);
        // Send a request to initialize all buttons status from server
        timerTask = new TimerTask() {
            @Override
            public void run() {
                updateFromServer();
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 10, 10000);

        registerDevice();

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
                        registerDevice();
                    }
                };
    }

    public void updateFromServer() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendHttp("");
            }
        });
    }

    private void registerDevice(){
        Intent intent = new Intent(getActivity(), GCMRegistrationService.class);
        getActivity().startService(intent);
    }

}
