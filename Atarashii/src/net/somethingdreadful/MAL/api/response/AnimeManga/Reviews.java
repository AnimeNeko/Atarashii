package net.somethingdreadful.MAL.api.response.AnimeManga;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class Reviews implements Serializable {
    // MyAnimeList
    @Getter @Setter private String date;
    @Getter @Setter private int rating;
    @Getter @Setter private String username;
    @Getter @Setter private int episodes;
    @Getter @Setter @SerializedName("watched_episodes") private int watchedEpisodes;
    @Getter @Setter private int chapters;
    @Getter @Setter @SerializedName("chapters_read") private int chaptersRead;
    @Getter @Setter private int helpful;
    @Getter @Setter @SerializedName("helpful_total") private int helpfulTotal;
    @Getter @Setter @SerializedName("avatar_url") private String avatarUrl;
    @Getter @Setter private String review;

    // Anilist
    @Getter @SerializedName("display_name") private String displayName;
    @Getter @SerializedName("image_url_lge") private String imageUrl;
}
