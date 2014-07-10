package com.acod.play.app.Database;

import android.provider.BaseColumns;

/**
 * Created by andrew on 10/07/14.
 */
public final class DatabaseContract {
    public static abstract class SongEntry implements BaseColumns {
        public static final String DB_NAME = "MySongs.db";
        public static final String TABLE_NAME = "Songs";

        public static final String COLUMN_NAME_TITLE = "songtitle";
        public static final String COLUMN_NAME_URL = "url";

    }

}
