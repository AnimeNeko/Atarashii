package net.somethingdreadful.MAL.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.api.MALModels.RecordStub;

import java.util.ArrayList;
import java.util.List;

public class Query {
    private String queryString = "";
    private static SQLiteDatabase db;

    public static Query newQuery(SQLiteDatabase db) {
        Query.db = db;
        return new Query();
    }

    public Query selectFrom(String column, String table) {
        queryString += " SELECT " + column + " FROM " + table;
        return this;
    }

    private Query innerJoinOn(String table1, String column1, String column2) {
        queryString += " INNER JOIN " + table1 + " ON " + column1 + " = " + column2;
        return this;
    }

    public Query where(String column1, String value) {
        queryString += " WHERE " + column1 + " = '" + value + "'";
        return this;
    }

    public Query whereEqGr(String column1, String value) {
        queryString += " WHERE " + column1 + " >= '" + value + "'";
        return this;
    }

    public Query isNotNull(String column1) {
        queryString += " WHERE " + column1 + " IS NOT NULL ";
        return this;
    }

    public Query andIsNotNull(String column1) {
        queryString += " AND " + column1 + " IS NOT NULL ";
        return this;
    }

    public Query andEquals(String column1, String value) {
        queryString += " AND " + column1 + " = '" + value + "'";
        return this;
    }

    public Query OrderBy(int type, String column) {
        switch (type) {
            case 1: // Name
                queryString += " ORDER BY " + column + " COLLATE NOCASE";
                break;
        }
        return this;
    }

    public Cursor run() {
        try {
            Cursor cursor = db.rawQuery(queryString, new String[]{});
            queryString = "";
            return cursor;
        } catch (Exception e) {
            log("run", e.getMessage(), true);
        }
        return null;
    }

    /**
     * Update or insert records.
     * @param table The table where the record should be updated
     * @param cv    The ContentValues which should be updated
     * @param id    The ID of the record
     */
    public int updateRecord(String table, ContentValues cv, int id) {
        int updateResult = db.update(table, cv, DatabaseTest.COLUMN_ID + " = " + id, new String[]{});
        if (updateResult == 0)
            return (int) db.insert(table, null, cv);
        return updateResult;
    }

    /**
     * Update or insert records.
     * @param table    The table where the record should be updated
     * @param cv       The ContentValues which should be updated
     * @param username The username of the record
     */
    public int updateRecord(String table, ContentValues cv, String username) {
        int updateResult = db.update(table, cv, "username" + " = '" + username + "'", new String[]{});
        if (updateResult == 0)
            return (int) db.insert(table, null, cv);
        return updateResult;
    }

    /**
     * The query to string.
     *
     * @return String Query
     */
    @Override
    public String toString() {
        return queryString;
    }

    /**
     * Update relations.
     *
     * @param table        The relation table name
     * @param relationType The relation type
     * @param id           The record id which should be related with
     * @param recordStubs  The records
     */
    public void updateRelation(String table, String relationType, int id, List<RecordStub> recordStubs) {
        if (id <= 0)
            log("updateRelation", "error saving relation: id <= 0", true);
        if (recordStubs == null || recordStubs.size() == 0)
            return;

        boolean relatedRecordExists;
        String recordTable;

        try {
            for (RecordStub relation : recordStubs) {
                recordTable = relation.isAnime() ? DatabaseTest.TABLE_ANIME : DatabaseTest.TABLE_MANGA;
                if (!recordExists(DatabaseTest.COLUMN_ID, recordTable, String.valueOf(relation.getId()))) {
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseTest.COLUMN_ID, relation.getId());
                    cv.put("title", relation.getTitle());
                    relatedRecordExists = db.insert(recordTable, null, cv) > 0;
                } else {
                    relatedRecordExists = true;
                }

                if (relatedRecordExists) {
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseTest.COLUMN_ID, id);
                    cv.put("relationId", relation.getId());
                    cv.put("relationType", relationType);
                    db.replace(table, null, cv);
                } else {
                    log("updateRelation", "error saving relation: record does not exist", true);
                }
            }
        } catch (Exception e) {
            log("updateRelation", e.getMessage(), true);
            e.printStackTrace();
        }
    }

    /**
     * Update Links for records.
     *
     * @param id       The anime/manga ID
     * @param list     Arraylist of strings
     * @param refTable The table where the references will be placed
     * @param table    The table where the records will be placed
     * @param column   The references column name
     *                 <p/>
     *                 Query.newQuery(db).updateLink(DatabaseTest.TABLE_GENRES, DatabaseTest.TABLE_ANIME_GENRES, anime.getId(), anime.getGenres(), "genre_id");
     */
    public void updateLink(String table, String refTable, int id, ArrayList<String> list, String column) {
        if (id <= 0)
            log("updateLink", "error saving link: id <= 0", true);
        if (list == null || list.size() == 0)
            return;

        String columnID = refTable.contains("anime") ? "anime_id" : "manga_id";
        // delete old links
        db.delete(refTable, columnID + " = ?", new String[]{String.valueOf(id)});

        try {
            for (String item : list) {
                int linkID = getRecordId(table, item);

                if (linkID != -1) {
                    // get the refID
                    ContentValues gcv = new ContentValues();
                    gcv.put(columnID, id);
                    gcv.put(column, linkID);
                    db.insert(refTable, null, gcv);
                }
            }
        } catch (Exception e) {
            log("updateLink", e.getMessage(), true);
        }
    }

    /**
     * Update titles for records.
     *
     * @param id    The anime/manga ID
     * @param anime True if the record is an anime type
     * @param jp    Arraylist of strings
     * @param en    Arraylist of strings
     * @param sy    Arraylist of strings
     */
    public void updateTitles(int id, boolean anime, ArrayList<String> jp, ArrayList<String> en, ArrayList<String> sy, ArrayList<String> ro) {
        String table = anime ? DatabaseTest.TABLE_ANIME_OTHER_TITLES : DatabaseTest.TABLE_MANGA_OTHER_TITLES;
        // delete old links
        db.delete(table, DatabaseTest.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

        updateTitles(id, table, DatabaseTest.TITLE_TYPE_JAPANESE, jp);
        updateTitles(id, table, DatabaseTest.TITLE_TYPE_ENGLISH, en);
        updateTitles(id, table, DatabaseTest.TITLE_TYPE_SYNONYM, sy);
        updateTitles(id, table, DatabaseTest.TITLE_TYPE_ROMAJI, ro);
    }

    /**
     * Update Links for records.
     *
     * @param id        The anime/manga ID
     * @param table     The table name where the record should be put
     * @param titleType The type of title
     * @param list      Arraylist of strings
     */
    private void updateTitles(int id, String table, int titleType, ArrayList<String> list) {
        if (id <= 0)
            log("updateTitles", "error saving relation: id <= 0", true);
        if (list == null || list.size() == 0)
            return;

        try {
            for (String item : list) {
                ContentValues gcv = new ContentValues();
                gcv.put(DatabaseTest.COLUMN_ID, id);
                gcv.put("titleType", titleType);
                gcv.put("title", item);
                db.insert(table, null, gcv);
            }
        } catch (Exception e) {
            log("updateTitles", e.getMessage(), true);
            e.printStackTrace();
        }
    }

    /**
     * Get titles from the database.
     *
     * @param id        The anime or manga ID
     * @param anime     True if the record is an anime
     * @param titleType The title type
     *
     * @return ArrayList with titles
     */
    public ArrayList<String> getTitles(int id, boolean anime, int titleType) {
        ArrayList<String> result = new ArrayList<>();
        Cursor cursor = selectFrom("*", anime ? DatabaseTest.TABLE_ANIME_OTHER_TITLES : DatabaseTest.TABLE_MANGA_OTHER_TITLES)
                .where(DatabaseTest.COLUMN_ID, String.valueOf(id)).andEquals("titleType", String.valueOf(titleType))
                .run();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                result.add(cursor.getString(2));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return result;
    }

    /**
     * Get a record by the ID.
     *
     * @param table The table where the record should be in
     * @param item  The title of the record
     * @return int Number of record
     */
    private int getRecordId(String table, String item) {
        Integer result = null;
        Cursor cursor = Query.newQuery(db).selectFrom("*", table).where("title", item).run();
        if (cursor.moveToFirst())
            result = cursor.getInt(0);
        cursor.close();

        if (result == null) {
            ContentValues cv = new ContentValues();
            cv.put("title", item);
            Long addResult = db.insert(table, null, cv);
            if (addResult > -1)
                result = addResult.intValue();
        }

        return result == null ? -1 : result;
    }

    /**
     * Log events.
     *
     * @param method  The method name to find easy crashes
     * @param message The thrown message
     * @param error   True if it is an error else false
     */
    @SuppressWarnings("deprecation")
    private void log(String method, String message, boolean error) {
        Crashlytics.log(error ? Log.ERROR : Log.INFO, "MALX", "Query." + method + "(" + toString() + "): " + message);
    }

    /**
     * Check if a records already exists.
     *
     * @param column      The column where we can find the record
     * @param columnValue The ID
     * @param table       The table where we can find the record
     * @return boolean True if it exists
     */
    private boolean recordExists(String column, String table, String columnValue) {
        Cursor cursor = selectFrom(column, table).where(column, columnValue).run();
        boolean result = cursor.moveToFirst();
        cursor.close();
        return result;
    }

    /**
     * Get relations.
     *
     * @param Id            The record ID
     * @param relationTable The table that contains relations
     * @param relationType  The type of the relation (String with number)
     * @param anime         True if the RecordStub are anime items
     * @return Arraylist of RecordStub
     */
    public ArrayList<RecordStub> getRelation(Integer Id, String relationTable, String relationType, boolean anime) {
        ArrayList<RecordStub> result = new ArrayList<>();

        try {
            String name = "mr.title";
            String id = "mr." + DatabaseTest.COLUMN_ID;

            Cursor cursor = selectFrom(id + ", " + name, (anime ? DatabaseTest.TABLE_ANIME : DatabaseTest.TABLE_MANGA) + " mr")
                    .innerJoinOn(relationTable + " rr", id, "rr.relationId")
                    .where("rr." + DatabaseTest.COLUMN_ID, String.valueOf(Id)).andEquals("rr.relationType", relationType).run();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    RecordStub recordStub = new RecordStub();
                    recordStub.setId(cursor.getInt(0), anime);
                    recordStub.setTitle(cursor.getString(1));
                    result.add(recordStub);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            log("getRelation", e.getMessage(), true);
        }
        return result;
    }

    /**
     * Get ArrayLists that are separated in the DB.
     *
     * @param id       The record ID
     * @param relTable The main table
     * @param table    The table which is separated in anime or manga records
     * @param column   The column name of the id's
     * @param anime    If the record is an anime.
     *
     * @return The requested arraylist
     */
    public ArrayList<String> getArrayList(int id, String relTable, String table, String column, boolean anime) {
        ArrayList<String> result = new ArrayList<>();

        try {
            Cursor cursor = selectFrom("*", table)
                    .innerJoinOn(relTable, table + "." + column, relTable + "." + DatabaseTest.COLUMN_ID)
                    .where(anime ? "anime_id" : "manga_id", String.valueOf(id))
                    .run();

            if (cursor != null && cursor.moveToFirst()) {
                do
                    result.add(cursor.getString(3));
                while (cursor.moveToNext());
                    cursor.close();
            }
        } catch (Exception e) {
            log("getArrayList", e.getMessage(), true);
        }
        return result;
    }
}