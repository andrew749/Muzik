package com.acod.play.app.Models;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by Andrew on 7/1/2014.
 */
public class Song implements Serializable {
    Bitmap bm;
    String name, artist;

    public Song(String name, String artist, Bitmap bm) {
        this.bm = bm;
        this.name = name;
        this.artist = artist;
    }

    public Song(String name, String artist) {
        this.name = name;
        this.artist = artist;
    }

    public Bitmap getArt() {
        return bm;
    }

    public String getArtist() {
        return artist;
    }

    public String getSongName() {
        return name;
    }

}
