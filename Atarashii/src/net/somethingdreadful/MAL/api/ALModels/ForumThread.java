package net.somethingdreadful.MAL.api.ALModels;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.BaseModels.Forum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class ForumThread implements Serializable {
    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    private Integer id;

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    @SerializedName("user_id")
    private Integer userId;


    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    private String title;

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    private String body;

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    private String comment;

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    private Integer sticky;

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    private Object locked;

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    @SerializedName("last_reply")
    private String lastReply;

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    @SerializedName("last_reply_user")
    private Integer lastReplyUser;

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    @SerializedName("reply_count")
    private Integer replyCount;

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    @SerializedName("view_count")
    private Integer viewCount;

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    @SerializedName("deleted_at")
    private String deletedAt;

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    @SerializedName("created_at")
    private String createdAt;

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    private List<Comment> comments = new ArrayList<>();

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    private Boolean subscribed;

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    @SerializedName("page_data")
    private PageData pageData;

    /**
     * Total number of threads.
     */
    @Setter
    @Getter
    private Profile user;

    public ArrayList<Forum> convertBaseModel() {
        ArrayList<Forum> result = new ArrayList<>();

        // Add the post creator details first
        Forum forum = new Forum();
        if (getPageData() != null)
            forum.setMaxPages(getPageData().getLastPage());
        forum.setId(getId());
        forum.setTime(getCreatedAt());
        forum.setComment(getBody() != null ? getBody() : getComment());
        forum.setUsername(getUser().getDisplayName());
        net.somethingdreadful.MAL.api.MALModels.Profile profile = new net.somethingdreadful.MAL.api.MALModels.Profile();
        profile.setAvatarUrl(getUser().getImageUrl());
        forum.setProfile(profile);
        result.add(forum);

        for (Comment item : getComments()) {
            Forum forumItem = new Forum();
            forumItem.setId(item.getId());
            forumItem.setTime(item.getCreatedAt());
            forumItem.setComment(item.getComment());
            forumItem.setUsername(item.getUser().getDisplayName());
            net.somethingdreadful.MAL.api.MALModels.Profile profileItem = new net.somethingdreadful.MAL.api.MALModels.Profile();
            profileItem.setAvatarUrl(item.getUser().getImageUrl());
            forumItem.setProfile(profileItem);
            forumItem.setChildren(convertBaseModel(item));
            result.add(forumItem);
        }
        return result;
    }

    public ArrayList<Forum> convertBaseModel(Comment comment) {
        ArrayList<Forum> result = new ArrayList<>();
        if (comment.getChildren() != null)
            for (ForumThread item : comment.getChildren()) {
                result.addAll(item.convertBaseModel());
            }
        return result;
    }

    public class PageData {
        /**
         * Total number of threads.
         */
        @Setter
        @Getter
        @SerializedName("total_root")
        private Integer totalRoot;

        /**
         * Total number of threads.
         */
        @Setter
        @Getter
        @SerializedName("per_page")
        private Integer perPage;

        /**
         * Total number of threads.
         */
        @Setter
        @Getter
        @SerializedName("current_page")
        private Integer currentPage;

        /**
         * Total number of threads.
         */
        @Setter
        @Getter
        @SerializedName("last_page")
        private Integer lastPage;

        /**
         * Total number of threads.
         */
        @Setter
        @Getter
        private Integer from;

        /**
         * Total number of threads.
         */
        @Setter
        @Getter
        private Integer to;
    }

    public class Comment {
        /**
         * Total number of threads.
         */
        @Setter
        @Getter
        private Integer id;

        /**
         * Total number of threads.
         */
        @SerializedName("parent_id")
        @Setter
        @Getter
        private Object parentId;

        /**
         * Total number of threads.
         */
        @Setter
        @Getter
        @SerializedName("depth")
        private Integer depth;

        /**
         * Total number of threads.
         */
        @SerializedName("user_id")
        @Setter
        @Getter
        private Integer userId;

        /**
         * Total number of threads.
         */
        @SerializedName("thread_id")
        @Setter
        @Getter
        private Integer threadId;

        /**
         * Total number of threads.
         */
        @SerializedName("comment")
        @Setter
        @Getter
        private String comment;

        /**
         * Total number of threads.
         */
        @SerializedName("created_at")
        @Setter
        @Getter
        private String createdAt;

        /**
         * Total number of threads.
         */
        @SerializedName("updated_at")
        @Setter
        @Getter
        private String updatedAt;

        /**
         * Total number of threads.
         */
        @SerializedName("user")
        @Setter
        @Getter
        private Profile user;

        /**
         * Total number of threads.
         */
        @SerializedName("children")
        @Setter
        @Getter
        private ArrayList<ForumThread> children = new ArrayList<>();
    }
}
