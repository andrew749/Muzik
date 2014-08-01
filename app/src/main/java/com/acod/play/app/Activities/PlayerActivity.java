package com.acod.play.app.Activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.acod.play.app.Interfaces.PlayerCommunication;
import com.acod.play.app.R;
import com.acod.play.app.fragments.AlbumFragment;
import com.acod.play.app.fragments.PlayerFragment;
import com.acod.play.app.services.MediaService;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * Created by andrew on 03/07/14.
 */
public class PlayerActivity extends SherlockFragmentActivity implements PlayerCommunication {

    public static final String PLAYER_READY = "com.acod.play.app.ready";
    public static boolean playing = false, visible = true;
    private Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            if (playing && visible) {
                playerFragment.updateTime(service.getCurrentTime());
                handler.postDelayed(this, 1000);
            }
        }
    };
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
    boolean infoready = false;
    boolean imageready = false;
    private Bitmap art;
    private String songName;
    private int maxTime;
    private String songUrl;
    //handler for ui update
    private PlayerFragment playerFragment;
    private AlbumFragment albumFragment;
    private Intent sintent;
    private BroadcastReceiver stop, ready;
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.playerlayout);

        playerFragment = new PlayerFragment();
        albumFragment = new AlbumFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.playerFragment, playerFragment).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.albumFragment, albumFragment).commit();
        getSupportFragmentManager().executePendingTransactions();
        playerFragment.setUpPlayer(maxTime);
        playerFragment.setUpSongName(songName, songUrl);
        albumFragment.setArt(art);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                stop();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void oncePrepared() {
        infoready = true;
        playerFragment.setUpPlayer(service.getMaxTime());
        playerFragment.setUpSongName(service.getSongName(), service.getSongURL());
        songName = service.getSongName();
        maxTime = service.getMaxTime();
        songUrl = service.getSongURL();
        play();
        updateUI.run();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playerlayout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        dialog = new ProgressDialog(this);

        playerFragment = new PlayerFragment();
        albumFragment = new AlbumFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.playerFragment, playerFragment).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.albumFragment, albumFragment).commit();
        sintent = new Intent(this, MediaService.class);
        sintent.putExtra("data", getIntent().getBundleExtra("data"));
        dialog.setMessage(getResources().getString(R.string.progressdialogmessage));
        dialog.setIndeterminate(true);
        dialog.setCancelMessage(Message.obtain(handler, 1, 0, 0));
        dialog.show();
        doDialog.start();
        startService(sintent);
        bindService(sintent, mConnection, BIND_AUTO_CREATE);

    }

    //set the imageview of the album to the appropriate image
    public void doneLoadingImage(Bitmap bm) {
        imageready = true;
        if (!(bm == null))
            albumFragment.setArt(bm);

        else {
            bm = BitmapFactory.decodeResource(getResources(), R.drawable.musicimage);
            albumFragment.setArt(bm);
        }
        art = bm;

    }

    @Override
    protected void onStart() {
        super.onStart();
        visible = true;
        updateUI.run();
        registerReceiver(stop = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stop();
            }
        }, new IntentFilter(HomescreenActivity.STOP_ACTION));


        registerReceiver(ready = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                oncePrepared();
                dialog.dismiss();
                doDialog.interrupt();
            }
        }, new IntentFilter(PLAYER_READY));
    }

    @Override
    protected void onStop() {
        visible = false;
        handler.removeCallbacks(updateUI);
        unregisterReceiver(stop);
        unregisterReceiver(ready);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        finish();

    }

    @Override
    protected void onResume() {
        super.onResume();
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
            handler.removeCallbacks(updateUI);
            updateUI.run();
        }
    }


    @Override
    public void pause() {
        if (!(service == null)) {
            service.pause();
        }
        playing = false;
        handler.removeCallbacks(updateUI);
    }

    @Override
    public void stop() {
        if (!(service == null)) {
            service.stop();
        }
        playing = false;
        finish();
    }

    @Override
    public void seek(int i) {
        service.seekPlayer(i);
    }
}
