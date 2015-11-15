package net.somethingdreadful.MAL.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

public class DatabaseTest extends SQLiteOpenHelper {
    public static final String NAME = "MAL.db";
    public static final int VERSION = 13;

    public static final String TABLE_ANIME = "anime";
    public static final String TABLE_MANGA = "manga";
    public static final String TABLE_PROFILE = "profile";
    public static final String TABLE_FRIENDLIST = "friendlist";
    public static final String TABLE_PRODUCER = "producer";
    public static final String TABLE_ANIME_PRODUCER = "anime_producer";
    public static final String TABLE_ANIME_OTHER_TITLES = "animeothertitles";
    public static final String TABLE_MANGA_OTHER_TITLES = "mangaothertitles";
    public static final String TABLE_ACTIVITIES = "activities";

    public static final String TABLE_ANIME_ANIME_RELATIONS = "rel_anime_anime";
    public static final String TABLE_ANIME_MANGA_RELATIONS = "rel_anime_manga";
    public static final String TABLE_MANGA_MANGA_RELATIONS = "rel_manga_manga";
    public static final String TABLE_MANGA_ANIME_RELATIONS = "rel_manga_anime";

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

    public static final String COLUMN_ID = "_id";

    private static final String CREATE_PRODUCER_TABLE = "CREATE TABLE "
            + TABLE_PRODUCER + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "title varchar UNIQUE"
            + ");";

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
  * below
  */

 /* relation types, as these constants are only used for database queries they can be stored as strings
  * to avoid conversion in every query
  */

    public static final String TABLE_GENRES = "genres";
    private static final String CREATE_GENRES_TABLE = "CREATE TABLE "
            + TABLE_GENRES + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "title varchar NOT NULL "
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
            + "title varchar NOT NULL "
            + ");";
    public static final String TABLE_ANIME_TAGS = "anime_tags";
    public static final String TABLE_ANIME_PERSONALTAGS = "anime_personaltags";
    public static final String TABLE_MANGA_TAGS = "manga_tags";
    public static final String TABLE_MANGA_PERSONALTAGS = "manga_personaltags";
    /* title types, working the same way as the relation types
     */
    public static final int TITLE_TYPE_JAPANESE = 0;
    public static final int TITLE_TYPE_ENGLISH = 1;
    public static final int TITLE_TYPE_SYNONYM = 2;

    private static final String CREATE_ACTIVITIES_TABLE = "CREATE TABLE "
            + TABLE_ACTIVITIES + "("
            + COLUMN_ID + " integer primary key, "
            + "type varchar NOT NULL, "
            + "created varchar NOT NULL, "
            + "reply_count integer NOT NULL, "
            + "series_anime integer, "
            + "series_manga integer, "
            + "status varchar, "
            + "value varchar"
            + ");";

    public DatabaseTest(Context context) {
        super(context, NAME, null, VERSION);
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(NAME);
    }

    @Override
    public String getDatabaseName() {
        return NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Table.create(db).createRecord(TABLE_ANIME);
        Table.create(db).createRecord(TABLE_MANGA);
        Table.create(db).createProfile();
        Table.create(db).createFriendlist();
        Table.create(db).createRelation(TABLE_ANIME_ANIME_RELATIONS, TABLE_ANIME, TABLE_ANIME);
        Table.create(db).createRelation(TABLE_ANIME_MANGA_RELATIONS, TABLE_ANIME, TABLE_MANGA);
        Table.create(db).createRelation(TABLE_MANGA_MANGA_RELATIONS, TABLE_MANGA, TABLE_MANGA);
        Table.create(db).createRelation(TABLE_MANGA_ANIME_RELATIONS, TABLE_MANGA, TABLE_ANIME);
        Table.create(db).createTags(TABLE_ANIME_TAGS, TABLE_ANIME, TABLE_TAGS);
        Table.create(db).createTags(TABLE_MANGA_TAGS, TABLE_MANGA, TABLE_TAGS);
        Table.create(db).createTags(TABLE_ANIME_PERSONALTAGS, TABLE_ANIME, TABLE_TAGS);
        Table.create(db).createTags(TABLE_MANGA_PERSONALTAGS, TABLE_MANGA, TABLE_TAGS);
        db.execSQL(CREATE_GENRES_TABLE);
        db.execSQL(CREATE_ANIME_GENRES_TABLE);
        db.execSQL(CREATE_MANGA_GENRES_TABLE);
        db.execSQL(CREATE_TAGS_TABLE);
        Table.create(db).createOtherTitles(TABLE_ANIME_OTHER_TITLES, TABLE_ANIME);
        Table.create(db).createOtherTitles(TABLE_MANGA_OTHER_TITLES, TABLE_MANGA);
        db.execSQL(CREATE_PRODUCER_TABLE);
        db.execSQL(CREATE_ANIME_PRODUCER_TABLE);
        db.execSQL(CREATE_ACTIVITIES_TABLE);
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
        Crashlytics.log(Log.INFO, "MALX", "DatabaseTest.OnUpgrade(): Upgrading database from version " + oldVersion + " to " + newVersion);
        /**
         * Date: 14-11-2015
         * Database version: 13
         * Application version: 2.2 Beta 1
         *
         * The models have been updated.
         * Instead of using 1 model for 2 websites (MAL & AL) we are using now separate models.
         * It will be easy to maintain.
         */
        try {
            if (oldVersion < 13) {
                // Drop existing tables if they exist
                db.execSQL("DROP TABLE IF EXISTS anime");
                db.execSQL("DROP TABLE IF EXISTS manga");
                db.execSQL("DROP TABLE IF EXISTS friends");
                db.execSQL("DROP TABLE IF EXISTS profile");
                db.execSQL("DROP TABLE IF EXISTS friendlist");
                db.execSQL("DROP TABLE IF EXISTS animelist");
                db.execSQL("DROP TABLE IF EXISTS mangalist");
                db.execSQL("DROP TABLE IF EXISTS producer");
                db.execSQL("DROP TABLE IF EXISTS anime_producer");
                db.execSQL("DROP TABLE IF EXISTS rel_anime_anime");
                db.execSQL("DROP TABLE IF EXISTS rel_anime_manga");
                db.execSQL("DROP TABLE IF EXISTS rel_manga_manga");
                db.execSQL("DROP TABLE IF EXISTS rel_manga_anime");
                db.execSQL("DROP TABLE IF EXISTS genres");
                db.execSQL("DROP TABLE IF EXISTS anime_genres");
                db.execSQL("DROP TABLE IF EXISTS manga_genres");
                db.execSQL("DROP TABLE IF EXISTS tags");
                db.execSQL("DROP TABLE IF EXISTS anime_tags");
                db.execSQL("DROP TABLE IF EXISTS anime_personaltags");
                db.execSQL("DROP TABLE IF EXISTS manga_tags");
                db.execSQL("DROP TABLE IF EXISTS manga_personaltags");
                db.execSQL("DROP TABLE IF EXISTS animeothertitles");
                db.execSQL("DROP TABLE IF EXISTS mangaothertitles");
                db.execSQL("DROP TABLE IF EXISTS activities");
                db.execSQL("DROP TABLE IF EXISTS activities_users");

                // Create new tables to replace the old ones
                onCreate(db);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseTest.OnUpgrade(): " + e.getMessage());
            Crashlytics.logException(e);
        }

        Crashlytics.log(Log.INFO, "MALX", "DatabaseTest.OnUpgrade(): Database upgrade finished");
    }
}
