package net.somethingdreadful.MAL.api.response.AnimeManga;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.response.UserProfile.Profile;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class Reviews implements Serializable {
    // MyAnimeList
    @Getter @Setter private String date;
    @Getter @Setter private int rating;
    @Setter private String username;
    @Getter @Setter private int episodes;
    @Getter @Setter @SerializedName("watched_episodes") private int watchedEpisodes;
    @Getter @Setter private int chapters;
    @Getter @Setter @SerializedName("chapters_read") private int chaptersRead;
    @Getter @Setter private int helpful;
    @Getter @Setter @SerializedName("helpful_total") private int helpfulTotal;
    @Setter @SerializedName("avatar_url") private String avatarUrl;
    @Setter private String review;

    // Anilist
    private int id;
    private int rating_amount;
    private String summary;
    private int user_rating;
    private String text;
    private int score;
    private Profile user;

    public String getReview() {
        return AccountService.isMAL() ? review : text;
    }

    public String getUsername(){
        return AccountService.isMAL() ? username : user.getDisplayName();
    }

    public String getAvatarUrl() {
        return AccountService.isMAL() ? avatarUrl : user.getImageUrl();
    }
}
