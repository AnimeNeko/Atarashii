package net.somethingdreadful.MAL.api.BaseModels.AnimeManga;

import android.database.Cursor;

import net.somethingdreadful.MAL.DateTools;
import net.somethingdreadful.MAL.database.DatabaseHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;

public class Schedule implements Serializable {
    /**
     * The list of monday releases.
     */
    @Getter
    private ArrayList<Anime> monday = new ArrayList<>();

    /**
     * The list of tuesday releases.
     */
    @Getter
    private ArrayList<Anime> tuesday = new ArrayList<>();

    /**
     * The list of wednesday releases.
     */
    @Getter
    private ArrayList<Anime> wednesday = new ArrayList<>();

    /**
     * The list of thursday releases.
     */
    @Getter
    private ArrayList<Anime> thursday = new ArrayList<>();

    /**
     * The list of friday releases.
     */
    @Getter
    private ArrayList<Anime> friday = new ArrayList<>();

    /**
     * The list of saturday releases.
     */
    @Getter
    private ArrayList<Anime> saturday = new ArrayList<>();

    /**
     * The list of sunday releases.
     */
    @Getter
    private ArrayList<Anime> sunday = new ArrayList<>();

    /**
     * Sort the records using their name.
     *
     * @param records The records which should be sorted
     */
    private void sort(ArrayList<Anime> records) {
        Collections.sort(records != null && records.size() > 0 ? records : new ArrayList<Anime>(), new Comparator<Anime>() {
            @Override
            public int compare(Anime GR1, Anime GR2) {
                return GR1.getTitle().toLowerCase().compareTo(GR2.getTitle().toLowerCase());
            }
        });
    }

    public void setMonday(ArrayList<Anime> records) {
        this.monday = records;
        sort(monday);
    }

    public void setTuesday(ArrayList<Anime> records) {
        this.tuesday = records;
        sort(tuesday);
    }

    public void setWednesday(ArrayList<Anime> records) {
        this.wednesday = records;
        sort(wednesday);
    }

    public void setThursday(ArrayList<Anime> records) {
        this.thursday = records;
        sort(thursday);
    }

    public void setFriday(ArrayList<Anime> records) {
        this.friday = records;
        sort(friday);
    }

    public void setSaturday(ArrayList<Anime> records) {
        this.saturday = records;
        sort(saturday);
    }

    public void setSunday(ArrayList<Anime> records) {
        this.sunday = records;
        sort(sunday);
    }

    public boolean isNull() {
        boolean result = getMonday() == null && getTuesday() == null & getWednesday() == null && getThursday() == null &&
                getFriday() == null && getSaturday() == null && getSunday() == null;
        if (!result)
            result = getMonday().size() == 0 && getTuesday().size() == 0 & getWednesday().size() == 0 && getThursday().size() == 0 &&
                    getFriday().size() == 0 && getSaturday().size() == 0 && getSunday().size() == 0;
        return result;
    }

    public static Anime fromCursor(Cursor cursor) {
        List<String> columnNames = Arrays.asList(cursor.getColumnNames());
        Anime result = new Anime();
        net.somethingdreadful.MAL.api.ALModels.AnimeManga.Anime.Airing airing = new net.somethingdreadful.MAL.api.ALModels.AnimeManga.Anime.Airing();
        result.setAiring(airing);

        result.setId(cursor.getInt(columnNames.indexOf(DatabaseHelper.COLUMN_ID)));
        result.setTitle(cursor.getString(columnNames.indexOf("title")));
        result.setImageUrl(cursor.getString(columnNames.indexOf("imageUrl")));
        result.setType(cursor.getString(columnNames.indexOf("type")));
        result.setEpisodes(cursor.getInt(columnNames.indexOf("episodes")));
        result.setAverageScore(cursor.getString(columnNames.indexOf("avarageScore")));
        result.setAverageScoreCount(cursor.getString(columnNames.indexOf("averageScoreCount")));
        result.getAiring().setTime(cursor.getString(columnNames.indexOf("broadcast")));
        if (result.getAiring().getTime() != null)
            result.getAiring().setNormaltime(DateTools.parseDate(result.getAiring().getTime(), true));
        return result;
    }
}
