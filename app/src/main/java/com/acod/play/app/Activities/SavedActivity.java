package com.acod.play.app.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;

import com.acod.play.app.Database.DatabaseContract;
import com.acod.play.app.Database.DatabaseHelper;
import com.acod.play.app.Interfaces.DataTransmission;
import com.acod.play.app.Models.SongResult;
import com.acod.play.app.R;
import com.acod.play.app.fragments.ResultsFragment;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;

/**
 * Created by andrew on 10/07/14.
 */
public class SavedActivity extends SherlockActivity implements DataTransmission {
    ArrayList<SongResult> results = new ArrayList<SongResult>();
    Thread getSongs = new Thread() {
        @Override
        public void run() {
            results = getSongsFromDatabase();
            frag.setResults(results);

        }
    };
    ResultsFragment frag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchview);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        frag = (ResultsFragment) getFragmentManager().findFragmentById(R.id.resultsFrag);
        getSongs.start();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<SongResult> getSongsFromDatabase() {
        ArrayList<SongResult> results = new ArrayList<SongResult>();
        DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
        SQLiteDatabase database = helper.getWritableDatabase();
        String[] projection = {DatabaseContract.SongEntry._ID, DatabaseContract.SongEntry.COLUMN_NAME_TITLE, DatabaseContract.SongEntry.COLUMN_NAME_URL};
        Cursor c = database.query(DatabaseContract.SongEntry.TABLE_NAME, projection, null, null, null, null, null, null);
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndex(DatabaseContract.SongEntry.COLUMN_NAME_TITLE));
            String url = c.getString(c.getColumnIndex(DatabaseContract.SongEntry.COLUMN_NAME_URL));
            long id = c.getLong(c.getColumnIndex(DatabaseContract.SongEntry._ID));
            if (HomescreenActivity.debugMode) {
                Log.d("Play", "Name=" + name + ",url=" + url);
            }
            SongResult result = new SongResult(name, url);
            result.setID(id);
            results.add(result);
        }
        return results;
    }

    @Override
    public void openPlayer(Bundle b) {
        if (HomescreenActivity.debugMode) {
            Log.d("PLAY", "Opening player");
        }
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("data", b);
        startActivity(intent);
    }
}
