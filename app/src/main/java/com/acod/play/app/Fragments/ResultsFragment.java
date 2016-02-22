package com.acod.play.app.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.acod.play.app.Adapters.SearchListAdapter;
import com.acod.play.app.Database.DatabaseManager;
import com.acod.play.app.Interfaces.DataTransmission;
import com.acod.play.app.Models.SongResult;
import com.acod.play.app.R;

import java.util.ArrayList;

/**
 * Created by Andrew on 6/14/2014.
 */
public class ResultsFragment extends Fragment {

    private static final String URL = "url";
    private static final String SONG_NAME = "name";
    private static final String RESULTS = "results";
    private ListView lv;

    private SearchListAdapter adapter;
    private DataTransmission transmission;

    /*Inflate layout.*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.search_list, null);
        lv = (ListView) v.findViewById(R.id.searchlist);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /*When the fragment attached, get an instance of the activity so you can use the callback methods.*/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        transmission = (DataTransmission) activity;

    }

    public void setResults(Bundle bundle) {
        final ArrayList<SongResult> results = (ArrayList<SongResult>) bundle.get(RESULTS);
        adapter = new SearchListAdapter(getActivity().getApplicationContext(), results);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle b = new Bundle();
                b.putString(URL, results.get(i).getUrl());
                b.putString(SONG_NAME, results.get(i).getName());
                //open the player via the activity
                transmission.openPlayer(b);
            }
        });
    }

    public void setResults(final ArrayList<SongResult> results) {
        adapter = new SearchListAdapter(getActivity().getApplicationContext(), results);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle b = new Bundle();
                b.putString(URL, results.get(i).getUrl());
                b.putString(SONG_NAME, results.get(i).getName());
                //open the player via the activity
                transmission.openPlayer(b);

            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                DatabaseManager manager = new DatabaseManager(getActivity());
                manager.remove(results.get(i).getID());
                results.remove(i);
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    public SearchListAdapter getAdapter() {
        return adapter;
    }

    public DataTransmission getTransmission() {
        return transmission;
    }

    public void setAdapter(SearchListAdapter adapter) {
        this.adapter = adapter;
    }

    public ListView getListView() {
        return lv;
    }
}
