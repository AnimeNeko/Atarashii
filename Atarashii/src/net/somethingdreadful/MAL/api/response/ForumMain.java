package net.somethingdreadful.MAL.api.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class ForumMain implements Serializable {
    @Setter @Getter private ArrayList<Forum> MyAnimeList;
    @Setter @Getter @SerializedName("Anime & Manga") private ArrayList<Forum> AnimeManga;
    @Setter @Getter private ArrayList<Forum> General;
    @Setter @Getter private ArrayList<Forum> list;
    @Setter @Getter private int pages;
}
