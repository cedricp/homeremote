package com.example.cedric.homeremote;

import java.util.TimerTask;

/**
 * Created by cedric on 5/26/15.
 */
public class StatusUpdaterTask extends TimerTask {
    private RemoteActivity activity;

    public StatusUpdaterTask(RemoteActivity remoteActivity) {
        activity = remoteActivity;
    }

    @Override
    public void run() {
        activity.updateFromServer();
    }
}