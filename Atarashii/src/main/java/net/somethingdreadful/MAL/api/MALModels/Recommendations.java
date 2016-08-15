package net.somethingdreadful.MAL.api.MALModels;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Recommendations implements Serializable {

    /**
     * The anime/manga details.
     */
    @Getter
    @Setter
    public net.somethingdreadful.MAL.api.MALModels.AnimeManga.Anime item;

    /**
     * The recommendations details.
     */
    @Getter
    @Setter
    private ArrayList<Recommendation> recommendations;

    public class Recommendation implements Serializable {

        /**
         * information of a record.
         **/
        @Getter
        @Setter
        private String information;

        /**
         * The username of an user.
         **/
        @Getter
        @Setter
        private String username;
    }
}
