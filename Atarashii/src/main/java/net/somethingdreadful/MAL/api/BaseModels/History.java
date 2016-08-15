package net.somethingdreadful.MAL.api.BaseModels;

import net.somethingdreadful.MAL.DateTools;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.GenericRecord;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class History implements Serializable {
    @Setter
    @Getter
    private int id;
    @Setter
    @Getter
    private int userId;
    @Setter
    @Getter
    private int replyCount;
    @Getter
    private String createdAt;
    @Setter
    @Getter
    private String status;
    @Setter
    @Getter
    private String value;

    /**
     * Type can be "A", "M", or "".
     */
    @Setter
    @Getter
    private String type = "";
    @Setter
    @Getter
    private String activityType;
    @Setter
    @Getter
    private ArrayList<Profile> users;
    @Setter
    @Getter
    private Anime anime;
    @Setter
    @Getter
    private Manga manga;

    public boolean isAnime() {
        return anime != null;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = DateTools.parseDate(createdAt, true);
    }

    public GenericRecord getSeries() {
        return isAnime() ? anime : manga;
    }
}
