package net.somethingdreadful.MAL.api.response;

import android.database.Cursor;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class Profile implements Serializable {
    @Getter @Setter private int id;

    // MyAnimeList
    @Getter @Setter @SerializedName("avatar_url") private String avatarUrl;
    @Getter @Setter private ProfileDetails details;

    @Getter @Setter @SerializedName("anime_stats") private ProfileAnimeStats animeStats;
    @Getter @Setter @SerializedName("manga_stats") private ProfileMangaStats mangaStats;

    // Anilist
    @Getter @Setter @SerializedName("display_name") private String displayName;
    @Getter @Setter @SerializedName("anime_time") private int animeTime;
    @Getter @Setter @SerializedName("manga_chap") private int mangaChapters;
    @Getter @Setter private String about;
    @Getter @Setter @SerializedName("list_order") private int listOrder;
    @Getter @Setter @SerializedName("adult_content") private boolean adultContent;
    @Getter @Setter private boolean following;
    @Getter @Setter @SerializedName("image_url_lge") private String imageUrl;
    @Getter @Setter @SerializedName("image_url_banner") private String imageUrlBanner;
    @Getter @Setter @SerializedName("title_language") private String titleLanguage;
    @Getter @Setter @SerializedName("score_type") private int scoreType;
    @Getter @Setter @SerializedName("custom_list_anime") private ArrayList<String> customAnime;
    @Getter @Setter @SerializedName("custom_list_manga") private ArrayList<String> customManga;
    @Getter @Setter @SerializedName("advanced_rating") private boolean advancedRating;
    @Getter @Setter @SerializedName("advanced_rating_names") private ArrayList<String> advancedRatingNames;
    @Getter @Setter private int notifications;


    public static Profile fromCursor(Cursor c) {
        Profile result = new Profile();

        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setAvatarUrl(c.getString(columnNames.indexOf("avatar_url")));
        result.setDetails(ProfileDetails.fromCursor(c));
        result.setAnimeStats(ProfileAnimeStats.fromCursor(c));
        result.setMangaStats(ProfileMangaStats.fromCursor(c));

        result.setDisplayName(c.getString(columnNames.indexOf("username")));
        result.setAnimeTime(c.getInt(columnNames.indexOf("anime_time")));
        result.setMangaChapters(c.getInt(columnNames.indexOf("manga_chap")));
        result.setAbout(c.getString(columnNames.indexOf("about")));
        result.setListOrder(c.getInt(columnNames.indexOf("list_order")));
        result.setImageUrlBanner(c.getString(columnNames.indexOf("image_url_banner")));
        result.setTitleLanguage(c.getString(columnNames.indexOf("title_language")));
        result.setScoreType(c.getInt(columnNames.indexOf("score_type")));
        result.setNotifications(c.getInt(columnNames.indexOf("notifications")));
        return result;
    }
}
