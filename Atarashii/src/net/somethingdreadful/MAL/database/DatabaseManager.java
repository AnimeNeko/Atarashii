package net.somethingdreadful.MAL.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALApi;

import java.util.ArrayList;

public class DatabaseManager {
    SQLiteDatabase db;

    public DatabaseManager(Context context) {
        this.db = DatabaseTest.getInstance(context).getWritableDatabase();
    }

    public void saveAnime(Anime anime) {
        ContentValues cv = listDetails(anime);
        cv.put("duration", anime.getDuration());
        cv.put("episodes", anime.getEpisodes());
        cv.put("youtubeId", anime.getYoutubeId());
        //cv.put("listStats", anime.getListStats()); TODO: investigate what this really is
        if (anime.getAiring() != null) {
            cv.put("airingTime", anime.getAiring().getTime());
            cv.put("nextEpisode", anime.getAiring().getNextEpisode());
        }
        if (anime.getWatchedStatus() != null) { // AniList does not provide this in the details
            cv.put("watchedStatus", anime.getWatchedStatus());
            cv.put("watchedEpisodes", anime.getWatchedEpisodes());
        }
        if (AccountService.isMAL()) {
            cv.put("watchingStart", anime.getWatchingStart());
            cv.put("watchingEnd", anime.getWatchingEnd());
            cv.put("fansubGroup", anime.getFansubGroup());
            cv.put("storage", anime.getStorage());
            cv.put("storageValue", anime.getStorageValue());
            cv.put("epsDownloaded", anime.getEpsDownloaded());
            cv.put("rewatching", anime.getRewatching());
            cv.put("rewatchCount", anime.getRewatchCount());
            cv.put("rewatchValue", anime.getRewatchValue());
        }

        try {
            db.beginTransaction();
            Query.newQuery(db).updateRecord(DatabaseTest.TABLE_ANIME, cv, anime.getId());
            Query.newQuery(db).updateRelation(DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_ALTERNATIVE, anime.getId(), anime.getAlternativeVersions());
            Query.newQuery(db).updateRelation(DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_CHARACTER, anime.getId(), anime.getCharacterAnime());
            Query.newQuery(db).updateRelation(DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_SIDE_STORY, anime.getId(), anime.getSideStories());
            Query.newQuery(db).updateRelation(DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_SPINOFF, anime.getId(), anime.getSpinOffs());
            Query.newQuery(db).updateRelation(DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_SUMMARY, anime.getId(), anime.getSummaries());
            Query.newQuery(db).updateRelation(DatabaseTest.TABLE_ANIME_MANGA_RELATIONS, DatabaseTest.RELATION_TYPE_ADAPTATION, anime.getId(), anime.getMangaAdaptations());
            Query.newQuery(db).updateRelation(DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_PREQUEL, anime.getId(), anime.getPrequels());
            Query.newQuery(db).updateRelation(DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_SEQUEL, anime.getId(), anime.getSequels());
            Query.newQuery(db).updateRelation(DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_PARENT_STORY, anime.getId(), anime.getParentStoryArray());
            Query.newQuery(db).updateRelation(DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_OTHER, anime.getId(), anime.getOther());
            Query.newQuery(db).updateLink(DatabaseTest.TABLE_GENRES, DatabaseTest.TABLE_ANIME_GENRES, anime.getId(), anime.getGenres(), "genre_id");
            Query.newQuery(db).updateLink(DatabaseTest.TABLE_GENRES, DatabaseTest.TABLE_ANIME_TAGS, anime.getId(), anime.getTags(), "tag_id");
            Query.newQuery(db).updateLink(DatabaseTest.TABLE_PRODUCER, DatabaseTest.TABLE_ANIME_PRODUCER, anime.getId(), anime.getProducers(), "producer_id");
            Query.newQuery(db).updateLink(DatabaseTest.TABLE_TAGS, DatabaseTest.TABLE_ANIME_PERSONALTAGS, anime.getId(), anime.getPersonalTags(), "tag_id");
            Query.newQuery(db).updateTitles(anime.getId(), true, anime.getTitleJapanese(), anime.getTitleEnglish(), anime.getTitleSynonyms(), anime.getTitleRomaji());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.saveAnime(): " + e.getMessage());
            Crashlytics.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void saveAnimeList(ArrayList<Anime> result) {
        for (Anime anime : result) {
            saveAnimeList(anime);
        }
    }

    /**
     * Save MAL AnimeList records
     *
     * @param anime The Anime model
     */
    public void saveAnimeList(Anime anime) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseTest.COLUMN_ID, anime.getId());
        cv.put("title", anime.getTitle());
        cv.put("type", anime.getType());
        cv.put("status", anime.getStatus());
        cv.put("episodes", anime.getEpisodes());
        cv.put("imageUrl", anime.getImageUrl());
        cv.put("watchedEpisodes", anime.getWatchedEpisodes());
        cv.put("score", anime.getScore());
        cv.put("watchedStatus", anime.getWatchedStatus());

        try {
            db.beginTransaction();
            Query.newQuery(db).updateRecord(DatabaseTest.TABLE_ANIME, cv, anime.getId());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.saveAnimeList(): " + e.getMessage());
            Crashlytics.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void saveManga(Manga manga) {
        ContentValues cv = listDetails(manga);
        cv.put("chapters", manga.getChapters());
        cv.put("volumes", manga.getVolumes());
        if (manga.getReadStatus() != null) { // AniList does not provide this in the details
            cv.put("readStatus", manga.getReadStatus());
            cv.put("chaptersRead", manga.getChaptersRead());
            cv.put("volumesRead", manga.getVolumesRead());
        }
        if (AccountService.isMAL()) {
            cv.put("readingStart", manga.getReadingStart());
            cv.put("readingEnd", manga.getReadingEnd());
            cv.put("chapDownloaded", manga.getChapDownloaded());
            cv.put("rereading", manga.getRereading());
            cv.put("rereadCount", manga.getRereadCount());
            cv.put("rereadValue", manga.getRereadValue());
        }

        try {
            db.beginTransaction();
            Query.newQuery(db).updateRecord(DatabaseTest.TABLE_MANGA, cv, manga.getId());
            Query.newQuery(db).updateRelation(DatabaseTest.TABLE_MANGA_MANGA_RELATIONS, DatabaseTest.RELATION_TYPE_RELATED, manga.getId(), manga.getRelatedManga());
            Query.newQuery(db).updateRelation(DatabaseTest.TABLE_MANGA_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_ADAPTATION, manga.getId(), manga.getAnimeAdaptations());
            Query.newQuery(db).updateRelation(DatabaseTest.TABLE_MANGA_MANGA_RELATIONS, DatabaseTest.RELATION_TYPE_ALTERNATIVE, manga.getId(), manga.getAlternativeVersions());
            Query.newQuery(db).updateLink(DatabaseTest.TABLE_GENRES, DatabaseTest.TABLE_MANGA_GENRES, manga.getId(), manga.getGenres(), "genre_id");
            Query.newQuery(db).updateLink(DatabaseTest.TABLE_GENRES, DatabaseTest.TABLE_MANGA_TAGS, manga.getId(), manga.getTags(), "tag_id");
            Query.newQuery(db).updateLink(DatabaseTest.TABLE_TAGS, DatabaseTest.TABLE_MANGA_PERSONALTAGS, manga.getId(), manga.getPersonalTags(), "tag_id");
            Query.newQuery(db).updateTitles(manga.getId(), false, manga.getTitleJapanese(), manga.getTitleEnglish(), manga.getTitleSynonyms(), manga.getTitleRomaji());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.saveManga(): " + e.getMessage());
            Crashlytics.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void saveMangaList(ArrayList<Manga> result) {
        for (Manga manga : result) {
            saveMangaList(manga);
        }
    }

    /**
     * Save MAL MangaList records
     *
     * @param manga The Anime model
     */
    public void saveMangaList(Manga manga) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseTest.COLUMN_ID, manga.getId());
        cv.put("title", manga.getTitle());
        cv.put("type", manga.getType());
        cv.put("status", manga.getStatus());
        cv.put("chapters", manga.getChapters());
        cv.put("volumes", manga.getVolumes());
        cv.put("imageUrl", manga.getImageUrl());
        cv.put("chaptersRead", manga.getChaptersRead());
        cv.put("volumesRead", manga.getVolumesRead());
        cv.put("score", manga.getScore());
        cv.put("readStatus", manga.getReadStatus());

        try {
            db.beginTransaction();
            Query.newQuery(db).updateRecord(DatabaseTest.TABLE_MANGA, cv, manga.getId());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.saveMangaList(): " + e.getMessage());
            Crashlytics.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    private ContentValues listDetails(GenericRecord record) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseTest.COLUMN_ID, record.getId());
        cv.put("title", record.getTitle());
        cv.put("type", record.getType());
        cv.put("imageUrl", record.getImageUrl());
        cv.put("synopsis", record.getSynopsisString());
        cv.put("status", record.getStatus());
        cv.put("startDate", record.getStartDate());
        cv.put("endDate", record.getEndDate());
        cv.put("score", record.getScore());
        cv.put("priority", record.getPriority());
        cv.put("classification", record.getClassification());
        cv.put("averageScore", record.getAverageScore());
        cv.put("averageScoreCount", record.getAverageScoreCount());
        cv.put("popularity", record.getPopularity());
        cv.put("rank", record.getRank());
        cv.put("notes", record.getNotes());
        cv.put("favoritedCount", record.getFavoritedCount());
        cv.put("dirty", record.getDirty() != null ? new Gson().toJson(record.getDirty()) : null);
        cv.put("createFlag", record.getCreateFlag());
        cv.put("deleteFlag", record.getDeleteFlag());
        return cv;
    }

    public Anime getAnime(int id) {
        Cursor cursor = Query.newQuery(db).selectFrom("*", DatabaseTest.TABLE_ANIME).where(DatabaseTest.COLUMN_ID, String.valueOf(id)).run();

        Anime result = null;
        if (cursor.moveToFirst()) {
            result = Anime.fromCursor(cursor);
            result.setTitleEnglish(Query.newQuery(db).getTitles(result.getId(), true, DatabaseTest.TITLE_TYPE_ENGLISH));
            result.setTitleSynonyms(Query.newQuery(db).getTitles(result.getId(), true, DatabaseTest.TITLE_TYPE_SYNONYM));
            result.setTitleJapanese(Query.newQuery(db).getTitles(result.getId(), true, DatabaseTest.TITLE_TYPE_JAPANESE));
            result.setTitleRomaji(Query.newQuery(db).getTitles(result.getId(), true, DatabaseTest.TITLE_TYPE_ROMAJI));
            result.setAlternativeVersions(Query.newQuery(db).getRelation(result.getId(), DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_ALTERNATIVE, true));
            result.setCharacterAnime(Query.newQuery(db).getRelation(result.getId(), DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_CHARACTER, true));
            result.setSideStories(Query.newQuery(db).getRelation(result.getId(), DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_SIDE_STORY, true));
            result.setSpinOffs(Query.newQuery(db).getRelation(result.getId(), DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_SPINOFF, true));
            result.setSummaries(Query.newQuery(db).getRelation(result.getId(), DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_SUMMARY, true));
            result.setMangaAdaptations(Query.newQuery(db).getRelation(result.getId(), DatabaseTest.TABLE_ANIME_MANGA_RELATIONS, DatabaseTest.RELATION_TYPE_ADAPTATION, false));
            result.setPrequels(Query.newQuery(db).getRelation(result.getId(), DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_PREQUEL, true));
            result.setSequels(Query.newQuery(db).getRelation(result.getId(), DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_SEQUEL, true));
            result.setParentStoryArray(Query.newQuery(db).getRelation(result.getId(), DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_PARENT_STORY, true));
            result.setOther(Query.newQuery(db).getRelation(result.getId(), DatabaseTest.TABLE_ANIME_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_OTHER, true));
            result.setGenres(Query.newQuery(db).getArrayList(result.getId(), DatabaseTest.TABLE_GENRES, DatabaseTest.TABLE_ANIME_GENRES, "genre_id", true));
            result.setTags(Query.newQuery(db).getArrayList(result.getId(), DatabaseTest.TABLE_TAGS, DatabaseTest.TABLE_ANIME_TAGS, "tag_id", true));
            result.setProducers(Query.newQuery(db).getArrayList(result.getId(), DatabaseTest.TABLE_PRODUCER, DatabaseTest.TABLE_ANIME_PRODUCER, "producer_id", true));
        }
        cursor.close();
        GenericRecord.setFromCursor(false);
        return result;
    }

    public Manga getManga(int id) {
        Cursor cursor = Query.newQuery(db).selectFrom("*", DatabaseTest.TABLE_MANGA).where(DatabaseTest.COLUMN_ID, String.valueOf(id)).run();

        Manga result = null;
        if (cursor.moveToFirst()) {
            result = Manga.fromCursor(cursor);
            result.setTitleEnglish(Query.newQuery(db).getTitles(result.getId(), false, DatabaseTest.TITLE_TYPE_ENGLISH));
            result.setTitleSynonyms(Query.newQuery(db).getTitles(result.getId(), false, DatabaseTest.TITLE_TYPE_SYNONYM));
            result.setTitleJapanese(Query.newQuery(db).getTitles(result.getId(), false, DatabaseTest.TITLE_TYPE_JAPANESE));
            result.setTitleRomaji(Query.newQuery(db).getTitles(result.getId(), false, DatabaseTest.TITLE_TYPE_ROMAJI));
            result.setGenres(Query.newQuery(db).getArrayList(result.getId(), DatabaseTest.TABLE_GENRES, DatabaseTest.TABLE_MANGA_GENRES, "genre_id", false));
            result.setTags(Query.newQuery(db).getArrayList(result.getId(), DatabaseTest.TABLE_TAGS, DatabaseTest.TABLE_MANGA_TAGS, "tag_id", false));
            result.setPersonalTags(Query.newQuery(db).getArrayList(result.getId(), DatabaseTest.TABLE_TAGS, DatabaseTest.TABLE_MANGA_PERSONALTAGS, "tag_id", false));
            result.setAlternativeVersions(Query.newQuery(db).getRelation(result.getId(), DatabaseTest.TABLE_MANGA_MANGA_RELATIONS, DatabaseTest.RELATION_TYPE_ALTERNATIVE, false));
            result.setRelatedManga(Query.newQuery(db).getRelation(result.getId(), DatabaseTest.TABLE_MANGA_MANGA_RELATIONS, DatabaseTest.RELATION_TYPE_RELATED, false));
            result.setAnimeAdaptations(Query.newQuery(db).getRelation(result.getId(), DatabaseTest.TABLE_MANGA_ANIME_RELATIONS, DatabaseTest.RELATION_TYPE_ADAPTATION, true));
        }
        cursor.close();
        GenericRecord.setFromCursor(false);
        return result;
    }

    public ArrayList<Anime> getDirtyAnimeList() {
        Cursor cursor = Query.newQuery(db).selectFrom("*", DatabaseTest.TABLE_ANIME).isNotNull("dirty").run();
        return getAnimeList(cursor);
    }

    public ArrayList<Manga> getDirtyMangaList() {
        Cursor cursor = Query.newQuery(db).selectFrom("*", DatabaseTest.TABLE_MANGA).isNotNull("dirty").run();
        return getMangaList(cursor);
    }

    public ArrayList<Anime> getAnimeList(String ListType) {
        Cursor cursor;
        Query query = Query.newQuery(db).selectFrom("*", DatabaseTest.TABLE_ANIME);
        switch (ListType) {
            case "": // All
                cursor = query.OrderBy(1, "title").run();
                break;
            case "rewatching": // rewatching/rereading
                cursor = query.whereEqGr("rewatchCount", "1").andEquals("watchedStatus", "watching").OrderBy(1, "title").run();
                break;
            default: // normal lists
                cursor = query.where("watchedStatus", ListType).OrderBy(1, "title").run();
                break;
        }
        return getAnimeList(cursor);
    }

    public ArrayList<Manga> getMangaList(String ListType) {
        Cursor cursor = Query.newQuery(db).selectFrom("*", DatabaseTest.TABLE_MANGA).where("readStatus", ListType).run();
        return getMangaList(cursor);
    }

    private ArrayList<Anime> getAnimeList(Cursor cursor) {
        ArrayList<Anime> result = new ArrayList<>();
        GenericRecord.setFromCursor(true);
        if (cursor.moveToFirst()) {
            do
                result.add(Anime.fromCursor(cursor));
            while (cursor.moveToNext());
        }
        Crashlytics.log(Log.INFO, "MALX", "DatabaseManager.getAnimeList(): got " + String.valueOf(cursor.getCount()));
        cursor.close();
        GenericRecord.setFromCursor(false);
        return result;
    }

    private ArrayList<Manga> getMangaList(Cursor cursor) {
        ArrayList<Manga> result = new ArrayList<>();
        GenericRecord.setFromCursor(true);
        if (cursor.moveToFirst()) {
            do
                result.add(Manga.fromCursor(cursor));
            while (cursor.moveToNext());
        }
        cursor.close();
        Crashlytics.log(Log.INFO, "MALX", "DatabaseManager.getMangaList(): got " + String.valueOf(cursor.getCount()));
        GenericRecord.setFromCursor(false);
        return result;
    }

    public void cleanupAnimeTable() {
        db.rawQuery("DELETE FROM " + DatabaseTest.TABLE_ANIME + " WHERE " +
                DatabaseTest.COLUMN_ID + " NOT IN (SELECT DISTINCT relationId FROM " + DatabaseTest.TABLE_ANIME_ANIME_RELATIONS + ") AND " +
                DatabaseTest.COLUMN_ID + " NOT IN (SELECT DISTINCT relationId FROM " + DatabaseTest.TABLE_MANGA_ANIME_RELATIONS + ")", null);
    }

    public void cleanupMangaTable() {
        db.rawQuery("DELETE FROM " + DatabaseTest.TABLE_MANGA + " WHERE " +
                DatabaseTest.COLUMN_ID + " NOT IN (SELECT DISTINCT relationId FROM " + DatabaseTest.TABLE_MANGA_MANGA_RELATIONS + ") AND " +
                DatabaseTest.COLUMN_ID + " NOT IN (SELECT DISTINCT relationId FROM " + DatabaseTest.TABLE_MANGA_ANIME_RELATIONS + ")", null);
    }

    public ArrayList<Profile> getFriendList() {
        ArrayList<Profile> result = new ArrayList<>();
        Cursor cursor = Query.newQuery(db).selectFrom("*", DatabaseTest.TABLE_FRIENDLIST).run();

        if (cursor.moveToFirst()) {
            do
                result.add(Profile.friendFromCursor(cursor));
            while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public void saveFriendList(ArrayList<Profile> list) {
        try {
            db.beginTransaction();
            for (Profile profile : list) {
                ContentValues cv = new ContentValues();
                cv.put("username", profile.getUsername());
                cv.put("imageUrl", profile.getImageUrl());
                cv.put("lastOnline", AccountService.isMAL() ? profile.getDetails().getLastOnline() : "");
                Query.newQuery(db).updateRecord(DatabaseTest.TABLE_FRIENDLIST, cv, profile.getUsername());
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.saveFriendList(): " + e.getMessage());
            Crashlytics.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    public Profile getProfile() {
        Cursor cursor = Query.newQuery(db).selectFrom("*", DatabaseTest.TABLE_PROFILE).run();
        Profile profile = null;
        if (cursor.moveToFirst())
            profile = Profile.fromCursor(cursor);
        cursor.close();
        return profile;
    }

    public void saveProfile(Profile profile) {
        ContentValues cv = new ContentValues();

        cv.put("username", profile.getUsername());
        cv.put("imageUrl", profile.getImageUrl());
        cv.put("imageUrlBanner", profile.getImageUrlBanner());
        cv.put("notifications", profile.getNotifications());

        if (AccountService.isMAL()) {
            cv.put("lastOnline", profile.getDetails().getLastOnline());
            cv.put("status", profile.getDetails().getStatus());
            cv.put("gender", profile.getDetails().getGender());
            cv.put("birthday", profile.getDetails().getBirthday());
            cv.put("location", profile.getDetails().getLocation());
            cv.put("website", profile.getDetails().getWebsite());
            cv.put("joinDate", profile.getDetails().getJoinDate());
            cv.put("accessRank", profile.getDetails().getAccessRank());

            cv.put("animeListViews", profile.getDetails().getAnimeListViews());
            cv.put("mangaListViews", profile.getDetails().getMangaListViews());
            cv.put("forumPosts", profile.getDetails().getForumPosts());
            cv.put("comments", profile.getDetails().getComments());

            cv.put("AnimetimeDays", profile.getAnimeStats().getTimeDays());
            cv.put("Animewatching", profile.getAnimeStats().getWatching());
            cv.put("Animecompleted", profile.getAnimeStats().getCompleted());
            cv.put("AnimeonHold", profile.getAnimeStats().getOnHold());
            cv.put("Animedropped", profile.getAnimeStats().getDropped());
            cv.put("AnimeplanToWatch", profile.getAnimeStats().getPlanToWatch());
            cv.put("AnimetotalEntries", profile.getAnimeStats().getTotalEntries());

            cv.put("MangatimeDays", profile.getMangaStats().getTimeDays());
            cv.put("Mangareading", profile.getMangaStats().getReading());
            cv.put("Mangacompleted", profile.getMangaStats().getCompleted());
            cv.put("MangaonHold", profile.getMangaStats().getOnHold());
            cv.put("Mangadropped", profile.getMangaStats().getDropped());
            cv.put("MangaplanToRead", profile.getMangaStats().getPlanToRead());
            cv.put("MangatotalEntries", profile.getMangaStats().getTotalEntries());
        }

        try {
            db.beginTransaction();
            Query.newQuery(db).updateRecord(DatabaseTest.TABLE_PROFILE, cv, profile.getUsername());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.saveProfile(): " + e.getMessage());
            Crashlytics.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void restoreLists(ArrayList<Anime> animeList, ArrayList<Manga> mangaList) {
        saveAnimeList(animeList);
        saveMangaList(mangaList);
    }

    public ArrayList<GenericRecord> getWidgetRecords() {
        ArrayList<GenericRecord> result = new ArrayList<>();
        result.addAll(getWidgetList(MALApi.ListType.ANIME));
        result.addAll(getWidgetList(MALApi.ListType.MANGA));
        return result;
    }

    private ArrayList getWidgetList(MALApi.ListType type) {
        ArrayList result = new ArrayList<>();
        Cursor cursor;
        if (type.equals(MALApi.ListType.ANIME))
            cursor = Query.newQuery(db).selectFrom("*", DatabaseTest.TABLE_ANIME).isNotNull("widget").run();
        else
            cursor = Query.newQuery(db).selectFrom("*", DatabaseTest.TABLE_MANGA).isNotNull("widget").run();

        if (cursor.moveToFirst()) {
            do
                if (type.equals(MALApi.ListType.ANIME)) {
                    Anime anime = Anime.fromCursor(cursor);
                    anime.isAnime = true;
                    result.add(anime);
                } else {
                    Manga manga = Manga.fromCursor(cursor);
                    manga.isAnime = false;
                    result.add(manga);
                }
            while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public boolean addWidgetRecord(int id, MALApi.ListType type) {
        if (checkWidgetID(id, type))
            return false;

        int number = getWidgetRecords().size() + 1;
        ContentValues cv = new ContentValues();
        cv.put("widget", number);

        try {
            db.beginTransaction();
            if (type.equals(MALApi.ListType.ANIME))
                db.update(DatabaseTest.TABLE_ANIME, cv, DatabaseTest.COLUMN_ID + " = ?", new String[]{Integer.toString(id)});
            else
                db.update(DatabaseTest.TABLE_MANGA, cv, DatabaseTest.COLUMN_ID + " = ?", new String[]{Integer.toString(id)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.addWidgetRecord(): " + e.getMessage());
            Crashlytics.logException(e);
        } finally {
            db.endTransaction();
        }
        return true;
    }

    public boolean updateWidgetRecord(int oldId, MALApi.ListType oldType, int id, MALApi.ListType type) {
        if (checkWidgetID(id, type))
            return false;

        // Remove old record
        ContentValues cv = new ContentValues();
        cv.putNull("widget");
        boolean anime = oldType.equals(MALApi.ListType.ANIME);

        try {
            db.beginTransaction();
            db.update(DatabaseTest.TABLE_ANIME, cv, DatabaseTest.COLUMN_ID + " = ?", new String[]{Integer.toString(oldId)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.updateWidgetRecord(): " + e.getMessage());
            Crashlytics.logException(e);
        } finally {
            db.endTransaction();
        }
        addWidgetRecord(id, type);
        return true;
    }

    /**
     * Check if records is already a widget
     *
     * @param id   The anime/manga id
     * @param type The List type
     * @return Boolean True if exists
     */
    private boolean checkWidgetID(int id, MALApi.ListType type) {
        if (type.equals(MALApi.ListType.ANIME))
            return Query.newQuery(db).selectFrom("*", DatabaseTest.TABLE_ANIME).where(DatabaseTest.COLUMN_ID, String.valueOf(id)).andIsNotNull("widget").run().getCount() > 0;
        else
            return Query.newQuery(db).selectFrom("*", DatabaseTest.TABLE_MANGA).where(DatabaseTest.COLUMN_ID, String.valueOf(id)).andIsNotNull("widget").run().getCount() > 0;
    }

    public void removeWidgetRecord() {
        try {
            db.beginTransaction();
            int number = getWidgetRecords().size() - 1;
            // Remove old record
            ContentValues cv = new ContentValues();
            cv.putNull("widget");
            db.update(DatabaseTest.TABLE_ANIME, cv, "widget = ?", new String[]{Integer.toString(number)});
            db.update(DatabaseTest.TABLE_MANGA, cv, "widget = ?", new String[]{Integer.toString(number)});

            // Replace id of the new record
            ContentValues cvn = new ContentValues();
            cvn.put("widget", number);
            db.update(DatabaseTest.TABLE_ANIME, cvn, "widget = ?", new String[]{Integer.toString(number + 1)});
            db.update(DatabaseTest.TABLE_MANGA, cvn, "widget = ?", new String[]{Integer.toString(number + 1)});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.removeWidgetRecord(): " + e.getMessage());
            Crashlytics.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    public boolean deleteAnime(int id) {
        boolean result = false;
        try {
            db.beginTransaction();
            result = db.delete(DatabaseTest.TABLE_ANIME, DatabaseTest.COLUMN_ID + " = ?", new String[]{String.valueOf(id)}) == 1;
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.deleteAnime(): " + e.getMessage());
            Crashlytics.logException(e);
        } finally {
            db.endTransaction();
        }
        if (result)
            cleanupAnimeTable();
        return result;
    }

    public boolean deleteManga(int id) {
        boolean result = false;
        try {
            db.beginTransaction();
            result = db.delete(DatabaseTest.TABLE_MANGA, DatabaseTest.COLUMN_ID + " = ?", new String[]{String.valueOf(id)}) == 1;
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.deleteManga(): " + e.getMessage());
            Crashlytics.logException(e);
        } finally {
            db.endTransaction();
        }
        if (result)
            cleanupMangaTable();
        return result;
    }
}
