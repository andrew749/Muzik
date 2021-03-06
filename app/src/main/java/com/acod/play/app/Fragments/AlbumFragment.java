package com.acod.play.app.Fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.acod.play.app.R;

/**
 * Created by andrew on 03/07/14.
 */

/**
 * Fragment to hold the Album Artwork for the current playing song
 * <p>
 * <p>
 * Must call the set Art method in order for the album art to change to the appropriate picture.
 */
public class AlbumFragment extends Fragment {
    ImageView albumArt;

    public AlbumFragment() {
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.album_fragment, null);
        albumArt = (ImageView) view.findViewById(R.id.albumArt);
        return view;
    }

    public void setArt(Bitmap bm) {
        albumArt.setImageBitmap(bm);
    }
}
