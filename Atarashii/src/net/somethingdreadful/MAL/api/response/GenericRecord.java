package net.somethingdreadful.MAL.api.response;

import android.text.Html;
import android.text.Spanned;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class GenericRecord implements Serializable {

    // these are the same for both, so put them in here
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_ONHOLD = "on-hold";
    public static final String STATUS_DROPPED = "dropped";

    private int id;
    private String title;
    private String image_url;
    private String type;
    private String status;
    private ArrayList<String> genres;
    private ArrayList<String> tags;
    private int score;
    private int rank;
    private float members_score;
    private int members_count;
    private int favorited_count;
    private int popularity_rank;
    private String synopsis;
    private HashMap<String, ArrayList<String>> other_titles;

    private boolean dirty;
    private Date lastUpdate;
    private boolean flag_create;
    private boolean flag_delete;

    private transient boolean from_cursor = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        // if not loaded from cursor the image might point to an thumbnail
        if (from_cursor)
            return image_url;
        else
            return image_url.replaceFirst("t.jpg$", ".jpg");
    }

    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public void setGenres(ArrayList<String> genres) {
        this.genres = genres;
    }

    public ArrayList<Integer> getGenresInt() {
        String[] genres = {
                "Action", "Adventure", "Cars",
                "Comedy", "Dementia", "Demons",
                "Drama", "Ecchi", "Fantasy",
                "Game", "Harem", "Hentai",
                "Historical", "Horror", "Josei",
                "Kids", "Magic", "Martial Arts",
                "Mecha", "Military", "Music",
                "Mystery", "Parody", "Police",
                "Psychological", "Romance", "Samurai",
                "School", "Sci-Fi", "Seinen",
                "Shoujo", "Shoujo Ai", "Shounen",
                "Shounen Ai", "Slice of Life", "Space",
                "Sports", "Super Power", "Supernatural",
                "Thriller", "Vampire", "Yaoi",
                "Yuri"
        };
        ArrayList<Integer> result = new ArrayList<Integer>();
        if (getGenres() != null)
            for (String genre : getGenres())
                result.add(Arrays.asList(genres).indexOf(genre));
        
        return result;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public float getMembersScore() {
        return members_score;
    }

    public void setMembersScore(float members_score) {
        this.members_score = members_score;
    }

    public int getMembersCount() {
        return members_count;
    }

    public void setMembersCount(int members_count) {
        this.members_count = members_count;
    }

    public int getFavoritedCount() {
        return favorited_count;
    }

    public void setFavoritedCount(int favorited_count) {
        this.favorited_count = favorited_count;
    }

    public int getPopularityRank() {
        return popularity_rank;
    }

    public void setPopularityRank(int popularity_rank) {
        this.popularity_rank = popularity_rank;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public HashMap<String, ArrayList<String>> getOtherTitles() {
        return other_titles;
    }

    public void setOtherTitles(HashMap<String, ArrayList<String>> other_titles) {
        this.other_titles = other_titles;
    }

    private ArrayList<String> getOtherTitlesByLanguage(String lang) {
        if (other_titles == null) {
            return null;
        }
        return other_titles.get(lang);
    }

    public ArrayList<String> getOtherTitlesJapanese() {
        return getOtherTitlesByLanguage("japanese");
    }

    public ArrayList<String> getOtherTitlesEnglish() {
        return getOtherTitlesByLanguage("english");
    }

    public ArrayList<String> getOtherTitlesSynonyms() {
        return getOtherTitlesByLanguage("synonyms");
    }

    public boolean getDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean getCreateFlag() {
        return flag_create;
    }

    public void setCreateFlag(boolean flag_create) {
        this.flag_create = flag_create;
    }

    public boolean getDeleteFlag() {
        return flag_delete;
    }

    public void setDeleteFlag(boolean flag_delete) {
        this.flag_delete = flag_delete;
    }

    // Use this to get a formatted version of the text suited for display in the application
    public Spanned getSpannedSynopsis() {
        return (getSynopsis() != null ? Html.fromHtml(getSynopsis()) : null);
    }

    public int getUserStatusInt(String statusString) {
        String[] status = {
                "completed",
                "on-hold",
                "dropped",
                "watching",
                "plan to watch",
                "reading",
                "plan to read"
        };
        return Arrays.asList(status).indexOf(statusString);
    }

    public boolean getCreatedFromCursor() {
        return from_cursor;
    }

    public void setCreatedFromCursor(boolean from_cursor) {
        this.from_cursor = from_cursor;
    }
}
