package net.somethingdreadful.MAL.api.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class ForumMain implements Serializable {
    private ArrayList<Forum> MyAnimeList;
    @SerializedName("Anime & Manga") private ArrayList<Forum> AnimeManga;
    private ArrayList<Forum> General;


    public void setMyAnimeList(ArrayList<Forum> MyAnimeList) {
        this.MyAnimeList = MyAnimeList;
    }

    public ArrayList<Forum> getMyAnimeList() {
        return MyAnimeList;
    }

    public void setAnimeManga(ArrayList<Forum> AnimeManga) {
        this.AnimeManga = AnimeManga;
    }

    public ArrayList<Forum> getAnimeManga() {
        return AnimeManga;
    }

    public void setGeneral(ArrayList<Forum> General) {
        this.General = General;
    }

    public ArrayList<Forum> getGeneral() {
        return General;
    }

    public void setList(ArrayList<Forum> MyAnimeList) {
        this.MyAnimeList = MyAnimeList;
    }

    public ArrayList<Forum> getList() {
        return MyAnimeList;
    }
}
