package com.mycode.cedric.swGate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.aef.widget.Widget;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.widget.BaseWidget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cedric on 12/9/15.
 */
public class swGateWidget extends BaseWidget implements swGateHttpRequest.onHttpRequestComplete{
    public swGateWidget(WidgetBundle bundle) {
        super(bundle);
    }
    private String httpServer, port, key, server;

    private int numActive = -1;

    @Override
    public void onHttpRequestComplete(String s) {
        if (s.equals("NOK")){
            numActive = -1;
            return;
        }
        try {
            JSONObject jsonOb = new JSONObject(s);
            JSONArray relays = jsonOb.getJSONArray("RELAYSSTAUS");
            numActive = 0;
            for (int i = 0; i < 4; ++i) {
                if (relays.get(i).toString().equals("1")) {
                    numActive++;
                }
            }
        }

        catch (JSONException e){
            e.printStackTrace();
            numActive = -1;
            return;
        }

        showWidget();
    }

    @Override
    public void onCreate() {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mContext);
        httpServer = SP.getString("host", "");
        port = SP.getString("port", "");
        key = SP.getString("key", "");
    }

    @Override
    public void onStartRefresh() {
        scheduleRepeatingRefresh(System.currentTimeMillis(), 600 * 1000,
                swGateExtensionService.EXTENSION_KEY);
        String request = "http://" + httpServer + ":" + port + "/control.py?key=" + key;
        (new swGateHttpRequest(this)).execute(httpServer);
        showWidget();
    }

    @Override
    public void onStopRefresh() {
        // Widget is no longer visible. Stop listening for changes.
        //mPreferences.unregisterOnSharedPreferenceChangeListener(mPreferenceListener);
        cancelScheduledRefresh(swGateExtensionService.EXTENSION_KEY);
    }

    @Override
    public void onScheduledRefresh() {
        // Update widget...
        (new swGateHttpRequest(this)).execute(httpServer);
        showWidget();
    }

    @Override
    public void onObjectClick(final int type, final int layoutReference) {
        Intent intent = new Intent(Control.Intents.CONTROL_START_REQUEST_INTENT);
        intent.putExtra(Control.Intents.EXTRA_AEA_PACKAGE_NAME, mContext.getPackageName());
        sendToHostApp(intent);
    }

    protected void showWidget() {
        // Prepare a bundle with the string and the resource id in the layout
        // that shall be updated.
        String iconExtension = ExtensionUtils.getUriString(mContext, R.mipmap.ic_launcher);

        Bundle bundle1 = new Bundle();
        bundle1.putInt(Widget.Intents.EXTRA_LAYOUT_REFERENCE, R.id.widget_icon);
        bundle1.putString(Control.Intents.EXTRA_DATA_URI, iconExtension);
        Bundle bundle2 = new Bundle();
        bundle2.putInt(Widget.Intents.EXTRA_LAYOUT_REFERENCE, R.id.widget_text);
        if (numActive >= 0)
            bundle2.putString(Control.Intents.EXTRA_TEXT, Integer.toString(numActive));
        else
            bundle2.putString(Control.Intents.EXTRA_TEXT, "X");
        Bundle[] layoutData = new Bundle[2];
        layoutData[0] = bundle1;
        layoutData[1] = bundle2;
        showLayout(getLayoutId(), layoutData);
    }

    protected int getLayoutId() {
        return R.layout.widget_gate_view;
    }

    @Override
    public int getWidth() {
        return (int)(mContext.getResources().getDimension(R.dimen.smart_watch_2_widget_cell_width) * 1);
    }

    @Override
    public int getHeight() {
        return (int)(mContext.getResources().getDimension(R.dimen.smart_watch_2_widget_cell_height) * 2);
    }

    @Override
    public int getPreviewUri() {
        return R.mipmap.ic_launcher;
    }

    @Override
    public int getName() {
        return R.string.widget_name;
    }

}
