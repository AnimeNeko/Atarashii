package net.somethingdreadful.MAL.api.ALModels.AnimeManga;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class BrowseAnimeList {
    @Getter
    @Setter
    private int total;
    @Getter
    @Setter
    @SerializedName("per_page")
    private int perPage;
    @Getter
    @Setter
    @SerializedName("current_page")
    private int currentPage;
    @Getter
    @Setter
    @SerializedName("last_page")
    private int lastPage;
    @Getter
    @Setter
    private int from;
    @Getter
    @Setter
    private int to;
    @Getter
    @Setter
    private ArrayList<Anime> data;

    public net.somethingdreadful.MAL.api.BaseModels.AnimeManga.BrowseList createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.BrowseList model = new net.somethingdreadful.MAL.api.BaseModels.AnimeManga.BrowseList();
        model.setTotal(getTotal());
        model.setPerPage(getPerPage());
        model.setCurrentPage(getCurrentPage());
        model.setLastPage(getLastPage());
        model.setFrom(getFrom());
        model.setTo(getTo());
        model.setAnime(convertBaseArray(getData()));
        return model;
    }

    public static ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime> convertBaseArray(ArrayList<Anime> ALArray) {
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime> base = new ArrayList<>();
        for (Anime anime : ALArray) {
            base.add(anime.createBaseModel());
        }
        return base;
    }
}
