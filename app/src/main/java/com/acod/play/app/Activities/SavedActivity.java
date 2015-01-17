package com.acod.play.app.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.acod.play.app.Database.DatabaseContract;
import com.acod.play.app.Database.DatabaseHelper;
import com.acod.play.app.Database.DatabaseManager;
import com.acod.play.app.Interfaces.DataTransmission;
import com.acod.play.app.Models.SongResult;
import com.acod.play.app.R;
import com.acod.play.app.XMLParser;
import com.acod.play.app.fragments.ResultsFragment;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ar.com.daidalos.afiledialog.FileChooserDialog;

/**
 * Created by andrew on 10/07/14.
 */
public class SavedActivity extends ActionBarActivity implements DataTransmission {
    ArrayList<SongResult> results = new ArrayList<SongResult>();

    ResultsFragment frag;
    FileChooserDialog dialog;

    private void getSongs() {
        results = getSongsFromDatabase();
        frag.setResults(results);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchview);
        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.savedactivitytitle));
        frag = (ResultsFragment) getFragmentManager().findFragmentById(R.id.resultsFrag);
        getSongs();
        dialog = new FileChooserDialog(this);
        dialog.setFolderMode(false);
        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {

            @Override
            public void onFileSelected(Dialog source, File file) {
                source.hide();
                Log.d("Play", "file");
                try {
                    InputStream is = new FileInputStream(file);
                    XMLParser parser = new XMLParser(getApplicationContext());
                    ArrayList<SongResult> imported = new ArrayList<SongResult>();
                    imported = parser.readFromXML(is);
                    DatabaseManager manager = new DatabaseManager(getApplicationContext());
                    for (SongResult result : imported) {
                        manager.putEntry(result);
                    }
                    results.addAll(imported);
                    //frag.setResults(results);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                frag.setResults(results);
            }

            @Override
            public void onFileSelected(Dialog source, File folder, String name) {
                source.hide();
                Log.d("Play", "folder");
            }

        });

    }

    @Override
    protected void onStop() {
        EasyTracker.getInstance(this).activityStop(this); // Add this method.

        super.onStop();
    }

    @Override
    protected void onStart() {
        EasyTracker.getInstance(this).activityStart(this); // Add this method.

        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final Context context = getApplicationContext();
        menu.add(R.string.savemenuitem).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //save to xml file
                XMLParser p = new XMLParser(getApplicationContext());
                try {
                    p.writeToXML(results);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("Play", "Failed to write file");
                }
                return false;
            }
        });
        menu.add(R.string.importmenuitem).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                dialog.show();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
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
