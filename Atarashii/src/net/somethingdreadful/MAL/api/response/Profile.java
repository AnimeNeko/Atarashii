package net.somethingdreadful.MAL.api.response;

import java.util.Arrays;
import java.util.List;

import android.database.Cursor;

public class Profile {
    int id;
    String avatar_url;
    ProfileAnimeStats anime_stats;
    ProfileMangaStats manga_stats;
    ProfileDetails details;
    
    public static Profile fromCursor(Cursor c) {
        return fromCursor(c, false);
    }
    
    public static Profile fromCursor(Cursor c, boolean friendDetails) {
        Profile result = new Profile();

        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setAvatarUrl(c.getString(columnNames.indexOf("avatar_url")));
        result.setDetails(ProfileDetails.fromCursor(c));
        // stats only available if full profile is loaded, not for friend records
        if ( !friendDetails ) {
            result.setAnimeStats(ProfileAnimeStats.fromCursor(c));
            result.setMangaStats(ProfileMangaStats.fromCursor(c));
        }
        return result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAvatarUrl() {
        return avatar_url;
    }

    public void setAvatarUrl(String avatarurl) {
        this.avatar_url = avatarurl;
    }

    public ProfileAnimeStats getAnimeStats() {
        return anime_stats;
    }

    public void setAnimeStats(ProfileAnimeStats animestats) {
        this.anime_stats = animestats;
    }

    public ProfileMangaStats getMangaStats() {
        return manga_stats;
    }

    public void setMangaStats(ProfileMangaStats mangastats) {
        this.manga_stats = mangastats;
    }

    public ProfileDetails getDetails() {
        return details;
    }

    public void setDetails(ProfileDetails details) {
        this.details = details;
    }
}
