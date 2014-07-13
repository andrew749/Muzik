package com.acod.play.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.acod.play.app.Database.DatabaseManager;
import com.acod.play.app.Interfaces.DataTransmission;
import com.acod.play.app.Models.SongResult;
import com.acod.play.app.R;
import com.acod.play.app.adapters.SearchListAdapter;

import java.util.ArrayList;

/**
 * Created by Andrew on 6/14/2014.
 */
public class ResultsFragment extends Fragment {

    ListView lv;
    SearchListAdapter adapter;
    DataTransmission transmission;

    public ResultsFragment() {

    }


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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        transmission = (DataTransmission) activity;
    }

    public void setResults(Bundle bundle) {
        final ArrayList<SongResult> results = (ArrayList<SongResult>) bundle.get("results");
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


}
