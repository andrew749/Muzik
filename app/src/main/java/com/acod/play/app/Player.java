package com.acod.play.app;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by Andrew on 6/12/2014.
 */
public class Player extends Activity implements View.OnClickListener {
    MediaPlayer player;
    private Runnable updatebar = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                int mCurrentPosition = player.getCurrentPosition();
                seek.setProgress(mCurrentPosition);
                currentTime.setText(milliSecondsToTimer(mCurrentPosition));
            }
            handler.postDelayed(this, 1000);
        }
    };
    Button play, pause, stop;
    TextView songName, currentTime, totalTime;
    SeekBar seek;
    boolean isPrepared = false;
    private android.os.Handler handler = new android.os.Handler();

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);
        Bundle b = this.getIntent().getExtras();
        Bundle data = b.getBundle("data");
        songName = (TextView) findViewById(R.id.nameText);
        totalTime = (TextView) findViewById(R.id.totalTime);
        currentTime = (TextView) findViewById(R.id.currentTime);
        seek = (SeekBar) findViewById(R.id.seekBar);


        songName.setText(data.getString("name"));
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        Uri uri = Uri.parse(data.getString("url"));
        try {
            player.setDataSource(getApplicationContext(), uri);
            player.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
        play = (Button) findViewById(R.id.play_button);
        play.setOnClickListener(this);
        pause = (Button) findViewById(R.id.pause_button);
        pause.setOnClickListener(this);
        stop = (Button) findViewById(R.id.stop_button);
        stop.setOnClickListener(this);

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                isPrepared = true;
            }
        });

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                player.seekTo(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_button:
                if (isPrepared) {
                    player.start();
                    seek.setMax(player.getDuration());
                    totalTime.setText(milliSecondsToTimer(player.getDuration()));
                    updatebar.run();
                }
                break;
            case R.id.stop_button:
                player.stop();

                finish();

                break;
            case R.id.pause_button:
                player.pause();
                break;
        }
    }
}
