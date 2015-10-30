package net.somethingdreadful.MAL.api.BaseModels.AnimeManga;

import net.somethingdreadful.MAL.api.BaseModels.Profile;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

public class Reviews implements Serializable {

    /**
     * The review ID
     */
    @Setter
    @Getter
    private int id;

    /**
     * The rating given by the user
     */
    @Setter
    @Getter
    private int rating;

    /**
     * The review content
     */
    @Setter
    @Getter
    private String review;

    /**
     * The creation date of the review
     */
    @Setter
    @Getter
    private String date;

    /**
     * Anime details
     */
    @Setter
    @Getter
    private Anime anime;

    /**
     * Manga details
     */
    @Setter
    @Getter
    private Manga manga;

    /**
     * User information
     */
    @Setter
    @Getter
    private Profile user;
}
