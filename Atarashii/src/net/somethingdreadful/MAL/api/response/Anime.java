package net.somethingdreadful.MAL.api.response;

import android.database.Cursor;
import android.text.TextUtils;

import com.google.gson.Gson;
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
    public static final String STATUS_REWATCHING = "rewatching";
    public static final String STATUS_PLANTOWATCH = "plan to watch";

    // MyAnimeList
    @Setter @Getter private int episodes;
    @Getter @SerializedName("watched_status") private String watchedStatus;
    @Getter @SerializedName("watched_episodes") private int watchedEpisodes;
    @Setter @Getter private String classification;
    @Setter @Getter @SerializedName("listed_anime_id") private int listedId;
    @Getter @SerializedName("fansub_group") private String fansubGroup;
    @Setter @Getter private ArrayList<String> producers;
    @Getter @SerializedName("eps_downloaded") private int epsDownloaded;
    @Getter @SerializedName("rewatch_count") private int rewatchCount;
    @Getter @SerializedName("rewatch_value") private int rewatchValue;
    @Setter @Getter @SerializedName("start_date") private String startDate;
    @Setter @Getter @SerializedName("end_date") private String endDate;
    @Getter @SerializedName("watching_start") private String watchingStart;
    @Getter @SerializedName("watching_end") private String watchingEnd;
    @Setter private boolean rewatching;
    @Getter @SerializedName("storage_value") private int storageValue;
    @Getter private int storage;
    @Setter @Getter @SerializedName("alternative_versions") private ArrayList<RecordStub> alternativeVersions;
    @Setter @Getter @SerializedName("character_anime") private ArrayList<RecordStub> characterAnime;
    @Setter @Getter private ArrayList<RecordStub> prequels;
    @Setter @Getter private ArrayList<RecordStub> sequels;
    @Setter @Getter @SerializedName("side_stories") private ArrayList<RecordStub> sideStories;
    @Setter @Getter private ArrayList<RecordStub> summaries;
    @Setter @Getter @SerializedName("spin_offs") private ArrayList<RecordStub> spinOffs;
    @Setter @Getter @SerializedName("manga_adaptations") private ArrayList<RecordStub> mangaAdaptions;
    @Setter @Getter @SerializedName("parent_story") private RecordStub parentStory;
    @Setter @Getter private ArrayList<RecordStub> other;

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
    private String notes;

    public class Airing {
        @Getter private String time;
        @Getter private int countdown;
        @Getter private int next_episode;
    }

    public Anime createBaseModel() {
        if (anime != null) { // animelist
            setId(anime.getId());
            setTitle(anime.title_romaji);
            setImageUrl(anime.image_url_lge);
            setStatus(anime.airing_status);
            setMembersScore(anime.average_score);
            setEpisodes(anime.total_episodes);
            setWatchedEpisodes(episodes_watched, false);
            setWatchedStatus(list_status, false);
            setPersonalComments(notes, false);
        } else { // anime details
            setTitle(title_romaji);
            setImageUrl(image_url_lge);
            setStatus(airing_status);
            if (average_score != null)
                setMembersScore(average_score);
            setEpisodes(total_episodes);
            setSynopsis(description);
        }
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
        result.setWatchedStatus(c.getString(columnNames.indexOf("myStatus")), false);
        result.setWatchedEpisodes(c.getInt(columnNames.indexOf("episodesWatched")), false);
        result.setEpisodes(c.getInt(columnNames.indexOf("episodesTotal")));
        result.setWatchingStart(c.getString(columnNames.indexOf("watchedStart")), false);
        result.setStorage(c.getInt(columnNames.indexOf("storage")), false);
        result.setStorageValue(c.getInt(columnNames.indexOf("storageValue")), false);
        result.setWatchingEnd(c.getString(columnNames.indexOf("watchedEnd")), false);
        result.setMembersScore(c.getFloat(columnNames.indexOf("memberScore")));
        result.setScore(c.getInt(columnNames.indexOf("myScore")), false);
        result.setSynopsis(c.getString(columnNames.indexOf("synopsis")));
        result.setImageUrl(c.getString(columnNames.indexOf("imageUrl")));
        if (!c.isNull(columnNames.indexOf("dirty"))) {
            result.setDirty(new Gson().fromJson(c.getString(columnNames.indexOf("dirty")), ArrayList.class));
        } else {
            result.setDirty(null);
        }
        result.setClassification(c.getString(columnNames.indexOf("classification")));
        result.setMembersCount(c.getInt(columnNames.indexOf("membersCount")));
        result.setFavoritedCount(c.getInt(columnNames.indexOf("favoritedCount")));
        result.setPopularityRank(c.getInt(columnNames.indexOf("popularityRank")));
        result.setWatchingStart(c.getString(columnNames.indexOf("watchedStart")), false);
        result.setWatchingEnd(c.getString(columnNames.indexOf("watchedEnd")), false);
        result.setFansubGroup(c.getString(columnNames.indexOf("fansub")), false);
        result.setPriority(c.getInt(columnNames.indexOf("priority")), false);
        result.setEpsDownloaded(c.getInt(columnNames.indexOf("downloaded")), false);
        result.setRewatchCount(c.getInt(columnNames.indexOf("rewatchCount")), false);
        result.setRewatchValue(c.getInt(columnNames.indexOf("rewatchValue")), false);
        result.setRewatching(c.getInt(columnNames.indexOf("rewatch")) > 0);
        result.setPersonalComments(c.getString(columnNames.indexOf("comments")), false);
        result.setStartDate(c.getString(columnNames.indexOf("startDate")));
        result.setEndDate(c.getString(columnNames.indexOf("endDate")));
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

    public boolean getRewatching() {
        return rewatching;
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

    public String getProducersString() {
        return getProducers() != null ? TextUtils.join(", ", getProducers()) : "";
    }

    public void setWatchedStatus(String status, boolean markDirty) {
        this.watchedStatus = status;
        if (markDirty) {
            addDirtyField("watchedStatus");
        }
    }

    public void setWatchedStatus(String status) {
        setWatchedStatus(status, true);
    }

    public void setWatchedEpisodes(int episodes, boolean markDirty) {
        this.watchedEpisodes = episodes;
        if (markDirty) {
            addDirtyField("watchedEpisodes");
        }
    }

    public void setWatchedEpisodes(int episodes) {
        setWatchedEpisodes(episodes, true);
    }

    public void setFansubGroup(String group, boolean markDirty) {
        this.fansubGroup = group;
        if (markDirty) {
            addDirtyField("fansubGroup");
        }
    }

    public void setFansubGroup(String group) {
        setFansubGroup(group, true);
    }

    public void setEpsDownloaded(int episodes, boolean markDirty) {
        this.epsDownloaded = episodes;
        if (markDirty) {
            addDirtyField("epsDownloaded");
        }
    }

    public void setEpsDownloaded(int episodes) {
        setEpsDownloaded(episodes, true);
    }

    public void setRewatchCount(int count, boolean markDirty) {
        this.rewatchCount = count;
        if (markDirty) {
            addDirtyField("rewatchCount");
        }
    }

    public void setRewatchCount(int count) {
        setRewatchCount(count, true);
    }

    public void setRewatchValue(int value, boolean markDirty) {
        this.rewatchValue = value;
        if (markDirty) {
            addDirtyField("rewatchValue");
        }
    }

    public void setRewatchValue(int value) {
        setRewatchValue(value, true);
    }

    public void setWatchingStart(String start, boolean markDirty) {
        this.watchingStart = start;
        if (markDirty) {
            addDirtyField("watchingStart");
        }
    }

    public void setWatchingStart(String start) {
        setWatchingStart(start, true);
    }

    public void setWatchingEnd(String end, boolean markDirty) {
        this.watchingEnd = end;
        if (markDirty) {
            addDirtyField("watchingEnd");
        }
    }

    public void setWatchingEnd(String end) {
        setWatchingEnd(end, true);
    }

    public void setStorageValue(int value, boolean markDirty) {
        this.storageValue = value;
        if (markDirty) {
            addDirtyField("storageValue");
        }
    }

    public void setStorageValue(int value) {
        setStorageValue(value, true);
    }

    public void setStorage(int storage, boolean markDirty) {
        this.storage = storage;
        if (markDirty) {
            addDirtyField("storage");
        }
    }

    public void setStorage(int storage) {
        setStorage(storage, true);
    }
}
