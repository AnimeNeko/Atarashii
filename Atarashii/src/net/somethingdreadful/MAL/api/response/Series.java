package net.somethingdreadful.MAL.api.response;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.MALApi;

import lombok.Getter;
import lombok.Setter;

public class Series {
    @Setter @Getter private boolean adult;
    @Setter @Getter @SerializedName("airing_status") private String airingStatus;
    @Setter @Getter @SerializedName("average_score") private float averageScore;
    @Setter @Getter private int id;
    @Setter @Getter @SerializedName("image_url_lge") private String imageUrlLge;
    @Setter @Getter @SerializedName("image_url_med") private String imageUrlMed;
    @Setter @Getter @SerializedName("image_url_sml") private String imageUrlSml;
    @Setter @Getter @SerializedName("publishing_status") private String publishingStatus;
    @Setter @Getter @SerializedName("series_type") private String seriesType;
    @Setter @Getter @SerializedName("title_english") private String titleEnglish;
    @Setter @Getter @SerializedName("title_japanese") private String titleJapanese;
    @Setter @Getter @SerializedName("title_romaji") private String titleRomaji;
    @Setter @Getter @SerializedName("total_chapters") private int totalChapters;
    @Setter @Getter @SerializedName("total_episodes") private int totalEpisodes;
    @Setter @Getter @SerializedName("total_volumes") private int totalVolumes;
    @Setter @Getter private String type;

    public static Series fromAnime(Anime anime) {
        Series result = new Series();
        result.setId(anime.getId());
        result.setTitleRomaji(anime.getTitle());
        result.setImageUrlLge(anime.getImageUrl());
        result.setAiringStatus(anime.getStatus());
        result.setAverageScore(anime.getMembersScore());
        result.setTotalEpisodes(anime.getEpisodes());
        result.setSeriesType("anime");
        return result;
    }

    public static Series fromManga(Manga manga) {
        Series result = new Series();
        result.setId(manga.getId());
        result.setTitleRomaji(manga.getTitle());
        result.setImageUrlLge(manga.getImageUrl());
        result.setPublishingStatus(manga.getStatus());
        result.setAverageScore(manga.getMembersScore());
        result.setTotalChapters(manga.getChapters());
        result.setTotalVolumes(manga.getVolumes());
        result.setSeriesType("manga");
        return result;
    }

    public Anime getAnime() {
        Anime result = null;
        if (seriesType.equals("anime")) {
            result = new Anime();
            result.setId(id);
            result.setTitle(titleRomaji);
            result.setImageUrl(imageUrlLge);
            result.setStatus(airingStatus);
            result.setMembersScore(averageScore);
            result.setEpisodes(totalEpisodes);
        }
        return result;
    }

    public Manga getManga() {
        Manga result = null;
        if (seriesType.equals("manga")) {
            result = new Manga();
            result.setId(id);
            result.setTitle(titleRomaji);
            result.setImageUrl(imageUrlLge);
            result.setStatus(publishingStatus);
            result.setMembersScore(averageScore);
            result.setChapters(totalChapters);
            result.setVolumes(totalVolumes);
        }
        return result;
    }
}
