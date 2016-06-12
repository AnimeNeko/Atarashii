package net.somethingdreadful.MAL.api.ALModels;

import android.nfc.Tag;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.BaseModels.Forum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class ForumAL implements Serializable {
    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    @SerializedName("total")
    private Integer total;

    /**
     * Total number of threads on each page.
     */
    @Setter
    @Getter
    @SerializedName("per_page")
    private Integer perPage;

    /**
     * Total number of this page.
     */
    @Setter
    @Getter
    @SerializedName("current_page")
    private Integer currentPage;

    /**
     * Total number of pages.
     */
    @Setter
    @Getter
    @SerializedName("last_page")
    private Integer lastPage;

    /**
     * The start thread number.
     */
    @Setter
    @Getter
    @SerializedName("from")
    private Integer from;

    /**
     * The last thread number.
     */
    @Setter
    @Getter
    @SerializedName("to")
    private Integer to;

    /**
     * The threads information.
     */
    @Setter
    @Getter
    @SerializedName("data")
    private ArrayList<ThreadInfo> data = new ArrayList<>();

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
     * @param id   The category id
     * @param name The category id name
     * @return Forum The created model
     */
    private static Forum createModel(int id, String name, String descripion) {
        Forum forum = new Forum();
        forum.setId(id);
        forum.setName(name);
        forum.setDescription(descripion);
        return forum;
    }

    /**
     * Create replies for base model.
     *
     * @param username Last reply username
     * @param time     Last reply time
     * @return Forum the reply
     */
    private static Forum createReplyConverter(String username, String time) {
        Forum forum = new Forum();
        forum.setUsername(username);
        forum.setTime(time);
        return forum;
    }

    /**
     * Returns thread list.
     *
     * @return Arraylist forums.
     */
    public ArrayList<Forum> getForumListBase() {
        ArrayList<Forum> result = new ArrayList<>();
        if (getData() != null) {
            for (ThreadInfo threadInfo : getData()) {
                Forum forum = new Forum();
                forum.setId(threadInfo.getId());
                forum.setName(threadInfo.getTitle());
                if (threadInfo.getReplyUser() != null)
                    forum.setReply(createReplyConverter(threadInfo.getReplyUser().getDisplayName(), threadInfo.getLastReply()));
                result.add(forum);
            }
            if (result.size() >= 1 && getLastPage() != null)
                result.get(0).setMaxPages(getLastPage());
            else
                result.get(0).setMaxPages(1);
        }
        return result;
    }

    public class ThreadInfo {
        /**
         * Tread ID.
         */
        @Setter
        @Getter
        @SerializedName("id")
        private Integer id;

        /**
         * Tread title.
         */
        @Setter
        @Getter
        @SerializedName("title")
        private String title;

        /**
         * TODO: investigate the object type
         */
        @Setter
        @Getter
        @SerializedName("sticky")
        private Object sticky;

        /**
         * Last post time information.
         */
        @Setter
        @Getter
        @SerializedName("last_reply")
        private String lastReply;

        /**
         * The total amount of replies.
         */
        @Setter
        @Getter
        @SerializedName("reply_count")
        private Integer replyCount;

        /**
         * The total amount of views.
         */
        @Setter
        @Getter
        @SerializedName("view_count")
        private Integer viewCount;

        /**
         * All tags for this tread.
         */
        @Setter
        @Getter
        @SerializedName("tags")
        private List<Tag> tags = new ArrayList<>();

        /**
         * All anime tags for this tread.
         */
        @Setter
        @Getter
        @SerializedName("tags_anime")
        private List<Object> tagsAnime = new ArrayList<>();

        /**
         * All manga tags for this tread.
         */
        @Setter
        @Getter
        @SerializedName("tags_manga")
        private List<Object> tagsManga = new ArrayList<>();

        /**
         * Info about tread creator.
         */
        @Setter
        @Getter
        @SerializedName("user")
        private Profile user;

        /**
         * Last reply information.
         */
        @Setter
        @Getter
        @SerializedName("reply_user")
        private Profile replyUser;
    }
}
