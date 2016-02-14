package net.somethingdreadful.MAL.api.ALModels.AnimeManga;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class GenericRecord implements Serializable {

    /**
     * The ID of the record
     */
    @Getter
    @Setter
    private int id;

    /**
     * Title of the record in Romaji
     */
    @Getter
    @Setter
    @SerializedName("title_romaji")
    private String titleRomaji;

    /**
     * Type of record
     * <p/>
     * Manga values: manga, Novel, One Shot, Doujin, Manwha, Manhua, OEL ("OEL manga" refers to "Original English-Language manga")
     * Anime values: TV, Movie, OVA, ONA, Special, or Music.
     */
    @Getter
    @Setter
    private String type;

    /**
     * URL of an image (mid size) of the record
     */
    @Getter
    @Setter
    @SerializedName("image_url_med")
    private String imageUrlMed;

    /**
     * URL of an image (small) of the record
     */
    @Getter
    @Setter
    @SerializedName("image_url_sml")
    private String imageUrlSml;

    /**
     * Beginning date from when this anime/manga will be aired/published.
     */
    @Getter
    @Setter
    @SerializedName("start_date")
    private String startDate;

    /**
     * End date when this anime/manga was ended being aired/published.
     */
    @Getter
    @Setter
    @SerializedName("end_date")
    private String endDate;

    /**
     * Rating of the anime/manga
     * <p/>
     * The rating is a freeform text field with no defined values.
     */
    @Getter
    @Setter
    private String classification;

    /**
     * The hashtag source of this record.
     * <p/>
     * TODO: Add UI support
     */
    @Getter
    @Setter
    private String hashtag;

    /**
     * The information source of this record.
     * <p/>
     * TODO: Add UI support
     */
    @Getter
    @Setter
    private String source;

    /**
     * Boolean value which indicates if the records is for elder people.
     * <p/>
     * TODO: Add UI support? not sure though.
     */
    @Getter
    @Setter
    private boolean adult;

    /**
     * Global rank of the record based on popularity (number of people with the title on the list)
     */
    @Getter
    @Setter
    private int popularity;

    /**
     * Title of the record in Japanese (with characters)
     */
    @Getter
    @Setter
    @SerializedName("title_japanese")
    private String titleJapanese;

    /**
     * Title of the record in English
     */
    @Getter
    @Setter
    @SerializedName("title_english")
    private String titleEnglish;

    /**
     * TODO: investigate what this really is XD
     */
    @Getter
    @Setter
    private ArrayList<String> synonyms;

    /**
     * Description of the record
     */
    @Getter
    @Setter
    private String description;

    /**
     * URL of an image (large) of the record
     */
    @Getter
    @Setter
    @SerializedName("image_url_lge")
    private String imageUrlLge;

    /**
     * URL of an image (rectangle) of the record used for the banner.
     * <p/>
     * TODO: Add UI support
     */
    @Getter
    @Setter
    @SerializedName("image_url_banner")
    private String imageUrlBanner;

    /**
     * The average score of the record chosen by the users
     */
    @Getter
    @Setter
    @SerializedName("average_score")
    private String averageScore;

    /**
     * A list of genres for the record
     */
    @Getter
    @Setter
    private ArrayList<String> genres;

    net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord createGeneralBaseModel(net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord model) {
        model.setId(getId());
        model.setTitle(getTitleRomaji());
        model.setTitleEnglish(createTitleArray(getTitleEnglish()));
        model.setTitleJapanese(createTitleArray(getTitleJapanese()));
        model.setTitleRomaji(createTitleArray(getTitleRomaji()));
        model.setTitleSynonyms(getSynonyms());
        model.setType(getType());
        model.setImageUrl(getImageUrlLge());
        model.setStartDate(getStartDate());
        model.setEndDate(getEndDate());
        model.setClassification(getClassification());
        model.setPopularity(getPopularity());
        model.setSynopsis(getDescription());
        model.setAverageScore(getAverageScore());
        model.setGenres(getGenres());
        return model;
    }

    private ArrayList<String> createTitleArray(String title) {
        ArrayList<String> titleArray = new ArrayList<>();
        titleArray.add(title);
        return titleArray;
    }
}
