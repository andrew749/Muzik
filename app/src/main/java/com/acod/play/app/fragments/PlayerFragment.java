package com.acod.play.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.acod.play.app.Activities.PlayerActivity;
import com.acod.play.app.Interfaces.PlayerCommunication;
import com.acod.play.app.R;

/**
 * Created by andrew on 03/07/14.
 */
public class PlayerFragment extends Fragment implements View.OnClickListener {
    /*
    Fragment to hold the controls for the player service.
     */
    SeekBar seek;
    TextView songName, totalTime, currentTime;
    ImageButton play, pause, stop;
    String songNameString = "";
    String artistName = "";
    PlayerCommunication communication;

    public void createUI(View v) {
        //ui elements
        songName = (TextView) v.findViewById(R.id.nameText);
        totalTime = (TextView) v.findViewById(R.id.totalTime);
        currentTime = (TextView) v.findViewById(R.id.currentTime);
        seek = (SeekBar) v.findViewById(R.id.seekBar);
        songName.setText(songNameString);
        play = (ImageButton) v.findViewById(R.id.play_button);
        play.setOnClickListener(this);
        pause = (ImageButton) v.findViewById(R.id.pause_button);
        pause.setOnClickListener(this);
        stop = (ImageButton) v.findViewById(R.id.stop_button);
        stop.setOnClickListener(this);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    communication.seek(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setUpPlayer(int maxTime) {
        if (!(totalTime == null && seek == null)) {
            totalTime.setText(PlayerActivity.milliSecondsToTimer(maxTime));
            seek.setMax(maxTime);
        }

    }

    public void setUpSongName(String name) {
        songName.setText(name);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.playercontrols, null);
        createUI(v);
        return v;

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        communication = (PlayerCommunication) activity;
    }

    public void updateTime(int time) {
        currentTime.setText(PlayerActivity.milliSecondsToTimer(time));
        seek.setProgress( time);

    }

    /*Handle ui button clicks*/
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_button:
                communication.play();


                break;
            case R.id.stop_button:
                communication.stop();


                break;
            case R.id.pause_button:
                communication.pause();
                break;
        }
    }
}
