package net.somethingdreadful.MAL.api.MALModels.AnimeManga;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.MALModels.RecordStub;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

public class GenericRecord implements Serializable {

    /**
     * The ID of the record
     */
    @Setter
    @Getter
    private int id;

    /**
     * Title of the record
     */
    @Setter
    @Getter
    private String title;

    /**
     * Map of other titles for the record
     */
    @Setter
    @Getter
    @SerializedName("other_titles")
    private HashMap<String, ArrayList<String>> otherTitles;

    /**
     * The global rank of the record
     */
    @Setter
    @Getter
    private int rank;

    /**
     * Global rank of the record based on popularity (number of people with the title on the list)
     */
    @Setter
    @Getter
    @SerializedName("popularity_rank")
    private int popularityRank;

    /**
     * URL of an image to the record
     */
    @Setter
    @SerializedName("image_url")
    private String imageUrl;

    /**
     * Type of record
     * <p/>
     * Manga values: manga, Novel, One Shot, Doujin, Manwha, Manhua, OEL ("OEL manga" refers to "Original English-Language manga")
     * Anime values: TV, Movie, OVA, ONA, Special, or Music.
     */
    @Setter
    @Getter
    private String type;

    /**
     * Publishing/Airing status of the record
     * <p/>
     * Manga values: finished, publishing, not yet published.
     * Anime values: finished airing, currently airing, or not yet aired.
     */
    @Setter
    @Getter
    private String status;

    /**
     * Weighted score of the record
     * <p/>
     * The score is calculated based on the ratings given by members.
     */
    @Setter
    @Getter
    @SerializedName("members_score")
    private double membersScore;

    /**
     * The number of members that have the recordon the list
     */
    @Setter
    @Getter
    @SerializedName("members_count")
    private int membersCount;

    /**
     * The number of members that have the record marked as a favorite
     */
    @Setter
    @Getter
    @SerializedName("favorited_count")
    private int favoritedCount;

    /**
     * Description of the record
     * <p/>
     * An HTML-formatted string describing the anime/manga
     */
    @Setter
    @Getter
    private String synopsis;

    /**
     * A list of genres for the record
     */
    @Setter
    @Getter
    private ArrayList<String> genres;

    /**
     * A list of popular tags for the record
     */
    @Setter
    @Getter
    private ArrayList<String> tags;

    /**
     * The user's personal tags
     */
    @Setter
    @Getter
    @SerializedName("personal_tags")
    private ArrayList<String> personalTags;

    /**
     * A list of alternative versions of this record
     */
    @Setter
    @Getter
    @SerializedName("alternative_versions")
    private ArrayList<RecordStub> alternativeVersions;

    /**
     * User's score for the record, from 1 to 10
     */
    @Setter
    @Getter
    private int score;

    /**
     * Reading priority level for the title.
     * <p/>
     * Integer corresponding to the reading priority of the record from 0 (low) to 2 (high).
     */
    @Setter
    @Getter
    private int priority;

    /**
     * The user's personal comments on the title
     */
    @Setter
    @Getter
    @SerializedName("personal_comments")
    private String personalComments;

    void createGeneralBaseModel(net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord model) {
        model.setId(getId());
        model.setTitle(getTitle()); // MAL is using default romaji
        model.setTitleEnglish(getOtherTitles().get("english"));
        model.setTitleJapanese(getOtherTitles().get("japanese"));
        model.setTitleSynonyms(getOtherTitles().get("synonyms"));
        model.setTitleRomaji(getTitleArray());
        model.setRank(getRank());
        model.setPopularity(getPopularityRank());
        model.setImageUrl(getImageUrl());
        model.setType(getType());
        model.setStatus(getStatus());
        model.setAverageScore(String.valueOf(getMembersScore()));
        model.setAverageScoreCount(String.valueOf(getMembersCount()));
        model.setFavoritedCount(getFavoritedCount());
        model.setSynopsis(getSynopsis());
        model.setGenres(getGenres());
        model.setTags(getTags());
        model.setAlternativeVersions(getAlternativeVersions());
        model.setScore(getScore());
        model.setPriority(getPriority());
        model.setNotes(getPersonalComments());
        model.setPersonalTags(getPersonalTags());
    }

    private ArrayList<String> getTitleArray() {
        ArrayList<String> title = new ArrayList<>();
        title.add(getTitle());
        return title;
    }

    public String getImageUrl() {
        if (imageUrl != null)
            return imageUrl.contains("t.jpg") ? imageUrl.replace("t.jpg", "l.jpg") : imageUrl.replace(".jpg", "l.jpg");
        else
            return "http://cdn.myanimelist.net/images/na_series.gif";
    }
}
