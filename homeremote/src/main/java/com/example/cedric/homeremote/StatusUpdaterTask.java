package com.example.cedric.homeremote;

import java.util.TimerTask;

/**
 * Created by cedric on 5/26/15.
 */
public class StatusUpdaterTask extends TimerTask {
    private ExteriorFragment fragment;

    public StatusUpdaterTask(ExteriorFragment frag) {
        fragment = frag;
    }

    @Override
    public void run() {
        fragment.updateFromServer();
    }
}