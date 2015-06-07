package com.acod.play.app.Services;

import android.app.Notification;
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
import android.widget.RemoteViews;

import com.acod.play.app.Activities.HomescreenActivity;
import com.acod.play.app.Activities.PlayerActivity;
import com.acod.play.app.Interfaces.PlayerCommunication;
import com.acod.play.app.Models.STATE;
import com.acod.play.app.Models.SongResult;
import com.acod.play.app.R;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

/**
 * Created by Andrew on 6/23/2014.
 */

public class MediaService extends Service implements PlayerCommunication {
    /*Chromecast stuff*/
    private static RemoteMediaPlayer remoteMediaPlayer = null;
    private final IBinder mBinder = new LocalBinder();
    public STATE.PLAY_STATE state = STATE.PLAY_STATE.NULL;
    boolean isImageLoading = true;
    SongResult currentSong;
    private MediaPlayer player = new MediaPlayer();
    private Uri uri;
    private Bundle data;
    MediaPlayer.OnPreparedListener mplistener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            //notify the ui that the song is isReady and pass on the various data
            state = STATE.PLAY_STATE.PAUSED;
            if (isImageLoading)
                displayNotification(BitmapFactory.decodeResource(getResources(), R.drawable.musicimage));
            sendBroadcast(new Intent().setAction(PlayerActivity.PLAYER_READY));
        }
    };
    private Bitmap albumBitmap = null;
    private PowerManager.WakeLock wakeLock;
    private BroadcastReceiver pause, play, stop;
    private PLAYING_DEVICE currentPlayingDevice = PLAYING_DEVICE.THIS;
    private GoogleApiClient mApiClient;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //creates the notification to run
        startForeground(989, new Notification());
        /*If the service isn't already runnign then get data from the bundle*/
        if (data == null)
            data = intent.getBundleExtra("data");
        //I'm assuming is a crash because I havent got any data.
        if (data == null)
            return -1;

        currentSong = new SongResult(data.getString("name"), data.getString("url"));

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

        new LinkCleaner(currentSong.url).execute();
        loadImageWithName(currentSong.name);

        return START_NOT_STICKY;
    }

    public void doPlayer(String s) {
        if(s!=null) {
            uri = Uri.parse(s);
            currentSong.updateURL(uri.toString());
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
        }else{fallback();}
    }

    public void loadImageWithName(String name) {
        //search for the album art
        FindInfo info = new FindInfo(name);
        info.execute();
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

    @Override
    public void onDestroy() {
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
            startForeground(989, notification);
        } else {
            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.playlogo).setLargeIcon(bm).setContentTitle(data.getString("name")).setContentText("Now Playing").addAction(R.drawable.stopbutton, "", stopIntent).addAction(R.drawable.playbutton, "", playIntent).addAction(R.drawable.pausebutton, "", pauseIntent);
            notification.setContentIntent(pendingIntent);
            notification.setOngoing(true);
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
        if (state == STATE.PLAY_STATE.PLAYING || state == STATE.PLAY_STATE.PAUSED) {
            if (currentPlayingDevice == PLAYING_DEVICE.THIS)
                return player.getCurrentPosition();
            else return (int) remoteMediaPlayer.getApproximateStreamPosition();
        } else {
            return 0;
        }
    }

    public int getMaxTime() {
        if (state == STATE.PLAY_STATE.PLAYING || state == STATE.PLAY_STATE.PAUSED)
            return player.getDuration();
        else return 0;
    }

    public String getSongURL() {
        return currentSong.url;
    }


    public void switchTrack(Bundle data) {
        if (state == STATE.PLAY_STATE.PLAYING) {
            if (remoteMediaPlayer != null && currentPlayingDevice == PLAYING_DEVICE.CHROMECAST)
                remoteMediaPlayer.stop(mApiClient);
            else {
                player.stop();
                player.release();
            }
        }
        state = STATE.PLAY_STATE.STOPPED;
        this.data = data;
        currentSong = new SongResult(data.getString("name"), data.getString("url"));
        removeNotification();
        displayNotification(null);
        try {
            uri = Uri.parse(new LinkCleaner(currentSong.url).execute().get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (remoteMediaPlayer != null && currentPlayingDevice == PLAYING_DEVICE.CHROMECAST) {
            MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
            mediaMetadata.putString(MediaMetadata.KEY_TITLE, data.getString("name"));
            MediaInfo mediaInfo = new MediaInfo.Builder(uri.toString())
                    .setContentType("audio/mpeg")
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setMetadata(mediaMetadata)
                    .build();
            remoteMediaPlayer.load(mApiClient, mediaInfo, true)
                    .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult) {
                            if (mediaChannelResult.getStatus().isSuccess()) {
                                state = STATE.PLAY_STATE.PLAYING;
                            }
                        }
                    });
        } else
            try {
                player = new MediaPlayer();
                player.setDataSource(getApplicationContext(), uri);
                player.setOnPreparedListener(mplistener);
                player.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        loadImageWithName(currentSong.getName());
    }

    public void switchToChromeCast(final GoogleApiClient mApiClient) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, data.getString("name"));
        MediaInfo mediaInfo = new MediaInfo.Builder(uri.toString())
                .setContentType("audio/mpeg")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();
        if (currentPlayingDevice == PLAYING_DEVICE.THIS) {
            final long currentTime = player.getCurrentPosition();
            remoteMediaPlayer = new RemoteMediaPlayer();
            remoteMediaPlayer.load(mApiClient, mediaInfo, true).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                @Override
                public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult) {
                    currentPlayingDevice = PLAYING_DEVICE.CHROMECAST;
                    state = STATE.PLAY_STATE.PLAYING;
                    remoteMediaPlayer.seek(mApiClient, currentTime);
                }
            });
            state = STATE.PLAY_STATE.LOADING;
            player.stop();
            player = null;
        }
    }

    public void removeChromeCast() {
        long currentTime;
        if (currentPlayingDevice == PLAYING_DEVICE.CHROMECAST) {
            currentTime = remoteMediaPlayer.getApproximateStreamPosition();
            remoteMediaPlayer.stop(mApiClient);
        }
        try {
            player = new MediaPlayer();
            player.setDataSource(getApplicationContext(), uri);
            player.setOnPreparedListener(mplistener);
            player.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
        currentPlayingDevice = PLAYING_DEVICE.THIS;
    }

    public void fallback() {
        //close fragment
        Intent intent = new Intent();
        intent.setAction(HomescreenActivity.STOP_ACTION);
        sendBroadcast(intent);
        stop();
    }

    //play the song
    @Override
    public void play() {
        if (state == STATE.PLAY_STATE.PAUSED) {
            if (remoteMediaPlayer != null && currentPlayingDevice == PLAYING_DEVICE.CHROMECAST)
                remoteMediaPlayer.play(mApiClient);
            else player.start();
            state = STATE.PLAY_STATE.PLAYING;
        }
    }

    //pause the playback
    @Override
    public void pause() {
        if (state == STATE.PLAY_STATE.PLAYING)
            if (remoteMediaPlayer != null && currentPlayingDevice == PLAYING_DEVICE.CHROMECAST)
                remoteMediaPlayer.pause(mApiClient);
            else player.pause();
        state = STATE.PLAY_STATE.PAUSED;
    }

    //stop the song from playing
    @Override
    public void stop() {
        if (remoteMediaPlayer != null && currentPlayingDevice == PLAYING_DEVICE.CHROMECAST)
            remoteMediaPlayer.stop(mApiClient);
        else if (state == STATE.PLAY_STATE.PLAYING || state == STATE.PLAY_STATE.PAUSED)
            player.stop();
        state = STATE.PLAY_STATE.STOPPED;
        stopForeground(true);
        stopSelf();
    }

    public String getSongName() {
        return (currentSong.getName());
    }

    @Override
    public void seek(int i) {
        if (currentPlayingDevice == PLAYING_DEVICE.THIS) {
            player.seekTo(i);
        } else if (currentPlayingDevice == PLAYING_DEVICE.CHROMECAST) {
            remoteMediaPlayer.seek(mApiClient, i);
        }
    }

    @Override
    public STATE.PLAY_STATE currentState() {
        return state;
    }

    public void handleImage(Bitmap bm) {
        if (!(bm == null)) {
            displayNotification(bm);
            data.putParcelable("image", bm);
        }
    }

    public Bitmap getAlbumArt() {
        return albumBitmap;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private enum PLAYING_DEVICE {THIS, CHROMECAST}

    class LinkCleaner extends AsyncTask<Void, Void, String> {
        String link;

        public LinkCleaner(String link) {
            this.link = link;
        }

        private String linkCleaner(String u) {
            String t = null;
            try {
                URL url = new URL(u);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setInstanceFollowRedirects(true);
                conn.getResponseCode();
                t = conn.getURL().toString().replace(" ", "%20");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return t;
        }

        @Override
        protected String doInBackground(Void... voids) {
            return linkCleaner(link);
        }

        @Override
        protected void onPostExecute(String s) {
            doPlayer(s);
            super.onPostExecute(s);
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
    public class FindInfo extends AsyncTask<Void, Void, Bitmap> {
        String query;

        public FindInfo(String query) {
            this.query = query;
        }


        @Override
        protected Bitmap doInBackground(Void... voids) {
            URL url = null;
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
            Intent intent = new Intent();
            intent.setAction(PlayerActivity.IMAGE_READY);
            sendBroadcast(intent);
        }
    }
}
