package com.acod.play.app.Models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by Andrew on 6/11/2014.
 */
public class SongResult implements Serializable {
    private String name;
    private String artist = "Unknown";
    private String url;
    private long id;

    public SongResult(@NonNull String name,
                      @Nullable String url) {
        this.name = name;
        this.url = url;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getID() {
        return this.id;
    }

    public void setID(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String aname) {
        artist = aname;
    }
}
