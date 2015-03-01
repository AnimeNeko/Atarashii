package net.somethingdreadful.MAL.api.response;

import android.database.Cursor;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.sql.MALSqlHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class Manga extends GenericRecord implements Serializable {

    public static final String STATUS_READING = "reading";
    public static final String STATUS_PLANTOREAD = "plan to read";

    @Setter @Getter @SerializedName("alternative_versions") ArrayList<RecordStub> alternativeVersions;
    @Setter @Getter @SerializedName("related_manga") ArrayList<RecordStub> relatedManga;
    @Setter @Getter @SerializedName("anime_adaptations") ArrayList<RecordStub> animeAdaptations;
    @Setter @Getter private int chapters;
    @Setter @Getter private int volumes;
    @Setter @Getter @SerializedName("read_status") private String readStatus;
    @Setter @Getter @SerializedName("chapters_read") private int chaptersRead;
    @Setter @Getter @SerializedName("volumes_read") private int volumesRead;
    @Setter @Getter @SerializedName("listed_manga_id") private int listedId;
    @Setter @Getter @SerializedName("reading_start") private String readingStart;
    @Setter @Getter @SerializedName("reading_end") private String readingEnd;

    // AniList
    public Manga manga;
    private String list_status;
    private String publishing_status;
    private Float average_score;
    private int total_chapters;
    private int total_volumes;
    private String image_url_lge;
    private String title_romaji;
    private String title_japanese;
    private String title_english;

    private ArrayList<String> synonyms;

    public Manga createBaseModel() {
        if (manga != null) {
            setId(manga.getId());
            setTitle(manga.title_romaji);
            setImageUrl(manga.image_url_lge);
            setStatus(manga.publishing_status);
            setMembersScore(manga.average_score);
            setVolumes(manga.total_volumes);
            setChapters(manga.total_chapters);
        } else {
            setTitle(title_romaji);
            setImageUrl(image_url_lge);
            setStatus(publishing_status);
            if (average_score != null)
                setMembersScore(average_score);
            setVolumes(total_volumes);
            setChapters(total_chapters);
        }
        setReadStatus(list_status);
        return this;
    }

    public static Manga fromCursor(Cursor c) {
        Manga result = new Manga();
        result.setFromCursor(true);
        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setId(c.getInt(columnNames.indexOf(MALSqlHelper.COLUMN_ID)));
        result.setTitle(c.getString(columnNames.indexOf("recordName")));
        result.setType(c.getString(columnNames.indexOf("recordType")));
        result.setStatus(c.getString(columnNames.indexOf("recordStatus")));
        result.setReadStatus(c.getString(columnNames.indexOf("myStatus")));
        result.setVolumesRead(c.getInt(columnNames.indexOf("volumesRead")));
        result.setChaptersRead(c.getInt(columnNames.indexOf("chaptersRead")));
        result.setReadingStart(c.getString(columnNames.indexOf("readStart")));
        result.setReadingEnd(c.getString(columnNames.indexOf("readEnd")));
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
