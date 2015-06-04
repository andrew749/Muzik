package com.acod.play.app.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.acod.play.app.Adapters.SearchListAdapter;
import com.acod.play.app.Models.SongResult;

import java.util.ArrayList;

/*
 * Created by Andrew on 7/12/2014.
 * Just an extension of results fragment since results are still very basic.
 */
public class SavedFragment extends ResultsFragment {
    public void setResults(final ArrayList<SongResult> results) {
        adapter = new SearchListAdapter(getActivity().getApplicationContext(), results);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle b = new Bundle();
                b.putString("url", results.get(i).url);
                b.putString("name", results.get(i).name);
                //open the player via the activity
                transmission.openPlayer(b);
            }
        });
    }
}
