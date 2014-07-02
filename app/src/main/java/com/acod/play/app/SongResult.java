package com.acod.play.app;

/**
 * Created by Andrew on 6/11/2014.
 */
public class SongResult {
    int duration = 0;
    String name;
    String artist = "Unknown";
    String url;


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
