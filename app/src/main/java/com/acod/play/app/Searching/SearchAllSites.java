package com.acod.play.app.Searching;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.acod.play.app.Interfaces.UpdateUI;
import com.acod.play.app.Models.SongResult;


import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Andrew on 7/1/2014.
 */
public class SearchAllSites extends AsyncTask<Void, Void, ArrayList<SongResult>> {
    private String query;
    private Context context;

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
        return searchMuzikApi();
    }


    private ArrayList<SongResult> searchMuzikApi() {
        return SearchMuzikApi.getSongs(query);
    }

    @Override
    protected void onPostExecute(ArrayList<SongResult> songResults) {
        super.onPostExecute(songResults);
        update.closeProgressDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable("results", songResults);

        if (songResults.size() > 0) {
            update.openResultsFragment(bundle);
        } else {
            Toast.makeText(context, "Song not found", Toast.LENGTH_LONG).show();
        }
    }
}
