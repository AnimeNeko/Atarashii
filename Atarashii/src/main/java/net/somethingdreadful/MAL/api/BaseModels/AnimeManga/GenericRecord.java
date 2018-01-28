package net.somethingdreadful.MAL.api.BaseModels.AnimeManga;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import com.google.gson.Gson;

import net.somethingdreadful.MAL.AppLog;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.MALModels.RecordStub;
import net.somethingdreadful.MAL.database.DatabaseHelper;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class GenericRecord implements Serializable {

    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_ONHOLD = "on-hold";
    public static final String STATUS_DROPPED = "dropped";

    public static final String STATUS_WATCHING = "watching";
    public static final String STATUS_REWATCHING = "rewatching";
    public static final String STATUS_PLANTOWATCH = "plan to watch";

    public static final String STATUS_READING = "reading";
    public static final String STATUS_REREADING = "rereading";
    public static final String STATUS_PLANTOREAD = "plan to read";

    public static final String CUSTOMLIST = "customList";

    private final String[] statusList = {"completed", "on-hold", "dropped", "watching", "plan to watch", "reading", "plan to read"};

    /**
     * The ID of the record
     */
    @Setter
    @Getter
    private int id;

    /**
     * Title of the record (Preference)
     */
    @Setter
    @Getter
    private String title;

    /**
     * Get the customList.
     *
     * Defined as 0 or 1 like 010010100101001
     */
    @Getter
    private String customList = "000000000000000";

    /**
     * Title of the record (Romaji)
     */
    @Setter
    @Getter
    private ArrayList<String> titleRomaji;

    /**
     * Title of the record (Japanese)
     */
    @Setter
    @Getter
    private ArrayList<String> titleJapanese;

    /**
     * Title of the record (English)
     */
    @Setter
    @Getter
    private ArrayList<String> titleEnglish;

    /**
     * Title of the record (Synonyms)
     */
    @Setter
    @Getter
    private ArrayList<String> titleSynonyms;

    /**
     * Type of record
     * <p/>
     * Manga values: manga, Novel, One Shot, Doujin, Manwha, Manhua, OEL ("OEL manga" refers to "Original English-Language manga")
     * Anime values: TV, Movie, OVA, ONA, Special, or Music.
     */
    @Setter
    @Getter
    private String type;

    /**
     * URL of an image to the record
     */
    @Setter
    @Getter
    private String imageUrl;

    /**
     * Description of the record
     * <p/>
     * An HTML-formatted string describing the anime/manga
     */
    @Setter
    private String synopsis;

    /**
     * Publishing/Airing status of the record
     * <p/>
     * Manga values: finished, publishing, not yet published.
     * Anime values: finished airing, currently airing, or not yet aired.
     */
    @Getter
    @Setter
    private String status;

    /**
     * Beginning date from when this anime/manga will be aired/published.
     */
    @Getter
    @Setter
    private String startDate;

    /**
     * End date when this anime/manga was ended being aired/published.
     */
    @Getter
    @Setter
    private String endDate;

    /**
     * User's score for the record, from 1 to 10
     */
    @Getter
    private int score;

    /**
     * Reading priority level for the title.
     * <p/>
     * Integer corresponding to the reading priority of the record from 0 (low) to 2 (high).
     * <p/>
     * Website: MyAnimeList
     */
    @Getter
    private int priority;

    /**
     * Rating of the anime/manga
     * <p/>
     * The rating is a freeform text field with no defined values.
     */
    @Getter
    @Setter
    private String classification;

    /**
     * The average score of the record chosen by the users.
     */
    @Setter
    @Getter
    private String averageScore;

    /**
     * The amount of users that contributed to the average score.
     * <p/>
     * Website: MyAnimeList
     */
    @Setter
    @Getter
    private String averageScoreCount;

    /**
     * Global rank of the record based on popularity (number of people with the title on the list).
     */
    @Setter
    @Getter
    private int popularity;

    /**
     * A list of popular tags for the record
     * <p/>
     * Website: MyAnimeList
     */
    @Setter
    @Getter
    private ArrayList<String> tags;

    /**
     * A list of genres for the record.
     */
    @Getter
    @Setter
    private ArrayList<String> genres;

    /**
     * The global rank of the record
     * <p/>
     * Website: MyAnimeList
     */
    @Setter
    @Getter
    private int rank;

    /**
     * A list of alternative versions of this record
     * <p/>
     * Website: MyAnimeList
     */
    @Setter
    @Getter
    private ArrayList<RecordStub> alternativeVersions;

    /**
     * A banner URL of the record
     * <p/>
     * Website: AniList
     */
    @Setter
    @Getter
    private String bannerUrl;

    /**
     * comments of an user.
     */
    @Getter
    private String notes;

    /**
     * Tags assigned by the user
     */
    @Getter
    private ArrayList<String> personalTags;

    /**
     * The number of members that have the record marked as a favorite
     * <p/>
     * Website: MyAnimeList
     */
    @Setter
    @Getter
    private int favoritedCount;

    /**
     * An array which contains the information which we should update.
     */
    @Setter
    @Getter
    private ArrayList<String> dirty;

    /**
     * The date of when the anime details got updated.
     */
    @Setter
    @Getter
    private Date lastSync;

    /**
     * Adds the record on the website after a sync.
     */
    private boolean createFlag;

    /**
     * Removes the record on the website after a sync.
     */
    private boolean deleteFlag;

    /**
     * The statics all user which indicates on which status they placed it.
     * <p/>
     * Website: AniList
     */
    @Getter
    @Setter
    private ListStats listStats;

    @Setter
    @Getter
    public static boolean fromCursor = false;
    public boolean isAnime;

    public void setScore(int score) {
        if (!fromCursor)
            addDirtyField("score");
        this.score = score;
    }

    public void setPriority(int priority) {
        if (!fromCursor)
            addDirtyField("priority");
        this.priority = priority;
    }

    public void setNotes(String notes) {
        if (!fromCursor)
            addDirtyField("notes");
        this.notes = notes;
    }

    public void setCustomList(String customList) {
        if (!fromCursor)
            addDirtyField("customList");
        this.customList = customList;
    }

    public String getCustomListAPI(){
        ArrayList<String> finalCustomLists = new ArrayList<>();
        for (int i = 0; i < getCustomList().length(); i++) {
                finalCustomLists.add(String.valueOf(getCustomList().charAt(i)));
        }
        for (int i = finalCustomLists.size(); i < 15; i++) {
            finalCustomLists.add("0");
        }
        return StringUtils.join(finalCustomLists, ",");
    }

    public void setPersonalTags(String tag) {
        ArrayList<String> tags = new ArrayList<>();
        Collections.addAll(tags, TextUtils.split(tag, ","));
        setPersonalTags(tags);
    }

    public String getSynopsisString() {
        return getSynopsis() != null ? synopsis.replace("/n/n/n/n", "/n/n") : null;
    }

    // Note: @Getter is not working on booleans
    public boolean getCreateFlag() {
        return createFlag;
    }

    // Note: @Getter is not working on booleans
    public boolean getDeleteFlag() {
        return deleteFlag;
    }

    private void setDeleteFlag(int deleteFlag) {
        this.deleteFlag = deleteFlag == 1;
    }

    private void setCreateFlag(int createFlag) {
        this.createFlag = createFlag == 1;
    }

    public void setDeleteFlag() {
        this.deleteFlag = true;
    }

    public void setCreateFlag() {
        this.createFlag = true;
    }

    void addDirtyField(String field) {
        if (dirty == null)
            dirty = new ArrayList<>();
        if (!dirty.contains((field)))
            dirty.add(field);
    }

    public void clearDirty() {
        dirty = null;
        this.createFlag = false;
        this.deleteFlag = false;
    }

    private ArrayList<Integer> getGenresInt(String[] fixedArray) {
        ArrayList<Integer> result = new ArrayList<>();
        if (getGenres() != null)
            for (String genre : getGenres())
                result.add(Arrays.asList(fixedArray).indexOf(genre));

        return result;
    }

    /**
     * Get the translation from strings.xml
     */
    String getStringFromResourceArray(Activity activity, int resArrayId, int index) {
        Resources res = activity.getResources();
        try {
            String[] types = res.getStringArray(resArrayId);
            if (index < 0 || index >= types.length) // make sure to have a valid array index
                return res.getString(R.string.unknown);
            else
                return types[index];
        } catch (Resources.NotFoundException e) {
            AppLog.logException(e);
            return res.getString(R.string.unknown);
        }
    }

    /**
     * Get the anime or manga genre translations
     */
    public ArrayList<String> getGenresString(Activity activity) {
        ArrayList<String> genres = new ArrayList<>();
        String[] fixedArray = activity.getResources().getStringArray(R.array.genresFixedArray_all);
        for (Integer genreInt : getGenresInt(fixedArray)) {
            genres.add(getStringFromResourceArray(activity, R.array.genresArray_all, genreInt));
        }
        return genres;
    }

    // Use this to get a formatted version of the text suited for display in the application
    public Spanned getSynopsis() {
        return (synopsis != null ? Html.fromHtml(synopsis) : null);
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
            if (c.getSuperclass() != null)
                return getField(c.getSuperclass(), property);
            else
                return null;
        }
    }

    private Object getPropertyValue(String property) {
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
        if (value != null)
            return (Integer) value;
        return null;
    }

    public String getStringPropertyValue(String property) {
        Object value = getPropertyValue(property);
        if (value != null)
            return (String) value;
        return null;
    }

    public String getArrayPropertyValue(String property) {
        ArrayList<String> array = (ArrayList<String>) getPropertyValue(property);
        Object value = array != null ? TextUtils.join(",", array) : "";
        return (String) value;
    }

    int getUserStatusInt(String statusString) {
        return Arrays.asList(statusList).indexOf(statusString);
    }

    public String getPersonalTagsString() {
        return getPersonalTags() != null ? TextUtils.join(",", getPersonalTags()) : "";
    }

    public void setPersonalTags(ArrayList<String> personalTags) {
        if (!fromCursor)
            addDirtyField("personalTags");
        this.personalTags = personalTags;
    }

    static GenericRecord fromCursor(GenericRecord result, Cursor cursor, List<String> columnNames) {
        GenericRecord.setFromCursor(true);
        result.setId(cursor.getInt(columnNames.indexOf(DatabaseHelper.COLUMN_ID)));
        result.setTitle(cursor.getString(columnNames.indexOf("title")));
        result.setCustomList(cursor.getString(columnNames.indexOf("customList")));
        result.setType(cursor.getString(columnNames.indexOf("type")));
        result.setImageUrl(cursor.getString(columnNames.indexOf("imageUrl")));
        result.setBannerUrl(cursor.getString(columnNames.indexOf("bannerUrl")));
        result.setSynopsis(cursor.getString(columnNames.indexOf("synopsis")));
        result.setStatus(cursor.getString(columnNames.indexOf("status")));
        result.setStartDate(cursor.getString(columnNames.indexOf("startDate")));
        result.setEndDate(cursor.getString(columnNames.indexOf("endDate")));
        result.setScore(cursor.getInt(columnNames.indexOf("score")));
        result.setPriority(cursor.getInt(columnNames.indexOf("priority")));
        result.setClassification(cursor.getString(columnNames.indexOf("classification")));
        result.setAverageScore(cursor.getString(columnNames.indexOf("averageScore")));
        result.setAverageScoreCount(cursor.getString(columnNames.indexOf("averageScoreCount")));
        result.setPopularity(cursor.getInt(columnNames.indexOf("popularity")));
        result.setRank(cursor.getInt(columnNames.indexOf("rank")));
        result.setNotes(cursor.getString(columnNames.indexOf("notes")));
        result.setFavoritedCount(cursor.getInt(columnNames.indexOf("favoritedCount")));
        result.setCreateFlag(cursor.getInt(columnNames.indexOf("createFlag")));
        result.setDeleteFlag(cursor.getInt(columnNames.indexOf("deleteFlag")));
        if (!cursor.isNull(columnNames.indexOf("dirty")))
            result.setDirty(new Gson().fromJson(cursor.getString(columnNames.indexOf("dirty")), ArrayList.class));

        if (!AccountService.isMAL()) {
            ListStats listStats = new ListStats();
            listStats.setPlanToRead(cursor.getInt(columnNames.indexOf("lsPlanned")));
            listStats.setPlanToWatch(cursor.getInt(columnNames.indexOf("lsPlanned")));
            listStats.setWatching(cursor.getInt(columnNames.indexOf("lsReadWatch")));
            listStats.setReading(cursor.getInt(columnNames.indexOf("lsReadWatch")));
            listStats.setCompleted(cursor.getInt(columnNames.indexOf("lsCompleted")));
            listStats.setOnHold(cursor.getInt(columnNames.indexOf("lsOnHold")));
            listStats.setDropped(cursor.getInt(columnNames.indexOf("lsDropped")));
            result.setListStats(listStats);
        }
        return result;
    }
}


