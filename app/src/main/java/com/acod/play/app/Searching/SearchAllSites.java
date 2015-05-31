package com.acod.play.app.Searching;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.acod.play.app.Interfaces.UpdateUI;
import com.acod.play.app.Models.SongResult;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Andrew on 7/1/2014.
 */
public class SearchAllSites extends AsyncTask<Void, Void, ArrayList<SongResult>> {
    private final HttpClient client = new DefaultHttpClient();
    String query;
    Context context;
    List newResults;
    /*Threads to run each search query*/
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
            newResults.addAll(getSongs());
        }
    };
    Thread muzikApi = new Thread() {
        @Override
        public void run() {
            newResults.addAll(searchMuzikApi());
        }
    };

    private UpdateUI update;

    public SearchAllSites(String query, Context context, UpdateUI update) {
        this.query = query;
        this.context = context;
        this.update = update;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //open a dialog to provide some user feedback.
        update.openProgressDialog();
    }

    /*parse the website and place the various song urls and names into a data object to be returned to the calling class*/
    @Override
    protected ArrayList<SongResult> doInBackground(Void... voids) {
        ArrayList<SongResult> results = new ArrayList<SongResult>();
        newResults = Collections.synchronizedList(new ArrayList<SongResult>());
        muzikApi.start();
        mp3Skull.start();
        youtubeSearch.start();
        downloadsnl.start();
        //Run each threaded search operation. In case server is down.
        try {
            muzikApi.join();
            mp3Skull.join();
            youtubeSearch.join();
            downloadsnl.join();
        } catch (Exception e) {
        }
        return new ArrayList<SongResult>(newResults);
    }

    private ArrayList<SongResult> searchMP3Skull() {
        return SearchMP3Skull.getSongs(query);
    }

    private ArrayList<SongResult> searchBeeMP3() {
        return SearchBeeMP3.getSongs(query);
    }

    private ArrayList<SongResult> searchYoutube() {
        SearchYouTube s = new SearchYouTube(query);
        return s.getSongs();
    }

    private ArrayList<SongResult> searchMuzikApi() {
        return SearchMuzikApi.getSongs(query);
    }

    public ArrayList<SongResult> getSongs() {
        return SearchDownloadsNL.getSongs(query);
    }

    @Override
    protected void onPostExecute(ArrayList<SongResult> songResults) {
        super.onPostExecute(songResults);
        Bundle bundle = new Bundle();
        update.closeProgressDialog();
        bundle.putSerializable("results", songResults);
        if (songResults.size() > 0) {
            update.openResultsFragment(bundle);
        } else
            Toast.makeText(context, "Song not found", Toast.LENGTH_LONG).show();
    }
}
