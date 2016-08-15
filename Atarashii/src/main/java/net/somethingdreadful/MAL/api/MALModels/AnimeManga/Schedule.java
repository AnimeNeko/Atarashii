package net.somethingdreadful.MAL.api.MALModels.AnimeManga;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Schedule implements Serializable {
    /**
     * The list of monday releases.
     */
    @Getter
    @Setter
    private ArrayList<Anime> monday;

    /**
     * The list of tuesday releases.
     */
    @Getter
    @Setter
    private ArrayList<Anime> tuesday;

    /**
     * The list of wednesday releases.
     */
    @Getter
    @Setter
    private ArrayList<Anime> wednesday;

    /**
     * The list of thursday releases.
     */
    @Getter
    @Setter
    private ArrayList<Anime> thursday;

    /**
     * The list of friday releases.
     */
    @Getter
    @Setter
    private ArrayList<Anime> friday;

    /**
     * The list of saturday releases.
     */
    @Getter
    @Setter
    private ArrayList<Anime> saturday;
    /**
     * The list of sunday releases.
     */
    @Getter
    @Setter
    private ArrayList<Anime> sunday;

    public net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule convertBaseSchedule() {
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule schedule = new net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule();
        schedule.setMonday(AnimeList.convertBaseArray(getMonday()));
        schedule.setTuesday(AnimeList.convertBaseArray(getTuesday()));
        schedule.setWednesday(AnimeList.convertBaseArray(getWednesday()));
        schedule.setThursday(AnimeList.convertBaseArray(getThursday()));
        schedule.setFriday(AnimeList.convertBaseArray(getFriday()));
        schedule.setSaturday(AnimeList.convertBaseArray(getSaturday()));
        schedule.setSunday(AnimeList.convertBaseArray(getSunday()));

        return schedule;
    }
}
