package com.acod.play.app.Models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.acod.play.app.R;

import java.io.Serializable;

/**
 * Created by Andrew on 7/1/2014.
 */
public class Song implements Serializable {
    Bitmap bm;
    String name, artist,imageUrl;
    public void setImageUrl(String link){
        imageUrl=link;
    }
    public String getImageUrl(){return imageUrl;}
    public Song(String name, String artist, String url) {
        this.imageUrl=url;
        this.name = name;
        this.artist = artist;
    }

    public Song(String name, String artist) {
        this.name = name;
        this.artist = artist;
    }
    public void setArt(Bitmap bm){
        this.bm=bm;
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
