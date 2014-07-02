package com.acod.play.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Andrew on 6/12/2014.
 */
public class Player extends Fragment implements View.OnClickListener {
    ImageButton play, pause, stop;
    TextView songName, currentTime, totalTime;
    ImageView albumart;
    SeekBar seek;
    Bundle b;
    MediaService service;
    //connection between service and the fragment
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaService.LocalBinder binder = (MediaService.LocalBinder) iBinder;
            service = binder.getService();
            mediaReady();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (service.bitmapReady()) {
                        doneLoading(service.getAlbumArt());
                        h.removeCallbacks(this);
                    }
                }
            }, 2000);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
    PlayerCommunication communication;
    android.os.Handler h = new android.os.Handler();
    //updates the seek bar to the appropriate time
    private Runnable updatebar = new Runnable() {
        @Override
        public void run() {
            int mCurrentPosition = (int) service.getCurrentTime();
            seek.setProgress(mCurrentPosition);
            currentTime.setText(milliSecondsToTimer(mCurrentPosition));

            handler.postDelayed(this, 1000);
        }
    };

    //gets data from calling class with song information
    private android.os.Handler handler = new android.os.Handler();

    public Player() {

    }

    //convert the given song time in milleseconds to a readable string.
    public String milliSecondsToTimer(long milliseconds) {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = getArguments();

        View v = inflater.inflate(R.layout.player, null);
        createUI(v);

        return v;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void createUI(View v) {
        //ui elements
        songName = (TextView) v.findViewById(R.id.nameText);
        totalTime = (TextView) v.findViewById(R.id.totalTime);
        currentTime = (TextView) v.findViewById(R.id.currentTime);
        seek = (SeekBar) v.findViewById(R.id.seekBar);
        albumart = (ImageView) v.findViewById(R.id.albumArt);
        songName.setText(b.getString("name"));
        play = (ImageButton) v.findViewById(R.id.play_button);
        play.setOnClickListener(this);
        pause = (ImageButton) v.findViewById(R.id.pause_button);
        pause.setOnClickListener(this);
        stop = (ImageButton) v.findViewById(R.id.stop_button);
        stop.setOnClickListener(this);
    }

    /*Handle ui button clicks*/
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_button:
                communication.play();
                mediaReady();
                handler.postDelayed(updatebar, 1000);
                //call method on service

                break;
            case R.id.stop_button:
                communication.stop();
                handler.removeCallbacks(updatebar);
                getFragmentManager().popBackStack();

                break;
            case R.id.pause_button:
                communication.pause();
                break;
        }
    }

    //bind the service to the fragment
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        communication = (PlayerCommunication) activity;
        getActivity().bindService(new Intent(getActivity(), MediaService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    public void mediaReady() {

        totalTime.setText(milliSecondsToTimer(service.getMaxTime()));
        seek.setMax(service.getMaxTime());
        //handle changing the position of a song.
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) service.seekPlayer(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        updatebar.run();
    }

    //set the imageview of the album to the appropriate image
    public void doneLoading(Bitmap bm) {
        if (!(bm == null))
            albumart.setImageBitmap(bm);
        else
            albumart.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.musicimage));
    }

    /*    TODO Get album art for an album be searching Google.
    */

}
