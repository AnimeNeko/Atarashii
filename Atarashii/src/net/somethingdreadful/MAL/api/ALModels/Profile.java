package net.somethingdreadful.MAL.api.ALModels;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.PrefManager;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Profile implements Serializable {
    @Getter
    @Setter
    @SerializedName("display_name")
    private String displayName;

    /**
     * TODO: Add UI support
     */
    @Getter
    @Setter
    @SerializedName("anime_time")
    private int animeTime;

    /**
     * TODO: Add UI support
     */
    @Getter
    @Setter
    @SerializedName("manga_chap")
    private int mangaChapters;

    /**
     * TODO: Add UI support
     */
    @Getter
    @Setter
    private String about;

    /**
     * TODO: Add UI support
     */
    @Getter
    @Setter
    @SerializedName("list_order")
    private int listOrder;
    @Getter
    @Setter
    @SerializedName("adult_content")
    private boolean adultContent;
    @Getter
    @Setter
    private boolean following;
    @Getter
    @Setter
    @SerializedName("image_url_lge")
    private String imageUrl;
    @Getter
    @Setter
    @SerializedName("image_url_med")
    private String imageUrlMed;
    @Getter
    @Setter
    @SerializedName("image_url_banner")
    private String imageUrlBanner;

    /**
     * TODO: Add UI support
     */
    @Getter
    @Setter
    @SerializedName("title_language")
    private String titleLanguage;
    @Getter
    @Setter
    @SerializedName("score_type")
    private int scoreType = -1;
    @Getter
    @Setter
    @SerializedName("custom_list_anime")
    private ArrayList<String> customAnime;
    @Getter
    @Setter
    @SerializedName("custom_list_manga")
    private ArrayList<String> customManga;

    /**
     * TODO: Add UI support
     */
    @Getter
    @Setter
    @SerializedName("advanced_rating")
    private boolean advancedRating;

    /**
     * TODO: Add UI support
     */
    @Getter
    @Setter
    @SerializedName("advanced_rating_names")
    private ArrayList<String> advancedRatingNames;
    @Getter
    @Setter
    private int notifications;

    public net.somethingdreadful.MAL.api.BaseModels.Profile createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.Profile model = new net.somethingdreadful.MAL.api.BaseModels.Profile();
        model.setUsername(getDisplayName());
        model.setAbout(getAbout());
        model.setImageUrl(getImageUrl());
        model.setImageUrlBanner(getImageUrlBanner());
        model.setScoreType(getScoreType());
        model.setCustomAnime(getCustomAnime());
        model.setCustomManga(getCustomManga());
        model.setNotifications(getNotifications());
        model.setAnimeStats(new net.somethingdreadful.MAL.api.MALModels.Profile.AnimeStats());
        model.setMangaStats(new net.somethingdreadful.MAL.api.MALModels.Profile.MangaStats());
        model.getAnimeStats().setTimeDays(Double.valueOf((new DecimalFormat("#.##")).format((double) getAnimeTime() / 60 / 24).replace(",", ".")));
        model.getMangaStats().setCompleted(getMangaChapters());

        if (scoreType != -1) {
            PrefManager.setScoreType(scoreType);
            PrefManager.commitChanges();
        }
        return model;
    }
}
