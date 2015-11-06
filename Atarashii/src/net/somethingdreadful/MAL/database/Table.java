package net.somethingdreadful.MAL.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

public class Table {
    String queryString = "";
    private static SQLiteDatabase db;

    public static Table create(SQLiteDatabase db) {
        Table.db = db;
        return new Table();
    }

    public void createOtherTitles(String table, String ListTypeTable) {
        queryString += "CREATE TABLE "
                + table + "("
                + getTagsColumn(table) + " integer NOT NULL REFERENCES " + ListTypeTable + "(" + DatabaseTest.COLUMN_ID + ") ON DELETE CASCADE, "
                + "titleType integer NOT NULL, "
                + "title varchar NOT NULL, "
                + "PRIMARY KEY(" + getTagsColumn(table) + ", titleType, title)"
                + ");";
        run();
    }

    public void createRecord(String table) {
        queryString += "create table "
                + table + "("
                + DatabaseTest.COLUMN_ID + " integer primary key, "
                + "title varchar, "
                + "type varchar, "
                + "imageUrl varchar, "
                + "synopsis varchar, "
                + "status varchar, "
                + "startDate varchar, "
                + "endDate varchar, "
                + "score integer, "
                + "priority integer, "
                + "classification varchar, "
                + "averageScore varchar, "
                + "averageScoreCount varchar, "
                + "popularity integer, "
                + "rank integer, "
                + "notes varchar, "
                + "favoritedCount integer, "
                + "dirty varchar, "
                + "createFlag integer, "
                + "deleteFlag integer, "
                + "widget integer, ";


        if (table.equals(DatabaseTest.TABLE_ANIME))
            queryString += "duration integer, "
                    + "episodes integer, "
                    + "youtubeId varchar, "
                    //+ "listStats , " TODO: investigate what this really is
                    + "airingTime varchar, "
                    + "nextEpisode integer, "
                    + "watchedStatus varchar, "
                    + "watchedEpisodes integer, "
                    + "watchingStart varchar, "
                    + "watchingEnd varchar, "
                    + "fansubGroup varchar, "
                    + "storage integer, "
                    + "storageValue float, "
                    + "epsDownloaded integer, "
                    + "rewatching integer, "
                    + "rewatchCount integer, "
                    + "rewatchValue integer "
                    + ");";
        else
            queryString += "chapters integer, "
                    + "volumes integer, "
                    + "readStatus varchar, "
                    + "chaptersRead integer, "
                    + "volumesRead integer, "
                    + "readingStart varchar, "
                    + "readingEnd varchar, "
                    + "chapDownloaded integer, "
                    + "rereading integer, "
                    + "rereadCount integer, "
                    + "rereadValue integer "
                    + ");";
        run();
    }

    public void createFriendlist() {
        queryString += "create table "
                + DatabaseTest.TABLE_FRIENDLIST + "("
                + "username varchar, "
                + "imageUrl varchar, "
                + "lastOnline varchar "
                + ");";
        run();
    }

    /**
     * Create the profile table.
     */
    public void createProfile() {
        queryString += "create table "
                + DatabaseTest.TABLE_PROFILE + "("
                + "username varchar UNIQUE, "
                + "imageUrl varchar, "
                + "imageUrlBanner varchar, "
                + "notifications integer, "
                + "lastOnline varchar, "
                + "status varchar, "
                + "gender varchar, "
                + "birthday varchar, "
                + "location varchar, "
                + "website varchar, "
                + "joinDate varchar, "
                + "accessRank varchar, "
                + "animeListViews integer, "
                + "mangaListViews integer, "
                + "forumPosts integer, "
                + "comments integer, "

                + "AnimetimeDays double, "
                + "Animewatching integer, "
                + "Animecompleted integer, "
                + "AnimeonHold integer, "
                + "Animedropped integer, "
                + "AnimeplanToWatch integer, "
                + "AnimetotalEntries integer, "

                + "MangatimeDays double, "
                + "Mangareading integer, "
                + "Mangacompleted integer, "
                + "MangaonHold integer, "
                + "Mangadropped integer, "
                + "MangaplanToRead integer, "
                + "MangatotalEntries integer "
                + ");";
        run();
    }

    /**
     * Create tags table.
     *
     * @param table     The table name
     * @param refTable1 The table that should get referenced
     * @param refTable2 The table that will be referenced with
     */
    public void createTags(String table, String refTable1, String refTable2) {
        queryString += "CREATE TABLE " + table + "("
                + getTagsColumn(table) + " integer NOT NULL REFERENCES " + refTable1 + "(" + DatabaseTest.COLUMN_ID + ") ON DELETE CASCADE, "
                + "tag_id integer NOT NULL REFERENCES " + refTable2 + "(" + DatabaseTest.COLUMN_ID + ") ON DELETE CASCADE, "
                + "PRIMARY KEY(" + getTagsColumn(table) + ", tag_id)"
                + ");";
        run();
    }

    /**
     * Get the id column name
     *
     * @param table The table name
     * @return String The id column name
     */
    private String getTagsColumn(String table) {
        return table.contains("anime") ? "anime_id" : "manga_id";
    }

    /**
     * Create relation table.
     *
     * @param table     The table name
     * @param refTable1 The table that should get referenced
     * @param refTable2 The table that will be referenced with
     */
    public void createRelation(String table, String refTable1, String refTable2) {
        queryString += "CREATE TABLE " + table + "("
                + DatabaseTest.COLUMN_ID + " integer NOT NULL REFERENCES " + refTable1 + "(" + DatabaseTest.COLUMN_ID + ") ON DELETE CASCADE, "
                + "relationId integer NOT NULL REFERENCES " + refTable2 + "(" + DatabaseTest.COLUMN_ID + ") ON DELETE CASCADE, "
                + "relationType integer NOT NULL, "
                + "PRIMARY KEY(" + DatabaseTest.COLUMN_ID + ", relationId)"
                + ");";
        run();
    }

    public void run() {
        try {
            db.execSQL(queryString);
        } catch (Exception e) {
            Crashlytics.log(Log.INFO, "MALX", "Table.run(" + toString() + "): " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return queryString;
    }
}