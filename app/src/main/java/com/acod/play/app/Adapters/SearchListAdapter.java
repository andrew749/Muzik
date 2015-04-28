package com.acod.play.app.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.acod.play.app.Models.SongResult;
import com.acod.play.app.R;

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
    }

    /*
    Set the song name on each search result element
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.search_list_element, null);
        }
        TextView name = (TextView) view.findViewById(R.id.songName);
        name.setText(results.get(i).getName());
        return view;
    }

}
