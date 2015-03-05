package net.somethingdreadful.MAL.api.response;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.account.AccountService;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

public class GenericRecord implements Serializable {

    // these are the same for both, so put them in here
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_ONHOLD = "on-hold";
    public static final String STATUS_DROPPED = "dropped";

    // MyAnimeList
    @Setter @Getter private int id;
    @Setter @Getter private String title;
    @Setter @SerializedName("image_url") private String imageUrl;
    @Setter @Getter private String type;
    @Setter @Getter private String status;
    @Setter @Getter private ArrayList<String> genres;
    @Setter @Getter private ArrayList<String> tags;
    @Getter private int priority;
    @Getter @SerializedName("personal_comments") private String personalComments;
    @Getter @SerializedName("personal_tags") private ArrayList<String> personalTags;
    @Getter private int score;
    @Setter @Getter private int rank;
    @Setter @Getter @SerializedName("members_score") private float membersScore;
    @Setter @Getter @SerializedName("members_count") private int membersCount;
    @Setter @Getter @SerializedName("favorited_count") private int favoritedCount;
    @Setter @Getter @SerializedName("popularity_rank") private int popularityRank;
    @Setter @Getter private String synopsis;
    @Setter @Getter @SerializedName("other_titles") private HashMap<String, ArrayList<String>> otherTitles;

    @Setter private ArrayList<String> dirty;
    @Setter @Getter private Date lastUpdate;
    @Setter private boolean createFlag;
    @Setter private boolean deleteFlag;

    @Setter @Getter private transient boolean fromCursor = false;

    public String getImageUrl() {
        // if not loaded from cursor the image might point to an thumbnail
        if (fromCursor || !AccountService.isMAL())
            return imageUrl;
        else
            return imageUrl.replaceFirst("t.jpg$", ".jpg");
    }

    // Note: @Getter is not working on booleans
    public boolean getCreateFlag() {
        return createFlag;
    }

    // Note: @Getter is not working on booleans
    public boolean getDeleteFlag() {
        return deleteFlag;
    }

    public ArrayList<String> getDirty() {
        return dirty;
    }

    public void addDirtyField(String field) {
        if (dirty == null) {
            dirty = new ArrayList<String>();
        }
        if (!dirty.contains((field))) {
            dirty.add(field);
        }
    }

    public void clearDirty() {
        dirty = null;
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

    private ArrayList<String> getOtherTitlesByLanguage(String lang) {
        if (otherTitles == null) {
            return null;
        }
        return otherTitles.get(lang);
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

    // Use this to get a formatted version of the text suited for display in the application
    public Spanned getSpannedSynopsis() {
        return (getSynopsis() != null ? Html.fromHtml(getSynopsis()) : null);
    }

    public void setPersonalComments(String message) {
        setPersonalComments(message, true);
    }

    public void setPersonalComments(String value, boolean markDirty) {
        this.personalComments = value;
        if (markDirty) {
            addDirtyField("personalComments");
        }
    }

    public void setPersonalTags(ArrayList<String> value, boolean markDirty) {
        this.personalTags = value;
        if (markDirty) {
            addDirtyField("personalTags");
        }
    }

    public void setPersonalTags(String tag) {
        ArrayList<String> tags = new ArrayList<String>();
        Collections.addAll(tags, TextUtils.split(tag, ","));
        setPersonalTags(tags, true);
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

    public void setScore(int value) {
        setScore(value, true);
    }

    public void setScore(int value, boolean markDirty) {
        this.score = value;
        if (markDirty) {
            addDirtyField("score");
        }
    }

    public void setPriority(int value) {
        setPriority(value, true);
    }

    public void setPriority(int value, boolean markDirty) {
        this.priority = value;
        if (markDirty) {
            addDirtyField("priority");
        }
    }

    public boolean isDirty() {
        return dirty != null && !dirty.isEmpty();
    }

    /*
     * some reflection magic used to get dirty values easier
     */
    public Class getPropertyType(String property) {
        try {
            Field field = getField(this.getClass(), property);
            return field.getType();
        } catch (Exception e) {
            return null;
        }
    }

    private Field getField(Class<?> c, String property) {
        try {
            return c.getDeclaredField(property);
        } catch (Exception e) {
            if (c.getSuperclass() != null) {
                return getField(c.getSuperclass(), property);
            } else {
                return null;
            }
        }
    }

    protected Object getPropertyValue(String property) {
        try {
            Field field = getField(this.getClass(), property);
            if (field != null) {
                field.setAccessible(true);
                return field.get(this);
            }
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public Integer getIntegerPropertyValue(String property) {
        Object value = getPropertyValue(property);
        if (value != null) {
            return (Integer) value;
        }
        return null;
    }

    public String getStringPropertyValue(String property) {
        Object value = getPropertyValue(property);
        if (value != null) {
            return (String) value;
        }
        return null;
    }

    public String getArrayPropertyValue(String property) {
        ArrayList<String> array = (ArrayList<String>) getPropertyValue(property);
        Object value = array != null ? TextUtils.join(",", array) : "";
        return (String) value;
    }

    public String getPersonalTagsString() {
        return getPersonalTags() != null ? TextUtils.join(",", getPersonalTags()) : "";
    }
}
