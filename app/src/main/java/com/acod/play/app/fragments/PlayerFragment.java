package com.acod.play.app.fragments;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.acod.play.app.Activities.PlayerActivity;
import com.acod.play.app.Database.DatabaseManager;
import com.acod.play.app.Interfaces.PlayerCommunication;
import com.acod.play.app.R;
import com.actionbarsherlock.app.SherlockFragment;

/**
 * Created by andrew on 03/07/14.
 */
public class PlayerFragment extends SherlockFragment implements View.OnClickListener {
    /*
    Fragment to hold the controls for the player service.
     */
    SeekBar seek;
    TextView songName, totalTime, currentTime;
    ImageButton control, stop, saveButton;
    String songNameString = "";
    String artistName = "";
    PlayerCommunication communication;
    String songURL;

    public PlayerFragment() {
        setRetainInstance(true);
    }

    /*
    * Initialize and create objects for ui elements.
    * */
    public void createUI(View v) {
        songName = (TextView) v.findViewById(R.id.nameText);
        totalTime = (TextView) v.findViewById(R.id.totalTime);
        currentTime = (TextView) v.findViewById(R.id.currentTime);
        seek = (SeekBar) v.findViewById(R.id.seekBar);
        songName.setText(songNameString);
        control = (ImageButton) v.findViewById(R.id.control_button);
        control.setOnClickListener(this);

        stop = (ImageButton) v.findViewById(R.id.stop_button);
        stop.setOnClickListener(this);
        saveButton = (ImageButton) v.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);
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

    /*
        Setup the player's maximum time both graphically and within the seekbar.
         */
    public void setUpPlayer(int maxTime) {
        if (!(totalTime == null && seek == null)) {
            totalTime.setText(PlayerActivity.milliSecondsToTimer(maxTime));
            seek.setMax(maxTime);
        }

    }

    /*Set the song name appropriately*/
    public void setUpSongName(String name, String url) {
        songName.setText(name);
        songNameString = name;
        songURL = url;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.playercontrols, null);
        createUI(v);
        return v;

    }

    /*Get the interface to communicate with the activity.*/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        communication = (PlayerCommunication) activity;
    }

    public void updateTime(int time) {
        currentTime.setText(PlayerActivity.milliSecondsToTimer(time));
        seek.setProgress(time);

    }

    public void switchImage(boolean songState) {
        if (songState) {
            control.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pausebuttonblack));
        } else {
            control.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.playbuttonblack));
        }
    }

    /*Handle ui button clicks*/
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.control_button:

                communication.toggle();


                break;
            case R.id.stop_button:
                communication.stop();


                break;

            case R.id.saveButton:
                DatabaseManager databaseManager = new DatabaseManager(getActivity());
                databaseManager.putValue(songNameString, songURL);
                Toast.makeText(getActivity(), "Saved Song", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
