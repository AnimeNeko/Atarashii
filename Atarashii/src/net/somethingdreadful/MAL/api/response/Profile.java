package net.somethingdreadful.MAL.api.response;

import android.database.Cursor;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class Profile implements Serializable {
    @Getter @Setter private int id;
    @Getter @Setter @SerializedName("avatar_url") private String avatarUrl;
    @Getter @Setter private ProfileDetails details;

    @Getter @Setter @SerializedName("anime_stats") private ProfileAnimeStats animeStats;
    @Getter @Setter @SerializedName("manga_stats") private ProfileMangaStats mangaStats;

    public static Profile fromCursor(Cursor c) {
        Profile result = new Profile();

        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setAvatarUrl(c.getString(columnNames.indexOf("avatar_url")));
        result.setDetails(ProfileDetails.fromCursor(c));
        result.setAnimeStats(ProfileAnimeStats.fromCursor(c));
        result.setMangaStats(ProfileMangaStats.fromCursor(c));
        return result;
    }
}
