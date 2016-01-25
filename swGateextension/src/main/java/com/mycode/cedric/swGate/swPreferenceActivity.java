package com.mycode.cedric.swGate;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;

/**
 * Created by cedric on 1/25/16.
 */
public class swPreferenceActivity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource.
        addPreferencesFromResource(R.xml.preferences);
    }


}
