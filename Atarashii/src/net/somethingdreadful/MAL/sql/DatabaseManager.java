package net.somethingdreadful.MAL.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import net.somethingdreadful.MAL.MALDateTools;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Activity;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.Profile;
import net.somethingdreadful.MAL.api.response.RecordStub;
import net.somethingdreadful.MAL.api.response.Series;
import net.somethingdreadful.MAL.api.response.User;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseManager {
    MALSqlHelper malSqlHelper;
    SQLiteDatabase dbRead;

    public DatabaseManager(Context context) {
        if (malSqlHelper == null)
            malSqlHelper = MALSqlHelper.getHelper(context);
    }

    public synchronized SQLiteDatabase getDBWrite() {
        return malSqlHelper.getWritableDatabase();
    }

    public SQLiteDatabase getDBRead() {
        if (dbRead == null)
            dbRead = malSqlHelper.getReadableDatabase();
        return dbRead;
    }

    public void saveAnimeList(ArrayList<Anime> list, String username) {
        Integer userId = getUserId(username);
        if (list != null && list.size() > 0 && userId != null) {
            try {
                getDBWrite().beginTransaction();
                for (Anime anime : list)
                    saveAnime(anime, true, userId);
                getDBWrite().setTransactionSuccessful();
            } catch (Exception e) {
                Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.saveAnimeList(): " + e.getMessage());
            } finally {
                getDBWrite().endTransaction();
            }
        }
    }

    public void saveAnime(Anime anime, boolean IGF, String username) {
        Integer userId;
        if (username.equals(""))
            userId = 0;
        else
            userId = getUserId(username);
        saveAnime(anime, IGF, userId);
    }

    public void saveAnime(Anime anime, boolean IGF, int userId) {
        saveAnime(anime, IGF, userId, false);
    }

    public void saveAnime(Anime anime, boolean IGF, int userId, boolean dontCreateBaseModel) {
        ContentValues cv = new ContentValues();

        if (!AccountService.isMAL() && anime.getWatchedStatus() == null && !dontCreateBaseModel)
            anime.createBaseModel();

        cv.put(MALSqlHelper.COLUMN_ID, anime.getId());
        cv.put("recordName", anime.getTitle());
        cv.put("recordType", anime.getType());
        cv.put("imageUrl", anime.getImageUrl());
        cv.put("recordStatus", anime.getStatus());
        cv.put("episodesTotal", anime.getEpisodes());
        if (!IGF) {
            cv.put("synopsis", anime.getSynopsis());
            cv.put("memberScore", anime.getMembersScore());
            cv.put("classification", anime.getClassification());
            cv.put("membersCount", anime.getMembersCount());
            cv.put("favoritedCount", anime.getFavoritedCount());
            cv.put("popularityRank", anime.getPopularityRank());
            cv.put("rank", anime.getRank());
            cv.put("startDate", anime.getStartDate());
            cv.put("endDate", anime.getEndDate());
            cv.put("listedId", anime.getListedId());
        }

        // don't use replace it replaces synopsis with null even when we don't put it in the ContentValues
        int updateResult = getDBWrite().update(MALSqlHelper.TABLE_ANIME, cv, MALSqlHelper.COLUMN_ID + " = ?", new String[]{Integer.toString(anime.getId())});
        if (updateResult == 0) {
            Long insertResult = getDBWrite().insert(MALSqlHelper.TABLE_ANIME, null, cv);
            if (insertResult > 0) {
                anime.setId(insertResult.intValue());
            }
        }

        if (anime.getId() > 0) { // save/update relations if saving was successful
            if (!IGF) {
                if (anime.getGenres() != null) {
                    // delete old relations
                    getDBWrite().delete(MALSqlHelper.TABLE_ANIME_GENRES, "anime_id = ?", new String[]{String.valueOf(anime.getId())});
                    for (String genre : anime.getGenres()) {
                        Integer genreId = getGenreId(genre);
                        if (genreId != null) {
                            ContentValues gcv = new ContentValues();
                            gcv.put("anime_id", anime.getId());
                            gcv.put("genre_id", genreId);
                            getDBWrite().insert(MALSqlHelper.TABLE_ANIME_GENRES, null, gcv);
                        }
                    }
                }
                if (anime.getTags() != null) {
                    // delete old relations
                    getDBWrite().delete(MALSqlHelper.TABLE_ANIME_TAGS, "anime_id = ?", new String[]{String.valueOf(anime.getId())});
                    for (String tag : anime.getTags()) {
                        Integer tagId = getTagId(tag);
                        if (tagId != null) {
                            ContentValues gcv = new ContentValues();
                            gcv.put("anime_id", anime.getId());
                            gcv.put("tag_id", tagId);
                            getDBWrite().replace(MALSqlHelper.TABLE_ANIME_TAGS, null, gcv);
                        }
                    }
                }

                saveAnimeToAnimeRelation(anime.getAlternativeVersions(), anime.getId(), MALSqlHelper.RELATION_TYPE_ALTERNATIVE);
                saveAnimeToAnimeRelation(anime.getOther(), anime.getId(), MALSqlHelper.RELATION_TYPE_OTHER);
                saveAnimeToAnimeRelation(anime.getCharacterAnime(), anime.getId(), MALSqlHelper.RELATION_TYPE_CHARACTER);
                saveAnimeToAnimeRelation(anime.getPrequels(), anime.getId(), MALSqlHelper.RELATION_TYPE_PREQUEL);
                saveAnimeToAnimeRelation(anime.getSequels(), anime.getId(), MALSqlHelper.RELATION_TYPE_SEQUEL);
                saveAnimeToAnimeRelation(anime.getSideStories(), anime.getId(), MALSqlHelper.RELATION_TYPE_SIDE_STORY);
                saveAnimeToAnimeRelation(anime.getSpinOffs(), anime.getId(), MALSqlHelper.RELATION_TYPE_SPINOFF);
                saveAnimeToAnimeRelation(anime.getSummaries(), anime.getId(), MALSqlHelper.RELATION_TYPE_SUMMARY);

                if (anime.getPersonalTags() != null) {
                    // delete old relations
                    getDBWrite().delete(MALSqlHelper.TABLE_ANIME_PERSONALTAGS, "anime_id = ?", new String[]{String.valueOf(anime.getId())});
                    for (String tag : anime.getPersonalTags()) {
                        Integer tagId = getTagId(tag);
                        if (tagId != null) {
                            ContentValues gcv = new ContentValues();
                            gcv.put("anime_id", anime.getId());
                            gcv.put("tag_id", tagId);
                            getDBWrite().replace(MALSqlHelper.TABLE_ANIME_PERSONALTAGS, null, gcv);
                        }
                    }
                }

                if (anime.getMangaAdaptions() != null) {
                    // delete old relations
                    getDBWrite().delete(MALSqlHelper.TABLE_ANIME_MANGA_RELATIONS, "anime_id = ? AND relationType = ?", new String[]{String.valueOf(anime.getId()), MALSqlHelper.RELATION_TYPE_ADAPTATION});

                    for (RecordStub mangaStub : anime.getMangaAdaptions()) {
                        saveAnimeToMangaRelation(anime.getId(), mangaStub, MALSqlHelper.RELATION_TYPE_ADAPTATION);
                    }
                }

                if (anime.getParentStory() != null) {
                    // delete old relations
                    getDBWrite().delete(MALSqlHelper.TABLE_ANIME_MANGA_RELATIONS, "anime_id = ? AND relationType = ?", new String[]{String.valueOf(anime.getId()), MALSqlHelper.RELATION_TYPE_PARENT_STORY});
                    saveAnimeToAnimeRelation(anime.getId(), anime.getParentStory(), MALSqlHelper.RELATION_TYPE_PARENT_STORY);
                }

                if (anime.getOtherTitles() != null) {
                    if (anime.getOtherTitlesEnglish() != null) {
                        // delete old relations
                        getDBWrite().delete(MALSqlHelper.TABLE_ANIME_OTHER_TITLES, "anime_id = ? and titleType = ?", new String[]{String.valueOf(anime.getId()), MALSqlHelper.TITLE_TYPE_ENGLISH});
                        for (String title : anime.getOtherTitlesEnglish()) {
                            saveAnimeOtherTitle(anime.getId(), title, MALSqlHelper.TITLE_TYPE_ENGLISH);
                        }
                    }

                    if (anime.getOtherTitlesJapanese() != null) {
                        // delete old relations
                        getDBWrite().delete(MALSqlHelper.TABLE_ANIME_OTHER_TITLES, "anime_id = ? and titleType = ?", new String[]{String.valueOf(anime.getId()), MALSqlHelper.TITLE_TYPE_JAPANESE});
                        for (String title : anime.getOtherTitlesJapanese()) {
                            saveAnimeOtherTitle(anime.getId(), title, MALSqlHelper.TITLE_TYPE_JAPANESE);
                        }
                    }

                    if (anime.getOtherTitlesSynonyms() != null) {
                        // delete old relations
                        getDBWrite().delete(MALSqlHelper.TABLE_ANIME_OTHER_TITLES, "anime_id = ? and titleType = ?", new String[]{String.valueOf(anime.getId()), MALSqlHelper.TITLE_TYPE_SYNONYM});
                        for (String title : anime.getOtherTitlesSynonyms()) {
                            saveAnimeOtherTitle(anime.getId(), title, MALSqlHelper.TITLE_TYPE_SYNONYM);
                        }
                    }
                }

                if (anime.getProducers() != null) {
                    // delete old relations
                    getDBWrite().delete(MALSqlHelper.TABLE_ANIME_PRODUCER, "anime_id = ?", new String[]{String.valueOf(anime.getId())});
                    for (String producer : anime.getProducers()) {
                        Integer producerId = getProducerId(producer);
                        if (producerId != null) {
                            ContentValues gcv = new ContentValues();
                            gcv.put("anime_id", anime.getId());
                            gcv.put("producer_id", producerId);
                            getDBWrite().replace(MALSqlHelper.TABLE_ANIME_PRODUCER, null, gcv);

                        }
                    }
                }
            }

            // update animelist if user id is provided
            if (userId > 0) {
                ContentValues alcv = new ContentValues();
                alcv.put("profile_id", userId);
                alcv.put("anime_id", anime.getId());
                alcv.put("status", anime.getWatchedStatus());
                alcv.put("score", anime.getScore());
                alcv.put("watched", anime.getWatchedEpisodes());
                alcv.put("watchedStart", anime.getWatchingStart());
                alcv.put("watchedEnd", anime.getWatchingEnd());
                alcv.put("fansub", anime.getFansubGroup());
                alcv.put("priority", anime.getPriority());
                alcv.put("downloaded", anime.getEpsDownloaded());
                alcv.put("rewatch", (anime.getRewatching() ? 1 : 0));
                alcv.put("storage", anime.getStorage());
                alcv.put("storageValue", anime.getStorageValue());
                alcv.put("rewatchCount", anime.getRewatchCount());
                alcv.put("rewatchValue", anime.getRewatchValue());
                alcv.put("comments", anime.getPersonalComments());
                alcv.put("dirty", anime.getDirty() != null ? new Gson().toJson(anime.getDirty()) : null);
                if (anime.getLastUpdate() != null)
                    alcv.put("lastUpdate", anime.getLastUpdate().getTime());
                getDBWrite().replace(MALSqlHelper.TABLE_ANIMELIST, null, alcv);
            }
        }
    }

    public void saveAnimeToAnimeRelation(ArrayList<RecordStub> record, int id, String relationType) {
        if (record != null) {
            // delete old relations
            getDBWrite().delete(MALSqlHelper.TABLE_ANIME_ANIME_RELATIONS, "anime_id = ? AND relationType = ?", new String[]{String.valueOf(id), relationType});

            for (RecordStub stub : record) {
                saveAnimeToAnimeRelation(id, stub, relationType);
            }
        }
    }

    public Anime getAnime(Integer id, String username) {
        Anime result = null;
        Cursor cursor = getDBRead().rawQuery("SELECT a.*, al.score AS myScore, al.status AS myStatus, al.watched AS episodesWatched, al.dirty, al.lastUpdate, " +
                " al.watchedStart, al.WatchedEnd, al.fansub, al.priority, al.downloaded, al.storage, al.storageValue, al.rewatch, al.rewatchCount, al.rewatchValue, al.comments" +
                " FROM animelist al INNER JOIN anime a ON al.anime_id = a." + MALSqlHelper.COLUMN_ID +
                " WHERE al.profile_id = ? AND a." + MALSqlHelper.COLUMN_ID + " = ?", new String[]{getUserId(username).toString(), id.toString()});
        if (cursor.moveToFirst()) {
            result = Anime.fromCursor(cursor);
            result.setGenres(getAnimeGenres(result.getId()));
            result.setTags(getAnimeTags(result.getId()));
            result.setAlternativeVersions(getAnimeToAnimeRelations(result.getId(), MALSqlHelper.RELATION_TYPE_ALTERNATIVE));
            result.setOther(getAnimeToAnimeRelations(result.getId(), MALSqlHelper.RELATION_TYPE_OTHER));
            result.setPersonalTags(getAnimePersonalTags(result.getId()), false);
            result.setCharacterAnime(getAnimeToAnimeRelations(result.getId(), MALSqlHelper.RELATION_TYPE_CHARACTER));
            result.setPrequels(getAnimeToAnimeRelations(result.getId(), MALSqlHelper.RELATION_TYPE_PREQUEL));
            result.setSequels(getAnimeToAnimeRelations(result.getId(), MALSqlHelper.RELATION_TYPE_SEQUEL));
            result.setSideStories(getAnimeToAnimeRelations(result.getId(), MALSqlHelper.RELATION_TYPE_SIDE_STORY));
            result.setSpinOffs(getAnimeToAnimeRelations(result.getId(), MALSqlHelper.RELATION_TYPE_SPINOFF));
            result.setSummaries(getAnimeToAnimeRelations(result.getId(), MALSqlHelper.RELATION_TYPE_SUMMARY));
            result.setMangaAdaptions(getAnimeToMangaRelations(result.getId(), MALSqlHelper.RELATION_TYPE_ADAPTATION));
            ArrayList<RecordStub> parentStory = getAnimeToAnimeRelations(result.getId(), MALSqlHelper.RELATION_TYPE_PARENT_STORY);
            if (parentStory != null && parentStory.size() > 0) {
                result.setParentStory(parentStory.get(0));
            }
            HashMap<String, ArrayList<String>> otherTitles = new HashMap<String, ArrayList<String>>();
            otherTitles.put("english", getAnimeOtherTitles(result.getId(), MALSqlHelper.TITLE_TYPE_ENGLISH));
            otherTitles.put("japanese", getAnimeOtherTitles(result.getId(), MALSqlHelper.TITLE_TYPE_JAPANESE));
            otherTitles.put("synonyms", getAnimeOtherTitles(result.getId(), MALSqlHelper.TITLE_TYPE_SYNONYM));
            result.setOtherTitles(otherTitles);
            result.setProducers(getAnimeProducers(result.getId()));
        }
        cursor.close();
        return result;
    }

    private boolean deleteAnime(Integer id) {
        return getDBWrite().delete(MALSqlHelper.TABLE_ANIME, MALSqlHelper.COLUMN_ID + " = ?", new String[]{id.toString()}) == 1;
    }

    public boolean deleteAnimeFromAnimelist(Integer id, String username) {
        boolean result = false;
        Integer userId = getUserId(username);
        if (userId != 0) {
            result = getDBWrite().delete(MALSqlHelper.TABLE_ANIMELIST, "profile_id = ? AND anime_id = ?", new String[]{userId.toString(), id.toString()}) == 1;
            if (result) {
                boolean isUsed = false;
                /* check if this record is used for other relations and delete if it's not to keep the database
                 * still used relations can be:
                 * - animelist of other user
                 * - record is related to other anime or manga (e.g. as sequel or adaptation)
                 */
                // used in other animelist?
                isUsed = recordExists(MALSqlHelper.TABLE_ANIMELIST, "anime_id", id.toString());
                if (!isUsed) { // no need to check more if its already used
                    // used as related record of other anime?
                    isUsed = recordExists(MALSqlHelper.TABLE_ANIME_ANIME_RELATIONS, "related_id", id.toString());
                }
                if (!isUsed) { // no need to check more if its already used
                    // used as related record of an manga?
                    isUsed = recordExists(MALSqlHelper.TABLE_MANGA_ANIME_RELATIONS, "related_id", id.toString());
                }

                if (!isUsed) {// its not used anymore, delete it
                    deleteAnime(id);
                }
            }
        }
        return result;
    }

    // delete all anime records without relations, because they're "dead" records
    public void cleanupAnimeTable() {
        getDBWrite().rawQuery("DELETE FROM anime WHERE " +
                MALSqlHelper.COLUMN_ID + " NOT IN (SELECT DISTINCT anime_id FROM " + MALSqlHelper.TABLE_ANIMELIST + ") AND " +
                MALSqlHelper.COLUMN_ID + " NOT IN (SELECT DISTINCT related_id FROM " + MALSqlHelper.TABLE_ANIME_ANIME_RELATIONS + ") AND " +
                MALSqlHelper.COLUMN_ID + " NOT IN (SELECT DISTINCT related_id FROM " + MALSqlHelper.TABLE_MANGA_ANIME_RELATIONS + ")", null);
    }

    public ArrayList<Anime> getAnimeList(String listType, String username) {
        if (listType == "")
            return getAnimeList(getUserId(username), "", false);
        else
            return getAnimeList(getUserId(username), listType, false);
    }

    public ArrayList<Anime> getDirtyAnimeList(String username) {
        return getAnimeList(getUserId(username), "", true);
    }

    private ArrayList<Anime> getAnimeList(int userId, String listType, boolean dirtyOnly) {
        ArrayList<Anime> result = null;
        Cursor cursor;
        try {
            ArrayList<String> selArgs = new ArrayList<String>();
            selArgs.add(String.valueOf(userId));
            if (listType != "" && !listType.equals(Anime.STATUS_REWATCHING))
                selArgs.add(listType);

            if (listType.equals(Anime.STATUS_REWATCHING))
                cursor = getDBRead().rawQuery("SELECT a.*, al.score AS myScore, al.status AS myStatus, al.watched AS episodesWatched, al.dirty, al.lastUpdate," +
                        " al.watchedStart, al.WatchedEnd, al.fansub, al.priority, al.downloaded, al.storage, al.storageValue, al.rewatch, al.rewatchCount, al.rewatchValue, al.comments" +
                        " FROM animelist al INNER JOIN anime a ON al.anime_id = a." + MALSqlHelper.COLUMN_ID +
                        " WHERE al.profile_id = ? AND al.rewatch = 1 " + (dirtyOnly ? " AND al.dirty IS NOT NULL " : "") + " ORDER BY a.recordName COLLATE NOCASE", selArgs.toArray(new String[selArgs.size()]));
            else
                cursor = getDBRead().rawQuery("SELECT a.*, al.score AS myScore, al.status AS myStatus, al.watched AS episodesWatched, al.dirty, al.lastUpdate," +
                        " al.watchedStart, al.WatchedEnd, al.fansub, al.priority, al.downloaded, al.storage, al.storageValue, al.rewatch, al.rewatchCount, al.rewatchValue, al.comments" +
                        " FROM animelist al INNER JOIN anime a ON al.anime_id = a." + MALSqlHelper.COLUMN_ID +
                        " WHERE al.profile_id = ? " + (listType != "" ? " AND al.status = ? " : "") + (dirtyOnly ? " AND al.dirty IS NOT NULL " : "") + " ORDER BY a.recordName COLLATE NOCASE", selArgs.toArray(new String[selArgs.size()]));
            if (cursor.moveToFirst()) {
                result = new ArrayList<Anime>();
                do {
                    result.add(Anime.fromCursor(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLException e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.getAnimeList(): " + e.getMessage());
        }

        return result;
    }

    public void saveMangaList(ArrayList<Manga> list, String username) {
        Integer userId = getUserId(username);
        if (list != null && list.size() > 0 && userId != null) {
            try {
                getDBWrite().beginTransaction();
                for (Manga manga : list)
                    saveManga(manga, true, userId);
                getDBWrite().setTransactionSuccessful();
            } catch (Exception e) {
                Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.saveMangaList(): " + e.getMessage());
            } finally {
                getDBWrite().endTransaction();
            }
        }
    }

    public void saveManga(Manga manga, boolean ignoreSynopsis, String username) {
        Integer userId;
        if (username.equals(""))
            userId = 0;
        else
            userId = getUserId(username);
        saveManga(manga, ignoreSynopsis, userId);
    }

    public void saveManga(Manga manga, boolean ignoreSynopsis, int userId) {
        saveManga(manga, ignoreSynopsis, userId, false);
    }

    public void saveManga(Manga manga, boolean ignoreSynopsis, int userId, boolean dontCreateBaseModel) {
        ContentValues cv = new ContentValues();

        if (!AccountService.isMAL() && !dontCreateBaseModel)
            manga.createBaseModel();

        cv.put(MALSqlHelper.COLUMN_ID, manga.getId());
        cv.put("recordName", manga.getTitle());
        cv.put("recordType", manga.getType());
        cv.put("imageUrl", manga.getImageUrl());
        cv.put("recordStatus", manga.getStatus());
        cv.put("volumesTotal", manga.getVolumes());
        cv.put("chaptersTotal", manga.getChapters());

        if (!ignoreSynopsis) {
            cv.put("synopsis", manga.getSynopsis());
            cv.put("membersCount", manga.getMembersCount());
            cv.put("memberScore", manga.getMembersScore());
            cv.put("favoritedCount", manga.getFavoritedCount());
            cv.put("popularityRank", manga.getPopularityRank());
            cv.put("rank", manga.getRank());
            cv.put("listedId", manga.getListedId());
        }

        // don't use replace it replaces synopsis with null even when we don't put it in the ContentValues
        int updateResult = getDBWrite().update(MALSqlHelper.TABLE_MANGA, cv, MALSqlHelper.COLUMN_ID + " = ?", new String[]{Integer.toString(manga.getId())});
        if (updateResult == 0) {
            Long insertResult = getDBWrite().insert(MALSqlHelper.TABLE_MANGA, null, cv);
            if (insertResult > 0) {
                manga.setId(insertResult.intValue());
            }
        }

        if (manga.getId() > 0) { // save/update relations if saving was successful
            if (!ignoreSynopsis) { // only on DetailView!
                if (manga.getGenres() != null) {
                    // delete old relations
                    getDBWrite().delete(MALSqlHelper.TABLE_MANGA_GENRES, "manga_id = ?", new String[]{String.valueOf(manga.getId())});
                    for (String genre : manga.getGenres()) {
                        Integer genreId = getGenreId(genre);
                        if (genreId != null) {
                            ContentValues gcv = new ContentValues();
                            gcv.put("manga_id", manga.getId());
                            gcv.put("genre_id", genreId);
                            getDBWrite().replace(MALSqlHelper.TABLE_MANGA_GENRES, null, gcv);
                        }
                    }
                }
                if (manga.getTags() != null) {
                    // delete old relations
                    getDBWrite().delete(MALSqlHelper.TABLE_MANGA_TAGS, "manga_id = ?", new String[]{String.valueOf(manga.getId())});
                    for (String tag : manga.getTags()) {
                        Integer tagId = getTagId(tag);
                        if (tagId != null) {
                            ContentValues gcv = new ContentValues();
                            gcv.put("manga_id", manga.getId());
                            gcv.put("tag_id", tagId);
                            getDBWrite().replace(MALSqlHelper.TABLE_MANGA_TAGS, null, gcv);
                        }
                    }
                }
                if (manga.getAlternativeVersions() != null) {
                    // delete old relations
                    getDBWrite().delete(MALSqlHelper.TABLE_MANGA_MANGA_RELATIONS, "manga_id = ? AND relationType = ?", new String[]{String.valueOf(manga.getId()), MALSqlHelper.RELATION_TYPE_ALTERNATIVE});

                    for (RecordStub mangaStub : manga.getAlternativeVersions()) {
                        saveMangaToMangaRelation(manga.getId(), mangaStub, MALSqlHelper.RELATION_TYPE_ALTERNATIVE);
                    }
                }

                if (manga.getRelatedManga() != null) {
                    // delete old relations
                    getDBWrite().delete(MALSqlHelper.TABLE_MANGA_MANGA_RELATIONS, "manga_id = ? AND relationType = ?", new String[]{String.valueOf(manga.getId()), MALSqlHelper.RELATION_TYPE_RELATED});

                    for (RecordStub mangaStub : manga.getRelatedManga()) {
                        saveMangaToMangaRelation(manga.getId(), mangaStub, MALSqlHelper.RELATION_TYPE_RELATED);
                    }
                }

                if (manga.getPersonalTags() != null) {
                    // delete old relations
                    getDBWrite().delete(MALSqlHelper.TABLE_MANGA_PERSONALTAGS, "manga_id = ?", new String[]{String.valueOf(manga.getId())});
                    for (String tag : manga.getPersonalTags()) {
                        Integer tagId = getTagId(tag);
                        if (tagId != null) {
                            ContentValues gcv = new ContentValues();
                            gcv.put("manga_id", manga.getId());
                            gcv.put("tag_id", tagId);
                            getDBWrite().replace(MALSqlHelper.TABLE_MANGA_PERSONALTAGS, null, gcv);
                        }
                    }
                }

                if (manga.getAnimeAdaptations() != null) {
                    // delete old relations
                    getDBWrite().delete(MALSqlHelper.TABLE_MANGA_ANIME_RELATIONS, "manga_id = ? AND relationType = ?", new String[]{String.valueOf(manga.getId()), MALSqlHelper.RELATION_TYPE_ADAPTATION});

                    for (RecordStub animeStub : manga.getAnimeAdaptations()) {
                        saveMangaToAnimeRelation(manga.getId(), animeStub, MALSqlHelper.RELATION_TYPE_ADAPTATION);
                    }
                }

                if (manga.getOtherTitles() != null) {
                    if (manga.getOtherTitlesEnglish() != null) {
                        // delete old relations
                        getDBWrite().delete(MALSqlHelper.TABLE_MANGA_OTHER_TITLES, "manga_id = ? and titleType = ?", new String[]{String.valueOf(manga.getId()), MALSqlHelper.TITLE_TYPE_ENGLISH});
                        for (String title : manga.getOtherTitlesEnglish()) {
                            saveMangaOtherTitle(manga.getId(), title, MALSqlHelper.TITLE_TYPE_ENGLISH);
                        }
                    }

                    if (manga.getOtherTitlesJapanese() != null) {
                        // delete old relations
                        getDBWrite().delete(MALSqlHelper.TABLE_MANGA_OTHER_TITLES, "manga_id = ? and titleType = ?", new String[]{String.valueOf(manga.getId()), MALSqlHelper.TITLE_TYPE_JAPANESE});
                        for (String title : manga.getOtherTitlesJapanese()) {
                            saveMangaOtherTitle(manga.getId(), title, MALSqlHelper.TITLE_TYPE_JAPANESE);
                        }
                    }

                    if (manga.getOtherTitlesSynonyms() != null) {
                        // delete old relations
                        getDBWrite().delete(MALSqlHelper.TABLE_MANGA_OTHER_TITLES, "manga_id = ? and titleType = ?", new String[]{String.valueOf(manga.getId()), MALSqlHelper.TITLE_TYPE_SYNONYM});
                        for (String title : manga.getOtherTitlesSynonyms()) {
                            saveMangaOtherTitle(manga.getId(), title, MALSqlHelper.TITLE_TYPE_SYNONYM);
                        }
                    }
                }
            }

            // update mangalist if user id is provided
            if (userId > 0) {
                ContentValues mlcv = new ContentValues();
                mlcv.put("profile_id", userId);
                mlcv.put("manga_id", manga.getId());
                mlcv.put("status", manga.getReadStatus());
                mlcv.put("score", manga.getScore());
                mlcv.put("volumesRead", manga.getVolumesRead());
                mlcv.put("chaptersRead", manga.getChaptersRead());
                mlcv.put("readStart", manga.getReadingStart());
                mlcv.put("readEnd", manga.getReadingEnd());
                mlcv.put("priority", manga.getPriority());
                mlcv.put("downloaded", manga.getChapDownloaded());
                mlcv.put("rereading", manga.getRereadValue());
                mlcv.put("rereadCount", manga.getRereadCount());
                mlcv.put("comments", manga.getPersonalComments());
                mlcv.put("dirty", manga.getDirty() != null ? new Gson().toJson(manga.getDirty()) : null);
                if (manga.getLastUpdate() != null)
                    mlcv.put("lastUpdate", manga.getLastUpdate().getTime());
                getDBWrite().replace(MALSqlHelper.TABLE_MANGALIST, null, mlcv);
            }
        }
    }

    public Manga getManga(Integer id, String username) {
        Manga result = null;
        Cursor cursor = getDBRead().rawQuery("SELECT m.*, ml.score AS myScore, ml.status AS myStatus, ml.chaptersRead, ml.volumesRead, ml.readStart, ml.readEnd," +
                " ml.priority, ml.downloaded, ml.rereading, ml.rereadCount, ml.comments, ml.dirty, ml.lastUpdate" +
                " FROM mangalist ml INNER JOIN manga m ON ml.manga_id = m." + MALSqlHelper.COLUMN_ID +
                " WHERE ml.profile_id = ? and m." + MALSqlHelper.COLUMN_ID + " = ?", new String[]{getUserId(username).toString(), id.toString()});
        if (cursor.moveToFirst()) {
            result = Manga.fromCursor(cursor);
            result.setGenres(getMangaGenres(result.getId()));
            result.setTags(getMangaTags(result.getId()));
            result.setPersonalTags(getMangaPersonalTags(result.getId()), false);
            result.setAlternativeVersions(getMangaToMangaRelations(result.getId(), MALSqlHelper.RELATION_TYPE_ALTERNATIVE));
            result.setRelatedManga(getMangaToMangaRelations(result.getId(), MALSqlHelper.RELATION_TYPE_RELATED));
            result.setAnimeAdaptations(getMangaToAnimeRelations(result.getId(), MALSqlHelper.RELATION_TYPE_ADAPTATION));
            HashMap<String, ArrayList<String>> otherTitles = new HashMap<String, ArrayList<String>>();
            otherTitles.put("english", getMangaOtherTitles(result.getId(), MALSqlHelper.TITLE_TYPE_ENGLISH));
            otherTitles.put("japanese", getMangaOtherTitles(result.getId(), MALSqlHelper.TITLE_TYPE_JAPANESE));
            otherTitles.put("synonyms", getMangaOtherTitles(result.getId(), MALSqlHelper.TITLE_TYPE_SYNONYM));
            result.setOtherTitles(otherTitles);
        }
        cursor.close();
        return result;
    }

    private boolean deleteManga(Integer id) {
        return getDBWrite().delete(MALSqlHelper.TABLE_MANGA, MALSqlHelper.COLUMN_ID + " = ?", new String[]{id.toString()}) == 1;
    }

    public boolean deleteMangaFromMangalist(Integer id, String username) {
        boolean result = false;
        Integer userId = getUserId(username);
        if (userId != 0) {
            result = getDBWrite().delete(MALSqlHelper.TABLE_MANGALIST, "profile_id = ? AND manga_id = ?", new String[]{userId.toString(), id.toString()}) == 1;
            if (result) {
                boolean isUsed = false;
                /* check if this record is used for other relations and delete if it's not to keep the database
                 * still used relations can be:
                 * - mangalist of other user
                 * - record is related to other anime or manga (e.g. as sequel or adaptation)
                 */
                // used in other mangalist?
                isUsed = recordExists(MALSqlHelper.TABLE_MANGALIST, "manga_id", id.toString());
                if (!isUsed) { // no need to check more if its already used
                    // used as related record of other manga?
                    isUsed = recordExists(MALSqlHelper.TABLE_MANGA_MANGA_RELATIONS, "related_id", id.toString());
                }
                if (!isUsed) { // no need to check more if its already used
                    // used as related record of an anime?
                    isUsed = recordExists(MALSqlHelper.TABLE_ANIME_MANGA_RELATIONS, "related_id", id.toString());
                }

                if (!isUsed) {// its not used anymore, delete it
                    deleteManga(id);
                }
            }
        }
        return result;
    }

    // delete all manga records without relations, because they're "dead" records
    public void cleanupMangaTable() {
        getDBWrite().rawQuery("DELETE FROM manga WHERE " +
                MALSqlHelper.COLUMN_ID + " NOT IN (SELECT DISTINCT manga_id FROM " + MALSqlHelper.TABLE_MANGALIST + ") AND " +
                MALSqlHelper.COLUMN_ID + " NOT IN (SELECT DISTINCT related_id FROM " + MALSqlHelper.TABLE_MANGA_MANGA_RELATIONS + ") AND " +
                MALSqlHelper.COLUMN_ID + " NOT IN (SELECT DISTINCT related_id FROM " + MALSqlHelper.TABLE_ANIME_MANGA_RELATIONS + ")", null);
    }

    public ArrayList<Manga> getMangaList(String listType, String username) {
        if (listType == "")
            return getMangaList(getUserId(username), "", false);
        else
            return getMangaList(getUserId(username), listType, false);
    }

    public ArrayList<Manga> getDirtyMangaList(String username) {
        return getMangaList(getUserId(username), "", true);
    }

    private ArrayList<Manga> getMangaList(int userId, String listType, boolean dirtyOnly) {
        ArrayList<Manga> result = null;
        Cursor cursor;
        try {
            ArrayList<String> selArgs = new ArrayList<String>();
            selArgs.add(String.valueOf(userId));
            if (listType != "") {
                selArgs.add(listType);
            }
            cursor = getDBRead().rawQuery("SELECT m.*, ml.score AS myScore, ml.status AS myStatus, ml.chaptersRead, ml.volumesRead, ml.readStart, ml.readEnd," +
                    " ml.priority, ml.downloaded, ml.rereading, ml.rereadCount, ml.comments, ml.dirty, ml.lastUpdate" +
                    " FROM mangalist ml INNER JOIN manga m ON ml.manga_id = m." + MALSqlHelper.COLUMN_ID +
                    " WHERE ml.profile_id = ? " + (listType != "" ? " AND ml.status = ? " : "") + (dirtyOnly ? " AND ml.dirty <> \"\" " : "") + " ORDER BY m.recordName COLLATE NOCASE", selArgs.toArray(new String[selArgs.size()]));
            if (cursor.moveToFirst()) {
                result = new ArrayList<Manga>();
                do {
                    result.add(Manga.fromCursor(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLException e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.getMangaList(): " + e.getMessage());
        }

        return result;
    }

    public void saveProfile(Profile profile) {
        ContentValues cv = new ContentValues();

        cv.put("username", profile.getDisplayName());
        cv.put("anime_time", profile.getAnimeTime());
        cv.put("manga_chap", profile.getMangaChapters());
        cv.put("about", profile.getAbout());
        cv.put("list_order", profile.getListOrder());
        cv.put("avatar_url", profile.getImageUrl());
        cv.put("image_url_banner", profile.getImageUrlBanner());
        cv.put("title_language", profile.getTitleLanguage());
        cv.put("score_type", profile.getScoreType());

        //cv.put("advanced_rating", profile.getAdvancedRating());

        cv.put("notifications", profile.getNotifications());

        // don't use replace it alters the autoincrement _id field!
        int updateResult = getDBWrite().update(MALSqlHelper.TABLE_PROFILE, cv, "username = ?", new String[]{profile.getDisplayName()});
        if (updateResult > 0) {// updated row
            profile.setId(getUserId(profile.getDisplayName()));
        } else {
            Long insertResult = getDBWrite().insert(MALSqlHelper.TABLE_PROFILE, null, cv);
            profile.setId(insertResult.intValue());
        }
    }

    private Profile getProfile(Integer userId) {
        Profile result = null;
        Cursor cursor = getDBRead().query(MALSqlHelper.TABLE_PROFILE, null, MALSqlHelper.COLUMN_ID + " = ?", new String[]{userId.toString()}, null, null, null);
        if (cursor.moveToFirst()) {
            result = Profile.fromCursor(cursor);
            cursor.close();
        }
        return result;
    }

    public void saveUser(User profile) {
        ContentValues cv = new ContentValues();

        cv.put("username", profile.getDisplayName());
        cv.put("avatar_url", profile.getImageUrl());

        // don't use replace it alters the autoincrement _id field!
        int updateResult = getDBWrite().update(MALSqlHelper.TABLE_PROFILE, cv, "username = ?", new String[]{profile.getDisplayName()});
        if (updateResult > 0) {// updated row
            profile.setId(getUserId(profile.getDisplayName()));
        } else {
            Long insertResult = getDBWrite().insert(MALSqlHelper.TABLE_PROFILE, null, cv);
            profile.setId(insertResult.intValue());
        }
    }

    public void saveUser(User user, Boolean profile) {
        ContentValues cv = new ContentValues();

        cv.put("username", user.getName());
        if (user.getProfile().getAvatarUrl().equals("http://cdn.myanimelist.net/images/questionmark_50.gif"))
            cv.put("avatar_url", "http://cdn.myanimelist.net/images/na.gif");
        else
            cv.put("avatar_url", user.getProfile().getAvatarUrl());
        if (user.getProfile().getDetails().getLastOnline() != null) {
            cv.put("last_online", user.getProfile().getDetails().getLastOnline());
        } else
            cv.putNull("last_online");

        if (profile) {
            if (user.getProfile().getDetails().getBirthday() != null) {
                String birthday = MALDateTools.parseMALDateToISO8601String(user.getProfile().getDetails().getBirthday());
                cv.put("birthday", birthday.equals("") ? user.getProfile().getDetails().getBirthday() : birthday);
            } else
                cv.putNull("birthday");
            cv.put("location", user.getProfile().getDetails().getLocation());
            cv.put("website", user.getProfile().getDetails().getWebsite());
            cv.put("comments", user.getProfile().getDetails().getComments());
            cv.put("forum_posts", user.getProfile().getDetails().getForumPosts());
            cv.put("gender", user.getProfile().getDetails().getGender());
            if (user.getProfile().getDetails().getJoinDate() != null) {
                String joindate = MALDateTools.parseMALDateToISO8601String(user.getProfile().getDetails().getJoinDate());
                cv.put("join_date", joindate.equals("") ? user.getProfile().getDetails().getJoinDate() : joindate);
            } else
                cv.putNull("join_date");
            cv.put("access_rank", user.getProfile().getDetails().getAccessRank());
            cv.put("anime_list_views", user.getProfile().getDetails().getAnimeListViews());
            cv.put("manga_list_views", user.getProfile().getDetails().getMangaListViews());

            cv.put("anime_time_days", user.getProfile().getAnimeStats().getTimeDays());
            cv.put("anime_watching", user.getProfile().getAnimeStats().getWatching());
            cv.put("anime_completed", user.getProfile().getAnimeStats().getCompleted());
            cv.put("anime_on_hold", user.getProfile().getAnimeStats().getOnHold());
            cv.put("anime_dropped", user.getProfile().getAnimeStats().getDropped());
            cv.put("anime_plan_to_watch", user.getProfile().getAnimeStats().getPlanToWatch());
            cv.put("anime_total_entries", user.getProfile().getAnimeStats().getTotalEntries());

            cv.put("manga_time_days", user.getProfile().getMangaStats().getTimeDays());
            cv.put("manga_reading", user.getProfile().getMangaStats().getReading());
            cv.put("manga_completed", user.getProfile().getMangaStats().getCompleted());
            cv.put("manga_on_hold", user.getProfile().getMangaStats().getOnHold());
            cv.put("manga_dropped", user.getProfile().getMangaStats().getDropped());
            cv.put("manga_plan_to_read", user.getProfile().getMangaStats().getPlanToRead());
            cv.put("manga_total_entries", user.getProfile().getMangaStats().getTotalEntries());
        }

        // don't use replace it alters the autoincrement _id field!
        int updateResult = getDBWrite().update(MALSqlHelper.TABLE_PROFILE, cv, "username = ?", new String[]{user.getName()});
        if (updateResult > 0) {// updated row
            user.setId(getUserId(user.getName()));
        } else {
            Long insertResult = getDBWrite().insert(MALSqlHelper.TABLE_PROFILE, null, cv);
            user.setId(insertResult.intValue());
        }
    }

    public void saveUserFriends(Integer userId, ArrayList<User> friends) {
        if (userId == null || friends == null) {
            return;
        }
        SQLiteDatabase db = getDBWrite();
        db.beginTransaction();
        try {
            db.delete(MALSqlHelper.TABLE_FRIENDLIST, "profile_id = ?", new String[]{userId.toString()});
            for (User friend : friends) {
                ContentValues cv = new ContentValues();
                cv.put("profile_id", userId);
                cv.put("friend_id", friend.getId());
                db.insert(MALSqlHelper.TABLE_FRIENDLIST, null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public User getProfile(String name) {
        User result = null;
        Cursor cursor;
        try {
            cursor = getDBRead().query(MALSqlHelper.TABLE_PROFILE, null, "username = ?", new String[]{name}, null, null, null);
            if (cursor.moveToFirst())
                result = User.fromCursor(cursor);
            cursor.close();
        } catch (SQLException e) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.getProfile(): " + e.getMessage());
        }
        return result;
    }

    public ArrayList<User> getFriendList(String username) {
        ArrayList<User> friendlist = new ArrayList<User>();
        Cursor cursor = getDBRead().rawQuery("SELECT p1.* FROM " + MALSqlHelper.TABLE_PROFILE + " AS p1" +                  // for result rows
                " INNER JOIN " + MALSqlHelper.TABLE_PROFILE + " AS p2" +                                                    // for getting user id to given name
                " INNER JOIN " + MALSqlHelper.TABLE_FRIENDLIST + " AS fl ON fl.profile_id = p2." + MALSqlHelper.COLUMN_ID + // for user<>friend relation
                " WHERE p2.username = ? AND p1." + MALSqlHelper.COLUMN_ID + " = fl.friend_id ORDER BY p1.username COLLATE NOCASE", new String[]{username});
        if (cursor.moveToFirst()) {
            do {
                friendlist.add(User.fromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return friendlist;
    }

    public void saveFriendList(ArrayList<User> friendlist, String username) {
        for (User friend : friendlist) {
            if (AccountService.isMAL())
                saveUser(friend, false);
            else
                saveUser(friend);
        }

        Integer userId = getUserId(username);
        saveUserFriends(userId, friendlist);
    }

    private Integer getGenreId(String genre) {
        return getRecordId(MALSqlHelper.TABLE_GENRES, MALSqlHelper.COLUMN_ID, "recordName", genre);
    }

    private Integer getTagId(String tag) {
        return getRecordId(MALSqlHelper.TABLE_TAGS, MALSqlHelper.COLUMN_ID, "recordName", tag);
    }

    private Integer getProducerId(String producer) {
        return getRecordId(MALSqlHelper.TABLE_PRODUCER, MALSqlHelper.COLUMN_ID, "recordName", producer);
    }

    private Integer getUserId(String username) {
        if (username == null || username.equals(""))
            return 0;
        Integer id = getRecordId(MALSqlHelper.TABLE_PROFILE, MALSqlHelper.COLUMN_ID, "username", username);
        if (id == null) {
            id = 0;
        }
        return id;
    }

    private Integer getRecordId(String table, String idField, String searchField, String value) {
        Integer result = null;
        Cursor cursor = getDBRead().query(table, new String[]{idField}, searchField + " = ?", new String[]{value}, null, null, null);
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }
        cursor.close();

        if (result == null) {
            ContentValues cv = new ContentValues();
            cv.put(searchField, value);
            Long addResult = getDBWrite().insert(table, null, cv);
            if (addResult > -1) {
                result = addResult.intValue();
            }
        }
        return result;
    }

    public ArrayList<String> getAnimeGenres(Integer animeId) {
        ArrayList<String> result = null;
        Cursor cursor = getDBRead().rawQuery("SELECT g.recordName FROM " + MALSqlHelper.TABLE_GENRES + " g " +
                "INNER JOIN " + MALSqlHelper.TABLE_ANIME_GENRES + " ag ON ag.genre_id = g." + MALSqlHelper.COLUMN_ID +
                " WHERE ag.anime_id = ? ORDER BY g.recordName COLLATE NOCASE", new String[]{animeId.toString()});
        if (cursor.moveToFirst()) {
            result = new ArrayList<String>();
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public ArrayList<String> getAnimeTags(Integer animeId) {
        ArrayList<String> result = null;
        Cursor cursor = getDBRead().rawQuery("SELECT t.recordName FROM " + MALSqlHelper.TABLE_TAGS + " t " +
                "INNER JOIN " + MALSqlHelper.TABLE_ANIME_TAGS + " at ON at.tag_id = t." + MALSqlHelper.COLUMN_ID +
                " WHERE at.anime_id = ? ORDER BY t.recordName COLLATE NOCASE", new String[]{animeId.toString()});
        if (cursor.moveToFirst()) {
            result = new ArrayList<String>();
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public ArrayList<String> getAnimePersonalTags(Integer animeId) {
        ArrayList<String> result = null;
        Cursor cursor = getDBRead().rawQuery("SELECT t.recordName FROM " + MALSqlHelper.TABLE_TAGS + " t " +
                "INNER JOIN " + MALSqlHelper.TABLE_ANIME_PERSONALTAGS + " at ON at.tag_id = t." + MALSqlHelper.COLUMN_ID +
                " WHERE at.anime_id = ? ORDER BY t.recordName COLLATE NOCASE", new String[]{animeId.toString()});
        if (cursor.moveToFirst()) {
            result = new ArrayList<String>();
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public ArrayList<String> getMangaPersonalTags(Integer mangaId) {
        ArrayList<String> result = null;
        Cursor cursor = getDBRead().rawQuery("SELECT t.recordName FROM " + MALSqlHelper.TABLE_TAGS + " t " +
                "INNER JOIN " + MALSqlHelper.TABLE_MANGA_PERSONALTAGS + " at ON at.tag_id = t." + MALSqlHelper.COLUMN_ID +
                " WHERE at.manga_id = ? ORDER BY t.recordName COLLATE NOCASE", new String[]{mangaId.toString()});
        if (cursor.moveToFirst()) {
            result = new ArrayList<String>();
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public ArrayList<String> getMangaGenres(Integer mangaId) {
        ArrayList<String> result = null;
        Cursor cursor = getDBRead().rawQuery("SELECT g.recordName FROM " + MALSqlHelper.TABLE_GENRES + " g " +
                "INNER JOIN " + MALSqlHelper.TABLE_MANGA_GENRES + " mg ON mg.genre_id = g." + MALSqlHelper.COLUMN_ID +
                " WHERE mg.manga_id = ? ORDER BY g.recordName COLLATE NOCASE", new String[]{mangaId.toString()});
        if (cursor.moveToFirst()) {
            result = new ArrayList<String>();
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public ArrayList<String> getMangaTags(Integer mangaId) {
        ArrayList<String> result = null;
        Cursor cursor = getDBRead().rawQuery("SELECT t.recordName FROM " + MALSqlHelper.TABLE_TAGS + " t " +
                "INNER JOIN " + MALSqlHelper.TABLE_MANGA_TAGS + " mt ON mt.tag_id = t." + MALSqlHelper.COLUMN_ID +
                " WHERE mt.manga_id = ? ORDER BY t.recordName COLLATE NOCASE", new String[]{mangaId.toString()});
        if (cursor.moveToFirst()) {
            result = new ArrayList<String>();
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public ArrayList<String> getAnimeProducers(Integer animeId) {
        ArrayList<String> result = null;
        Cursor cursor = getDBRead().rawQuery("SELECT p.recordName FROM " + MALSqlHelper.TABLE_PRODUCER + " p " +
                "INNER JOIN " + MALSqlHelper.TABLE_ANIME_PRODUCER + " ap ON ap.producer_id = p." + MALSqlHelper.COLUMN_ID +
                " WHERE ap.anime_id = ? ORDER BY p.recordName COLLATE NOCASE", new String[]{animeId.toString()});

        if (cursor.moveToFirst()) {
            result = new ArrayList<String>();
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    private boolean recordExists(String table, String searchField, String searchValue) {
        boolean result = false;
        Cursor cursor = getDBRead().query(table, null, searchField + " = ?", new String[]{searchValue}, null, null, null);
        if (cursor.moveToFirst()) {
            result = true;
        }
        cursor.close();
        return result;
    }

    /* Storing relations is a little more complicated as we need to look if the related anime is
     * stored in the database, if not we need to create a new record before storing the information.
     * This record then only has the few informations that are available in the relation object
     * returned by the API (only id and title)
     */
    private void saveAnimeToAnimeRelation(int animeId, RecordStub relatedAnime, String relationType) {
        if (relatedAnime.getId() == 0) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.saveAnimeToAnimeRelation(): error saving relation: anime id must not be 0; title: " + relatedAnime.getTitle());
            return;
        }
        boolean relatedRecordExists;
        if (!recordExists(MALSqlHelper.TABLE_ANIME, MALSqlHelper.COLUMN_ID, String.valueOf(relatedAnime.getId()))) {
            ContentValues cv = new ContentValues();
            cv.put(MALSqlHelper.COLUMN_ID, relatedAnime.getId());
            cv.put("recordName", relatedAnime.getTitle());
            relatedRecordExists = getDBWrite().insert(MALSqlHelper.TABLE_ANIME, null, cv) > 0;
        } else {
            relatedRecordExists = true;
        }

        if (relatedRecordExists) {
            ContentValues cv = new ContentValues();
            cv.put("anime_id", animeId);
            cv.put("related_id", relatedAnime.getId());
            cv.put("relationType", relationType);
            getDBWrite().replace(MALSqlHelper.TABLE_ANIME_ANIME_RELATIONS, null, cv);
        }
    }

    private void saveAnimeToMangaRelation(int animeId, RecordStub relatedManga, String relationType) {
        if (relatedManga.getId() == 0) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.saveAnimeToMangaRelation(): error saving relation: manga id must not be 0; title: " + relatedManga.getTitle());
            return;
        }
        boolean relatedRecordExists;
        if (!recordExists(MALSqlHelper.TABLE_MANGA, MALSqlHelper.COLUMN_ID, String.valueOf(relatedManga.getId()))) {
            ContentValues cv = new ContentValues();
            cv.put(MALSqlHelper.COLUMN_ID, relatedManga.getId());
            cv.put("recordName", relatedManga.getTitle());
            relatedRecordExists = getDBWrite().insert(MALSqlHelper.TABLE_MANGA, null, cv) > 0;
        } else {
            relatedRecordExists = true;
        }

        if (relatedRecordExists) {
            ContentValues cv = new ContentValues();
            cv.put("anime_id", animeId);
            cv.put("related_id", relatedManga.getId());
            cv.put("relationType", relationType);
            getDBWrite().replace(MALSqlHelper.TABLE_ANIME_MANGA_RELATIONS, null, cv);
        }
    }

    private void saveMangaToMangaRelation(int mangaId, RecordStub relatedManga, String relationType) {
        if (relatedManga.getId() == 0) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.saveMangaToMangaRelation(): error saving relation: manga id must not be 0; title: " + relatedManga.getTitle());
            return;
        }
        boolean relatedRecordExists;
        if (!recordExists(MALSqlHelper.TABLE_MANGA, MALSqlHelper.COLUMN_ID, String.valueOf(relatedManga.getId()))) {
            ContentValues cv = new ContentValues();
            cv.put(MALSqlHelper.COLUMN_ID, relatedManga.getId());
            cv.put("recordName", relatedManga.getTitle());
            relatedRecordExists = getDBWrite().insert(MALSqlHelper.TABLE_MANGA, null, cv) > 0;
        } else {
            relatedRecordExists = true;
        }

        if (relatedRecordExists) {
            ContentValues cv = new ContentValues();
            cv.put("manga_id", mangaId);
            cv.put("related_id", relatedManga.getId());
            cv.put("relationType", relationType);
            getDBWrite().replace(MALSqlHelper.TABLE_MANGA_MANGA_RELATIONS, null, cv);
        }
    }

    private void saveMangaToAnimeRelation(int mangaId, RecordStub relatedAnime, String relationType) {
        if (relatedAnime.getId() == 0) {
            Crashlytics.log(Log.ERROR, "MALX", "DatabaseManager.saveMangaToAnimeRelation(): error saving relation: anime id must not be 0; title: " + relatedAnime.getTitle());
            return;
        }
        boolean relatedRecordExists;
        if (!recordExists(MALSqlHelper.TABLE_ANIME, MALSqlHelper.COLUMN_ID, String.valueOf(relatedAnime.getId()))) {
            ContentValues cv = new ContentValues();
            cv.put(MALSqlHelper.COLUMN_ID, relatedAnime.getId());
            cv.put("recordName", relatedAnime.getTitle());
            relatedRecordExists = getDBWrite().insert(MALSqlHelper.TABLE_ANIME, null, cv) > 0;
        } else {
            relatedRecordExists = true;
        }

        if (relatedRecordExists) {
            ContentValues cv = new ContentValues();
            cv.put("manga_id", mangaId);
            cv.put("related_id", relatedAnime.getId());
            cv.put("relationType", relationType);
            getDBWrite().replace(MALSqlHelper.TABLE_MANGA_ANIME_RELATIONS, null, cv);
        }
    }

    private ArrayList<RecordStub> getAnimeToAnimeRelations(Integer animeId, String relationType) {
        ArrayList<RecordStub> result = null;
        Cursor cursor = getDBRead().rawQuery("SELECT a." + MALSqlHelper.COLUMN_ID + ", a.recordName FROM " + MALSqlHelper.TABLE_ANIME + " a " +
                "INNER JOIN " + MALSqlHelper.TABLE_ANIME_ANIME_RELATIONS + " ar ON a." + MALSqlHelper.COLUMN_ID + " = ar.related_id " +
                "WHERE ar.anime_id = ? AND ar.relationType = ? ORDER BY a.recordName COLLATE NOCASE", new String[]{animeId.toString(), relationType});
        if (cursor.moveToFirst()) {
            result = new ArrayList<RecordStub>();
            do {
                RecordStub animeStub = new RecordStub();
                animeStub.setId(cursor.getInt(0), MALApi.ListType.ANIME);
                animeStub.setTitle(cursor.getString(1));
                result.add(animeStub);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    private ArrayList<RecordStub> getAnimeToMangaRelations(Integer animeId, String relationType) {
        ArrayList<RecordStub> result = null;
        Cursor cursor = getDBRead().rawQuery("SELECT m." + MALSqlHelper.COLUMN_ID + ", m.recordName FROM " + MALSqlHelper.TABLE_MANGA + " m " +
                "INNER JOIN " + MALSqlHelper.TABLE_ANIME_MANGA_RELATIONS + " ar ON m." + MALSqlHelper.COLUMN_ID + " = ar.related_id " +
                "WHERE ar.anime_id = ? AND ar.relationType = ? ORDER BY m.recordName COLLATE NOCASE", new String[]{animeId.toString(), relationType});
        if (cursor.moveToFirst()) {
            result = new ArrayList<RecordStub>();
            do {
                RecordStub mangaStub = new RecordStub();
                mangaStub.setId(cursor.getInt(0), MALApi.ListType.MANGA);
                mangaStub.setTitle(cursor.getString(1));
                result.add(mangaStub);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    private ArrayList<RecordStub> getMangaToMangaRelations(Integer mangaId, String relationType) {
        ArrayList<RecordStub> result = null;
        Cursor cursor = getDBRead().rawQuery("SELECT m." + MALSqlHelper.COLUMN_ID + ", m.recordName FROM " + MALSqlHelper.TABLE_MANGA + " m " +
                "INNER JOIN " + MALSqlHelper.TABLE_MANGA_MANGA_RELATIONS + " mr ON m." + MALSqlHelper.COLUMN_ID + " = mr.related_id " +
                "WHERE mr.manga_id = ? AND mr.relationType = ? ORDER BY m.recordName COLLATE NOCASE", new String[]{mangaId.toString(), relationType});
        if (cursor.moveToFirst()) {
            result = new ArrayList<RecordStub>();
            do {
                RecordStub mangaStub = new RecordStub();
                mangaStub.setId(cursor.getInt(0), MALApi.ListType.MANGA);
                mangaStub.setTitle(cursor.getString(1));
                result.add(mangaStub);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    private ArrayList<RecordStub> getMangaToAnimeRelations(Integer mangaId, String relationType) {
        ArrayList<RecordStub> result = null;
        Cursor cursor = getDBRead().rawQuery("SELECT a." + MALSqlHelper.COLUMN_ID + ", a.recordName FROM " + MALSqlHelper.TABLE_ANIME + " a " +
                "INNER JOIN " + MALSqlHelper.TABLE_MANGA_ANIME_RELATIONS + " mr ON a." + MALSqlHelper.COLUMN_ID + " = mr.related_id " +
                "WHERE mr.manga_id = ? AND mr.relationType = ? ORDER BY a.recordName COLLATE NOCASE", new String[]{mangaId.toString(), relationType});
        if (cursor.moveToFirst()) {
            result = new ArrayList<RecordStub>();
            do {
                RecordStub animeStub = new RecordStub();
                animeStub.setId(cursor.getInt(0), MALApi.ListType.ANIME);
                animeStub.setTitle(cursor.getString(1));
                result.add(animeStub);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    private void saveAnimeOtherTitle(int animeId, String title, String titleType) {
        ContentValues cv = new ContentValues();
        cv.put("anime_id", animeId);
        cv.put("titleType", titleType);
        cv.put("title", title);
        getDBWrite().replace(MALSqlHelper.TABLE_ANIME_OTHER_TITLES, null, cv);
    }

    private void saveMangaOtherTitle(int mangaId, String title, String titleType) {
        ContentValues cv = new ContentValues();
        cv.put("manga_id", mangaId);
        cv.put("titleType", titleType);
        cv.put("title", title);
        getDBWrite().replace(MALSqlHelper.TABLE_MANGA_OTHER_TITLES, null, cv);
    }

    private ArrayList<String> getAnimeOtherTitles(Integer animeId, String titleType) {
        ArrayList<String> result = null;
        Cursor cursor = getDBRead().query(MALSqlHelper.TABLE_ANIME_OTHER_TITLES, new String[]{"title"}, "anime_id = ? AND titleType = ?", new String[]{animeId.toString(), titleType}, null, null, "title COLLATE NOCASE");
        if (cursor.moveToFirst()) {
            result = new ArrayList<String>();
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    private ArrayList<String> getMangaOtherTitles(Integer mangaId, String titleType) {
        ArrayList<String> result = null;
        Cursor cursor = getDBRead().query(MALSqlHelper.TABLE_MANGA_OTHER_TITLES, new String[]{"title"}, "manga_id = ? AND titleType = ?", new String[]{mangaId.toString(), titleType}, null, null, "title COLLATE NOCASE");
        if (cursor.moveToFirst()) {
            result = new ArrayList<String>();
            do {
                result.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public void saveActivity(ArrayList<Activity> activities, String username) {
        Integer userId = getUserId(username);
        if (userId > 0) {
            for (Activity activity : activities) {
                ContentValues cv = new ContentValues();
                cv.put(MALSqlHelper.COLUMN_ID, activity.getId());
                cv.put("user", userId);
                cv.put("type", activity.getActivityType());
                cv.put("created", activity.getCreatedAt());
                cv.put("reply_count", activity.getReplyCount());
                cv.put("status", activity.getStatus());
                cv.put("value", activity.getValue());
                if (activity.getSeries() != null) {
                    if (activity.getSeries().getSeriesType().equals("anime")) {
                        Anime anime = activity.getSeries().getAnime();
                        if (anime != null) {
                            saveAnime(anime, true, 0, true);
                            cv.put("series_anime", activity.getSeries().getId());
                        }
                    } else if (activity.getSeries().getSeriesType().equals("manga")) {
                        Manga manga = activity.getSeries().getManga();
                        if (manga != null) {
                            saveManga(manga, true, 0, true);
                            cv.put("series_manga", activity.getSeries().getId());
                        }
                    }
                }
                getDBWrite().replace(MALSqlHelper.TABLE_ACTIVITIES, null, cv);
                if (activity.getUsers() != null) {
                    for (Profile user : activity.getUsers()) {
                        ContentValues ucv = new ContentValues();
                        ucv.put("profile_id", getUserId(user.getDisplayName()));
                        ucv.put("activity_id", activity.getId());
                        getDBWrite().replace(MALSqlHelper.TABLE_ACTIVITIES_USERS, null, ucv);
                    }
                }
            }
        }
    }

    public ArrayList<Activity> getActivity(String username) {
        ArrayList<Activity> result = null;
        Cursor cursor = getDBRead().query(MALSqlHelper.TABLE_ACTIVITIES, null, "user = ?", new String[]{getUserId(username).toString()}, null, null, MALSqlHelper.COLUMN_ID + " DESC");
        if (cursor.moveToFirst()) {
            result = new ArrayList<>();
            do {
                Activity activity = new Activity();
                activity.setId(cursor.getInt(cursor.getColumnIndex(MALSqlHelper.COLUMN_ID)));
                activity.setUserId(cursor.getInt(cursor.getColumnIndex("user")));
                activity.setActivityType(cursor.getString(cursor.getColumnIndex("type")));
                activity.setCreatedAt(cursor.getString(cursor.getColumnIndex("created")));
                activity.setReplyCount(cursor.getInt(cursor.getColumnIndex("reply_count")));
                activity.setStatus(cursor.getString(cursor.getColumnIndex("status")));
                activity.setValue(cursor.getString(cursor.getColumnIndex("value")));
                if (!cursor.isNull(cursor.getColumnIndex("series_anime"))) {
                    Anime anime = getAnime(cursor.getInt(cursor.getColumnIndex("series_anime")), username);
                    if (anime != null) {
                        activity.setSeries(Series.fromAnime(anime));
                    }
                }
                if (!cursor.isNull(cursor.getColumnIndex("series_manga"))) {
                    Manga manga = getManga(cursor.getInt(cursor.getColumnIndex("series_manga")), username);
                    if (manga != null) {
                        activity.setSeries(Series.fromManga(manga));
                    }
                }
                Cursor userCursor = getDBWrite().rawQuery("SELECT p.* FROM " + MALSqlHelper.TABLE_PROFILE + " p INNER JOIN " + MALSqlHelper.TABLE_ACTIVITIES_USERS +
                        " au ON p." + MALSqlHelper.COLUMN_ID + " = au.profile_id WHERE au.activity_id = ?", new String[]{String.valueOf(activity.getId())});
                if (userCursor.moveToFirst()) {
                    ArrayList<Profile> users = new ArrayList<>();
                    do {
                        Profile profile = Profile.fromCursor(userCursor);
                        users.add(profile);
                    } while (userCursor.moveToNext());
                    activity.setUsers(users);
                }
                userCursor.close();
                result.add(activity);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }
}

