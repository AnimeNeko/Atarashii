package net.somethingdreadful.MAL.api.ALModels.AnimeManga;

import com.google.gson.annotations.SerializedName;

import net.somethingdreadful.MAL.api.ALModels.Profile;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Reviews implements Serializable {
    @Setter
    @Getter
    private int id;
    @Setter
    @Getter
    @SerializedName("rating_amount")
    private int ratingAmount;
    @Setter
    @Getter
    private String summary;
    @Setter
    @Getter
    @SerializedName("user_rating")
    private int userRating;
    @Setter
    @Getter
    private String text;
    @Setter
    @Getter
    private int score;
    @Setter
    @Getter
    private Profile user;
    @Setter
    @Getter
    private String date;
    @Setter
    @Getter
    private int rating;
    @Setter
    @Getter
    private Anime anime;
    @Setter
    @Getter
    private Manga manga;

    private net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews model = new net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews();
        model.setId(getId());
        model.setRating(getScore());
        String comment = getText().replaceAll("(\r\n|\n)", "<br>");
        comment = comment.replaceAll("__((.|\\n)+?)__", "<b>$1</b>");
        comment = comment.replaceAll("_((.|\\n)+?)_", "<i>$1</i>");
        comment = comment.replaceAll("~~~((.|\\n)+?)~~~", "<center>$1</center>");
        comment = comment.replaceAll("(.*)##(.*)", "<h1>$2</h1>");
        comment = comment.replaceAll("(.*)#(.*)", "<h1>$2</h1>");
        comment = comment.replaceAll("@(\\w+)", "<font color=\"#022f70\"><b>@$1</b></font>");
        model.setReview(comment);
        model.setUser(getUser().createBaseModel());
        model.setDate(getDate());
        if (getAnime() != null)
            model.setAnime(getAnime().createBaseModel());
        if (getManga() != null)
            model.setManga(getManga().createBaseModel());
        return model;
    }

    public static ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews> convertBaseArray(ArrayList<Reviews> ALArray) {
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews> base = new ArrayList<>();
        for (Reviews reviews : ALArray) {
            base.add(reviews.createBaseModel());
        }
        return base;
    }
}
