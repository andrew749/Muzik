package com.acod.play.app.fragments;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.acod.play.app.Activities.HomescreenActivity;
import com.acod.play.app.Activities.SearchActivity;
import com.acod.play.app.Models.Song;
import com.acod.play.app.R;
import com.acod.play.app.adapters.CardAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;


/**
 * Created by Andrew on 7/1/2014.
 */
public class HomeFragment extends Fragment implements SearchView.OnQueryTextListener {

    //holds the main view which is the billboard top 100 songs
    CardAdapter adapter;
    SearchView sv;
    GridView layout;

    public HomeFragment() {


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        adapter = new CardAdapter(null, getActivity().getApplicationContext());

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        if (pref.getFloat("lastopenedversion", 0) < HomescreenActivity.APP_VERSION) {
            //do stuff on first run like tutorial
            SharedPreferences.Editor editor = pref.edit();

            editor.putFloat("lastopenedversion", HomescreenActivity.APP_VERSION);
            editor.commit();
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.homescreen, menu);
        MenuItem item = menu.findItem(R.id.search);
        sv = (SearchView) MenuItemCompat.getActionView(item);
        SearchManager manager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        sv.setSearchableInfo(manager.getSearchableInfo(new ComponentName(getActivity().getApplicationContext(), SearchActivity.class)));
        sv.setOnQueryTextListener(this);
        sv.setIconifiedByDefault(false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.homelayout, null);
        layout = (GridView) v.findViewById(R.id.cardview);

        AdView adView = (AdView) v.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        return v;

    }

    public void setupView(final ArrayList<Song> songs) {
        layout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //pass the name of the song into the search activity to list results
                sv.setQuery(songs.get(i).getSongName() + " " + songs.get(i).getArtist().substring(0, (songs.get(i).getArtist() + " ").indexOf(" ")), true);
            }
        });
        adapter = new CardAdapter(songs, getActivity());
        layout.setAdapter(adapter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        startActivity(new Intent(getActivity().getApplicationContext(), SearchActivity.class).putExtra(SearchManager.QUERY, query).setAction("android.intent.action.SEARCH"));
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

}
