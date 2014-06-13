package com.acod.play.app;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by Andrew on 6/12/2014.
 */
public class Player extends Activity implements View.OnClickListener {
    MediaPlayer player;
    Button play, pause, stop;
    TextView songName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);
        Bundle b = this.getIntent().getExtras();
        Bundle data = b.getBundle("data");
        songName = (TextView) findViewById(R.id.nameText);
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
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_button:
                player.start();
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
