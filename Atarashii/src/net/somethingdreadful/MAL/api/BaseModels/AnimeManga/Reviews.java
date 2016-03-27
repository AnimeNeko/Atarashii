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
     * The amount of episodes that has been seen
     */
    private String episodesSeen = "";

    /**
     * The amount of chapters that has been read
     */
    private String chaptersRead = "";

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

    /**
     * Return only a small part.
     *
     * @return A small part of the entire review
     */
    public String getShortReview() {
        if (getReview().length() > 250) {
            int i = getReview().indexOf("<br>", 220); // Check for new lines
            if (i == -1)
                i = getReview().indexOf(".", 220); // Check for dots to be sure...
            if (i == -1)
                i = getReview().indexOf(" ", 220); // If the review does not contain spaces...
            if (i == -1)
                return getReview().substring(0, 220); // Don't even bother to understand this
            return getReview().substring(0, i);
        }
        return getReview();
    }

    public void setEpisodesSeen(int episodesSeen) {
        if (episodesSeen > 0)
            this.episodesSeen = String.valueOf(episodesSeen);
    }

    public void setChaptersRead(int chaptersRead) {
        if (chaptersRead > 0)
            this.chaptersRead = String.valueOf(chaptersRead);
    }

    public String getEpisodesSeen(String string) {
        return episodesSeen.length() > 0 ? string + " " + episodesSeen : "";
    }

    public String getChaptersRead(String string) {
        return chaptersRead.length() > 0 ? string + " " + chaptersRead : "";
    }
}
