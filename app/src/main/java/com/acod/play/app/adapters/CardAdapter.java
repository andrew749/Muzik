package com.acod.play.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.acod.play.app.Models.Song;
import com.acod.play.app.R;

import java.util.ArrayList;

/**
 * Created by Andrew on 7/1/2014.
 */
public class CardAdapter extends BaseAdapter {
    TextView songName, artistName;
    ImageView albumArt;
    Context context;
    private ArrayList<Song> songs;

    public CardAdapter(ArrayList<Song> songs, Context context) {
        this.songs = songs;
        this.context = context;

        //inflate the cardlayout with the given information

    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return songs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newView = inflater.inflate(R.layout.songcard, null);


        TextView name = (TextView) newView.findViewById(R.id.card_songname);
        TextView artist = (TextView) newView.findViewById(R.id.card_artist);
        ImageView albumArt = (ImageView) newView.findViewById(R.id.card_albumimage);
        name.setText(songs.get(i).getSongName());
        artist.setText(songs.get(i).getArtist());
        albumArt.setImageBitmap(songs.get(i).getArt());
        return newView;
    }


}
