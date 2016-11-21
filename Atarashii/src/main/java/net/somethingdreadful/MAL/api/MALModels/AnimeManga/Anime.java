package net.somethingdreadful.MAL.api.MALModels.AnimeManga;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.MALModels.RecordStub;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;

public class Anime extends GenericRecord implements Serializable {

    /**
     * Total number of episodes of the anime.
     * <p/>
     * This value is the number of episodes of the anime, or null if unknown.
     */
    @Getter
    private int episodes;

    /**
     * Opening themes of the record
     */
    @Getter
    @SerializedName("opening_theme")
    private ArrayList<String> openingTheme;

    /**
     * Ending themes of the record
     */
    @Getter
    @SerializedName("ending_theme")
    private ArrayList<String> endingTheme;

    /**
     * The amount of minutes how long an episode lasts.
     */
    @Getter
    private int duration;

    /**
     * The next broadcast date.
     */
    @Getter
    private String broadcast;

    /**
     * Beginning date from which this anime was/will be air.
     */
    @Getter
    @SerializedName("start_date")
    private String startDate;

    /**
     * Airing end date for the anime
     */
    @Getter
    @SerializedName("end_date")
    private String endDate;

    /**
     * Rating of the anime
     * <p/>
     * The rating is a freeform text field with no defined values.
     */
    @Getter
    private String classification;

    /**
     * A list of producers for the anime
     */
    @Getter
    private ArrayList<String> producers;

    /**
     * A list of characters
     */
    @Getter
    @SerializedName("character_anime")
    private ArrayList<RecordStub> characterAnime;

    /**
     * A list of manga adaptations of this anime (or conversely, manga from which this anime is adapted).
     */
    @Getter
    @SerializedName("manga_adaptations")
    private ArrayList<RecordStub> mangaAdaptations;

    /**
     * A list of anime prequels of this anime.
     */
    @Getter
    private ArrayList<RecordStub> prequels;

    /**
     * A list of anime sequels of this anime.
     */
    @Getter
    private ArrayList<RecordStub> sequels;

    /**
     * A list of anime side stories of this anime.
     */
    @Getter
    @SerializedName("side_stories")
    private ArrayList<RecordStub> sideStories;

    /**
     * Parent story of this anime.
     */
    @Getter
    @SerializedName("parent_story")
    private RecordStub parentStory;

    /**
     * A list of spin-offs of this anime.
     */
    @Getter
    @SerializedName("spin_offs")
    private ArrayList<RecordStub> spinOffs;

    /**
     * A list of summaries of this anime.
     */
    @Getter
    private ArrayList<RecordStub> summaries;

    /**
     * A list of other related animes.
     */
    @Getter
    private ArrayList<RecordStub> other;

    /**
     * Personal watched status of the anime
     * <p/>
     * Defined string. Value will be one of watching, completed, on-hold, dropped, or plan to watch.
     */
    @Getter
    @SerializedName("watched_status")
    private String watchedStatus;

    /**
     * Number of episodes watched by the user
     */
    @Getter
    @SerializedName("watched_episodes")
    private int watchedEpisodes;

    /**
     * The date the user started watching the show
     */
    @Getter
    @SerializedName("watching_start")
    private String watchingStart;

    /**
     * The date the user finished watching the show
     */
    @Getter
    @SerializedName("watching_end")
    private String watchingEnd;


    /**
     * The fansub group the user used, if any
     */
    @Getter
    @SerializedName("fansub_group")
    private String fansubGroup;


    /**
     * The fansub group the user used, if any
     */
    @Getter
    @SerializedName("preview")
    private String preview;

    /**
     * Storage type for the series
     */
    @Getter
    private int storage;

    /**
     * The value for the storage chosen
     * <p/>
     * This number may either be the number of discs (for DVDs, VHS, etc) or size in GB for HD types
     */
    @Getter
    @SerializedName("storage_value")
    private float storageValue;

    /**
     * Set if the user is rewatching the anime
     */
    private boolean rewatching;

    /**
     * The number of times the user has re-watched the title. (Does not include the first time.)
     */
    @Getter
    @SerializedName("rewatch_count")
    private int rewatchCount;

    /**
     * How much value the user thinks there is in rewatching the series.
     */
    @Getter
    @SerializedName("rewatch_value")
    private int rewatchValue;

    /**
     * Lombok has a problem with boolean values.
     */
    private boolean getRewatching() {
        return rewatching;
    }

    public net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime model = new net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime();
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord.setFromCursor(true);
        createGeneralBaseModel(model);
        model.setAiring(new net.somethingdreadful.MAL.api.ALModels.AnimeManga.Anime.Airing());
        model.getAiring().setNextEpisode(getEpisodes() + 1);
        model.getAiring().setTime(getBroadcast());
        model.setDuration(getDuration());
        model.setEpisodes(getEpisodes());
        model.setStatus(getStatus());
        model.setStartDate(getStartDate());
        model.setEndDate(getEndDate());
        model.setCharacterAnime(getCharacterAnime());
        model.setClassification(getClassification());
        model.setProducers(getProducers());
        model.setMangaAdaptations(getMangaAdaptations());
        model.setPrequels(getPrequels());
        model.setSequels(getSequels());
        model.setSideStories(getSideStories());
        model.setParentStory(getParentStory());
        model.setSpinOffs(getSpinOffs());
        model.setSummaries(getSummaries());
        model.setOther(getOther());
        model.setWatchedStatus(getWatchedStatus());
        model.setWatchedEpisodes(getWatchedEpisodes());
        model.setWatchingStart(getWatchingStart());
        model.setWatchingEnd(getWatchingEnd());
        model.setStorage(getStorage());
        model.setStorageValue(getStorageValue());
        model.setRewatching(getRewatching());
        model.setRewatchCount(getRewatchCount());
        model.setRewatchValue(getRewatchValue());
        model.setOpeningTheme(getOpeningTheme());
        model.setEndingTheme(getEndingTheme());
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord.setFromCursor(false);
        return model;
    }
}
