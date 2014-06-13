package com.acod.play.app;

/**
 * Created by Andrew on 6/11/2014.
 */
public class SongResult {
    String duration;
    String name;
    String artist;
    String url;
    String genre;

    public SongResult(String name, String duration, String url) {
        this.name = name;
        this.duration = duration;
        this.url = url;
    }

    public SongResult(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

}
