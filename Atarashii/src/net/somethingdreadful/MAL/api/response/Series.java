package net.somethingdreadful.MAL.api.response;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

public class Series {
    @Setter @Getter private int id;
    @Setter @Getter private boolean adult;
    @Setter @Getter @SerializedName("average_score") private float averageScore;
    @Setter @Getter @SerializedName("image_url_lge") private String imageUrlLge;
    @Setter @Getter @SerializedName("image_url_med") private String imageUrlMed;
    @Setter @Getter @SerializedName("image_url_sml") private String imageUrlSml;
    @Setter @Getter @SerializedName("airing_status") private String airingStatus;
    @Setter @Getter @SerializedName("publishing_status") private String publishingStatus;
    @Setter @Getter @SerializedName("series_type") private String seriesType;
    @Setter @Getter @SerializedName("title_english") private String titleEnglish;
    @Setter @Getter @SerializedName("title_japanese") private String titleJapanese;
    @Setter @Getter @SerializedName("title_romaji") private String titleRomaji;
    @Setter @Getter private String type;
}
