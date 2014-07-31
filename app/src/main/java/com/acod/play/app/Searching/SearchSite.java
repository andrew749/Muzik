package com.acod.play.app.Searching;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.acod.play.app.Activities.HomescreenActivity;
import com.acod.play.app.Interfaces.updateui;
import com.acod.play.app.Models.SongResult;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Andrew on 7/1/2014.
 */
public class SearchSite extends AsyncTask<Void, Void, ArrayList<SongResult>> {
    private final HttpClient client = new DefaultHttpClient();
    String query;
    Context context;
    List newResults;
    Thread mp3Skull = new Thread() {
        @Override
        public void run() {
            newResults.addAll(searchMP3Skull());
        }
    };
    Thread youtubeSearch = new Thread() {
        @Override
        public void run() {
            newResults.addAll(searchYoutube());
        }
    };
    Thread downloadsnl = new Thread() {
        @Override
        public void run() {
            newResults.addAll(getSongs3());
        }
    };
    private updateui update;

    public SearchSite(String query, Context context, updateui update) {
        this.query = query;
        this.context = context;
        this.update = update;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        update.openProgressDialog();
    }

    /*
                    parse the webstie and place the various song urls and names into a data object to be returned to the calling class
                     */
    @Override
    protected ArrayList<SongResult> doInBackground(Void... voids) {
        ArrayList<SongResult> results = new ArrayList<SongResult>();
        newResults = Collections.synchronizedList(new ArrayList<SongResult>());
        /*results.addAll(searchYoutube());
        results.addAll(searchMP3Skull());
        results.addAll(getSongs3());
        results.addAll(searchBeeMP3());
        return results;*/
        mp3Skull.start();
        youtubeSearch.start();
        downloadsnl.start();
        try {
            mp3Skull.join();
            youtubeSearch.join();
            downloadsnl.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ArrayList<SongResult>(newResults);
    }

    //query mp3skull and return an arraylist with all of the results
    //working fully
    private ArrayList<SongResult> searchMP3Skull() {
        ArrayList<SongResult> tempresults = new ArrayList<SongResult>();

        String tempquery = "http://mp3skull.com/mp3/" + query + ".html";
        Elements searchresults = new Elements();
        try {
            Document document = Jsoup.connect(tempquery).get();
            searchresults = document.select("div#song_html");

        } catch (IOException e) {
        }
        if (!searchresults.isEmpty()) {
            for (Element inputelement : searchresults) {
                String name, duration, url;
                name = inputelement.select("b").text();
                url = inputelement.select("a[href]").first().attr("href");
                if (HomescreenActivity.debugMode) {
                    Log.d("PLAY", "Found entry name=" + name + " url=" + url);
                }
                tempresults.add(new SongResult(name, url));
            }
        }

        return tempresults;
    }

    //query beemp3 and return an array list with all of the results
    private ArrayList<SongResult> searchBeeMP3() {

        return SearchBee.getSongs(query);
    }

    private ArrayList<SongResult> searchYoutube() {

        SearchYou s = new SearchYou(query);
        return s.getSongs();
    }


    public ArrayList<SongResult> getSongs3() {

        return Searchnl.getSongs(query);
    }

    @Override
    protected void onPostExecute(ArrayList<SongResult> songResults) {
        super.onPostExecute(songResults);
        Log.d("Play", "Done loading query");
        Bundle bundle = new Bundle();
        update.closeProgressDialog();

        bundle.putSerializable("results", songResults);
        if (songResults.size() > 0) {
            update.openResultsFragment(bundle);
        } else
            Toast.makeText(context, "Song not found", Toast.LENGTH_LONG).show();
    }
}
