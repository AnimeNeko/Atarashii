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

public class Anime extends GenericRecord implements Serializable {

    public static final String STATUS_WATCHING = "watching";
    public static final String STATUS_PLANTOWATCH = "plan to watch";

    // MyAnimeList
    @Setter @Getter private int episodes;
    @Setter @Getter @SerializedName("watched_status") private String watchedStatus;
    @Setter @Getter @SerializedName("watched_episodes") private int watchedEpisodes;
    @Setter @Getter private String classification;
    @Setter @Getter @SerializedName("listed_anime_id") private int listedId;
    /*private Date start_date;
    private Date end_date;*/
    @Setter @Getter @SerializedName("watching_start") private String watchingStart;
    @Setter @Getter @SerializedName("watching_end") private String watchingEnd;
    @Setter @Getter @SerializedName("alternative_versions") private ArrayList<RecordStub> alternativeVersions;
    @Setter @Getter @SerializedName("character_anime") private ArrayList<RecordStub> characterAnime;
    @Setter @Getter private ArrayList<RecordStub> prequels;
    @Setter @Getter private ArrayList<RecordStub> sequels;
    @Setter @Getter @SerializedName("side_stories") private ArrayList<RecordStub> sideStories;
    @Setter @Getter private ArrayList<RecordStub> summaries;
    @Setter @Getter @SerializedName("spin_offs") private ArrayList<RecordStub> spinOffs;
    @Setter @Getter @SerializedName("manga_adaptations") private ArrayList<RecordStub> mangaAdaptions;
    @Setter @Getter @SerializedName("parent_story") private RecordStub parentStory;

    // AniList
    public Anime anime;
    private String list_status;
    private String airing_status;
    private Float average_score;
    private int total_episodes;
    private String image_url_lge;
    private String title_romaji;
    private String title_japanese;
    private String title_english;

    private int episodes_watched;
    private String description;
    @Getter private Airing airing;
    private ArrayList<String> synonyms;

    public class Airing {
        @Getter private String time;
        @Getter private int countdown;
        @Getter private int next_episode;
    }

    public Anime createBaseModel() {
        if (anime != null) {
            setId(anime.getId());
            setTitle(anime.title_romaji);
            setImageUrl(anime.image_url_lge);
            setStatus(anime.airing_status);
            setMembersScore(anime.average_score);
            setEpisodes(anime.total_episodes);
        } else {
            setTitle(title_romaji);
            setImageUrl(image_url_lge);
            setStatus(airing_status);
            if (average_score != null)
                setMembersScore(average_score);
            setEpisodes(total_episodes);
        }
        setWatchedEpisodes(episodes_watched);
        setWatchedStatus(list_status);
        setSynopsis(description);
        return this;
    }

    public static Anime fromCursor(Cursor c) {
        Anime result = new Anime();
        result.setFromCursor(true);
        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setId(c.getInt(columnNames.indexOf(MALSqlHelper.COLUMN_ID)));
        result.setTitle(c.getString(columnNames.indexOf("recordName")));
        result.setType(c.getString(columnNames.indexOf("recordType")));
        result.setStatus(c.getString(columnNames.indexOf("recordStatus")));
        result.setWatchedStatus(c.getString(columnNames.indexOf("myStatus")));
        result.setWatchedEpisodes(c.getInt(columnNames.indexOf("episodesWatched")));
        result.setEpisodes(c.getInt(columnNames.indexOf("episodesTotal")));
        result.setWatchingStart(c.getString(columnNames.indexOf("watchedStart")));
        result.setWatchingEnd(c.getString(columnNames.indexOf("watchedEnd")));
        result.setMembersScore(c.getFloat(columnNames.indexOf("memberScore")));
        result.setScore(c.getInt(columnNames.indexOf("myScore")));
        result.setSynopsis(c.getString(columnNames.indexOf("synopsis")));
        result.setImageUrl(c.getString(columnNames.indexOf("imageUrl")));
        result.setDirty(c.getInt(columnNames.indexOf("dirty")) > 0);
        result.setClassification(c.getString(columnNames.indexOf("classification")));
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

    public Integer getClassificationInt() {
        String[] classification = {
                "G - All Ages",
                "PG - Children",
                "PG-13 - Teens 13 or older",
                "R - 17+ (violence \u0026 profanity)",
                "R+ - Mild Nudity",
                "Rx - Hentai"
        };
        return Arrays.asList(classification).indexOf(getClassification());
    }

    public int getTypeInt() {
        String[] types = {
                "TV",
                "Movie",
                "OVA",
                "ONA",
                "Special",
                "Music"
        };
        return Arrays.asList(types).indexOf(getType());
    }

    public int getStatusInt() {
        String[] status = {
                "finished airing",
                "currently airing",
                "not yet aired"
        };
        return Arrays.asList(status).indexOf(getStatus());
    }

    public int getWatchedStatusInt() {
        return getUserStatusInt(getWatchedStatus());
    }
}
