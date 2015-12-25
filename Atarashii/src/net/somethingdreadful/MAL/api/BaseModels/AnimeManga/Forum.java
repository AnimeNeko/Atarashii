package net.somethingdreadful.MAL.api.BaseModels.AnimeManga;

import net.somethingdreadful.MAL.api.MALModels.Profile;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Forum implements Serializable {

    /**
     * The ID used to get topic/board
     */
    @Setter
    @Getter
    private int id = 0;

    /**
     * The forum board/topic name.
     */
    @Setter
    @Getter
    private String name;

    /**
     * The username of the topic creator
     */
    @Setter
    @Getter
    private String username;

    /**
     * The number of replies of a topic
     */
    @Setter
    @Getter
    private int replies = 0;

    /**
     * The description of a board
     */
    @Setter
    @Getter
    private String description;

    /**
     * The info of the last reply inside a topic
     */
    @Setter
    @Getter
    private Forum reply;

    /**
     * The children of a forumboard
     */
    @Setter
    @Getter
    private ArrayList<Forum> children;

    /**
     * The comment content in an post
     */
    @Setter
    @Getter
    private String comment;

    /**
     * The creation time of this post
     */
    @Setter
    @Getter
    private String time;

    /**
     * The userprofile for the user details in topics
     */
    @Setter
    @Getter
    private Profile profile;
}
