package com.mycode.cedric.swGate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by cedric on 12/9/15.
 */
public class swGateExtensionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(swGateExtensionService.LOG_TAG, "onReceive: " + intent.getAction());
        intent.setClass(context, swGateExtensionService.class);
        context.startService(intent);
    }
}