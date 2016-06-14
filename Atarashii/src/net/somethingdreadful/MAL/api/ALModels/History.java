package net.somethingdreadful.MAL.api.ALModels;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.BaseModels.Profile;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class History implements Serializable {
    @Setter
    @Getter
    private int id;
    @Setter
    @Getter
    @SerializedName("user_id")
    private int userId;
    @Setter
    @Getter
    @SerializedName("reply_count")
    private int replyCount;
    @Setter
    @Getter
    @SerializedName("created_at")
    private String createdAt;
    @Setter
    @Getter
    private String status;
    @Setter
    @Getter
    private String value;
    @Setter
    @Getter
    @SerializedName("activity_type")
    private String activityType;
    @Setter
    @Getter
    private ArrayList<net.somethingdreadful.MAL.api.ALModels.Profile> users;
    @Setter
    @Getter
    private Series series;

    private class Series {
        @Setter
        @Getter
        private boolean adult;
        @Setter
        @Getter
        @SerializedName("airing_status")
        private String airingStatus;
        @Setter
        @Getter
        @SerializedName("average_score")
        private float averageScore;
        @Setter
        @Getter
        private int id;
        @Setter
        @Getter
        @SerializedName("image_url_lge")
        private String imageUrlLge;
        @Setter
        @Getter
        @SerializedName("image_url_med")
        private String imageUrlMed;
        @Setter
        @Getter
        @SerializedName("image_url_sml")
        private String imageUrlSml;
        @Setter
        @Getter
        @SerializedName("publishing_status")
        private String publishingStatus;
        @Setter
        @Getter
        @SerializedName("series_type")
        private String seriesType;
        @Setter
        @Getter
        @SerializedName("title_english")
        private String titleEnglish;
        @Setter
        @Getter
        @SerializedName("title_japanese")
        private String titleJapanese;
        @Setter
        @Getter
        @SerializedName("title_romaji")
        private String titleRomaji;
        @Setter
        @Getter
        @SerializedName("total_chapters")
        private int totalChapters;
        @Setter
        @Getter
        @SerializedName("total_episodes")
        private int totalEpisodes;
        @Setter
        @Getter
        @SerializedName("total_volumes")
        private int totalVolumes;
        @Setter
        @Getter
        private String type;
    }

    private net.somethingdreadful.MAL.api.BaseModels.History createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.History model = new net.somethingdreadful.MAL.api.BaseModels.History();
        if (getSeries() != null && getSeries().getSeriesType() != null) {
            if (getSeries().getSeriesType().equals("anime")) {
                model.setAnime(new Anime());
                model.getAnime().setId(getSeries().getId());
                model.getAnime().setTitle(getSeries().getTitleRomaji() == null ? getSeries().getTitleEnglish() : getSeries().getTitleRomaji());
                model.getAnime().setImageUrl(getSeries().getImageUrlLge() == null ? getSeries().getImageUrlMed() : getSeries().getImageUrlLge());
                model.getAnime().setStatus(getSeries().getAiringStatus());
                model.getAnime().setAverageScore(String.valueOf(getSeries().getAverageScore()));
                model.getAnime().setEpisodes(getSeries().getTotalEpisodes());
            } else if (getSeries().getSeriesType().equals("manga")) {
                model.setManga(new Manga());
                model.getManga().setId(getSeries().getId());
                model.getManga().setTitle(getSeries().getTitleRomaji() == null ? getSeries().getTitleEnglish() : getSeries().getTitleRomaji());
                model.getManga().setImageUrl(getSeries().getImageUrlLge() == null ? getSeries().getImageUrlMed() : getSeries().getImageUrlLge());
                model.getManga().setStatus(getSeries().getPublishingStatus());
                model.getManga().setAverageScore(String.valueOf(getSeries().getAverageScore()));
                model.getManga().setChapters(getSeries().getTotalChapters());
                model.getManga().setVolumes(getSeries().getTotalVolumes());
            }
        }
        model.setStatus(getStatus());
        model.setValue(getValue());
        model.setActivityType(getActivityType());
        model.setCreatedAt(getCreatedAt());

        // set username
        ArrayList<Profile> users = new ArrayList<>();
        Profile profile = new Profile();
        profile.setUsername(getUsers().get(0).getDisplayName());
        profile.setImageUrl(getUsers().get(0).getImageUrl());
        users.add(profile);
        model.setUsers(users);
        return model;
    }

    public static ArrayList<net.somethingdreadful.MAL.api.BaseModels.History> convertBaseHistoryList(ArrayList<History> histories) {
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.History> historyArrayList = new ArrayList<>();
        if (histories != null) {
            for (History history : histories) {
                historyArrayList.add(history.createBaseModel());
            }
        }
        return historyArrayList;
    }
}
