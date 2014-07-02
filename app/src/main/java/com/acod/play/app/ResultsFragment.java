package com.acod.play.app;

import android.app.Activity;
import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.acod.play.app.adapters.SearchListAdapter;

import java.util.ArrayList;

/**
 * Created by Andrew on 6/14/2014.
 */
public class ResultsFragment extends Fragment {

    ArrayList<SongResult> results = new ArrayList<SongResult>();
    ListView lv;
    SearchListAdapter adapter;
    MediaPlayer player;
    DataTransmission transmission;

    public ResultsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.search_list, null);
        lv = (ListView) v.findViewById(R.id.searchlist);
        results = (ArrayList<SongResult>) getArguments().get("results");
        adapter = new SearchListAdapter(getActivity().getApplicationContext(), results);
        lv.setAdapter(adapter);
        player = new MediaPlayer();
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


    public interface DataTransmission {
        public void openPlayer(Bundle b);

    }


}
