package net.somethingdreadful.MAL.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.somethingdreadful.MAL.MALDateTools;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.User;

import java.util.ArrayList;

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
                Log.e("MALX", "error saving animelist to db");
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
        if (userId != null)
            saveAnime(anime, IGF, userId);
    }

    public void saveAnime(Anime anime, boolean IGF, int userId) {
        ContentValues cv = new ContentValues();

        cv.put(MALSqlHelper.COLUMN_ID, anime.getId());
        cv.put("recordName", anime.getTitle());
        cv.put("recordType", anime.getType());
        cv.put("imageUrl", anime.getImageUrl());
        cv.put("recordStatus", anime.getStatus());
        cv.put("episodesTotal", anime.getEpisodes());
        if (!IGF) {
            cv.put("synopsis", anime.getSynopsis());
            cv.put("memberScore", anime.getMembersScore());
        }

        // don't use replace it replaces synopsis with null even when we don't put it in the ContentValues
        int updateResult = getDBWrite().update(MALSqlHelper.TABLE_ANIME, cv, MALSqlHelper.COLUMN_ID + " = ?", new String[]{Integer.toString(anime.getId())});
        if (updateResult == 0) {
            getDBWrite().insert(MALSqlHelper.TABLE_ANIME, null, cv);
        }

        // update animelist if user id is provided
        if (userId > 0) {
            ContentValues alcv = new ContentValues();
            alcv.put("profile_id", userId);
            alcv.put("anime_id", anime.getId());
            alcv.put("status", anime.getWatchedStatus());
            alcv.put("score", anime.getScore());
            alcv.put("watched", anime.getWatchedEpisodes());
            alcv.put("dirty", anime.getDirty());
            if (anime.getLastUpdate() != null)
                alcv.put("lastUpdate", anime.getLastUpdate().getTime());
            getDBWrite().replace(MALSqlHelper.TABLE_ANIMELIST, null, alcv);
        }
    }

    public Anime getAnime(int id) {
        Anime result = null;
        Cursor cursor = getDBRead().query(MALSqlHelper.TABLE_ANIME, null, MALSqlHelper.COLUMN_ID + " = ?", new String[]{Integer.toString(id)}, null, null, null);
        if (cursor.moveToFirst())
            result = Anime.fromCursor(cursor);
        cursor.close();
        return result;
    }

    public boolean deleteAnime(int id) {
        return getDBWrite().delete(MALSqlHelper.TABLE_ANIME, MALSqlHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)}) == 1;
    }

    public boolean deleteAnimeFromAnimelist(int id, String username) {
        boolean result = false;
        Integer userId = getUserId(username);
        if (userId != null)
            result = getDBWrite().delete(MALSqlHelper.TABLE_ANIMELIST, "profile_id = ? AND anime_id = ?", new String[]{userId.toString(), String.valueOf(id)}) == 1;
        return result;
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
            if (listType != "") {
                selArgs.add(listType);
            }
            cursor = getDBRead().rawQuery("SELECT a.*, al.score AS myScore, al.status AS myStatus, al.watched AS episodesWatched, al.dirty, al.lastUpdate" +
                    " FROM animelist al INNER JOIN anime a ON al.anime_id = a." + MALSqlHelper.COLUMN_ID +
                    " WHERE al.profile_id = ? " + (listType != "" ? " AND al.status = ? " : "") + (dirtyOnly ? " AND al.dirty = 1 " : "") + " ORDER BY a.recordName COLLATE NOCASE", selArgs.toArray(new String[selArgs.size()]));
            if (cursor.moveToFirst()) {
                result = new ArrayList<Anime>();
                do {
                    result.add(Anime.fromCursor(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLException e) {
            Log.e("MALX", "DatabaseManager.getAnimeList exception: " + e.getMessage());
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
                Log.e("MALX", "error saving mangalist to db: " + e.getMessage());
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
        if (userId != null)
            saveManga(manga, ignoreSynopsis, userId);
    }

    public void saveManga(Manga manga, boolean ignoreSynopsis, int userId) {
        ContentValues cv = new ContentValues();

        cv.put(MALSqlHelper.COLUMN_ID, manga.getId());
        cv.put("recordName", manga.getTitle());
        cv.put("recordType", manga.getType());
        cv.put("imageUrl", manga.getImageUrl());
        cv.put("recordStatus", manga.getStatus());
        cv.put("memberScore", manga.getMembersScore());
        cv.put("volumesTotal", manga.getVolumes());
        cv.put("chaptersTotal", manga.getChapters());

        if (!ignoreSynopsis) {
            cv.put("synopsis", manga.getSynopsis());
        }

        // don't use replace it replaces synopsis with null even when we don't put it in the ContentValues
        int updateResult = getDBWrite().update(MALSqlHelper.TABLE_MANGA, cv, MALSqlHelper.COLUMN_ID + " = ?", new String[]{Integer.toString(manga.getId())});
        if (updateResult == 0) {
            getDBWrite().insert(MALSqlHelper.TABLE_MANGA, null, cv);
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
            mlcv.put("dirty", manga.getDirty());
            if (manga.getLastUpdate() != null)
                mlcv.put("lastUpdate", manga.getLastUpdate().getTime());
            getDBWrite().replace(MALSqlHelper.TABLE_MANGALIST, null, mlcv);
        }
    }

    public Manga getManga(int id) {
        Manga result = null;
        Cursor cursor = getDBRead().query(MALSqlHelper.TABLE_MANGA, null, MALSqlHelper.COLUMN_ID + " = ?", new String[]{Integer.toString(id)}, null, null, null);
        if (cursor.moveToFirst())
            result = Manga.fromCursor(cursor);
        cursor.close();
        return result;
    }

    public boolean deleteManga(int id) {
        return getDBWrite().delete(MALSqlHelper.TABLE_MANGA, MALSqlHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)}) == 1;
    }

    public boolean deleteMangaFromMangalist(int id, String username) {
        boolean result = false;
        Integer userId = getUserId(username);
        if (userId != null)
            result = getDBWrite().delete(MALSqlHelper.TABLE_MANGALIST, "profile_id = ? AND manga_id = ?", new String[]{userId.toString(), String.valueOf(id)}) == 1;
        return result;
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
            cursor = getDBRead().rawQuery("SELECT m.*, ml.score AS myScore, ml.status AS myStatus, ml.chaptersRead, ml.volumesRead, ml.dirty, ml.lastUpdate" +
                    " FROM mangalist ml INNER JOIN manga m ON ml.manga_id = m." + MALSqlHelper.COLUMN_ID +
                    " WHERE ml.profile_id = ? " + (listType != "" ? " AND ml.status = ? " : "") + (dirtyOnly ? " AND ml.dirty = 1 " : "") + " ORDER BY m.recordName COLLATE NOCASE", selArgs.toArray(new String[selArgs.size()]));
            if (cursor.moveToFirst()) {
                result = new ArrayList<Manga>();
                do {
                    result.add(Manga.fromCursor(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLException e) {
            Log.e("MALX", "DatabaseManager.getMangaList exception: " + e.getMessage());
        }

        return result;
    }

    public void saveUser(User user, Boolean profile) {
        ContentValues cv = new ContentValues();

        cv.put("username", user.getName());
        if (user.getProfile().getAvatarUrl().equals("http://cdn.myanimelist.net/images/questionmark_50.gif"))
            cv.put("avatar_url", "http://cdn.myanimelist.net/images/na.gif");
        else
            cv.put("avatar_url", user.getProfile().getAvatarUrl());
        if (user.getProfile().getDetails().getLastOnline() != null) {
            String lastOnline = MALDateTools.parseMALDateToISO8601String(user.getProfile().getDetails().getLastOnline());
            cv.put("last_online", lastOnline.equals("") ? user.getProfile().getDetails().getLastOnline() : lastOnline);
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
            Log.e("MALX", "DatabaseManager.getProfile exception: " + e.getMessage());
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

    private Integer getUserId(String username) {
        Integer result = null;
        Cursor cursor = getDBRead().query(MALSqlHelper.TABLE_PROFILE, new String[]{MALSqlHelper.COLUMN_ID}, "username = ?", new String[]{username}, null, null, null);
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }
        cursor.close();

        if (result == null) { // the users profile does not exist until now, so add it as simple dummy (will get all data once the user clicks on his profile)
            ContentValues cv = new ContentValues();
            cv.put("username", username);
            Long userAddResult = getDBWrite().insert(MALSqlHelper.TABLE_PROFILE, null, cv);
            if (userAddResult > -1) {
                result = userAddResult.intValue();
            }
        }
        return result;
    }

    public void saveFriendList(ArrayList<User> friendlist, String username) {
        for (User friend : friendlist) {
            saveUser(friend, false);
        }

        Integer userId = getUserId(username);
        saveUserFriends(userId, friendlist);
    }
}

