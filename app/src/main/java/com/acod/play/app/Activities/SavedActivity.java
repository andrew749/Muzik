package com.acod.play.app.Activities;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.acod.play.app.Database.DatabaseContract;
import com.acod.play.app.Database.DatabaseHelper;
import com.acod.play.app.Models.Song;

/**
 * Created by andrew on 10/07/14.
 */
public class SavedActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseHelper mhelper = new DatabaseHelper(getApplicationContext());
        SQLiteDatabase db = mhelper.getReadableDatabase();
        String[] projection = {
                DatabaseContract.SongEntry._ID, DatabaseContract.SongEntry.COLUMN_NAME_TITLE, DatabaseContract.SongEntry.COLUMN_NAME_URL
        };
        Cursor c =db.query(DatabaseContract.SongEntry.TABLE_NAME,projection,null,null,null,null,null);
        //adapt data to list
    }

}
