package com.acod.play.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Andrew on 6/11/2014.
 */
public class SearchListAdapter extends ArrayAdapter<SongResult> {
    Context context;
    ArrayList<SongResult> results = new ArrayList<SongResult>();

    public SearchListAdapter(Context context, ArrayList<SongResult> results) {
        super(context, 0, results);
        this.results = results;
        this.context = context;
        Log.d("Play", "Adapter intialized");
    }

    /*
    Set the song name and running time to the proper list elements
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View newView = view;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        newView = inflater.inflate(R.layout.search_list_element, null);
        TextView name = (TextView) newView.findViewById(R.id.songName);
        // TextView time=(TextView)view.findViewById(R.id.songLength);
        name.setText(results.get(i).getName());
        return newView;
    }

}
