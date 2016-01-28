package com.example.cedric.homeremote;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;


/**
 * Created by cedric on 1/21/16.
 */
public class GCMRegistrationService extends IntentService implements HttpRequest.onHttpRequestComplete {
    private static final String TAG = "GCMRegistrationService";
    public GCMRegistrationService() {
        super(TAG);
        Log.i(TAG, "Service running");
    }

    @Override
    public void onHttpRequestComplete(String s){
        if (s.equals("NOK")){
            Context context = getApplicationContext();
            CharSequence text = "Cannot add GCM service";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        Log.i(TAG, s);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.d(TAG, "GCM Registration Token: " + token);

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token);

        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }
    }

    private boolean sendHttp(String s){
        (new HttpRequest(this)).execute(s);
        return true;
    }

    private void sendRegistrationToServer(String token) {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String host = SP.getString("host", "");
        String port = SP.getString("port", "");
        if (host.isEmpty() || port.isEmpty()) {
            Log.d(TAG, "Host name is empty");
            return;
        }
        String httpServer = "http://" + host + ":" + port + "/control.py?newdevice=";
        Log.d(TAG, "Send GCM id to " + httpServer);
        sendHttp(httpServer + token);
    }
}
