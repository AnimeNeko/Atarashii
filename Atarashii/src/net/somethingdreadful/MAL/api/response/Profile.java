package net.somethingdreadful.MAL.api.response;

import android.database.Cursor;

import java.util.Arrays;
import java.util.List;

public class Profile {
    private int id;
    private String avatar_url;
    private ProfileDetails details;

    private ProfileAnimeStats anime_stats;
    private ProfileMangaStats manga_stats;

    public static Profile fromCursor(Cursor c, boolean friendDetails) {
        Profile result = new Profile();

        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setAvatarUrl(c.getString(columnNames.indexOf("avatar_url")));
        result.setDetails(ProfileDetails.fromCursor(c));
        result.setAnimeStats(ProfileAnimeStats.fromCursor(c));
        result.setMangaStats(ProfileMangaStats.fromCursor(c));
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

    public ProfileDetails getDetails() {
        return details;
    }

    public void setDetails(ProfileDetails details) {
        this.details = details;
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
}
