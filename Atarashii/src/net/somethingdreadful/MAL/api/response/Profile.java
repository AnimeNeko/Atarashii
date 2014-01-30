package net.somethingdreadful.MAL.api.response;

import java.util.Arrays;
import java.util.List;

import android.database.Cursor;

public class Profile extends BasicProfile {
    private ProfileAnimeStats anime_stats;
    private ProfileMangaStats manga_stats;

    public static Profile fromCursor(Cursor c) {
        Profile result = new Profile();

        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setAvatarUrl(c.getString(columnNames.indexOf("avatar_url")));
        result.setDetails(ProfileDetails.fromCursor(c));
        result.setAnimeStats(ProfileAnimeStats.fromCursor(c));
        result.setMangaStats(ProfileMangaStats.fromCursor(c));
        return result;
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
