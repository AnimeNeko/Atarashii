package net.somethingdreadful.MAL.api.response;

import java.util.Arrays;
import java.util.List;

import android.database.Cursor;

/*
 * the friends API returns an different profile than the profile API (anime-/mangastats as empty array, not object),
 * so we need to handle this separately :(
 */
public class BasicProfile {
    int id;
    String avatar_url;
    ProfileDetails details;
    
    public static BasicProfile fromCursor(Cursor c) {
        BasicProfile result = new BasicProfile();

        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setAvatarUrl(c.getString(columnNames.indexOf("avatar_url")));
        result.setDetails(ProfileDetails.fromCursor(c));
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
}
