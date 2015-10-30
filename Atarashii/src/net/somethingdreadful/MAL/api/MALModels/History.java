package net.somethingdreadful.MAL.api.MALModels;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class History implements Serializable {

    /**
     * The anime/manga details.
     */
    @Getter
    @Setter
    private Series item;

    /**
     * The type of record.
     * <p/>
     * It indicates what kind of record the item is.
     * Example: "anime" or "manga"
     */
    @Getter
    @Setter
    private String type;

    /**
     * The time when the anime or manga was updated
     */
    @Getter
    @Setter
    @SerializedName("time_updated")
    private String timeUpdated;

    class Series {

        /**
         * The record of the ID.
         */
        @Getter
        @Setter
        private int id;

        /**
         * Total number of chapters of the manga.
         * <p/>
         * This value is the number of chapters of the manga, or null if unknown.
         */
        @Getter
        @Setter
        private int chapters;

        /**
         * Total number of episodes of the anime.
         * <p/>
         * This value is the number of episodes of the anime, or null if unknown.
         */
        @Getter
        @Setter
        private int episodes;

        /**
         * Title of a record.
         **/
        @Getter
        @Setter
        private String title;

        /**
         * The time when the record has been updated.
         **/
        @Getter
        @Setter
        @SerializedName("time_updated")
        private String timeUpdated;
    }

    public net.somethingdreadful.MAL.api.BaseModels.History createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.History model = new net.somethingdreadful.MAL.api.BaseModels.History();
        if (type.equals("anime")) {
            model.setAnime(new Anime());
            model.getAnime().setId(getItem().getId());
            model.getAnime().setEpisodes(getItem().getEpisodes());
            model.getAnime().setTitle(getItem().getTitle());
        } else {
            model.setManga(new Manga());
            model.getManga().setId(getItem().getId());
            model.getManga().setChapters(getItem().getChapters());
            model.getManga().setTitle(getItem().getTitle());
        }
        model.setCreatedAt(getTimeUpdated());
        return model;
    }

    public static ArrayList<net.somethingdreadful.MAL.api.BaseModels.History> convertBaseHistoryList(ArrayList<History> histories) {
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.History> historyArrayList = new ArrayList<>();
        for (History history: histories) {
            historyArrayList.add(history.createBaseModel());
        }
        return historyArrayList;
    }
}
