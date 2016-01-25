package com.example.cedric.homeremote;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import java.io.IOException;

/**
 * Created by cedric on 1/21/16.
 */
public class GcmReceiver extends GcmListenerService {
    static int mId = 0;
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("HomeRemote")
                        .setContentText(message);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mId, mBuilder.build());
        mId++;
        playSound();
    }

    void playSound(){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (SP.getBoolean("notification", true) == false)
            return;

        String alarms = SP.getString("notif_sound", "default ringtone");
        Uri uri = Uri.parse(alarms);

        Context context = getApplicationContext();
        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp)
                {
                    mp.release();
                }
            });
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
