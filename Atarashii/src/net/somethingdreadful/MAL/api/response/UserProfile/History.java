package net.somethingdreadful.MAL.api.response.UserProfile;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.response.AnimeManga.Series;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class History implements Serializable {
    @Setter @Getter private int id;
    @Setter @Getter @SerializedName("user_id") private int userId;
    @Setter @Getter @SerializedName("reply_count") private int replyCount;
    @Setter @Getter @SerializedName("created_at") private String createdAt;
    @Setter @Getter private String status;
    @Setter @Getter private String value;
    @Setter @Getter @SerializedName("activity_type") private String activityType;
    @Setter @Getter private ArrayList<Profile> users;
    @Setter @Getter private Series series;
}
