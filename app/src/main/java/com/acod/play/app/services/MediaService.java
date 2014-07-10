package com.acod.play.app.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.acod.play.app.Activities.HomescreenActivity;
import com.acod.play.app.Activities.PlayerActivity;
import com.acod.play.app.Interfaces.PlayerCommunication;
import com.acod.play.app.R;

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

public class MediaService extends IntentService implements PlayerCommunication {
    public static boolean ready = false;
    private final IBinder mBinder = new LocalBinder();
    MediaPlayer player = new MediaPlayer();
    Uri uri;
    Bundle data;
    Bitmap b = null;
    NotificationManager manager;
    private BroadcastReceiver pause, play, stop;

    public MediaService() {
        super("MediaService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        data = intent.getBundleExtra("data");
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //notify the ui that the song is ready and pass on the various data
                Log.d("Play", "Player Ready");
                ready = true;
                displayNotification(null);
                sendBroadcast(new Intent().setAction(PlayerActivity.PLAYER_READY));
            }
        });
        uri = Uri.parse(data.getString("url"));
        try {
            player.setDataSource(getApplicationContext(), uri);

        } catch (IOException e) {
            e.printStackTrace();
            fallback();
        }

        InitializeService i = new InitializeService();
        i.start();
        findInfo info = new findInfo(data.getString("name"));
        info.execute();
    }

    @Override
    public void onCreate() {
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

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
        super.onCreate();
    }

    public boolean isReady() {
        return ready;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(play);
        unregisterReceiver(pause);
        unregisterReceiver(stop);
        removeNotification();
        super.onDestroy();

    }


    public void displayNotification(Bitmap bm) {
        Log.d("Play", "Displaying Notification");
        Intent resultIntent = new Intent(this, HomescreenActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent stopIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(HomescreenActivity.STOP_ACTION), 0);
        PendingIntent pauseIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(HomescreenActivity.PAUSE_ACTION), 0);
        PendingIntent playIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(HomescreenActivity.PLAY_ACTION), 0);

        if (Build.VERSION.SDK_INT >= 16) {
            Notification notification = new Notification.Builder(this).setSmallIcon(R.drawable.playlogo).setLargeIcon(bm).setContentTitle(data.getString("name")).setContentText("Now Playing").addAction(R.drawable.stopbutton, "", stopIntent).addAction(R.drawable.playbutton, "", playIntent).addAction(R.drawable.pausebutton, "", pauseIntent).build();

            notification.bigContentView = customNotification(bm);
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notification.contentIntent = pendingIntent;
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(989, notification);
        } else {
            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.playlogo).setLargeIcon(bm).setContentTitle(data.getString("name")).setContentText("Now Playing").addAction(R.drawable.stopbutton, "", stopIntent).addAction(R.drawable.playbutton, "", playIntent).addAction(R.drawable.pausebutton, "", pauseIntent);
            notification.setContentIntent(pendingIntent);

            notification.setOngoing(true);
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(989, notification.build());
        }


    }

    public void removeNotification() {
        manager.cancel(989);
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
        if (ready) {
            return player.getCurrentPosition();
        } else {
            return 0;
        }
    }

    public int getMaxTime() {
        if (ready)
            return player.getDuration();
        else return 0;
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

    public void fallback() {
        //close fragment
        Intent intent = new Intent();
        intent.setAction(HomescreenActivity.STOP_ACTION);
        sendBroadcast(intent);
        this.stop();
    }

    public void seekPlayer(int i) {
        player.seekTo(i);
    }

    //play the song
    @Override
    public void play() {
        if (ready) player.start();
    }

    //pause the playback
    @Override
    public void pause() {
        if (ready)
            player.pause();
    }

    //stop the song from playing
    @Override
    public void stop() {

        if (ready) {
            player.stop();
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(989);
            ready = false;
            player = new MediaPlayer();
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
            Log.d("Play", "Done Loading Image");

            displayNotification(bm);
            data.putParcelable("image", bm);


        }
    }

    //determine if the bitmap is ready
    public boolean bitmapReady() {
        if (b == null) {
            return false;
        } else {
            return true;
        }
    }

    public Bitmap getAlbumArt() {
        return b;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.release();
        data = null;
        player = null;

        return super.onUnbind(intent);
    }

    class InitializeService extends Thread {
        @Override
        public void run() {
            try {
                player.prepare();
                Log.d("Play", "Player prepared");
            } catch (IOException e) {
                e.printStackTrace();
                fallback();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                fallback();
            }

        }
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
    public class findInfo extends AsyncTask<Void, Void, Bitmap> {
        String query;

        public findInfo(String query) {
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
                    Log.d("Play", "album image:" + imageuri);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    URL urla = new URL(urlb);
                    b = BitmapFactory.decodeStream(urla.openConnection().getInputStream());

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return b;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {
            super.onPostExecute(bm);

            if (bm == null) {
                bm = BitmapFactory.decodeResource(getResources(), R.drawable.musicimage);

            }
            b = bm;
            handleImage(bm);

        }
    }
}
