package net.somethingdreadful.MAL.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule;
import net.somethingdreadful.MAL.api.BaseModels.Profile;

import java.util.ArrayList;

public class DatabaseManager {
    private final SQLiteDatabase db;

    public DatabaseManager(Context context) {
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    public void saveAnime(Anime anime) {
        ContentValues cv = listDetails(anime);
        cv.put("duration", anime.getDuration());
        cv.put("episodes", anime.getEpisodes());
        cv.put("youtubeId", anime.getYoutubeId());
        if (anime.getAiring() != null) {
            cv.put("airingTime", anime.getAiring().getTime());
            cv.put("nextEpisode", anime.getAiring().getNextEpisode());
        }

        // The app is offline
        if (anime.getWatchedStatus() != null) {
            cv.put("watchedStatus", anime.getWatchedStatus());
            cv.put("watchedEpisodes", anime.getWatchedEpisodes());
        }

        // AniList does not provide this in the details
        if (AccountService.isMAL()) {
            cv.put("watchedStatus", anime.getWatchedStatus());
            cv.put("watchedEpisodes", anime.getWatchedEpisodes());
            cv.put("watchingStart", anime.getWatchingStart());
            cv.put("watchingEnd", anime.getWatchingEnd());
            cv.put("storage", anime.getStorage());
            cv.put("storageValue", anime.getStorageValue());
            cv.put("rewatching", anime.getRewatching() ? 1 : 0);
            cv.put("rewatchCount", anime.getRewatchCount());
            cv.put("rewatchValue", anime.getRewatchValue());

            cv.put("officialSite", anime.getExternalLinks().getOfficialSite());
            cv.put("animeDB", anime.getExternalLinks().getAnimeDB());
            cv.put("wikipedia", anime.getExternalLinks().getWikipedia());
            cv.put("animeNewsNetwork", anime.getExternalLinks().getAnimeNewsNetwork());
        }

        try {
            db.beginTransaction();
            Query.newQuery(db).updateRecord(DatabaseHelper.TABLE_ANIME, cv, anime.getId());
            Query.newQuery(db).updateRelation(DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_ALTERNATIVE, anime.getId(), anime.getAlternativeVersions());
            Query.newQuery(db).updateRelation(DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_CHARACTER, anime.getId(), anime.getCharacterAnime());
            Query.newQuery(db).updateRelation(DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_SIDE_STORY, anime.getId(), anime.getSideStories());
            Query.newQuery(db).updateRelation(DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_SPINOFF, anime.getId(), anime.getSpinOffs());
            Query.newQuery(db).updateRelation(DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_SUMMARY, anime.getId(), anime.getSummaries());
            Query.newQuery(db).updateRelation(DatabaseHelper.TABLE_ANIME_MANGA_RELATIONS, DatabaseHelper.RELATION_TYPE_ADAPTATION, anime.getId(), anime.getMangaAdaptations());
            Query.newQuery(db).updateRelation(DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_PREQUEL, anime.getId(), anime.getPrequels());
            Query.newQuery(db).updateRelation(DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_SEQUEL, anime.getId(), anime.getSequels());
            Query.newQuery(db).updateRelation(DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_PARENT_STORY, anime.getId(), anime.getParentStoryArray());
            Query.newQuery(db).updateRelation(DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_OTHER, anime.getId(), anime.getOther());
            Query.newQuery(db).updateLink(DatabaseHelper.TABLE_GENRES, DatabaseHelper.TABLE_ANIME_GENRES, anime.getId(), anime.getGenres(), "genre_id");
            Query.newQuery(db).updateLink(DatabaseHelper.TABLE_GENRES, DatabaseHelper.TABLE_ANIME_TAGS, anime.getId(), anime.getTags(), "tag_id");
            Query.newQuery(db).updateLink(DatabaseHelper.TABLE_PRODUCER, DatabaseHelper.TABLE_ANIME_PRODUCER, anime.getId(), anime.getProducers(), "producer_id");
            Query.newQuery(db).updateLink(DatabaseHelper.TABLE_TAGS, DatabaseHelper.TABLE_ANIME_PERSONALTAGS, anime.getId(), anime.getPersonalTags(), "tag_id");
            Query.newQuery(db).updateTitles(anime.getId(), true, anime.getTitleJapanese(), anime.getTitleEnglish(), anime.getTitleSynonyms(), anime.getTitleRomaji());
            Query.newQuery(db).updateMusic(anime.getId(), anime.getOpeningTheme(), anime.getEndingTheme());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.saveAnime(): " + e.getMessage());
            AppLog.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void clearOldRecords(GenericRecord record, String table, String methodName) {
        Long lastSync = record.getLastSync().getTime();
        AppLog.log(Log.INFO, "Atarashii", "DatabaseManager." + methodName + "(): removing records before" + lastSync);

        try {
            db.beginTransaction();
            Query.newQuery(db).clearOldRecords(table, lastSync);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager." + methodName + "(): " + e.getMessage());
            AppLog.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void saveAnimeList(ArrayList<Anime> result) {
        for (Anime anime : result) {
            saveAnimeList(anime);
        }
        clearOldRecords(result.get(0), DatabaseHelper.TABLE_ANIME, "saveAnimeList");
    }

    /**
     * Save MAL AnimeList records
     *
     * @param anime The Anime model
     */
    private void saveAnimeList(Anime anime) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_ID, anime.getId());
        cv.put("title", anime.getTitle());
        cv.put("type", anime.getType());
        cv.put("status", anime.getStatus());
        cv.put("episodes", anime.getEpisodes());
        cv.put("imageUrl", anime.getImageUrl());
        cv.put("watchedEpisodes", anime.getWatchedEpisodes());
        cv.put("score", anime.getScore());
        cv.put("watchedStatus", anime.getWatchedStatus());
        cv.put("lastSync", anime.getLastSync().getTime());

        // AniList details only
        if (!AccountService.isMAL()) {
            cv.put("popularity", anime.getPopularity());
            cv.put("averageScore", anime.getAverageScore());
            cv.put("priority", anime.getPriority());
            cv.put("rewatching", anime.getRewatching() ? 1 : 0);
            cv.put("notes", anime.getNotes());
            cv.put("customList", anime.getCustomList());
        }

        try {
            db.beginTransaction();
            Query.newQuery(db).updateRecord(DatabaseHelper.TABLE_ANIME, cv, anime.getId());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.saveAnimeList(): " + e.getMessage());
            AppLog.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void saveManga(Manga manga) {
        ContentValues cv = listDetails(manga);
        cv.put("chapters", manga.getChapters());
        cv.put("volumes", manga.getVolumes());

        // The app is offline
        if (manga.getReadStatus() != null) {
            cv.put("readStatus", manga.getReadStatus());
            cv.put("chaptersRead", manga.getChaptersRead());
            cv.put("volumesRead", manga.getVolumesRead());
        }

        // AniList does not provide this in the details
        if (AccountService.isMAL()) {
            cv.put("readingStart", manga.getReadingStart());
            cv.put("readingEnd", manga.getReadingEnd());
            cv.put("rereading", manga.getRereading() ? 1 : 0);
            cv.put("rereadCount", manga.getRereadCount());
            cv.put("rereadValue", manga.getRereadValue());
        }

        try {
            db.beginTransaction();
            Query.newQuery(db).updateRecord(DatabaseHelper.TABLE_MANGA, cv, manga.getId());
            Query.newQuery(db).updateRelation(DatabaseHelper.TABLE_MANGA_MANGA_RELATIONS, DatabaseHelper.RELATION_TYPE_RELATED, manga.getId(), manga.getRelatedManga());
            Query.newQuery(db).updateRelation(DatabaseHelper.TABLE_MANGA_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_ADAPTATION, manga.getId(), manga.getAnimeAdaptations());
            Query.newQuery(db).updateRelation(DatabaseHelper.TABLE_MANGA_MANGA_RELATIONS, DatabaseHelper.RELATION_TYPE_ALTERNATIVE, manga.getId(), manga.getAlternativeVersions());
            Query.newQuery(db).updateLink(DatabaseHelper.TABLE_GENRES, DatabaseHelper.TABLE_MANGA_GENRES, manga.getId(), manga.getGenres(), "genre_id");
            Query.newQuery(db).updateLink(DatabaseHelper.TABLE_GENRES, DatabaseHelper.TABLE_MANGA_TAGS, manga.getId(), manga.getTags(), "tag_id");
            Query.newQuery(db).updateLink(DatabaseHelper.TABLE_TAGS, DatabaseHelper.TABLE_MANGA_PERSONALTAGS, manga.getId(), manga.getPersonalTags(), "tag_id");
            Query.newQuery(db).updateTitles(manga.getId(), false, manga.getTitleJapanese(), manga.getTitleEnglish(), manga.getTitleSynonyms(), manga.getTitleRomaji());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.saveManga(): " + e.getMessage());
            AppLog.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void saveMangaList(ArrayList<Manga> result) {
        for (Manga manga : result) {
            saveMangaList(manga);
        }
        clearOldRecords(result.get(0), DatabaseHelper.TABLE_MANGA, "saveMangaList");
    }

    /**
     * Save MAL MangaList records
     *
     * @param manga The Anime model
     */
    private void saveMangaList(Manga manga) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_ID, manga.getId());
        cv.put("title", manga.getTitle());
        cv.put("type", manga.getType());
        cv.put("status", manga.getStatus());
        cv.put("chapters", manga.getChapters());
        cv.put("volumes", manga.getVolumes());
        cv.put("imageUrl", manga.getImageUrl());
        cv.put("rereading", manga.getRereading() ? 1 : 0);
        cv.put("chaptersRead", manga.getChaptersRead());
        cv.put("volumesRead", manga.getVolumesRead());
        cv.put("score", manga.getScore());
        cv.put("readStatus", manga.getReadStatus());
        cv.put("customList", manga.getCustomList());
        cv.put("lastSync", manga.getLastSync().getTime());

        try {
            db.beginTransaction();
            Query.newQuery(db).updateRecord(DatabaseHelper.TABLE_MANGA, cv, manga.getId());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.saveMangaList(): " + e.getMessage());
            AppLog.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    private ContentValues listDetails(GenericRecord record) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_ID, record.getId());
        cv.put("title", record.getTitle());
        cv.put("type", record.getType());
        cv.put("imageUrl", record.getImageUrl());
        cv.put("bannerUrl", record.getBannerUrl());
        cv.put("synopsis", record.getSynopsisString());
        cv.put("status", record.getStatus());
        cv.put("startDate", record.getStartDate());
        cv.put("endDate", record.getEndDate());

        // MyAnimeList details only
        if (AccountService.isMAL()) {
            cv.put("score", record.getScore());
            cv.put("priority", record.getPriority());
            cv.put("averageScoreCount", record.getAverageScoreCount());
            cv.put("rank", record.getRank());
            cv.put("notes", record.getNotes());
            cv.put("favoritedCount", record.getFavoritedCount());
        } else if (record.getNotes() != null) { // Offline details
            cv.put("notes", record.getNotes());
        } else { // AniList details only
            cv.put("lsPlanned", record.getListStats().getPlanned());
            cv.put("lsReadWatch", record.getListStats().getReadWatch());
            cv.put("lsCompleted", record.getListStats().getCompleted());
            cv.put("lsOnHold", record.getListStats().getOnHold());
            cv.put("lsDropped", record.getListStats().getDropped());

            if (-1 < record.getScore()) {
                cv.put("score", record.getScore());
                cv.put("customList", record.getCustomList());
            }
        }
        cv.put("classification", record.getClassification());
        cv.put("averageScore", record.getAverageScore());
        cv.put("popularity", record.getPopularity());
        cv.put("dirty", record.getDirty() != null ? new Gson().toJson(record.getDirty()) : null);
        cv.put("createFlag", record.getCreateFlag());
        cv.put("deleteFlag", record.getDeleteFlag());
        return cv;
    }

    public Anime getAnime(int id) {
        Cursor cursor = Query.newQuery(db).selectFrom("*", DatabaseHelper.TABLE_ANIME).where(DatabaseHelper.COLUMN_ID, String.valueOf(id)).run();

        Anime result = null;
        if (cursor.moveToFirst()) {
            result = Anime.fromCursor(cursor);
            result.setTitleEnglish(Query.newQuery(db).getTitles(result.getId(), true, DatabaseHelper.TITLE_TYPE_ENGLISH));
            result.setTitleSynonyms(Query.newQuery(db).getTitles(result.getId(), true, DatabaseHelper.TITLE_TYPE_SYNONYM));
            result.setTitleJapanese(Query.newQuery(db).getTitles(result.getId(), true, DatabaseHelper.TITLE_TYPE_JAPANESE));
            result.setTitleRomaji(Query.newQuery(db).getTitles(result.getId(), true, DatabaseHelper.TITLE_TYPE_ROMAJI));
            result.setOpeningTheme(Query.newQuery(db).getMusic(result.getId(), DatabaseHelper.MUSIC_TYPE_OPENING));
            result.setEndingTheme(Query.newQuery(db).getMusic(result.getId(), DatabaseHelper.MUSIC_TYPE_ENDING));
            result.setAlternativeVersions(Query.newQuery(db).getRelation(result.getId(), DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_ALTERNATIVE, true));
            result.setCharacterAnime(Query.newQuery(db).getRelation(result.getId(), DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_CHARACTER, true));
            result.setSideStories(Query.newQuery(db).getRelation(result.getId(), DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_SIDE_STORY, true));
            result.setSpinOffs(Query.newQuery(db).getRelation(result.getId(), DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_SPINOFF, true));
            result.setSummaries(Query.newQuery(db).getRelation(result.getId(), DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_SUMMARY, true));
            result.setMangaAdaptations(Query.newQuery(db).getRelation(result.getId(), DatabaseHelper.TABLE_ANIME_MANGA_RELATIONS, DatabaseHelper.RELATION_TYPE_ADAPTATION, false));
            result.setPrequels(Query.newQuery(db).getRelation(result.getId(), DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_PREQUEL, true));
            result.setSequels(Query.newQuery(db).getRelation(result.getId(), DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_SEQUEL, true));
            result.setParentStoryArray(Query.newQuery(db).getRelation(result.getId(), DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_PARENT_STORY, true));
            result.setOther(Query.newQuery(db).getRelation(result.getId(), DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_OTHER, true));
            result.setGenres(Query.newQuery(db).getArrayList(result.getId(), DatabaseHelper.TABLE_GENRES, DatabaseHelper.TABLE_ANIME_GENRES, "genre_id", true));
            result.setTags(Query.newQuery(db).getArrayList(result.getId(), DatabaseHelper.TABLE_TAGS, DatabaseHelper.TABLE_ANIME_TAGS, "tag_id", true));
            result.setPersonalTags(Query.newQuery(db).getArrayList(result.getId(), DatabaseHelper.TABLE_TAGS, DatabaseHelper.TABLE_ANIME_PERSONALTAGS, "tag_id", true));
            result.setProducers(Query.newQuery(db).getArrayList(result.getId(), DatabaseHelper.TABLE_PRODUCER, DatabaseHelper.TABLE_ANIME_PRODUCER, "producer_id", true));
        }
        cursor.close();
        GenericRecord.setFromCursor(false);
        return result;
    }

    public Manga getManga(int id) {
        Cursor cursor = Query.newQuery(db).selectFrom("*", DatabaseHelper.TABLE_MANGA).where(DatabaseHelper.COLUMN_ID, String.valueOf(id)).run();

        Manga result = null;
        if (cursor.moveToFirst()) {
            result = Manga.fromCursor(cursor);
            result.setTitleEnglish(Query.newQuery(db).getTitles(result.getId(), false, DatabaseHelper.TITLE_TYPE_ENGLISH));
            result.setTitleSynonyms(Query.newQuery(db).getTitles(result.getId(), false, DatabaseHelper.TITLE_TYPE_SYNONYM));
            result.setTitleJapanese(Query.newQuery(db).getTitles(result.getId(), false, DatabaseHelper.TITLE_TYPE_JAPANESE));
            result.setTitleRomaji(Query.newQuery(db).getTitles(result.getId(), false, DatabaseHelper.TITLE_TYPE_ROMAJI));
            result.setGenres(Query.newQuery(db).getArrayList(result.getId(), DatabaseHelper.TABLE_GENRES, DatabaseHelper.TABLE_MANGA_GENRES, "genre_id", false));
            result.setTags(Query.newQuery(db).getArrayList(result.getId(), DatabaseHelper.TABLE_TAGS, DatabaseHelper.TABLE_MANGA_TAGS, "tag_id", false));
            result.setPersonalTags(Query.newQuery(db).getArrayList(result.getId(), DatabaseHelper.TABLE_TAGS, DatabaseHelper.TABLE_MANGA_PERSONALTAGS, "tag_id", false));
            result.setAlternativeVersions(Query.newQuery(db).getRelation(result.getId(), DatabaseHelper.TABLE_MANGA_MANGA_RELATIONS, DatabaseHelper.RELATION_TYPE_ALTERNATIVE, false));
            result.setRelatedManga(Query.newQuery(db).getRelation(result.getId(), DatabaseHelper.TABLE_MANGA_MANGA_RELATIONS, DatabaseHelper.RELATION_TYPE_RELATED, false));
            result.setAnimeAdaptations(Query.newQuery(db).getRelation(result.getId(), DatabaseHelper.TABLE_MANGA_ANIME_RELATIONS, DatabaseHelper.RELATION_TYPE_ADAPTATION, true));
        }
        cursor.close();
        GenericRecord.setFromCursor(false);
        return result;
    }

    public ArrayList<Anime> getDirtyAnimeList() {
        Cursor cursor = Query.newQuery(db).selectFrom("*", DatabaseHelper.TABLE_ANIME).isNotNull("dirty").run();
        return getAnimeList(cursor);
    }

    public ArrayList<Manga> getDirtyMangaList() {
        Cursor cursor = Query.newQuery(db).selectFrom("*", DatabaseHelper.TABLE_MANGA).isNotNull("dirty").run();
        return getMangaList(cursor);
    }

    private String regCustomList(String ListType) {
        String reg = "";
        int listNumber = Integer.parseInt(ListType.replace(GenericRecord.CUSTOMLIST, ""));
        for (int i = 1; i < 16; i++) {
            if (i == listNumber)
                reg = reg + "1";
            else
                reg = reg + "_";
        }
        return reg;
    }

    public ArrayList<Anime> getAnimeList(String ListType, int sortType, int inv) {
        Cursor cursor;
        Query query = Query.newQuery(db).selectFrom("*", DatabaseHelper.TABLE_ANIME);
        if (ListType.contains(GenericRecord.CUSTOMLIST)) {
            cursor = sort(query.like("customList", regCustomList(ListType)), sortType, inv);
        } else {
            switch (ListType) {
                case "": // All
                    cursor = sort(query.isNotNull("type"), sortType, inv);
                    break;
                case "rewatching": // rewatching/rereading
                    cursor = sort(query.whereEqGr("rewatching", "1"), sortType, inv);
                    break;
                default: // normal lists
                    cursor = sort(query.where("watchedStatus", ListType), sortType, inv);
                    break;
            }
        }
        return getAnimeList(cursor);
    }

    public ArrayList<Manga> getMangaList(String ListType, int sortType, int inv) {
        sortType = sortType == 5 ? -5 : sortType;
        Cursor cursor;
        Query query = Query.newQuery(db).selectFrom("*", DatabaseHelper.TABLE_MANGA);
        if (ListType.contains(GenericRecord.CUSTOMLIST)) {
            cursor = sort(query.like("customList", regCustomList(ListType)), sortType, inv);
        } else {
            switch (ListType) {
                case "": // All
                    cursor = sort(query.isNotNull("type"), sortType, inv);
                    break;
                case "rereading": // rewatching/rereading
                    cursor = sort(query.whereEqGr("rereading", "1"), sortType, inv);
                    break;
                default: // normal lists
                    cursor = sort(query.where("readStatus", ListType), sortType, inv);
                    break;
            }
        }
        return getMangaList(cursor);
    }

    /*
     * Do not forget to modify the IGF sortList method also!
     */
    private Cursor sort(Query query, int sortType, int inverse) {
        switch (sortType) {
            case 1:
                return query.OrderBy(inverse, "title").run();
            case 2:
                return query.OrderBy(inverse == 2 ? 1 : 2, "score").andOrderBy(inverse, "title").run();
            case 3:
                return query.OrderBy(inverse, "type").andOrderBy(inverse, "title").run();
            case 4:
                return query.OrderBy(inverse, "status").andOrderBy(inverse, "title").run();
            case 5:
                return query.OrderBy(inverse, "watchedEpisodes").andOrderBy(inverse, "title").run();
            case -5:
                return query.OrderBy(inverse, "chaptersRead").andOrderBy(inverse, "title").run();
            default:
                return query.OrderBy(inverse, "title").run();
        }
    }

    private ArrayList<Anime> getAnimeList(Cursor cursor) {
        ArrayList<Anime> result = new ArrayList<>();
        GenericRecord.setFromCursor(true);
        if (cursor.moveToFirst()) {
            do
                result.add(Anime.fromCursor(cursor));
            while (cursor.moveToNext());
        }
        AppLog.log(Log.INFO, "Atarashii", "DatabaseManager.getAnimeList(): got " + String.valueOf(cursor.getCount()));
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
        AppLog.log(Log.INFO, "Atarashii", "DatabaseManager.getMangaList(): got " + String.valueOf(cursor.getCount()));
        GenericRecord.setFromCursor(false);
        return result;
    }

    public void cleanupAnimeTable() {
        db.rawQuery("DELETE FROM " + DatabaseHelper.TABLE_ANIME + " WHERE " +
                DatabaseHelper.COLUMN_ID + " NOT IN (SELECT DISTINCT relationId FROM " + DatabaseHelper.TABLE_ANIME_ANIME_RELATIONS + ") AND " +
                DatabaseHelper.COLUMN_ID + " NOT IN (SELECT DISTINCT relationId FROM " + DatabaseHelper.TABLE_MANGA_ANIME_RELATIONS + ")", null);
    }

    public void cleanupMangaTable() {
        db.rawQuery("DELETE FROM " + DatabaseHelper.TABLE_MANGA + " WHERE " +
                DatabaseHelper.COLUMN_ID + " NOT IN (SELECT DISTINCT relationId FROM " + DatabaseHelper.TABLE_MANGA_MANGA_RELATIONS + ") AND " +
                DatabaseHelper.COLUMN_ID + " NOT IN (SELECT DISTINCT relationId FROM " + DatabaseHelper.TABLE_MANGA_ANIME_RELATIONS + ")", null);
    }

    public ArrayList<Profile> getFriendList() {
        ArrayList<Profile> result = new ArrayList<>();
        Cursor cursor = Query.newQuery(db).selectFrom("*", DatabaseHelper.TABLE_FRIENDLIST).OrderBy(1, "username").run();

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
                Query.newQuery(db).updateRecord(DatabaseHelper.TABLE_FRIENDLIST, cv, profile.getUsername());
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.saveFriendList(): " + e.getMessage());
            AppLog.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    public Profile getProfile() {
        Cursor cursor = Query.newQuery(db).selectFrom("*", DatabaseHelper.TABLE_PROFILE).run();
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
        cv.put("about", profile.getAbout());

        // AniList also supports these
        cv.put("AnimetimeDays", profile.getAnimeStats().getTimeDays());
        cv.put("Mangacompleted", profile.getMangaStats().getCompleted());

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

            cv.put("Animewatching", profile.getAnimeStats().getWatching());
            cv.put("Animecompleted", profile.getAnimeStats().getCompleted());
            cv.put("AnimeonHold", profile.getAnimeStats().getOnHold());
            cv.put("Animedropped", profile.getAnimeStats().getDropped());
            cv.put("AnimeplanToWatch", profile.getAnimeStats().getPlanToWatch());
            cv.put("AnimetotalEntries", profile.getAnimeStats().getTotalEntries());

            cv.put("MangatimeDays", profile.getMangaStats().getTimeDays());
            cv.put("Mangareading", profile.getMangaStats().getReading());
            cv.put("MangaonHold", profile.getMangaStats().getOnHold());
            cv.put("Mangadropped", profile.getMangaStats().getDropped());
            cv.put("MangaplanToRead", profile.getMangaStats().getPlanToRead());
            cv.put("MangatotalEntries", profile.getMangaStats().getTotalEntries());
        }

        try {
            db.beginTransaction();
            Query.newQuery(db).updateRecord(DatabaseHelper.TABLE_PROFILE, cv, profile.getUsername());
            db.setTransactionSuccessful();
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.saveProfile(): " + e.getMessage());
            AppLog.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    public void deleteAnime(int id) {
        boolean result = false;
        try {
            db.beginTransaction();
            result = db.delete(DatabaseHelper.TABLE_ANIME, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)}) == 1;
            db.setTransactionSuccessful();
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.deleteAnime(): " + e.getMessage());
            AppLog.logException(e);
        } finally {
            db.endTransaction();
        }
        if (result)
            cleanupAnimeTable();
    }

    public void deleteManga(int id) {
        boolean result = false;
        try {
            db.beginTransaction();
            result = db.delete(DatabaseHelper.TABLE_MANGA, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)}) == 1;
            db.setTransactionSuccessful();
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.deleteManga(): " + e.getMessage());
            AppLog.logException(e);
        } finally {
            db.endTransaction();
        }
        if (result)
            cleanupMangaTable();
    }

    public void saveSchedule(Schedule schedule) {
        Query.newQuery(db).clear(DatabaseHelper.TABLE_SCHEDULE);
        saveScheduleDay(schedule.getMonday(), 2);
        saveScheduleDay(schedule.getTuesday(), 3);
        saveScheduleDay(schedule.getWednesday(), 4);
        saveScheduleDay(schedule.getThursday(), 5);
        saveScheduleDay(schedule.getFriday(), 6);
        saveScheduleDay(schedule.getSaturday(), 7);
        saveScheduleDay(schedule.getSunday(), 1);
    }

    private void saveScheduleDay(ArrayList<Anime> list, int day) {
        try {
            db.beginTransaction();
            for (Anime anime : list) {
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COLUMN_ID, anime.getId());
                cv.put("title", anime.getTitle());
                cv.put("imageUrl", anime.getImageUrl());
                cv.put("type", anime.getType());
                cv.put("episodes", anime.getEpisodes());
                cv.put("avarageScore", anime.getAverageScore());
                cv.put("averageScoreCount", anime.getAverageScoreCount());
                if (anime.getAiring() != null && anime.getAiring().getTime() != null)
                    cv.put("broadcast", anime.getAiring().getTime());
                cv.put("day", day);
                Query.newQuery(db).updateRecord(DatabaseHelper.TABLE_SCHEDULE, cv, anime.getId());
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            AppLog.log(Log.ERROR, "Atarashii", "DatabaseManager.saveScheduleDay(): " + e.getMessage());
            AppLog.logException(e);
        } finally {
            db.endTransaction();
        }
    }

    private ArrayList<Anime> getScheduleDay(int day) {
        ArrayList<Anime> result = new ArrayList<>();
        Cursor cursor = Query.newQuery(db).selectFrom("*", DatabaseHelper.TABLE_SCHEDULE).where("day", String.valueOf(day)).run();

        if (cursor.moveToFirst()) {
            do
                result.add(Schedule.fromCursor(cursor));
            while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public Schedule getSchedule() {
        Schedule schedule = new Schedule();
        schedule.setMonday(getScheduleDay(2));
        schedule.setTuesday(getScheduleDay(3));
        schedule.setWednesday(getScheduleDay(4));
        schedule.setThursday(getScheduleDay(5));
        schedule.setFriday(getScheduleDay(6));
        schedule.setSaturday(getScheduleDay(7));
        schedule.setSunday(getScheduleDay(1));
        return schedule;
    }
}
