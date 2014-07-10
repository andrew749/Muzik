package com.acod.play.app.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


/**
 * Created by andrew on 10/07/14.
 */
public class Injector {
    SQLiteDatabase db;
    DatabaseHelper helper;

    public Injector(Context context) {
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

}
