package net.somethingdreadful.MAL.api.response;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.database.Cursor;

public class Anime extends GenericRecord {
	
	public static final String STATUS_WATCHING = "watching";
	public static final String STATUS_PLANTOWATCH = "plan to watch";
	
	int episodes;
	String watched_status;
	int watched_episodes;

	public int getEpisodes() {
		return episodes;
	}
	public void setEpisodes(int episodes) {
		this.episodes = episodes;
	}
	public String getWatchedStatus() {
		return watched_status;
	}
	public void setWatchedStatus(String watched_status) {
		this.watched_status = watched_status;
	}
	public int getWatchedEpisodes() {
		return watched_episodes;
	}
	public void setWatchedEpisodes(int watched_episodes) {
		this.watched_episodes = watched_episodes;
	}
	
	public static Anime fromCursor(Cursor c) {
		Anime result = new Anime();
		List<String> columnNames = Arrays.asList(c.getColumnNames());
		result.setId(c.getInt(columnNames.indexOf("recordID")));
		result.setTitle(c.getString(columnNames.indexOf("recordName")));
		result.setType(c.getString(columnNames.indexOf("recordType")));
		result.setStatus(c.getString(columnNames.indexOf("recordStatus")));
		result.setWatchedStatus(c.getString(columnNames.indexOf("myStatus")));
		result.setWatchedEpisodes(c.getInt(columnNames.indexOf("episodesWatched")));
		result.setEpisodes(c.getInt(columnNames.indexOf("episodesTotal")));
		result.setMembersScore(c.getFloat(columnNames.indexOf("memberScore")));
		result.setScore(c.getInt(columnNames.indexOf("myScore")));
		result.setSynopsis(c.getString(columnNames.indexOf("synopsis")));
		result.setImageUrl(c.getString(columnNames.indexOf("imageUrl")));
		result.setDirty(c.getInt(columnNames.indexOf("dirty"))>0);
		Date lastUpdateDate;
		try {
			long lastUpdate = c.getLong(columnNames.indexOf("lastUpdate"));
			lastUpdateDate = new Date(lastUpdate);
		} catch (Exception e) { // database entry was null
			lastUpdateDate = null;
		}
		result.setLastUpdate(lastUpdateDate);
		return result;
	}
}
