package com.acod.play.app.Activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.widget.Toast;

import com.acod.play.app.Database.DatabaseContract;
import com.acod.play.app.Database.DatabaseHelper;
import com.acod.play.app.Interfaces.DataTransmission;
import com.acod.play.app.Models.SongResult;
import com.acod.play.app.R;
import com.acod.play.app.XMLParser;
import com.acod.play.app.fragments.ResultsFragment;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import ar.com.daidalos.afiledialog.FileChooserActivity;

/**
 * Created by andrew on 10/07/14.
 */
public class SavedActivity extends SherlockActivity implements DataTransmission {
    ArrayList<SongResult> results = new ArrayList<SongResult>();

    ResultsFragment frag;

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
        getActionBar().setDisplayHomeAsUpEnabled(true);
        frag = (ResultsFragment) getFragmentManager().findFragmentById(R.id.resultsFrag);
        getSongs();


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
                Intent intent = new Intent(getApplicationContext(), FileChooserActivity.class);
                startActivityForResult(intent, 0);

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    //handle the file picker result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            boolean fileCreated = false;
            String filePath = "";

            Bundle bundle = data.getExtras();
            if (bundle != null) {
                if (bundle.containsKey(FileChooserActivity.OUTPUT_NEW_FILE_NAME)) {
                    fileCreated = true;
                    File folder = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
                    String name = bundle.getString(FileChooserActivity.OUTPUT_NEW_FILE_NAME);
                    filePath = folder.getAbsolutePath() + "/sdcard/" + name;
                } else {
                    fileCreated = false;
                    File file = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
                    filePath = file.getAbsolutePath();
                    XMLParser p = new XMLParser(getApplicationContext());
                    try {
                        results = p.readFromXML(new FileInputStream(file));
                        frag.setResults(results);
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            String message = fileCreated ? "File created" : "File opened";
            message += ": " + filePath;
            Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
            toast.show();
        }
        super.onActivityResult(requestCode, resultCode, data);
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
