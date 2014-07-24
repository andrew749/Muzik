package com.acod.play.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;

import com.acod.play.app.Activities.HomescreenActivity;
import com.acod.play.app.Models.Song;
import com.acod.play.app.R;
import com.acod.play.app.adapters.CardAdapter;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseViews;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


/**
 * Created by Andrew on 7/1/2014.
 */
public class HomeFragment extends Fragment {

    //holds the main view which is the billboard top 100 songs
    ArrayList<Song> songs = new ArrayList<Song>();
    CardAdapter adapter;
    SearchView sv;
    ShowcaseViews views;

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

        adapter = new CardAdapter(songs, getActivity().getApplicationContext());
        if (songs.size() <= 0) {
            BillboardLoader l = new BillboardLoader();
            l.execute();
        }
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

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
        GridView layout = (GridView) v.findViewById(R.id.cardview);
        layout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //pass the name of the song into the search activity to list results
                sv.setQuery(songs.get(i).getSongName() + " " + songs.get(i).getArtist().substring(0, (songs.get(i).getArtist() + " ").indexOf(" ")), true);
            }
        });
        layout.setAdapter(adapter);
        AdView adView = (AdView) v.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        return v;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    class BillboardLoader extends AsyncTask<Void, Void, ArrayList<Song>> {

        BillboardLoader() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Song> doInBackground(Void... voids) {
            String songName = "Unknown", artistName = "Unknown";
            Bitmap image = null;
            String query = "http://www.billboard.com/charts/hot-100";
            Elements elements = null;
            try {
                Document doc = Jsoup.connect(query).get();
                elements = doc.select("article");
            } catch (IOException e) {
                e.printStackTrace();
            }
            int i = 0;
            for (Element x : elements) {
                i++;
                image = BitmapFactory.decodeResource(getResources(), R.drawable.musicimage);
                songName = x.select("h1").text();
                artistName = x.select("p").select("a").text();
                String imageurl = x.select("img").attr("src");
                if (HomescreenActivity.debugMode) {
                    Log.d("Play", "Top:" + songName + " Artist:" + artistName + " Image Source=" + imageurl);
                }
                try {
                    image = BitmapFactory.decodeStream(new URL(imageurl).openConnection().getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }


                if (image == null)
                    songs.add(new Song(songName, artistName));
                else
                    songs.add(new Song(songName, artistName, image));
                if (i >= 10) {
                    break;
                }
            }
            return songs;
        }

        @Override
        protected void onPostExecute(ArrayList<Song> finalsongs) {
            super.onPostExecute(songs);
            if (HomescreenActivity.debugMode) {
                Log.d("Play", "Done Loading");
            }
            adapter.notifyDataSetChanged();
        }
    }


}
