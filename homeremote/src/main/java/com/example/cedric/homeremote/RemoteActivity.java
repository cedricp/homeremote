package com.example.cedric.homeremote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;

public class RemoteActivity extends AppCompatActivity implements HttpRequest.onHttpRequestComplete {
    private ToggleButton[] toggles;
    private Timer timer;
    private StatusUpdaterTask timerTask;
    static String httpServer, port, key;

    private boolean sendHttp(String s){
        if (httpServer.isEmpty() || port.isEmpty()){
            Log.d(">>>", "no sendHttp");
            return false;
        }
        String data = "/control.py?key=" + key + "&" + s;
        String request = "http://" + httpServer + ":" + port + data;
        Log.d(">>>", "Request : " + request);
        (new HttpRequest(this)).execute(request);
        return true;
    }

    private void showMessage(String message){
        Context context = getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        return;
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
            TextView airview = (TextView) findViewById(R.id.AirTemp);
            airview.setText("Air : " + air_temp);
            TextView waterview = (TextView) findViewById(R.id.WaterTemp);
            waterview.setText("Water : " + water_temp);
        }

        catch (JSONException e){
            e.printStackTrace();
            showMessage("Bad server response !");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
        timer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        httpServer = SP.getString("host", "");
        port = SP.getString("port", "");
        key = SP.getString("key", "");
        Log.d(">>>", "Preference host : " + httpServer);
        // Send a request to initialize all buttons status from server
        timerTask = new StatusUpdaterTask(this);
        timer = new Timer();
        timer.schedule(timerTask, 10, 5000);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);

        toggles = new ToggleButton[] {
            (ToggleButton) findViewById(R.id.wallLight1),
            (ToggleButton) findViewById(R.id.wallLight2),
            (ToggleButton) findViewById(R.id.backYardLight),
            (ToggleButton) findViewById(R.id.swimToggle),
            (ToggleButton) findViewById(R.id.counterCurrentButton),
            (ToggleButton) findViewById(R.id.pumpAuto),
            (ToggleButton) findViewById(R.id.pumpForceOn)
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


        ((Button) findViewById(R.id.allOnButton)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String cmd = String.format("setrelay=0,1,2,3&status=1");
                sendHttp(cmd);
            }
        });

        ((Button) findViewById(R.id.allOffButton)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String cmd = String.format("setrelay=0,1,2,3&status=0");
                sendHttp(cmd);
            }
        });

    }

    public void updateFromServer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendHttp("");

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_remote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                }).setNegativeButton("No", null).show();
    }

    private void openSettings() {
        Intent i = new Intent(getApplicationContext(), SettingsRemoteActivity.class);
        startActivityForResult(i, SETTINGS_RESULT);
    }

    private void registerDevice(){
        Intent intent = new Intent(this, GCMRegistrationIntentService.class);
        startService(intent);
    }

    static final int SETTINGS_RESULT = 1;
}



