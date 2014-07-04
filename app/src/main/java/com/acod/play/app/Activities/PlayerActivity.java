package com.acod.play.app.Activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;

import com.acod.play.app.Interfaces.PlayerCommunication;
import com.acod.play.app.R;
import com.acod.play.app.fragments.AlbumFragment;
import com.acod.play.app.fragments.PlayerFragment;
import com.acod.play.app.services.MediaService;

/**
 * Created by andrew on 03/07/14.
 */
public class PlayerActivity extends Activity implements PlayerCommunication {

    Handler handler = new Handler();
    PlayerFragment playerFragment;
    AlbumFragment albumFragment;
    MediaService service;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaService.LocalBinder binder = (MediaService.LocalBinder) iBinder;
            service = binder.getService();
            playerFragment.setUpPlayer(service.getMaxTime());
            play();
            h2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (service.bitmapReady()) {
                        doneLoading(service.getAlbumArt());
                        h2.removeCallbacks(this);
                    }
                }
            }, 1000);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    private Intent sintent;
    private Handler h2 = new Handler();
    private Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            playerFragment.updateTime(milliSecondsToTimer(service.getCurrentTime()));
            playerFragment.changeSeek((int) service.getCurrentTime());
            handler.postDelayed(this, 1000);
        }
    };

    //convert the given song time in milleseconds to a readable string.
    public static String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playerlayout);


        playerFragment = (PlayerFragment) getFragmentManager().findFragmentById(R.id.playerFragment);
        albumFragment = (AlbumFragment) getFragmentManager().findFragmentById(R.id.albumFragment);
        sintent = new Intent(this, MediaService.class);
        sintent.putExtra("data", getIntent().getBundleExtra("data"));
        bindService(sintent, mConnection, Context.BIND_AUTO_CREATE);
        startService(sintent);

    }

    //set the imageview of the album to the appropriate image
    public void doneLoading(Bitmap bm) {
        if (!(bm == null))
            albumFragment.setArt(bm);
        else
            albumFragment.setArt(BitmapFactory.decodeResource(getResources(), R.drawable.musicimage));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void play() {
        updateUI.run();
        service.play();
    }

    @Override
    public void pause() {
        service.pause();
    }

    @Override
    public void stop() {
        service.stop();
        finish();
    }

    @Override
    public void seek(int i) {
        service.seekPlayer(i);
    }
}
