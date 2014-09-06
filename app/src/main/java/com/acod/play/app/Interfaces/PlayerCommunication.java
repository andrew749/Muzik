package com.acod.play.app.Interfaces;

import android.media.MediaPlayer;

/**
 * Created by Andrew on 6/23/2014.
 */
public interface PlayerCommunication {
    static MediaPlayer player = new MediaPlayer();

    public void toggle();

    public void stop();

    public void seek(int i);
}
