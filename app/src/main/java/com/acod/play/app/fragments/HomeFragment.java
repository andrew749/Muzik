package com.acod.play.app.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;

import com.acod.play.app.Activities.HomescreenActivity;
import com.acod.play.app.Models.Song;
import com.acod.play.app.R;
import com.acod.play.app.adapters.CardAdapter;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseViews;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;


/**
 * Created by Andrew on 7/1/2014.
 */
public class HomeFragment extends SherlockFragment {

    //holds the main view which is the billboard top 100 songs
    CardAdapter adapter;
    SearchView sv;
    ShowcaseViews views;
    GridView layout;

    public HomeFragment() {


    }

    public static ShowcaseViews setupShowcase(Activity activity) {
        ShowcaseViews view = new ShowcaseViews(activity);
        ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.insert = ShowcaseView.INSERT_TO_DECOR;

        view.addView(new ShowcaseViews.ItemViewProperties(R.id.content_frame, R.string.homescreenelementtitle, R.string.homescreenelementdescription));
        view.addView(new ShowcaseViews.ItemViewProperties(android.R.id.home, R.string.mainmenutitle, R.string.mainmenudescription, ShowcaseView.ITEM_ACTION_HOME));
        view.addView(new ShowcaseViews.ItemViewProperties(R.string.searchtitle, R.string.searchdescription));
        return view;
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
            views = setupShowcase(getActivity());
            views.show();
            SharedPreferences.Editor editor = pref.edit();

            editor.putFloat("lastopenedversion", HomescreenActivity.APP_VERSION);
            editor.commit();
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        sv = (SearchView) menu.findItem(R.id.search).getActionView();

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


}
