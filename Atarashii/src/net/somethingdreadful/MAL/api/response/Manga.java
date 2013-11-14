package net.somethingdreadful.MAL.api.response;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.database.Cursor;

public class Manga extends GenericRecord {

	public static final String STATUS_READING = "reading";
	public static final String STATUS_PLANTOREAD = "plan to read";

	int chapters;
	int volumes;
	String read_status;
	int chapters_read;
	int volumes_read;

	public int getChapters() {
		return chapters;
	}
	public void setChapters(int chapters) {
		this.chapters = chapters;
	}
	public int getVolumes() {
		return volumes;
	}
	public void setVolumes(int volumes) {
		this.volumes = volumes;
	}
	public String getReadStatus() {
		return read_status;
	}
	public void setReadStatus(String read_status) {
		this.read_status = read_status;
	}
	public int getChaptersRead() {
		return chapters_read;
	}
	public void setChaptersRead(int chapters_read) {
		this.chapters_read = chapters_read;
	}
	public int getVolumesRead() {
		return volumes_read;
	}
	public void setVolumesRead(int volumes_read) {
		this.volumes_read = volumes_read;
	}
	
	public static Manga fromCursor(Cursor c) {
		Manga result = new Manga();
		List<String> columnNames = Arrays.asList(c.getColumnNames());
		result.setId(c.getInt(columnNames.indexOf("recordID")));
		result.setTitle(c.getString(columnNames.indexOf("recordName")));
		result.setType(c.getString(columnNames.indexOf("recordType")));
		result.setStatus(c.getString(columnNames.indexOf("recordStatus")));
		result.setReadStatus(c.getString(columnNames.indexOf("myStatus")));
		result.setVolumesRead(c.getInt(columnNames.indexOf("volumesRead")));
		result.setChaptersRead(c.getInt(columnNames.indexOf("chaptersRead")));
		result.setVolumes(c.getInt(columnNames.indexOf("volumesTotal")));
		result.setChapters(c.getInt(columnNames.indexOf("chaptersTotal")));
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
	
	public int getProgress(boolean useSecondaryAmount) {
		return useSecondaryAmount ? getVolumesRead() : getChaptersRead();
	}
	
	public void setProgress(boolean useSecondaryAmount, int progress) {
		if ( useSecondaryAmount )
			setVolumesRead(progress);
		else
			setChaptersRead(progress);
	}
	
	public int getTotal(boolean useSecondaryAmount) {
		return useSecondaryAmount ? getVolumes() : getChapters();
	}
}
