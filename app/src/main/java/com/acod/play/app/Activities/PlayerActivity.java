package com.acod.play.app.Activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.view.Menu;
import android.view.MenuItem;

import com.acod.play.app.Interfaces.PlayerCommunication;
import com.acod.play.app.R;
import com.acod.play.app.fragments.AlbumFragment;
import com.acod.play.app.fragments.PlayerFragment;
import com.acod.play.app.services.MediaService;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.IOException;

import static android.support.v4.view.MenuItemCompat.getActionProvider;

/**
 * Created by andrew on 03/07/14.
 */
public class PlayerActivity extends ActionBarActivity implements PlayerCommunication {

    public static final String PLAYER_READY = "com.acod.play.app.ready";
    public static final String FLOAT_PREFERENCE = "floatingtoggle";
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
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastDevice mSelectedDevice;
    private GoogleApiClient mApiClient;
    private RemoteMediaPlayer mRemoteMediaPlayer;
    private Cast.Listener mCastClientListener;
    private boolean mWaitingForReconnect = false;
    private boolean mApplicationStarted = false;
    private boolean mSongIsLoaded;
    private boolean mIsPlaying;




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
    private Runnable checkBitmap = new Runnable() {
        @Override
        public void run() {
            if (service != null && service.bitmapReady()) {
                doneLoadingImage(service.getAlbumArt());
                handler.removeCallbacks(this);
            } else {
                handler.postDelayed(this, 1000);
            }
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
        getFragmentManager().beginTransaction().replace(R.id.playerFragment, playerFragment).commit();
        getFragmentManager().beginTransaction().replace(R.id.albumFragment, albumFragment).commit();
        getFragmentManager().executePendingTransactions();
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
        if(playerFragment!=null&&service!=null) {
            playerFragment.setUpPlayer(service.getMaxTime());
            playerFragment.setUpSongName(service.getSongName(), service.getSongURL());
            songName = service.getSongName();
            maxTime = service.getMaxTime();
            songUrl = service.getSongURL();
            play();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playerlayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.nowplaying));
        //Create and place fragments into the view
        playerFragment = new PlayerFragment();
        albumFragment = new AlbumFragment();
        getFragmentManager().beginTransaction().replace(R.id.playerFragment, playerFragment).commit();
        getFragmentManager().beginTransaction().replace(R.id.albumFragment, albumFragment).commit();
    initMediaRouter();
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
        Bundle mdata = getIntent().getBundleExtra("data");
        mdata.putBoolean(FLOAT_PREFERENCE, getPreferences(MODE_PRIVATE).getBoolean(FLOAT_PREFERENCE, true));
        sintent.putExtra("data", mdata);

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
    public boolean onCreateOptionsMenu( Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.playermenu, menu);
        MenuItem mediaRouteMenuItem = menu.findItem( R.id.media_route_menu_item );
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector( mMediaRouteSelector );
        final SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        if (pref.getBoolean(FLOAT_PREFERENCE, true)) {
            menu.findItem(R.id.floattoggle).setChecked(true);
        } else {
            menu.findItem(R.id.floattoggle).setChecked(false);

        }
        menu.findItem(R.id.floattoggle).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences.Editor editor = pref.edit();
                if (pref.getBoolean(FLOAT_PREFERENCE, true)) {
                    editor.putBoolean(FLOAT_PREFERENCE, false);
                    item.setChecked(false);
                    service.closeFloat();
                } else {
                    editor.putBoolean(FLOAT_PREFERENCE, true);
                    item.setChecked(true);
                    service.openFloat();
                }
                editor.commit();
                return false;
            }
        });
    return true;
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
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);

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
    private void initCastClientListener() {
        mCastClientListener = new Cast.Listener() {
            @Override
            public void onApplicationStatusChanged() {
            }

            @Override
            public void onVolumeChanged() {
            }

            @Override
            public void onApplicationDisconnected( int statusCode ) {
                   teardown();
           }
        };
    }
    private void initRemoteMediaPlayer() {
        mRemoteMediaPlayer = new RemoteMediaPlayer();
        mRemoteMediaPlayer.setOnStatusUpdatedListener( new RemoteMediaPlayer.OnStatusUpdatedListener() {
            @Override
            public void onStatusUpdated() {
                MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
                mIsPlaying = mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING;
            }
        });

        mRemoteMediaPlayer.setOnMetadataUpdatedListener( new RemoteMediaPlayer.OnMetadataUpdatedListener() {
            @Override
            public void onMetadataUpdated() {
            }
        });
    }
    private void startVideo() {
        MediaMetadata mediaMetadata = new MediaMetadata( MediaMetadata.MEDIA_TYPE_MOVIE );
        mediaMetadata.putString( MediaMetadata.KEY_TITLE, songName  );

        MediaInfo mediaInfo = new MediaInfo.Builder( songUrl )
                .setContentType( "song/mp3" )
                .setStreamType( MediaInfo.STREAM_TYPE_BUFFERED )
                .setMetadata( mediaMetadata )
                .build();
        try {
            mRemoteMediaPlayer.load( mApiClient, mediaInfo, true )
                    .setResultCallback( new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult( RemoteMediaPlayer.MediaChannelResult mediaChannelResult ) {
                            if( mediaChannelResult.getStatus().isSuccess() ) {
                                mSongIsLoaded = true;
                            }
                        }
                    } );
        } catch( Exception e ) {
        }
    }
    private void reconnectChannels( Bundle hint ) {
        if( ( hint != null ) && hint.getBoolean( Cast.EXTRA_APP_NO_LONGER_RUNNING ) ) {
            //Log.e( TAG, "App is no longer running" );
            teardown();
        } else {
            try {
                Cast.CastApi.setMessageReceivedCallbacks( mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer );
            } catch( IOException e ) {
                //Log.e( TAG, "Exception while creating media channel ", e );
            } catch( NullPointerException e ) {
                //Log.e( TAG, "Something wasn't reinitialized for reconnectChannels" );
            }
        }
    }
    @Override
    protected void onPause() {
        if ( isFinishing() ) {
            // End media router discovery
            mMediaRouter.removeCallback( mMediaRouterCallback );
        }
        super.onPause();
    }

    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed( ConnectionResult connectionResult ) {
            teardown();
        }
    }
    private void teardown() {
        if( mApiClient != null ) {
            if( mApplicationStarted ) {
                try {
                    Cast.CastApi.stopApplication( mApiClient );
                    if( mRemoteMediaPlayer != null ) {
                        Cast.CastApi.removeMessageReceivedCallbacks( mApiClient, mRemoteMediaPlayer.getNamespace() );
                        mRemoteMediaPlayer = null;
                    }
                } catch( IOException e ) {
                    //Log.e( TAG, "Exception while removing application " + e );
                }
                mApplicationStarted = false;
            }
            if( mApiClient.isConnected() )
                mApiClient.disconnect();
            mApiClient = null;
        }
        mSelectedDevice = null;
        mSongIsLoaded = false;
    }
    private void controlVideo() {
        if( mRemoteMediaPlayer == null || !mSongIsLoaded )
            return;

        if( mIsPlaying ) {
            mRemoteMediaPlayer.pause( mApiClient );
        } else {
            mRemoteMediaPlayer.play( mApiClient );
        }
    }
    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected( Bundle hint ) {
            if( mWaitingForReconnect ) {
                mWaitingForReconnect = false;
                reconnectChannels( hint );
            } else {
                try {
                    Cast.CastApi.launchApplication( mApiClient, getString( R.string.app_name ), false )
                            .setResultCallback(
                                    new ResultCallback<Cast.ApplicationConnectionResult>() {
                                        @Override
                                        public void onResult(
                                                Cast.ApplicationConnectionResult applicationConnectionResult) {
                                            Status status = applicationConnectionResult.getStatus();
                                            if( status.isSuccess() ) {
                                                //Values that can be useful for storing/logic
                                                ApplicationMetadata applicationMetadata =
                                                        applicationConnectionResult.getApplicationMetadata();
                                                String sessionId =
                                                        applicationConnectionResult.getSessionId();
                                                String applicationStatus =
                                                        applicationConnectionResult.getApplicationStatus();
                                                boolean wasLaunched =
                                                        applicationConnectionResult.getWasLaunched();

                                                mApplicationStarted = true;
                                                reconnectChannels( null );
                                            }
                                        }
                                    }
                            );
                } catch ( Exception e ) {

                }
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            mWaitingForReconnect = true;
        }
    }
    private void launchReceiver() {
        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                .builder( mSelectedDevice, mCastClientListener );

        ConnectionCallbacks mConnectionCallbacks = new ConnectionCallbacks();
        ConnectionFailedListener mConnectionFailedListener = new ConnectionFailedListener();
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Cast.API, apiOptionsBuilder.build() )
                .addConnectionCallbacks( mConnectionCallbacks )
                .addOnConnectionFailedListener( mConnectionFailedListener )
                .build();

        mApiClient.connect();
    }
    private void initMediaRouter() {
        // Configure Cast device discovery
        mMediaRouter = MediaRouter.getInstance( this );
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent.categoryForCast(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID) )
                .build();
        mMediaRouterCallback = new MediaRouterCallback();
    }
    private class MediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            initCastClientListener();
            initRemoteMediaPlayer();

            mSelectedDevice = CastDevice.getFromBundle( info.getExtras() );

            launchReceiver();
        }

        @Override
        public void onRouteUnselected( MediaRouter router, MediaRouter.RouteInfo info ) {
            teardown();
            mSelectedDevice = null;
            mSongIsLoaded = false;
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
        service = null;
        finish();
    }

    @Override
    public void seek(int i) {
        service.seekPlayer(i);
    }


}
