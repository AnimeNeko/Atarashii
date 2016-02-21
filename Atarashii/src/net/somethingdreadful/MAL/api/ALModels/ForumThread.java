package net.somethingdreadful.MAL.api.ALModels;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.BaseModels.Forum;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class ForumThread implements Serializable {
    /**
     * The thread id.
     */
    @Setter
    @Getter
    private Integer id;

    /**
     * The user thread creator id.
     */
    @Setter
    @Getter
    @SerializedName("user_id")
    private Integer userId;


    /**
     * The thread title.
     */
    @Setter
    @Getter
    private String title;

    /**
     * The thread content.
     */
    @Setter
    @Getter
    private String body;

    /**
     * The Comment comment.
     */
    @Setter
    @Getter
    private String comment;

    /**
     * If the thread is sticky.
     */
    @Setter
    @Getter
    private Integer sticky;

    /**
     * If the thread is locked.
     */
    @Setter
    @Getter
    private Object locked;

    /**
     * TODO: watch what this is
     */
    @Setter
    @Getter
    @SerializedName("last_reply")
    private String lastReply;

    /**
     * TODO: watch what this is
     */
    @Setter
    @Getter
    @SerializedName("last_reply_user")
    private Integer lastReplyUser;

    /**
     * Total number of replies.
     */
    @Setter
    @Getter
    @SerializedName("reply_count")
    private Integer replyCount;

    /**
     * Total number of views.
     */
    @Setter
    @Getter
    @SerializedName("view_count")
    private Integer viewCount;

    /**
     * The date of the deleted post/thread.
     */
    @Setter
    @Getter
    @SerializedName("deleted_at")
    private String deletedAt;

    /**
     * The date of the created post/thread.
     */
    @Setter
    @Getter
    @SerializedName("created_at")
    private String createdAt;

    /**
     * Arraylist of replies.
     */
    @Setter
    @Getter
    private ArrayList<Comment> comments = new ArrayList<>();

    /**
     * If you are a sub.
     */
    @Setter
    @Getter
    private Boolean subscribed;

    /**
     * Page information.
     */
    @Setter
    @Getter
    @SerializedName("page_data")
    private PageData pageData;

    /**
     * The user info of the replied user.
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

        if (getComments() != null && getComments().size() > 0)
            result.addAll(convert(getComments()));

        return result;
    }

    public ArrayList<Forum> convert(ArrayList<Comment> comments) {
        ArrayList<Forum> result = new ArrayList<>();
        if (comments != null && comments.size() > 0)
            for (Comment item : comments) {
                Forum forumItem = new Forum();
                forumItem.setId(item.getId());
                forumItem.setTime(item.getCreatedAt());
                forumItem.setComment(item.getComment());
                forumItem.setUsername(item.getUser().getDisplayName());
                net.somethingdreadful.MAL.api.MALModels.Profile profileItem = new net.somethingdreadful.MAL.api.MALModels.Profile();
                profileItem.setAvatarUrl(item.getUser().getImageUrl());
                forumItem.setProfile(profileItem);
                forumItem.setChildren(convert(item.getChildren()));
                result.add(forumItem);
            }
        return result;
    }

    public class PageData {
        /**
         * TODO: watch what this is
         */
        @Setter
        @Getter
        @SerializedName("total_root")
        private Integer totalRoot;

        /**
         * Total number of comments per page.
         */
        @Setter
        @Getter
        @SerializedName("per_page")
        private Integer perPage;

        /**
         * The page number.
         */
        @Setter
        @Getter
        @SerializedName("current_page")
        private Integer currentPage;

        /**
         * Total amount of pages.
         */
        @Setter
        @Getter
        @SerializedName("last_page")
        private Integer lastPage;

        /**
         * First page number.
         */
        @Setter
        @Getter
        private Integer from;

        /**
         * Last page number.
         */
        @Setter
        @Getter
        private Integer to;
    }

    public class Comment {
        /**
         * The comment ID.
         */
        @Setter
        @Getter
        private Integer id;

        /**
         * The id of the parent comment.
         */
        @SerializedName("parent_id")
        @Setter
        @Getter
        private Object parentId;

        /**
         * TODO: watch what this is
         */
        @Setter
        @Getter
        @SerializedName("depth")
        private Integer depth;

        /**
         * The replied userID
         */
        @SerializedName("user_id")
        @Setter
        @Getter
        private Integer userId;

        /**
         * Thread ID
         */
        @SerializedName("thread_id")
        @Setter
        @Getter
        private Integer threadId;

        /**
         * The comment.
         */
        @SerializedName("comment")
        @Setter
        @Getter
        private String comment;

        /**
         * The create date.
         */
        @SerializedName("created_at")
        @Setter
        @Getter
        private String createdAt;

        /**
         * The updated date.
         */
        @SerializedName("updated_at")
        @Setter
        @Getter
        private String updatedAt;

        /**
         * The user info of the comment
         */
        @SerializedName("user")
        @Setter
        @Getter
        private Profile user;

        /**
         * Sub comments.
         */
        @SerializedName("children")
        @Setter
        @Getter
        private ArrayList<Comment> children = new ArrayList<>();
    }
}
