package com.acod.play.app;

import android.media.MediaPlayer;

import com.acod.play.app.Models.STATE;
import com.acod.play.app.Models.Song;
import com.google.android.gms.cast.RemoteMediaPlayer;

/**
 * Created by andrewcodispoti on 2015-06-26.
 */
public class MusicManager {
    static MediaPlayer player;
    static RemoteMediaPlayer remoteMediaPlayer;
    Song currentSong;
    //hold current state of song
    STATE.PLAY_STATE playState = STATE.PLAY_STATE.NULL;

    private enum PLAYING_DEVICE {THIS, CHROMECAST}

    private PLAYING_DEVICE device = PLAYING_DEVICE.THIS;


    //stuff for a singleton
    static MusicManager manager;
    public MusicManager getInstance(){
        if(manager==null){
            manager= new MusicManager();
        }
        return manager;
    }
    public MusicManager() {
        player = new MediaPlayer();
        remoteMediaPlayer = new RemoteMediaPlayer();
    }

    public void play() {
        if (isLoaded()) {
            if (onDevice()) {
                player.start();
            }else{
                remoteMediaPlayer.play(Muzik.mApiClient);
            }
        }
    }

    public void pause() {
        if (isLoaded()) {
            if (onDevice()) {
                player.pause();
            }else{
                remoteMediaPlayer.pause(Muzik.mApiClient);
            }
        }
    }

    public void stop() {
        if(onDevice()){
            player.stop();
        }else{
            remoteMediaPlayer.stop(Muzik.mApiClient);
        }
    }

    public void switchTrack() {

    }

    public void loadSong() {
        //if loaded switch track else load song normally
        if (isLoaded()) {

        }
    }

    private boolean isLoaded() {
        return playState == STATE.PLAY_STATE.PAUSED || playState == STATE.PLAY_STATE.PLAYING;
    }

    private boolean onDevice() {
        return device == PLAYING_DEVICE.THIS;
    }

}
