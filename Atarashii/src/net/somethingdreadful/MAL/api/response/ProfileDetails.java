package net.somethingdreadful.MAL.api.response;

import java.util.Arrays;
import java.util.List;

import android.database.Cursor;

public class ProfileDetails {
    private String access_rank;
    private int anime_list_views;
    private String birthday;
    private int comments;
    private int forum_posts;
    private String gender;
    private String join_date;
    private String last_online;
    private String location;
    private int manga_list_views;
    private String website;
    
    public static ProfileDetails fromCursor(Cursor c) {
        return fromCursor(c, false);
    }
    
    public static ProfileDetails fromCursor(Cursor c, boolean friendDetails) {
        ProfileDetails result = new ProfileDetails();

        List<String> columnNames = Arrays.asList(c.getColumnNames());
        
        result.setLastOnline(c.getString(columnNames.indexOf("last_online")));
        if ( !friendDetails ) {
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

    public String getAccessRank() {
        return access_rank;
    }

    public void setAccessRank(String access_rank) {
        this.access_rank = access_rank;
    }

    public int getAnimeListViews() {
        return anime_list_views;
    }

    public void setAnimeListViews(int anime_list_views) {
        this.anime_list_views = anime_list_views;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public int getForumPosts() {
        return forum_posts;
    }

    public void setForumPosts(int forum_posts) {
        this.forum_posts = forum_posts;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getJoinDate() {
        return join_date;
    }

    public void setJoinDate(String join_date) {
        this.join_date = join_date;
    }

    public String getLastOnline() {
        return last_online;
    }

    public void setLastOnline(String last_online) {
        this.last_online = last_online;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getMangaListViews() {
        return manga_list_views;
    }

    public void setMangaListViews(int manga_list_views) {
        this.manga_list_views = manga_list_views;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getGenderInt() {
        String[] gender = {
                "Female",
                "Male"
        };
        return Arrays.asList(gender).indexOf(getGender());
    }
}
