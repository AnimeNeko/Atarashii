package net.somethingdreadful.MAL.api.response;

import android.database.Cursor;

import net.somethingdreadful.MAL.sql.MALSqlHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Manga extends GenericRecord implements Serializable {

    public static final String STATUS_READING = "reading";
    public static final String STATUS_PLANTOREAD = "plan to read";

    private int chapters;
    private int volumes;
    private String read_status;
    private int chapters_read;
    private int volumes_read;
    private int listed_manga_id;
    ArrayList<MangaRecordStub> alternative_versions;
    ArrayList<MangaRecordStub> related_manga;
    ArrayList<AnimeRecordStub> anime_adaptations;


    public static Manga fromCursor(Cursor c) {
        Manga result = new Manga();
        result.setCreatedFromCursor(true);
        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setId(c.getInt(columnNames.indexOf(MALSqlHelper.COLUMN_ID)));
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
        result.setDirty(c.getInt(columnNames.indexOf("dirty")) > 0);
        result.setMembersCount(c.getInt(columnNames.indexOf("membersCount")));
        result.setFavoritedCount(c.getInt(columnNames.indexOf("favoritedCount")));
        result.setPopularityRank(c.getInt(columnNames.indexOf("popularityRank")));
        result.setRank(c.getInt(columnNames.indexOf("rank")));
        result.setListedId(c.getInt(columnNames.indexOf("listedId")));
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

    public int getListedId() {
        return listed_manga_id;
    }

    public void setListedId(int listed_manga_id) {
        this.listed_manga_id = listed_manga_id;
    }

    public ArrayList<MangaRecordStub> getAlternativeVersions() {
        return alternative_versions;
    }

    public void setAlternativeVersions(ArrayList<MangaRecordStub> alternative_versions) {
        this.alternative_versions = alternative_versions;
    }

    public ArrayList<MangaRecordStub> getRelatedManga() {
        return related_manga;
    }

    public void setRelatedManga(ArrayList<MangaRecordStub> related_manga) {
        this.related_manga = related_manga;
    }

    public ArrayList<AnimeRecordStub> getAnimeAdaptations() {
        return anime_adaptations;
    }

    public void setAnimeAdaptations(ArrayList<AnimeRecordStub> anime_adaptations) {
        this.anime_adaptations = anime_adaptations;
    }

    public int getTypeInt() {
        String[] types = {
                "Manga",
                "Novel",
                "One Shot",
                "Doujin",
                "Manwha",
                "Manhua",
                "OEL"
        };
        return Arrays.asList(types).indexOf(getType());
    }

    public int getStatusInt() {
        String[] status = {
                "finished",
                "publishing",
                "not yet published"
        };
        return Arrays.asList(status).indexOf(getStatus());
    }

    public int getReadStatusInt() {
        return getUserStatusInt(getReadStatus());
    }

    public int getProgress(boolean useSecondaryAmount) {
        return useSecondaryAmount ? getVolumesRead() : getChaptersRead();
    }

    public void setProgress(boolean useSecondaryAmount, int progress) {
        if (useSecondaryAmount)
            setVolumesRead(progress);
        else
            setChaptersRead(progress);
    }

    public int getTotal(boolean useSecondaryAmount) {
        return useSecondaryAmount ? getVolumes() : getChapters();
    }
}
