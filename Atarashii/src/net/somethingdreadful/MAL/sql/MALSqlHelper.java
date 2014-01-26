package net.somethingdreadful.MAL.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MALSqlHelper extends SQLiteOpenHelper {

    protected static final String DATABASE_NAME = "MAL.db";
    private static final int DATABASE_VERSION = 7;

    private static MALSqlHelper instance;

    public static final String COLUMN_ID = "_id";
    public static final String TABLE_ANIME = "anime";
    public static final String TABLE_MANGA = "manga";
    public static final String TABLE_FRIENDS = "friends";
    public static final String TABLE_PROFILE = "profile";

    private static final String CREATE_ANIME_TABLE = "create table "
            + TABLE_ANIME + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "recordID integer UNIQUE, "
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
            + "recordID integer UNIQUE, "
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
 
    private static final String CREATE_FRIENDS_TABLE = "create table "
            + TABLE_FRIENDS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "username varchar UNIQUE, "
            + "avatar_url varchar, "
            + "last_online varchar, "
            + "friend_since varchar "
            + ");";
    
    private static final String CREATE_PROFILE_TABLE = "create table "
            + TABLE_PROFILE + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "username varchar UNIQUE, "
            + "avatar_url varchar, "
            + "birthday varchar, "
            + "location varchar, "
            + "website varchar, "
            + "comments integer, "
            + "forum_posts integer, "
            + "last_online varchar, "
            + "gender varchar, "
            + "join_date varchar, "
            + "access_rank varchar, "
            + "anime_list_views integer, "
            + "manga_list_views integer, "
            + "anime_time_days_d double, "	//anime
            + "anime_time_days integer, "
            + "anime_watching integer, "
            + "anime_completed integer, "
            + "anime_on_hold integer, "
            + "anime_dropped integer, "
            + "anime_plan_to_watch integer, "
            + "anime_total_entries integer, "
            + "manga_time_days_d double, "	//manga
            + "manga_time_days integer, "
            + "manga_reading integer, "
            + "manga_completed integer, "
            + "manga_on_hold integer, "
            + "manga_dropped integer, "
            + "manga_plan_to_read integer, "
            + "manga_total_entries integer "
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
    
    /*
     * Update for unique declaration of recordID (as this is the anime/manga id it should be unique anyway)
     * and unique declaration of username in friends/profile table
     * this gives us the ability to update easier because we can call SQLiteDatabase.replace() which inserts 
     * new records and updates existing records automatically
     */
    private static final String ADD_ANIME_UNIQUE_RECORDID = "ALTER TABLE "
    		+ TABLE_ANIME
    		+ " ADD UNIQUE(recordID)";
    private static final String ADD_MANGA_UNIQUE_RECORDID = "ALTER TABLE "
    		+ TABLE_MANGA
    		+ " ADD UNIQUE(recordID)";
    private static final String ADD_FRIENDS_UNIQUE_USERNAME = "ALTER TABLE "
            + TABLE_FRIENDS
            + " ADD UNIQUE(username)";
    private static final String ADD_PROFILE_UNIQUE_USERNAME = "ALTER TABLE "
            + TABLE_PROFILE
            + " ADD UNIQUE(username)";

    /*
     * fix unnecessary anime_time_days(_d) and manga_time_days(_d) definitions: storing the same value
     * with different field types is bad practice, better convert them when needed
     */
    private static final String FIX_PROFILE_TIME_DAYS = "ALTER TABLE "
            + TABLE_ANIME
            + " DROP COLUMN anime_time_days_d,"
            + " MODIFY anime_time_days double,"
            + " DROP COLUMN manga_time_days_d,"
            + " MODIFY manga_time_days double";

    public MALSqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized MALSqlHelper getHelper(Context context) {
        if (instance == null) {
            instance = new MALSqlHelper(context);
        }
        return instance;

    }

    @Override
    public String getDatabaseName() {
        return DATABASE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ANIME_TABLE);
        db.execSQL(CREATE_MANGA_TABLE);
        db.execSQL(CREATE_FRIENDS_TABLE);
        db.execSQL(CREATE_PROFILE_TABLE);
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
        
        if (oldVersion < 6) {            
        	db.execSQL("create table temp_table as select * from " + TABLE_FRIENDS);
            db.execSQL("drop table " + TABLE_FRIENDS);
            db.execSQL(CREATE_FRIENDS_TABLE);
            db.execSQL("insert into " + TABLE_FRIENDS + " select * from temp_table;");
            db.execSQL("drop table temp_table;");
            
            db.execSQL("create table temp_table as select * from " + TABLE_PROFILE);
            db.execSQL("drop table " + TABLE_PROFILE);
            db.execSQL(CREATE_PROFILE_TABLE);
            db.execSQL("insert into " + TABLE_PROFILE + " select * from temp_table;");
            db.execSQL("drop table temp_table;");
        }
        
        if (oldVersion < 7) {
        	db.execSQL(ADD_ANIME_UNIQUE_RECORDID);
        	db.execSQL(ADD_MANGA_UNIQUE_RECORDID);
        	db.execSQL(ADD_FRIENDS_UNIQUE_USERNAME);
        	db.execSQL(ADD_PROFILE_UNIQUE_USERNAME);
        	db.execSQL(FIX_PROFILE_TIME_DAYS);
        }
    }
}
