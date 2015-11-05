package net.somethingdreadful.MAL.api.MALModels.AnimeManga;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.MALModels.RecordStub;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Anime extends GenericRecord implements Serializable {

    /**
     * Total number of episodes of the anime.
     * <p/>
     * This value is the number of episodes of the anime, or null if unknown.
     */
    @Setter
    @Getter
    private int episodes;

    /**
     * Beginning date from which this anime was/will be air.
     */
    @Setter
    @Getter
    @SerializedName("start_date")
    private String startDate;

    /**
     * Airing end date for the anime
     */
    @Setter
    @Getter
    @SerializedName("end_date")
    private String endDate;

    /**
     * Rating of the anime
     * <p/>
     * The rating is a freeform text field with no defined values.
     */
    @Setter
    @Getter
    private String classification;

    /**
     * A list of producers for the anime
     */
    @Setter
    @Getter
    private ArrayList<String> producers;

    /**
     * A list of characters
     */
    @Setter
    @Getter
    @SerializedName("character_anime")
    private ArrayList<RecordStub> characterAnime;

    /**
     * A list of manga adaptations of this anime (or conversely, manga from which this anime is adapted).
     */
    @Setter
    @Getter
    @SerializedName("manga_adaptions")
    private ArrayList<RecordStub> mangaAdaptations;

    /**
     * A list of anime prequels of this anime.
     */
    @Setter
    @Getter
    private ArrayList<RecordStub> prequels;

    /**
     * A list of anime sequels of this anime.
     */
    @Setter
    @Getter
    private ArrayList<RecordStub> sequels;

    /**
     * A list of anime side stories of this anime.
     */
    @Setter
    @Getter
    @SerializedName("side_stories")
    private ArrayList<RecordStub> sideStories;

    /**
     * Parent story of this anime.
     */
    @Setter
    @Getter
    @SerializedName("parent_story")
    private RecordStub parentStory;

    /**
     * A list of spin-offs of this anime.
     */
    @Setter
    @Getter
    @SerializedName("spin_offs")
    private ArrayList<RecordStub> spinOffs;

    /**
     * A list of summaries of this anime.
     */
    @Setter
    @Getter
    private ArrayList<RecordStub> summaries;

    /**
     * A list of other related animes.
     */
    @Setter
    @Getter
    private ArrayList<RecordStub> other;

    /**
     * Personal watched status of the anime
     * <p/>
     * Defined string. Value will be one of watching, completed, on-hold, dropped, or plan to watch.
     */
    @Getter
    @Setter
    @SerializedName("watched_status")
    private String watchedStatus;

    /**
     * Number of episodes watched by the user
     */
    @Getter
    @Setter
    @SerializedName("watched_episodes")
    private int watchedEpisodes;

    /**
     * The date the user started watching the show
     */
    @Getter
    @Setter
    @SerializedName("watching_start")
    private String watchingStart;

    /**
     * The date the user finished watching the show
     */
    @Getter
    @Setter
    @SerializedName("watching_end")
    private String watchingEnd;


    /**
     * The fansub group the user used, if any
     */
    @Getter
    @Setter
    @SerializedName("fansub_group")
    private String fansubGroup;

    /**
     * Storage type for the series
     */
    @Setter
    @Getter
    private int storage;

    /**
     * The value for the storage chosen
     * <p/>
     * This number may either be the number of discs (for DVDs, VHS, etc) or size in GB for HD types
     */
    @Getter
    @Setter
    @SerializedName("storage_value")
    private float storageValue;

    /**
     * The number of episodes downloaded by the user
     */
    @Getter
    @Setter
    @SerializedName("eps_downloaded")
    private int epsDownloaded;

    /**
     * Set if the user is rewatching the anime
     */
    @Setter
    private boolean rewatching;

    /**
     * The number of times the user has re-watched the title. (Does not include the first time.)
     */
    @Getter
    @Setter
    @SerializedName("rewatch_count")
    private int rewatchCount;

    /**
     * How much value the user thinks there is in rewatching the series.
     */
    @Getter
    @Setter
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
        model.setFansubGroup(getFansubGroup());
        model.setStorage(getStorage());
        model.setStorageValue(getStorageValue());
        model.setEpsDownloaded(getEpsDownloaded());
        model.setRewatching(getRewatching());
        model.setRewatchCount(getRewatchCount());
        model.setRewatchValue(getRewatchValue());
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord.setFromCursor(false);
        return model;
    }
}
