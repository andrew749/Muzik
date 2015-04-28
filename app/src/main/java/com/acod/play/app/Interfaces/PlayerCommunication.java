package com.acod.play.app.Interfaces;

import android.media.MediaPlayer;
import com.acod.play.app.Models.STATE;

/**
 * Created by Andrew on 6/23/2014.
 */
public interface PlayerCommunication {
    static MediaPlayer player = new MediaPlayer();

    public void play();

    public void pause();

    public void stop();

    public void seek(int i);
    public STATE.PLAY_STATE currentState();
}
