package com.mycode.cedric.swGate;

import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

/**
 * Created by cedric on 12/9/15.
 */
public class swGateExtensionService extends ExtensionService {
    public static final String LOG_TAG = "GateWidgetExtension";
    public static final String EXTENSION_KEY = "com.mycode.cedric.swGate.key";

    public swGateExtensionService() {
        super(EXTENSION_KEY);
    }

    @Override
    protected RegistrationInformation getRegistrationInformation() {
        return new swGateRegistrationInformation(this);
    }

    @Override
    protected boolean keepRunningWhenConnected() {
        // The service only needs to run when the accessory is connected.
        return false;
    }

    @Override
    public ControlExtension createControlExtension(String hostAppPackageName) {
        // Create the control object.
        return new swGateControl(this, hostAppPackageName);
    }
}
