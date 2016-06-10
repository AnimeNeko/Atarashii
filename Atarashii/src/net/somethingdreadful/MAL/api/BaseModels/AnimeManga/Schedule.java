package net.somethingdreadful.MAL.api.BaseModels.AnimeManga;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import lombok.Getter;

public class Schedule implements Serializable {
    /**
     * The list of monday releases.
     */
    @Getter
    private ArrayList<Anime> monday;

    /**
     * The list of tuesday releases.
     */
    @Getter
    private ArrayList<Anime> tuesday;

    /**
     * The list of wednesday releases.
     */
    @Getter
    private ArrayList<Anime> wednesday;

    /**
     * The list of thursday releases.
     */
    @Getter
    private ArrayList<Anime> thursday;

    /**
     * The list of friday releases.
     */
    @Getter
    private ArrayList<Anime> friday;

    /**
     * The list of saturday releases.
     */
    @Getter
    private ArrayList<Anime> saturday;
    /**
     * The list of sunday releases.
     */
    @Getter
    private ArrayList<Anime> sunday;

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
}
