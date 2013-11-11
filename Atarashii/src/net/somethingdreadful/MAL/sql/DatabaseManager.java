package net.somethingdreadful.MAL.sql;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.Manga;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseManager {

	static MALSqlHelper malSqlHelper;

	public DatabaseManager(Context context) {
		if (malSqlHelper == null) {
			malSqlHelper = MALSqlHelper.getHelper(context);
		}
	}

	public synchronized static SQLiteDatabase getDBWrite() {
		return malSqlHelper.getWritableDatabase();
	}

	public void saveAnimeList(ArrayList<Anime> list) {
		if ( list != null && list.size() > 0 ) {
			try {
				getDBWrite().beginTransaction();
				for(Anime anime: list)
					saveAnime(anime, true);
				getDBWrite().setTransactionSuccessful();
			} catch (Exception e) {
				Log.e("MALX", "error saving animelist to db: " + e.getMessage());
			} finally {
				getDBWrite().endTransaction();
			}
		}
	}

	public void saveAnime(Anime anime, boolean ignoreSynopsis) {
		ContentValues cv = new ContentValues();

		cv.put("recordID", anime.getId());
		cv.put("recordName", anime.getTitle());
		cv.put("recordType", anime.getType());
		cv.put("imageUrl", anime.getImageUrl());
		cv.put("recordStatus", anime.getStatus());
		cv.put("myStatus", anime.getWatchedStatus());
		cv.put("memberScore", anime.getMembersScore());
		cv.put("myScore", anime.getScore());
		cv.put("episodesWatched", anime.getWatchedEpisodes());
		cv.put("episodesTotal", anime.getEpisodes());
		cv.put("dirty", anime.getDirty());
		cv.put("lastUpdate", anime.getLastUpdate().toString());

		if (!ignoreSynopsis) {
			cv.put("synopsis", anime.getSynopsis());
		}

		getDBWrite().replace(MALSqlHelper.TABLE_ANIME, null, cv);
	}

	public void saveMangaList(ArrayList<Manga> list) {
		if ( list != null && list.size() > 0 ) {
			try {
				getDBWrite().beginTransaction();
				for(Manga manga: list)
					saveManga(manga, true);
				getDBWrite().setTransactionSuccessful();
			} catch (Exception e) {
				Log.e("MALX", "error saving mangalist to db: " + e.getMessage());
			} finally {
				getDBWrite().endTransaction();
			}
		}
	}

	public void saveManga(Manga manga, boolean ignoreSynopsis) {
		ContentValues cv = new ContentValues();

		cv.put("recordID", manga.getId());
		cv.put("recordName", manga.getTitle());
		cv.put("recordType", manga.getType());
		cv.put("imageUrl", manga.getImageUrl());
		cv.put("recordStatus", manga.getStatus());
		cv.put("myStatus", manga.getReadStatus());
		cv.put("memberScore", manga.getMembersScore());
		cv.put("myScore", manga.getScore());
		cv.put("volumesRead", manga.getVolumesRead());
		cv.put("chaptersRead", manga.getChaptersRead());
		cv.put("volumesTotal", manga.getVolumes());
		cv.put("chaptersTotal", manga.getChapters());
		cv.put("dirty", manga.getDirty());
		cv.put("lastUpdate", manga.getLastUpdate().toString());

		if (!ignoreSynopsis) {
			cv.put("synopsis", manga.getSynopsis());
		}

		getDBWrite().replace(MALSqlHelper.TABLE_MANGA, null, cv);
	}
}
