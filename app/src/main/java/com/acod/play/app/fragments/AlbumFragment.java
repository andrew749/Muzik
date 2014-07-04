package com.acod.play.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.media.Image;
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
 */
public class AlbumFragment extends Fragment {
    Bundle arguments;
    public AlbumFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.albumfragment,null);
        ImageView albumArt=(ImageView)view.findViewById(R.id.albumArt);
        albumArt.setImageBitmap((Bitmap)arguments.getParcelable("albumArt"));
        return view;
    }
/*
The passed bundle must contain a bitmap with the album image.
 */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       arguments= getArguments();
    }
}
