package net.somethingdreadful.MAL.api.ALModels;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.BaseModels.Forum;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class ForumAL implements Serializable {

    /**
     * The MyAnimeList category.
     */
    @Setter
    @Getter
    private ArrayList<Forum> MyAnimeList;

    /**
     * The Anime & Manga category.
     */
    @Setter
    @Getter
    @SerializedName("Anime & Manga")
    private ArrayList<Forum> AnimeManga;

    /**
     * The General category.
     */
    @Setter
    @Getter
    private ArrayList<Forum> General;

    /**
     * A general list for multi use.
     */
    @Setter
    @Getter
    private ArrayList<Forum> list;

    /**
     * Amount of pages.
     */
    @Setter
    @Getter
    private int pages;

    public static ArrayList<Forum> getForum() {
        ArrayList<Forum> result = new ArrayList<>();
        result.add(createModel(1, "Anime", "Discussion about anime only."));
        result.add(createModel(2, "Manga", "Discussion about manga only."));
        result.add(createModel(3, "Light Novels", "Discussion about light novels only."));
        result.add(createModel(4, "Visual Novels", "Discussion about visual novels only."));
        result.add(createModel(5, "Release Discussion", "Discussion regarding a new release, e.g. a new anime episode or manga chapter."));
        //result.add(createModel(6, "(Unused)"));
        result.add(createModel(7, "General", "Discussion which are common to the most."));
        result.add(createModel(8, "News", "The latest news about anime & manga."));
        result.add(createModel(9, "Music", "Discussion about music you like, dislike or discover new albums."));
        result.add(createModel(10, "Gaming", "Discussion about games only."));
        result.add(createModel(11, "Site Feedback", "Post here your feature requests (only for the website)."));
        result.add(createModel(12, "Bug Reports", "Report (the website) bugs here to receive support."));
        result.add(createModel(13, "Site Announcements", "AniList site announcements by Mods or Admins."));
        result.add(createModel(14, "List Customisation", "Discussion or help regarding list CSS and customisation."));
        result.add(createModel(15, "Recommendations", "Receive personal or general recommendations."));
        result.add(createModel(16, "Forum Games", "Forum games to kill time and make friends."));
        result.add(createModel(17, "Misc", "Any kind of post which doesn't really fit in the other categories."));
        result.add(createModel(18, "AniList Apps", "Dissussion of AniList API apps and services."));
        return result;
    }

    /**
     * Create forum models for AL category board.
     *
     * @param id The category id
     * @param name The category id name
     * @return Forum The created model
     */
    public static Forum createModel(int id, String name, String descripion) {
        Forum forum = new Forum();
        forum.setId(id);
        forum.setName(name);
        forum.setDescription(descripion);
        return forum;
    }


    public ArrayList<Forum> createBaseModel() {
        ArrayList<Forum> model = new ArrayList<>();
        if (getMyAnimeList() != null)
            model.addAll(getMyAnimeList());
        if (getAnimeManga() != null)
            model.addAll(getAnimeManga());
        if (getGeneral() != null)
            model.addAll(getGeneral());
        if (getList() != null) {
            model.addAll(getList());
            model.get(0).setMaxPages(getPages());
        }
        return model;
    }
}
