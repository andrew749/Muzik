package com.acod.play.app.Models;

import java.io.Serializable;

/**
 * Created by Andrew on 6/11/2014.
 */
public class SongResult implements Serializable {
    public int duration = 0;
    public String name;
    public String artist = "Unknown";
    public String url;
    public long id;

    public SongResult(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getID() {
        return this.id;
    }

    public void setID(long id) {
        this.id = id;
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
