package net.somethingdreadful.MAL.api.MALModels.AnimeManga;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.MALModels.RecordStub;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Manga extends GenericRecord implements Serializable {

    /**
     * Total number of chapters of the manga.
     * <p/>
     * This value is the number of chapters of the anime, or null if unknown.
     */
    @Setter
    @Getter
    private int chapters;

    /**
     * Total number of volumes of the manga.
     * <p/>
     * This value is the number of volumes of the manga, or null if unknown.
     */
    @Setter
    @Getter
    private int volumes;

    /**
     * A list of anime adaptations of this manga (or conversely, anime from which this manga is adapted)
     */
    @Setter
    @Getter
    @SerializedName("anime_adaptations")
    private ArrayList<RecordStub> animeAdaptations;

    /**
     * A list of related manga
     */
    @Setter
    @Getter
    @SerializedName("related_manga")
    private ArrayList<RecordStub> relatedManga;

    /**
     * User's read status of the manga
     * <p/>
     * This is a string that is one of: reading, completed, on-hold, dropped, plan to read
     */
    @Setter
    @Getter
    @SerializedName("read_status")
    private String readStatus;

    /**
     * Number of chapters already read by the user
     */
    @Setter
    @Getter
    @SerializedName("chapters_read")
    private int chaptersRead;

    /**
     * Number of volumes already read by the user.
     */
    @Setter
    @Getter
    @SerializedName("volumes_read")
    private int volumesRead;

    /**
     * Tags assigned by the user
     */
    @Setter
    @Getter
    @SerializedName("personal_tags")
    private ArrayList<String> personalTags;

    /**
     * The date the user started reading the title
     */
    @Setter
    @Getter
    @SerializedName("reading_start")
    private String readingStart;

    /**
     * The date the user finished reading the title
     */
    @Setter
    @Getter
    @SerializedName("reading_end")
    private String readingEnd;

    /**
     * The number of chapters downloaded by the user
     */
    @Setter
    @Getter
    @SerializedName("chap_downloaded")
    private int chapDownloaded;

    /**
     * Set if the user is rerereading the manga
     */
    @Setter
    private boolean rereading;

    /**
     * The number of times the user has re-read the title. (Does not include the first time.)
     */
    @Setter
    @Getter
    @SerializedName("reread_count")
    private int rereadCount;

    /**
     * How much value the user thinks there is in rereading the series.
     */
    @Setter
    @Getter
    @SerializedName("reread_value")
    private int rereadValue;

    private boolean getRereading() {
        return rereading;
    }

    public net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga model = new net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga();
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord.setFromCursor(true);
        createGeneralBaseModel(model);
        model.setChapters(getChapters());
        model.setVolumes(getVolumes());
        model.setAnimeAdaptations(getAnimeAdaptations());
        model.setRelatedManga(getRelatedManga());
        model.setReadStatus(getReadStatus());
        model.setChaptersRead(getChaptersRead());
        model.setVolumesRead(getVolumesRead());
        model.setPersonalTags(getPersonalTags());
        model.setReadingStart(getReadingStart());
        model.setReadingEnd(getReadingEnd());
        model.setChapDownloaded(getChapDownloaded());
        model.setRereading(getRereading() ? 1 : 0);
        model.setRereadCount(getRereadCount());
        model.setRereadValue(getRereadValue());
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord.setFromCursor(false);
        return model;
    }
}
