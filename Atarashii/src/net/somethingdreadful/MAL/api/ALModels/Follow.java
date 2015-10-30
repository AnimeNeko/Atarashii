package net.somethingdreadful.MAL.api.ALModels;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.BaseModels.Profile;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Follow implements Serializable {
    @Setter
    @Getter
    private int id;
    @Setter
    @Getter
    @SerializedName("display_name")
    private String displayName;
    @Setter
    @Getter
    @SerializedName("image_url_lge")
    private String imageUrlLge;
    @Setter
    @Getter
    @SerializedName("image_url_med")
    private String imageUrlMed;

    public Profile createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.Profile model = new net.somethingdreadful.MAL.api.BaseModels.Profile();
        model.setUsername(getDisplayName());
        model.setImageUrl(getImageUrlLge());
        return model;
    }

    public static ArrayList<Profile> convertBaseFollowList(ArrayList<Follow> follows) {
        ArrayList<Profile> profiles = new ArrayList<>();
        for (Follow follower: follows) {
            profiles.add(follower.createBaseModel());
        }
        return profiles;
    }
}
