package net.somethingdreadful.MAL.api.response;

import android.database.Cursor;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class ProfileDetails implements Serializable {
    @Getter @Setter @SerializedName("access_rank") private String accessRank;
    @Getter @Setter @SerializedName("anime_list_views") private int animeListViews;
    @Getter @Setter private String birthday;
    @Getter @Setter private int comments;
    @Getter @Setter @SerializedName("forum_posts") private int forumPosts;
    @Getter @Setter private String gender;
    @Getter @Setter @SerializedName("join_date") private String joinDate;
    @Getter @Setter @SerializedName("last_online") private String lastOnline;
    @Getter @Setter private String location;
    @Getter @Setter @SerializedName("manga_list_views") private int mangaListViews;
    @Getter @Setter private String website;

    public static ProfileDetails fromCursor(Cursor c) {
        return fromCursor(c, false);
    }

    public static ProfileDetails fromCursor(Cursor c, boolean friendDetails) {
        ProfileDetails result = new ProfileDetails();

        List<String> columnNames = Arrays.asList(c.getColumnNames());

        result.setLastOnline(c.getString(columnNames.indexOf("last_online")));
        if (!friendDetails) {
            result.setBirthday(c.getString(columnNames.indexOf("birthday")));
            result.setLocation(c.getString(columnNames.indexOf("location")));
            result.setWebsite(c.getString(columnNames.indexOf("website")));
            result.setComments(c.getInt(columnNames.indexOf("comments")));
            result.setForumPosts(c.getInt(columnNames.indexOf("forum_posts")));
            result.setGender(c.getString(columnNames.indexOf("gender")));
            result.setJoinDate(c.getString(columnNames.indexOf("join_date")));
            result.setAccessRank(c.getString(columnNames.indexOf("access_rank")));
            result.setAnimeListViews(c.getInt(columnNames.indexOf("anime_list_views")));
            result.setMangaListViews(c.getInt(columnNames.indexOf("manga_list_views")));
        }
        return result;
    }

    public int getGenderInt() {
        String[] gender = {
                "Female",
                "Male"
        };
        return Arrays.asList(gender).indexOf(getGender());
    }
}
