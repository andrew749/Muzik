package com.acod.play.app.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.acod.play.app.Activities.HomescreenActivity;
import com.acod.play.app.Activities.PlayerActivity;
import com.acod.play.app.FloatingControl;
import com.acod.play.app.Interfaces.PlayerCommunication;
import com.acod.play.app.R;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Andrew on 6/23/2014.
 */

public class MediaService extends Service implements PlayerCommunication {
    public static boolean isReady = false, playing = false;
    private final IBinder mBinder = new LocalBinder();
    boolean isImageLoading = true;
    private MediaPlayer player = new MediaPlayer();
    private Uri uri;
    private Bundle data;
    private Bitmap albumBitmap = null;
    private PowerManager.WakeLock wakeLock;
    private FloatingControl control;
    private BroadcastReceiver pause, play, stop;
    private boolean switchingTrack = false;
    private boolean startFloating = false;
    private RemoteMediaPlayer remoteMediaPlayer = null;
    private GoogleApiClient mApiClient;
    MediaPlayer.OnPreparedListener mplistener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            //notify the ui that the song is isReady and pass on the various data
            isReady = true;
            if (isImageLoading)
                displayNotification(BitmapFactory.decodeResource(getResources(), R.drawable.musicimage));
            if (control != null && startFloating) {
                openFloat();
            }
            sendBroadcast(new Intent().setAction(PlayerActivity.PLAYER_READY));
            switchingTrack = false;
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(989, new Notification());
        /*If the service isn't already runnign then get data from the bundle*/
        if (data == null)
            data = intent.getBundleExtra("data");
        if (data == null)
            //im assuming is a crash
            return -1;

        //check to see if the floating controls are checked in preferences
        startFloating = data.getBoolean(PlayerActivity.FLOAT_PREFERENCE);
        //if the floating controls don't exist yet
        if (control == null)
            control = new FloatingControl((albumBitmap == null) ? BitmapFactory.decodeResource(getResources(), R.drawable.musicimage) : albumBitmap, getApplicationContext());

        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(mplistener);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                PendingIntent stopIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent().setAction(HomescreenActivity.STOP_ACTION), 0);
                try {
                    stopIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
        });
        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                fallback();
                return false;
            }
        });
        //url to play from data bundle
        uri = Uri.parse(data.getString("url"));
        if (!switchingTrack && !playing) {
            try {
                player.setDataSource(getApplicationContext(), uri);
                player.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                fallback();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                fallback();
            }
        }

        //search for the album art
        FindInfo info = new FindInfo(data.getString("name"));
        info.execute();
        return START_NOT_STICKY;
    }

    public void setRemoteMediaPlayer(RemoteMediaPlayer remoteMediaPlayer, GoogleApiClient mApiClient) {
        this.remoteMediaPlayer = remoteMediaPlayer;
        this.mApiClient = mApiClient;
    }

    @Override
    public void onCreate() {
        registerReceiver(stop = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stop();

            }
        }, new IntentFilter(HomescreenActivity.STOP_ACTION));
        registerReceiver(pause = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                pause();

            }
        }, new IntentFilter(HomescreenActivity.PAUSE_ACTION));
        registerReceiver(play = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                play();
            }
        }, new IntentFilter(HomescreenActivity.PLAY_ACTION));
        PowerManager mgr = (PowerManager) getApplication().getSystemService(Context.POWER_SERVICE);
        wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();

        super.onCreate();
    }

    public boolean isReady() {
        return isReady;
    }

    @Override
    public void onDestroy() {
        closeFloat();
        unregisterReceiver(play);
        unregisterReceiver(pause);
        unregisterReceiver(stop);
        stop();
        wakeLock.release();
        player.release();
        removeNotification();
        super.onDestroy();

    }

    public void displayNotification(Bitmap bm) {

        Intent resultIntent = new Intent(this, PlayerActivity.class);
        resultIntent.putExtra("data", data);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent stopIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(HomescreenActivity.STOP_ACTION), 0);
        PendingIntent pauseIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(HomescreenActivity.PAUSE_ACTION), 0);
        PendingIntent playIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(HomescreenActivity.PLAY_ACTION), 0);
        if (bm == null) {
            bm = BitmapFactory.decodeResource(getResources(), R.drawable.musicimage);
        }
        if (Build.VERSION.SDK_INT >= 16) {
            Notification notification = new Notification.Builder(this).setSmallIcon(R.drawable.playlogo).setLargeIcon(bm).setContentTitle(data.getString("name")).setContentText("Now Playing").addAction(R.drawable.stopbutton, "", stopIntent).addAction(R.drawable.playbutton, "", playIntent).addAction(R.drawable.pausebutton, "", pauseIntent).setOngoing(true).build();

            notification.bigContentView = customNotification(bm);
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notification.contentIntent = pendingIntent;
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            mNotifyMgr.notify(989, notification);
            startForeground(989, notification);
        } else {
            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.playlogo).setLargeIcon(bm).setContentTitle(data.getString("name")).setContentText("Now Playing").addAction(R.drawable.stopbutton, "", stopIntent).addAction(R.drawable.playbutton, "", playIntent).addAction(R.drawable.pausebutton, "", pauseIntent);
            notification.setContentIntent(pendingIntent);

            notification.setOngoing(true);
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            mNotifyMgr.notify(989, notification.build());
            startForeground(989, notification.build());
        }


    }

    public void removeNotification() {
        stopForeground(true);
    }

    public RemoteViews customNotification(Bitmap bm) {
        PendingIntent stopIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(HomescreenActivity.STOP_ACTION), 0);
        PendingIntent pauseIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(HomescreenActivity.PAUSE_ACTION), 0);
        PendingIntent playIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(HomescreenActivity.PLAY_ACTION), 0);
        RemoteViews notification = new RemoteViews(getPackageName(), R.layout.customnotification);
        notification.setTextViewText(R.id.notificationSong, data.getString("name"));
        notification.setImageViewBitmap(R.id.notificationImage, bm);
        notification.setOnClickPendingIntent(R.id.notificationStop, stopIntent);
        notification.setOnClickPendingIntent(R.id.notificationPlay, playIntent);
        notification.setOnClickPendingIntent(R.id.notificationPause, pauseIntent);
        return notification;
    }

    public int getCurrentTime() {
        if (isReady && playing) {
            return player.getCurrentPosition();
        } else {
            return 0;
        }
    }

    public int getMaxTime() {
        if (isReady)
            return player.getDuration();
        else return 0;
    }

    public String getSongURL() {
        return data.getString("url");
    }

    public void switchTrack(Bundle data) {
        switchingTrack = true;
        albumBitmap = null;
        if (playing)
            player.stop();
        player.release();
        this.data = data;
        uri = Uri.parse(data.getString("url"));
        if (control != null && control.viewExists()) {
            control.destroyView();
        }
        try {
            player = new MediaPlayer();
            player.setDataSource(getApplicationContext(), uri);
            player.setOnPreparedListener(mplistener);
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public void fallback() {
        //close fragment
        Intent intent = new Intent();
        intent.setAction(HomescreenActivity.STOP_ACTION);
        sendBroadcast(intent);
        stop();
    }

    public void seekPlayer(int i) {
        player.seekTo(i);
    }

    //play the song
    @Override
    public void play() {
        if (isReady) {
            if (remoteMediaPlayer != null) remoteMediaPlayer.play(mApiClient);
            else player.start();
            playing = true;
        }
    }

    public void openFloat() {
        control.displayControl();
    }

    public void closeFloat() {
        if (control != null && control.viewExists())
            control.destroyView();
    }

    //pause the playback
    @Override
    public void pause() {
        if (isReady) if (remoteMediaPlayer != null) remoteMediaPlayer.pause(mApiClient);
        else player.pause();
        playing = false;
    }

    //stop the song from playing
    @Override
    public void stop() {

        if (isReady) {
            if (remoteMediaPlayer != null) remoteMediaPlayer.stop(mApiClient);
            else player.stop();
            closeFloat();
            stopForeground(true);
            isReady = false;
            player = new MediaPlayer();
            playing = false;

            stopSelf();

        }
    }

    public String getSongName() {
        return (data.getString("name"));
    }

    @Override
    public void seek(int i) {
        player.seekTo(i);
    }

    public void handleImage(Bitmap bm) {
        if (!(bm == null)) {
            displayNotification(bm);
            data.putParcelable("image", bm);


        }
    }

    //determine if the bitmap is isReady
    public boolean bitmapReady() {
        if (albumBitmap == null) {
            return false;
        } else {
            return true;
        }
    }

    public Bitmap getAlbumArt() {
        return albumBitmap;
    }

    @Override
    public boolean onUnbind(Intent intent) {
       /* player.release();
        data = null;
        player = null;*/

        return super.onUnbind(intent);
    }

    //A class to return an instance of this service object
    public class LocalBinder extends Binder {
        public MediaService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MediaService.this;
        }
    }

    /**
     * queries google image search and returns the first image that corresponds to the query string
     */
    public class FindInfo extends AsyncTask<Void, Void, Bitmap> {
        String query;

        public FindInfo(String query) {
            this.query = query;
        }


        @Override
        protected Bitmap doInBackground(Void... voids) {
            URL url = null;
            Uri imageuri = null;
            BufferedReader reader;
            String urlb = null;
            try {
                url = new URL("https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=%22" + Uri.encode(query) + "%22&rsz=8");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            URLConnection urlConnection;
            if (url != null) {
                try {
                    urlConnection = url.openConnection();
                    InputStream io = new BufferedInputStream(urlConnection.getInputStream());
                    reader = new BufferedReader(new InputStreamReader(io));
                    StringBuilder responseStrBuilder = new StringBuilder();
                    String i;
                    while ((i = reader.readLine()) != null)
                        responseStrBuilder.append(i);

                    JSONObject json = new JSONObject(responseStrBuilder.toString());
                    JSONObject object = json.getJSONObject("responseData");
                    JSONArray subobject = object.getJSONArray("results");
                    urlb = subobject.getJSONObject(0).getString("url");
                    if (HomescreenActivity.debugMode) {
                        Log.d("Play", "album image:" + imageuri);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    URL urla = new URL(urlb);
                    albumBitmap = BitmapFactory.decodeStream(urla.openConnection().getInputStream());

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return albumBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {
            super.onPostExecute(bm);
            isImageLoading = false;
            if (bm == null) {
                bm = BitmapFactory.decodeResource(getResources(), R.drawable.musicimage);
            }
            albumBitmap = bm;
            handleImage(bm);
            if (!(control == null)) {
                control.changeImage(bm);
            }
        }
    }
}
