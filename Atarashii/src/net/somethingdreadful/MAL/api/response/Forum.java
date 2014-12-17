package net.somethingdreadful.MAL.api.response;

import java.io.Serializable;
import java.util.ArrayList;

public class Forum implements Serializable {
    private int id;
    private String name;
    private String username;
    private int replies;
    private String description;
    private Forum reply;
    private ArrayList<Forum> children;
    private String comment;
    private String time;
    private Profile profile;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getReplies() {
        return replies;
    }

    public void setReplies(int replies) {
        this.replies = replies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Forum getReply() {
        return reply;
    }

    public void setReply(Forum reply) {
        this.reply = reply;
    }

    public ArrayList<Forum> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Forum> children) {
        this.children = children;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
