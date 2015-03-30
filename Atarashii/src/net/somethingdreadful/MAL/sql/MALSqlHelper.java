package net.somethingdreadful.MAL.sql;

import android.accounts.Account;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.account.AccountService;

public class MALSqlHelper extends SQLiteOpenHelper {
    Context context;

    public static final String COLUMN_ID = "_id";
    public static final String TABLE_ANIME = "anime";
    private static final String CREATE_ANIME_TABLE = "create table "
            + TABLE_ANIME + "("
            + COLUMN_ID + " integer primary key, "
            + "recordName varchar, "
            + "recordType varchar, "
            + "imageUrl varchar, "
            + "recordStatus varchar, "
            + "memberScore float, "
            + "synopsis varchar, "
            + "episodesTotal integer, "
            + "classification string, "
            + "membersCount integer, "
            + "favoritedCount integer, "
            + "popularityRank integer, "
            + "rank integer, "
            + "startDate varchar, "
            + "endDate varchar, "
            + "listedId integer"
            + ");";
    //Since SQLite doesn't allow "dynamic" dates, we set the default timestamp an adequate distance in the
    //past (1 December 1982) to make sure it will be in the past for update calculations. This should be okay,
    //since we are going to update the column whenever we sync.
    private static final String ADD_ANIME_SYNC_TIME = "ALTER TABLE "
            + TABLE_ANIME
            + " ADD COLUMN lastUpdate integer NOT NULL DEFAULT 407570400";
    public static final String TABLE_MANGA = "manga";
    private static final String CREATE_MANGA_TABLE = "create table "
            + TABLE_MANGA + "("
            + COLUMN_ID + " integer primary key, "
            + "recordName varchar, "
            + "recordType varchar, "
            + "imageUrl varchar, "
            + "recordStatus varchar, "
            + "memberScore float, "
            + "synopsis varchar, "
            + "chaptersTotal integer, "
            + "volumesTotal integer, "
            + "membersCount integer, "
            + "favoritedCount integer, "
            + "popularityRank integer, "
            + "rank integer, "
            + "listedId integer"
            + ");";
    private static final String ADD_MANGA_SYNC_TIME = "ALTER TABLE "
            + TABLE_MANGA
            + " ADD COLUMN lastUpdate integer NOT NULL DEFAULT 407570400";
    public static final String TABLE_FRIENDS = "friends";
    public static final String TABLE_PROFILE = "profile";
    private static final String CREATE_PROFILE_TABLE = "create table "
            + TABLE_PROFILE + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "username varchar UNIQUE, "
            + "avatar_url varchar, "
            + "anime_time integer, "
            + "manga_chap integer, "
            + "about varchar, "
            + "list_order integer, "
            + "image_url_lge varchar, "
            + "image_url_banner varchar, "
            + "title_language varchar, "
            + "score_type integer, "
            + "notifications integer, "
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
            + "anime_time_days double, "
            + "anime_watching integer, "
            + "anime_completed integer, "
            + "anime_on_hold integer, "
            + "anime_dropped integer, "
            + "anime_plan_to_watch integer, "
            + "anime_total_entries integer, "
            + "manga_time_days double, "
            + "manga_reading integer, "
            + "manga_completed integer, "
            + "manga_on_hold integer, "
            + "manga_dropped integer, "
            + "manga_plan_to_read integer, "
            + "manga_total_entries integer "
            + ");";
    public static final String TABLE_FRIENDLIST = "friendlist";
    private static final String CREATE_FRIENDLIST_TABLE = "CREATE TABLE "
            + TABLE_FRIENDLIST + "("
            + "profile_id INTEGER NOT NULL, "
            + "friend_id INTEGER NOT NULL, "
            + "PRIMARY KEY(profile_id, friend_id)"
            + ");";

    public static final String TABLE_ANIMELIST = "animelist";
    private static final String CREATE_ANIMELIST_TABLE = "CREATE TABLE "
            + TABLE_ANIMELIST + "("
            + "profile_id integer NOT NULL REFERENCES " + TABLE_PROFILE + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "anime_id integer NOT NULL REFERENCES " + TABLE_ANIME + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "status varchar, "
            + "watched integer, "
            + "score integer, "
            + "watchedStart varchar, "
            + "watchedEnd varchar, "
            + "fansub varchar, "
            + "priority integer, "
            + "downloaded integer, "
            + "rewatch integer, "
            + "storage integer, "
            + "storageValue integer, "
            + "rewatchCount integer, "
            + "rewatchValue integer, "
            + "comments varchar, "
            + "dirty varchar DEFAULT NULL, "
            + "lastUpdate integer NOT NULL DEFAULT (strftime('%s','now')),"
            + "PRIMARY KEY(profile_id, anime_id)"
            + ");";

    public static final String TABLE_MANGALIST = "mangalist";
    private static final String CREATE_MANGALIST_TABLE = "CREATE TABLE "
            + TABLE_MANGALIST + "("
            + "profile_id integer NOT NULL REFERENCES " + TABLE_PROFILE + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "manga_id integer NOT NULL REFERENCES " + TABLE_MANGA + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "status varchar, "
            + "chaptersRead integer, "
            + "volumesRead integer, "
            + "score integer, "
            + "readStart varchar, "
            + "readEnd varchar, "
            + "priority integer, "
            + "downloaded integer, "
            + "rereading boolean, "
            + "rereadCount integer, "
            + "rereadValue integer, "
            + "comments varchar, "
            + "dirty varchar DEFAULT NULL, "
            + "lastUpdate integer NOT NULL DEFAULT (strftime('%s','now')),"
            + "PRIMARY KEY(profile_id, manga_id)"
            + ");";

    public static final String TABLE_PRODUCER = "producer";
    private static final String CREATE_PRODUCER_TABLE = "CREATE TABLE "
            + TABLE_PRODUCER + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "recordName varchar UNIQUE"
            + ");";

    public static final String TABLE_ANIME_PRODUCER = "anime_producer";
    private static final String CREATE_ANIME_PRODUCER_TABLE = "CREATE TABLE "
            + TABLE_ANIME_PRODUCER + "("
            + "anime_id integer NOT NULL REFERENCES " + TABLE_ANIME + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "producer_id integer NOT NULL REFERENCES " + TABLE_PRODUCER + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "PRIMARY KEY(anime_id, producer_id)"
            + ");";

    /*
     * Anime-/Manga-relation tables
     *
     * Structure for these tables is
     * - anime id
     * - related id
     * - relation type (side story, summary, alternative version etc.), see RELATION_TYPE-constants
     *   below
     */

    /* relation types, as these constants are only used for database queries they can be stored as strings
     * to avoid conversion in every query
     */
    public static final String RELATION_TYPE_ALTERNATIVE = "0";
    public static final String RELATION_TYPE_CHARACTER = "1";
    public static final String RELATION_TYPE_SIDE_STORY = "2";
    public static final String RELATION_TYPE_SPINOFF = "3";
    public static final String RELATION_TYPE_SUMMARY = "4";
    public static final String RELATION_TYPE_ADAPTATION = "5";
    public static final String RELATION_TYPE_RELATED = "6";
    public static final String RELATION_TYPE_PREQUEL = "7";
    public static final String RELATION_TYPE_SEQUEL = "8";
    public static final String RELATION_TYPE_PARENT_STORY = "9";
    public static final String RELATION_TYPE_OTHER = "10";

    public static final String TABLE_ANIME_ANIME_RELATIONS = "rel_anime_anime";
    private static final String CREATE_ANIME_ANIME_RELATIONS_TABLE = "CREATE TABLE "
            + TABLE_ANIME_ANIME_RELATIONS + "("
            + "anime_id integer NOT NULL REFERENCES " + TABLE_ANIME + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "related_id integer NOT NULL REFERENCES " + TABLE_ANIME + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "relationType integer NOT NULL, "
            + "PRIMARY KEY(anime_id, related_id)"
            + ");";
    public static final String TABLE_ANIME_MANGA_RELATIONS = "rel_anime_manga";
    private static final String CREATE_ANIME_MANGA_RELATIONS_TABLE = "CREATE TABLE "
            + TABLE_ANIME_MANGA_RELATIONS + "("
            + "anime_id integer NOT NULL REFERENCES " + TABLE_ANIME + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "related_id integer NOT NULL REFERENCES " + TABLE_MANGA + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "relationType integer NOT NULL, " // can currently only be RELATION_TYPE_ADAPTATION
            + "PRIMARY KEY(anime_id, related_id)"
            + ");";

    public static final String TABLE_MANGA_MANGA_RELATIONS = "rel_manga_manga";
    private static final String CREATE_MANGA_MANGA_RELATIONS_TABLE = "CREATE TABLE "
            + TABLE_MANGA_MANGA_RELATIONS + "("
            + "manga_id integer NOT NULL REFERENCES " + TABLE_MANGA + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "related_id integer NOT NULL REFERENCES " + TABLE_MANGA + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "relationType integer NOT NULL, "
            + "PRIMARY KEY(manga_id, related_id)"
            + ");";
    public static final String TABLE_MANGA_ANIME_RELATIONS = "rel_manga_anime";
    private static final String CREATE_MANGA_ANIME_RELATIONS_TABLE = "CREATE TABLE "
            + TABLE_MANGA_ANIME_RELATIONS + "("
            + "manga_id integer NOT NULL REFERENCES " + TABLE_MANGA + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "related_id integer NOT NULL REFERENCES " + TABLE_ANIME + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "relationType integer NOT NULL, " // can currently only be RELATION_TYPE_ADAPTATION
            + "PRIMARY KEY(manga_id, related_id)"
            + ");";

    public static final String TABLE_GENRES = "genres";
    private static final String CREATE_GENRES_TABLE = "CREATE TABLE "
            + TABLE_GENRES + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "recordName varchar NOT NULL "
            + ");";
    public static final String TABLE_ANIME_GENRES = "anime_genres";
    private static final String CREATE_ANIME_GENRES_TABLE = "CREATE TABLE "
            + TABLE_ANIME_GENRES + "("
            + "anime_id integer NOT NULL REFERENCES " + TABLE_ANIME + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "genre_id integer NOT NULL REFERENCES " + TABLE_GENRES + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "PRIMARY KEY(anime_id, genre_id)"
            + ");";
    public static final String TABLE_MANGA_GENRES = "manga_genres";
    private static final String CREATE_MANGA_GENRES_TABLE = "CREATE TABLE "
            + TABLE_MANGA_GENRES + "("
            + "manga_id integer NOT NULL REFERENCES " + TABLE_MANGA + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "genre_id integer NOT NULL REFERENCES " + TABLE_GENRES + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "PRIMARY KEY(manga_id, genre_id)"
            + ");";

    public static final String TABLE_TAGS = "tags";
    private static final String CREATE_TAGS_TABLE = "CREATE TABLE "
            + TABLE_TAGS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "recordName varchar NOT NULL "
            + ");";
    public static final String TABLE_ANIME_TAGS = "anime_tags";
    private static final String CREATE_ANIME_TAGS_TABLE = "CREATE TABLE "
            + TABLE_ANIME_TAGS + "("
            + "anime_id integer NOT NULL REFERENCES " + TABLE_ANIME + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "tag_id integer NOT NULL REFERENCES " + TABLE_TAGS + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "PRIMARY KEY(anime_id, tag_id)"
            + ");";
    public static final String TABLE_ANIME_PERSONALTAGS = "anime_personaltags";
    private static final String CREATE_ANIME_PERSONALTAGS_TABLE = "CREATE TABLE "
            + TABLE_ANIME_PERSONALTAGS + "("
            + "anime_id integer NOT NULL REFERENCES " + TABLE_ANIME + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "tag_id integer NOT NULL REFERENCES " + TABLE_TAGS + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "PRIMARY KEY(anime_id, tag_id)"
            + ");";
    public static final String TABLE_MANGA_TAGS = "manga_tags";
    private static final String CREATE_MANGA_TAGS_TABLE = "CREATE TABLE "
            + TABLE_MANGA_TAGS + "("
            + "manga_id integer NOT NULL REFERENCES " + TABLE_MANGA + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "tag_id integer NOT NULL REFERENCES " + TABLE_TAGS + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "PRIMARY KEY(manga_id, tag_id)"
            + ");";
    public static final String TABLE_MANGA_PERSONALTAGS = "manga_personaltags";
    private static final String CREATE_MANGA_PERSONALTAGS_TABLE = "CREATE TABLE "
            + TABLE_MANGA_PERSONALTAGS + "("
            + "manga_id integer NOT NULL REFERENCES " + TABLE_MANGA + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "tag_id integer NOT NULL REFERENCES " + TABLE_TAGS + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "PRIMARY KEY(manga_id, tag_id)"
            + ");";
    /* title types, working the same way as the relation types
     */
    public static final String TITLE_TYPE_JAPANESE = "0";
    public static final String TITLE_TYPE_ENGLISH = "1";
    public static final String TITLE_TYPE_SYNONYM = "2";
    public static final String TABLE_ANIME_OTHER_TITLES = "animeothertitles";
    private static final String CREATE_ANIME_OTHER_TITLES_TABLE = "CREATE TABLE "
            + TABLE_ANIME_OTHER_TITLES + "("
            + "anime_id integer NOT NULL REFERENCES " + TABLE_ANIME + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "titleType integer NOT NULL, "
            + "title varchar NOT NULL, "
            + "PRIMARY KEY(anime_id, titleType, title)"
            + ");";
    public static final String TABLE_MANGA_OTHER_TITLES = "mangaothertitles";
    private static final String CREATE_MANGA_OTHER_TITLES_TABLE = "CREATE TABLE "
            + TABLE_MANGA_OTHER_TITLES + "("
            + "manga_id integer NOT NULL REFERENCES " + TABLE_MANGA + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "titleType integer NOT NULL, "
            + "title varchar NOT NULL, "
            + "PRIMARY KEY(manga_id, titleType, title)"
            + ");";

    public static final String TABLE_ACTIVITIES = "activities";
    private static final String CREATE_ACTIVITIES_TABLE = "CREATE TABLE "
            + TABLE_ACTIVITIES + "("
            + COLUMN_ID + " integer primary key, "
            + "user integer NOT NULL REFERENCES " + TABLE_PROFILE + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "type varchar NOT NULL, "
            + "created varchar NOT NULL, "
            + "reply_count integer NOT NULL, "
            + "series_anime integer, "
            + "series_manga integer, "
            + "status varchar, "
            + "value varchar"
            + ");";

    public static final String TABLE_ACTIVITIES_USERS = "activities_users";
    private static final String CREATE_ACTIVITIES_USERS_TABLE = "CREATE TABLE "
            + TABLE_ACTIVITIES_USERS + "("
            + "profile_id integer NOT NULL REFERENCES " + TABLE_PROFILE + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "activity_id integer NOT NULL REFERENCES " + TABLE_ACTIVITIES + "(" + COLUMN_ID + ") ON DELETE CASCADE, "
            + "PRIMARY KEY(profile_id, activity_id)"
            + ");";

    protected static final String DATABASE_NAME = "MAL.db";
    private static final int DATABASE_VERSION = 11;
    private static MALSqlHelper instance;

    public MALSqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized MALSqlHelper getHelper(Context context) {
        if (instance == null) {
            instance = new MALSqlHelper(context);
        }
        instance.context = context;
        return instance;

    }

    public void deleteDatabase(Context context) {
        instance = null;
        context.deleteDatabase(DATABASE_NAME);
    }

    @Override
    public String getDatabaseName() {
        return DATABASE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ANIME_TABLE);
        db.execSQL(CREATE_MANGA_TABLE);
        db.execSQL(CREATE_PROFILE_TABLE);
        db.execSQL(CREATE_FRIENDLIST_TABLE);
        db.execSQL(CREATE_ANIMELIST_TABLE);
        db.execSQL(CREATE_MANGALIST_TABLE);
        db.execSQL(CREATE_ANIME_ANIME_RELATIONS_TABLE);
        db.execSQL(CREATE_ANIME_MANGA_RELATIONS_TABLE);
        db.execSQL(CREATE_MANGA_MANGA_RELATIONS_TABLE);
        db.execSQL(CREATE_MANGA_ANIME_RELATIONS_TABLE);
        db.execSQL(CREATE_GENRES_TABLE);
        db.execSQL(CREATE_ANIME_GENRES_TABLE);
        db.execSQL(CREATE_MANGA_GENRES_TABLE);
        db.execSQL(CREATE_TAGS_TABLE);
        db.execSQL(CREATE_ANIME_TAGS_TABLE);
        db.execSQL(CREATE_MANGA_TAGS_TABLE);
        db.execSQL(CREATE_ANIME_OTHER_TITLES_TABLE);
        db.execSQL(CREATE_MANGA_OTHER_TITLES_TABLE);
        db.execSQL(CREATE_PRODUCER_TABLE);
        db.execSQL(CREATE_ANIME_PRODUCER_TABLE);
        db.execSQL(CREATE_ANIME_PERSONALTAGS_TABLE);
        db.execSQL(CREATE_MANGA_PERSONALTAGS_TABLE);
        db.execSQL(CREATE_ACTIVITIES_TABLE);
        db.execSQL(CREATE_ACTIVITIES_USERS_TABLE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Crashlytics.log(Log.VERBOSE, "MALX", "MALSQLHelper.OnUpgrade: Upgrading database from version " + oldVersion + " to " + newVersion);

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
            /*
             * sadly SQLite does not have good alter table support, so the profile table needs to be
             * recreated :(
             *
             * profile table changes:
             *
             * fix unnecessary anime_time_days(_d) and manga_time_days(_d) definitions: storing the same value
             * with different field types is bad practice, better convert them when needed
             * 
             * Update for unique declaration of recordID (as this is the anime/manga id returned by the API it should be unique anyway)
             * and unique declaration of username in friends/profile table
             * this gives us the ability to update easier because we can call SQLiteDatabase.replace() which inserts 
             * new records and updates existing records automatically
             */

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

            db.execSQL(CREATE_PROFILE_TABLE);
        }

        if (oldVersion < 7) {
            /*
             * We are dropping the existing friendlist and made a relation table
             * This way we can pass the data simply to the friendlist
             *
             * The friend_since date has been removed due inconsistency
             */
            db.execSQL("drop table if exists " + TABLE_FRIENDS);
            db.execSQL("drop table " + TABLE_PROFILE);

            db.execSQL(CREATE_PROFILE_TABLE);
            db.execSQL(CREATE_FRIENDLIST_TABLE);
        }

        if (oldVersion < 8) {
            /*
             * upgrade database to support multiple anime-/mangalists
             * because of SQLite's basic ALTER TABLE support which does not allow to delete columns
             * we have to recreate the tables
             */
            // we need the username for building the relation tables, so get the account here
            Integer userId = null;
            Account account = AccountService.getAccount();
            if (account != null) {
                Cursor userCursor = db.query(TABLE_PROFILE, new String[]{COLUMN_ID}, "username = ?", new String[]{account.name}, null, null, null);
                if (userCursor.moveToFirst()) {
                    userId = userCursor.getInt(0);
                }
                userCursor.close();

                if (userId == null) { // the users profile does not exist until now, so add it as simple dummy (will get all data once the user clicks on his profile)
                    ContentValues cv = new ContentValues();
                    cv.put("username", account.name);
                    Long userAddResult = db.insert(TABLE_PROFILE, null, cv);
                    if (userAddResult > -1) {
                        userId = userAddResult.intValue();
                    }
                }
            }

            // "SELECT * ..." won't work here because recordId is remapped to COLUMN_ID
            String animeUpdateFields = COLUMN_ID + ", recordName, "
                    + "recordType, imageUrl, recordStatus, memberScore, episodesTotal";
            db.execSQL("create table temp_table as select recordId as " + animeUpdateFields + ", episodesWatched, myStatus, myScore, dirty, lastUpdate from " + TABLE_ANIME);
            db.execSQL("drop table " + TABLE_ANIME);
            db.execSQL(CREATE_ANIME_TABLE);
            db.execSQL("insert into " + TABLE_ANIME + "(" + animeUpdateFields + ") select " + animeUpdateFields + " from temp_table;");
            db.execSQL(CREATE_ANIMELIST_TABLE);
            // build relations in animelist table
            if (userId != null) {
                try {
                    Cursor acursor = db.query("temp_table", new String[]{COLUMN_ID, "myStatus", "myScore", "episodesWatched", "dirty", "lastUpdate"}, null, null, null, null, COLUMN_ID);
                    if (acursor.moveToFirst()) {
                        do {
                            ContentValues cv = new ContentValues();
                            cv.put("profile_id", userId);
                            cv.put("anime_id", acursor.getInt(0));
                            cv.put("status", acursor.getString(1));
                            cv.put("score", acursor.getInt(2));
                            cv.put("watched", acursor.getInt(3));
                            cv.put("dirty", acursor.getInt(4));
                            cv.put("lastUpdate", acursor.getInt(5));
                            db.insert(TABLE_ANIMELIST, null, cv);
                        } while (acursor.moveToNext());
                    }
                    acursor.close();
                } catch (Exception e) {
                    Crashlytics.log(Log.ERROR, "MALX", "MALSQLHelper.OnUpgrade: <8: building animelist after database upgrade: " + e.getMessage());
                }
            }
            db.execSQL("drop table temp_table;");

            // "SELECT * ..." won't work here because recordId is remapped to COLUMN_ID
            String mangaUpdateFields = COLUMN_ID + ", recordName, "
                    + "recordType, imageUrl, recordStatus, memberScore, chaptersTotal, volumesTotal";
            db.execSQL("create table temp_table as select recordId as " + mangaUpdateFields + ", myStatus, myScore, chaptersRead, volumesRead, dirty, lastUpdate from " + TABLE_MANGA);
            db.execSQL("drop table " + TABLE_MANGA);
            db.execSQL(CREATE_MANGA_TABLE);
            db.execSQL("insert into " + TABLE_MANGA + "(" + mangaUpdateFields + ") select " + mangaUpdateFields + " from temp_table;");
            db.execSQL(CREATE_MANGALIST_TABLE);
            // build relations in mangalist table
            if (userId != null) {
                try {
                    Cursor mcursor = db.query("temp_table", new String[]{COLUMN_ID, "myStatus", "myScore", "chaptersRead", "volumesRead", "dirty", "lastUpdate"}, null, null, null, null, COLUMN_ID);
                    if (mcursor.moveToFirst()) {
                        do {
                            ContentValues cv = new ContentValues();
                            cv.put("profile_id", userId);
                            cv.put("manga_id", mcursor.getInt(0));
                            cv.put("status", mcursor.getString(1));
                            cv.put("score", mcursor.getInt(2));
                            cv.put("chaptersRead", mcursor.getInt(3));
                            cv.put("volumesRead", mcursor.getInt(4));
                            cv.put("dirty", mcursor.getInt(5));
                            cv.put("lastUpdate", mcursor.getInt(6));
                            db.insert(TABLE_MANGALIST, null, cv);
                        } while (mcursor.moveToNext());
                    }
                    mcursor.close();
                } catch (Exception e) {
                    Crashlytics.log(Log.ERROR, "MALX", "MALSQLHelper.OnUpgrade: <8: building mangalist after database upgrade: " + e.getMessage());
                }
            }
            db.execSQL("drop table temp_table;");
            db.execSQL(CREATE_ANIME_ANIME_RELATIONS_TABLE);
            db.execSQL(CREATE_ANIME_MANGA_RELATIONS_TABLE);
            db.execSQL(CREATE_MANGA_MANGA_RELATIONS_TABLE);
            db.execSQL(CREATE_MANGA_ANIME_RELATIONS_TABLE);
            db.execSQL(CREATE_GENRES_TABLE);
            db.execSQL(CREATE_ANIME_GENRES_TABLE);
            db.execSQL(CREATE_MANGA_GENRES_TABLE);
            db.execSQL(CREATE_TAGS_TABLE);
            db.execSQL(CREATE_ANIME_TAGS_TABLE);
            db.execSQL(CREATE_MANGA_TAGS_TABLE);
            db.execSQL(CREATE_ANIME_OTHER_TITLES_TABLE);
            db.execSQL(CREATE_MANGA_OTHER_TITLES_TABLE);
        }

        if (oldVersion < 9) {
            /*
             * In version 9 We added the start/end dates for anime & manga records.
             */
            // update animelist table
            String animeUpdateFields = "profile_id, anime_id, status, watched, score, dirty, lastUpdate";
            db.execSQL("create table temp_table as select " + animeUpdateFields + " from " + TABLE_ANIMELIST);
            db.execSQL("drop table " + TABLE_ANIMELIST);
            db.execSQL(CREATE_ANIMELIST_TABLE);
            db.execSQL("insert into " + TABLE_ANIMELIST + " (" + animeUpdateFields + ") select " + animeUpdateFields + " from temp_table;");
            db.execSQL("drop table temp_table;");

            // update mangalist table
            String mangaUpdateFields = "profile_id, manga_id, status, chaptersRead, volumesRead, score, dirty, lastUpdate";
            db.execSQL("create table temp_table as select " + mangaUpdateFields + " from " + TABLE_MANGALIST);
            db.execSQL("drop table " + TABLE_MANGALIST);
            db.execSQL(CREATE_MANGALIST_TABLE);
            db.execSQL("insert into " + TABLE_MANGALIST + " (" + mangaUpdateFields + ") select " + mangaUpdateFields + " from temp_table;");
            db.execSQL("drop table temp_table;");
        }

        if (oldVersion < 10) {
            /*
             * In version 10 We added new personal details, AL support & are using the new dirty flag system
             */
            // update animelist table
            db.execSQL("create table temp_table as select * from " + TABLE_ANIMELIST);
            db.execSQL("update temp_table set dirty = NULL");
            db.execSQL("drop table " + TABLE_ANIMELIST);
            db.execSQL(CREATE_ANIMELIST_TABLE);
            db.execSQL("insert into " + TABLE_ANIMELIST + " select * from temp_table;");
            db.execSQL("drop table temp_table;");

            // update mangalist table
            db.execSQL("create table temp_table as select * from " + TABLE_MANGALIST);
            db.execSQL("update temp_table set dirty = NULL");
            db.execSQL("drop table " + TABLE_MANGALIST);
            db.execSQL(CREATE_MANGALIST_TABLE);
            db.execSQL("insert into " + TABLE_MANGALIST + " select * from temp_table;");
            db.execSQL("drop table temp_table;");

            // add new tables
            db.execSQL(CREATE_PRODUCER_TABLE);
            db.execSQL(CREATE_ANIME_PRODUCER_TABLE);
            db.execSQL(CREATE_ANIME_PERSONALTAGS_TABLE);
            db.execSQL(CREATE_MANGA_PERSONALTAGS_TABLE);
            db.execSQL(CREATE_ACTIVITIES_TABLE);
            db.execSQL(CREATE_ACTIVITIES_USERS_TABLE);
        }
    }
}
