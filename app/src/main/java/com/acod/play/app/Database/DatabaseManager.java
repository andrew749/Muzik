package com.acod.play.app.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.acod.play.app.Models.SongResult;


/**
 * Created by andrew on 10/07/14.
 */
public class DatabaseManager {
    SQLiteDatabase db;
    DatabaseHelper helper;

    public DatabaseManager(Context context) {
        helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }

    public long putValue(String title, String url) {
        ContentValues v = new ContentValues();
        v.put(DatabaseContract.SongEntry.COLUMN_NAME_TITLE, title);
        v.put(DatabaseContract.SongEntry.COLUMN_NAME_URL, url);
        long newRowId;

        newRowId = db.insert(DatabaseContract.SongEntry.TABLE_NAME, null, v);
        return newRowId;
    }

    public long putEntry(SongResult result) {
        ContentValues v = new ContentValues();
        v.put(DatabaseContract.SongEntry.COLUMN_NAME_TITLE, result.getName());
        v.put(DatabaseContract.SongEntry.COLUMN_NAME_URL, result.getUrl());
        long newRowId;

        newRowId = db.insert(DatabaseContract.SongEntry.TABLE_NAME, null, v);
        return newRowId;
    }

    public void remove(long id) {
        String string = String.valueOf(id);
        db.execSQL("DELETE FROM " + DatabaseContract.SongEntry.TABLE_NAME + " WHERE _id = '" + string + "'");
    }

    public void remove(String id) {
        db.execSQL("DELETE FROM " + DatabaseContract.SongEntry.TABLE_NAME + " WHERE _id = '" + id + "'");
    }
}
