package com.acod.play.app.Models;

/**
 * Created by Andrew on 6/11/2014.
 */
public class SongResult {
    public int duration = 0;
    public String name;
    public String artist = "Unknown";
    public String url;


    public SongResult(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String aname) {
        artist = aname;
    }
}
