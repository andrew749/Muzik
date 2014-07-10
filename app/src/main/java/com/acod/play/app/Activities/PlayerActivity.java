package com.acod.play.app.Activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

import com.acod.play.app.Interfaces.PlayerCommunication;
import com.acod.play.app.R;
import com.acod.play.app.fragments.AlbumFragment;
import com.acod.play.app.fragments.PlayerFragment;
import com.acod.play.app.services.MediaService;

/**
 * Created by andrew on 03/07/14.
 */
public class PlayerActivity extends FragmentActivity implements PlayerCommunication {

    public static final String PLAYER_READY = "com.acod.play.app.ready";
    public static boolean playing = false;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    dialog.setMessage(getResources().getString(R.string.progressdialoglongmessage));
                    break;
                case 1:
                    finish();
                    break;
            }
        }
    };
    Thread doDialog = new Thread() {

        long startTime = System.currentTimeMillis();

        @Override
        public void run() {

            while (System.currentTimeMillis() - startTime < 10000) {

            }
            handler.sendMessage(Message.obtain(handler, 0, 0, 0));
        }
    };
    //handler for ui update
    PlayerFragment playerFragment;
    AlbumFragment albumFragment;
    MediaService service;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaService.LocalBinder binder = (MediaService.LocalBinder) iBinder;
            service = binder.getService();
            checkBitmap.run();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    ProgressDialog dialog;
    private Intent sintent;
    private BroadcastReceiver stop, ready;
    private Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            playerFragment.updateTime(service.getCurrentTime());
            handler.postDelayed(this, 1000);
        }
    };
    private Runnable checkBitmap = new Runnable() {
        @Override
        public void run() {
            if (service.bitmapReady()) {
                doneLoadingImage(service.getAlbumArt());
                handler.removeCallbacks(this);
            } else {
                handler.postDelayed(this, 1000);
            }
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

    private void oncePrepared() {
        playerFragment.setUpPlayer(service.getMaxTime());
        playerFragment.setUpSongName(service.getSongName());
        play();
        updateUI.run();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playerlayout);
        dialog = new ProgressDialog(this);

        playerFragment = (PlayerFragment) getFragmentManager().findFragmentById(R.id.playerFragment);
        albumFragment = (AlbumFragment) getFragmentManager().findFragmentById(R.id.albumFragment);
        Log.d("Play", "Player Created UI");
        sintent = new Intent(this, MediaService.class);
        sintent.putExtra("data", getIntent().getBundleExtra("data"));
        bindService(sintent, mConnection, Context.BIND_AUTO_CREATE);
        startService(sintent);
        registerReceiver(stop = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stop();
            }
        }, new IntentFilter(HomescreenActivity.STOP_ACTION));
        dialog.setMessage(getResources().getString(R.string.progressdialogmessage));
        dialog.setIndeterminate(true);
        dialog.setCancelMessage(Message.obtain(handler, 1, 0, 0));
        dialog.show();
        doDialog.start();

        registerReceiver(ready = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                oncePrepared();
                dialog.dismiss();
                doDialog.interrupt();
            }
        }, new IntentFilter(PLAYER_READY));
    }

    //set the imageview of the album to the appropriate image
    public void doneLoadingImage(Bitmap bm) {
        if (!(bm == null))
            albumFragment.setArt(bm);
        else
            albumFragment.setArt(BitmapFactory.decodeResource(getResources(), R.drawable.musicimage));
    }


    @Override
    protected void onStop() {
        unregisterReceiver(stop);
        unregisterReceiver(ready);
        finish();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        stopService(sintent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void play() {
        if (!playing && (!(service == null)) && service.isReady()) {
            service.play();
            playing = true;
        }
    }

    @Override
    public void pause() {
        if (!(service == null)) {
            service.pause();
        }
        playing = false;
    }

    @Override
    public void stop() {
        if (!(service == null)) {
            service.stop();
        }
        handler.removeCallbacks(updateUI);
        playing = false;
        finish();
    }

    @Override
    public void seek(int i) {
        service.seekPlayer(i);
    }
}
