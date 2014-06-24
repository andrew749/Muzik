package com.acod.play.app;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;

/**
 * Created by Andrew on 6/23/2014.
 */
public class MediaService extends IntentService implements PlayerCommunication {
    private final IBinder mBinder = new LocalBinder();
    MediaPlayer player = new MediaPlayer();
    boolean ready = false;
    Uri uri;
    Bundle data;

    public MediaService() {
        super("MediaService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onCreate() {

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        player.stop();
        player.release();
        super.onDestroy();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        data = intent.getBundleExtra("data");
        displayNotification();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //notify the ui that the song is ready and pass on the various data
                ready = true;
                play();
            }
        });
        uri = Uri.parse(data.getString("url"));
        try {
            player.setDataSource(getApplicationContext(), uri);
            player.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);


    }

    public void displayNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(data.getString("name"))
                        .setContentText("Playing");

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(989, mBuilder.build());

    }

    public long getCurrentTime() {
        return player.getCurrentPosition();
    }

    public int getMaxTime() {
        return player.getDuration();
    }

    public void switchTrack(String url) {
        uri = Uri.parse(url);
        try {
            player.setDataSource(getApplicationContext(), uri);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seekPlayer(int i) {
        player.seekTo(i);
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    //play the song
    @Override
    public void play() {
        player.start();
    }

    //pause the playback
    @Override
    public void pause() {
        player.pause();
    }

    //stop the song from playing
    @Override
    public void stop() {
        player.stop();
        player = new MediaPlayer();
    }

    public interface ready {
        public void mediaReady();
    }

    //A class to return an instance of this service object
    public class LocalBinder extends Binder {
        MediaService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MediaService.this;
        }
    }

}
