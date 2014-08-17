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
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;

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
    static MediaService service;
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
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (!(dialog == null))
                        dialog.setMessage(getResources().getString(R.string.progressdialoglongmessage));
                    break;
                case 1:
                    stop();
                    break;
                default:
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
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void oncePrepared() {
        if (!(dialog == null))
            dialog.dismiss();
        dialog = null;
        infoready = true;
        playerFragment.setUpPlayer(service.getMaxTime());
        playerFragment.setUpSongName(service.getSongName(), service.getSongURL());
        songName = service.getSongName();
        maxTime = service.getMaxTime();
        songUrl = service.getSongURL();
        play();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playerlayout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //Create and place fragments into the view
        playerFragment = new PlayerFragment();
        albumFragment = new AlbumFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.playerFragment, playerFragment).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.albumFragment, albumFragment).commit();


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
        EasyTracker.getInstance(this).activityStart(this); // Add this method.

        //Initialize the intent which launches the service
        sintent = new Intent(this, MediaService.class);
        sintent.putExtra("data", getIntent().getBundleExtra("data"));

        startService(sintent);
        bindService(sintent, mConnection, 0);

        visible = true;
        //check if the service is already playing a song and if so switch the song
        if ((service != null && service.isPlaying() && !(getIntent().getBundleExtra("data").getString("url").equals(service.getSongURL()))) || (service != null && !service.isReady())) {
            service.switchTrack(getIntent().getBundleExtra("data"));
            loadDialog();
            updateUI.run();
            playing = false;
        } else {
            if (!(service == null) && service.isReady()) {

                oncePrepared();
                ready = null;
            } else {
                loadDialog();

            }
            updateUI.run();
        }
//Create the recievers to listen for stop events.
        registerReceiver(stop = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stop();
            }
        }, new IntentFilter(HomescreenActivity.STOP_ACTION));


    }

    private void loadDialog() {
        dialog = new ProgressDialog(this);

        dialog.setMessage(getResources().getString(R.string.progressdialogmessage));
        dialog.setIndeterminate(true);
        dialog.setCancelMessage(Message.obtain(handler, 1, 0, 0));
        dialog.show();
        doDialog.start();
        //register the reciever to check if the song is ready
        registerReceiver(ready = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                oncePrepared();
                doDialog.interrupt();
            }
        }, new IntentFilter(PLAYER_READY));

    }

    @Override
    protected void onStop() {
        EasyTracker.getInstance(this).activityStop(this); // Add this method.

        visible = false;
        handler.removeCallbacks(updateUI);
        unbindService(mConnection);
        if (!(stop == null))
            unregisterReceiver(stop);
        if (!(ready == null))
            unregisterReceiver(ready);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        visible = false;
        if (!(dialog == null))
            dialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
