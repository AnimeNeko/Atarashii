package net.somethingdreadful.MAL.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MALSqlHelper extends SQLiteOpenHelper {

    protected static final String DATABASE_NAME = "MAL.db";
    private static final int DATABASE_VERSION = 5;


    public static final String COLUMN_ID = "_id";
    public static final String TABLE_ANIME = "anime";
    public static final String TABLE_MANGA = "manga";

    private static final String CREATE_ANIME_TABLE = "create table "
            + TABLE_ANIME + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "recordID integer, "
            + "recordName varchar, "
            + "recordType varchar, "
            + "imageUrl varchar, "
            + "recordStatus varchar, "
            + "myStatus varchar, "
            + "memberScore float, "
            + "myScore integer, "
            + "synopsis varchar, "
            + "episodesWatched integer, "
            + "episodesTotal integer, "
            + "dirty boolean DEFAULT false, "
            + "lastUpdate integer NOT NULL DEFAULT (strftime('%s','now'))"
            + ");";

    private static final String CREATE_MANGA_TABLE = "create table "
            + TABLE_MANGA + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "recordID integer, "
            + "recordName varchar, "
            + "recordType varchar, "
            + "imageUrl varchar, "
            + "recordStatus varchar, "
            + "myStatus varchar, "
            + "memberScore float, "
            + "myScore integer, "
            + "synopsis varchar, "
            + "chaptersRead integer, "
            + "chaptersTotal integer, "
            + "volumesRead integer, "
            + "volumesTotal integer, "
            + "dirty boolean DEFAULT false, "
            + "lastUpdate integer NOT NULL DEFAULT (strftime('%s','now'))"
            + ");";

    //Since SQLite doesn't allow "dynamic" dates, we set the default timestamp an adequate distance in the
    //past (1 December 1982) to make sure it will be in the past for update calculations. This should be okay,
    //since we are going to update the column whenever we sync.
    private static final String ADD_ANIME_SYNC_TIME = "ALTER TABLE "
            + TABLE_ANIME
            + " ADD COLUMN lastUpdate integer NOT NULL DEFAULT 407570400";

    private static final String ADD_MANGA_SYNC_TIME = "ALTER TABLE "
            + TABLE_MANGA
            + " ADD COLUMN lastUpdate integer NOT NULL DEFAULT 407570400";


    public MALSqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ANIME_TABLE);
        db.execSQL(CREATE_MANGA_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("MALX", "Upgrading database from version " + oldVersion + " to " + newVersion);

        if ((oldVersion < 3)) {
            db.execSQL(CREATE_MANGA_TABLE);
        }

        if (oldVersion < 4) {
            db.execSQL(ADD_ANIME_SYNC_TIME);
            db.execSQL(ADD_MANGA_SYNC_TIME);
        }

        if (oldVersion < 5) {
            db.execSQL("create table temp_table as select * from " + TABLE_ANIME);
            db.execSQL("drop table " + TABLE_ANIME);
            db.execSQL(CREATE_ANIME_TABLE);
            db.execSQL("insert into " + TABLE_ANIME + " select * from temp_table;");
            db.execSQL("drop table temp_table;");

            db.execSQL("create table temp_table as select * from " + TABLE_MANGA);
            db.execSQL("drop table " + TABLE_MANGA);
            db.execSQL(CREATE_MANGA_TABLE);
            db.execSQL("insert into " + TABLE_MANGA + " select * from temp_table;");
            db.execSQL("drop table temp_table;");
        }
    }
}
