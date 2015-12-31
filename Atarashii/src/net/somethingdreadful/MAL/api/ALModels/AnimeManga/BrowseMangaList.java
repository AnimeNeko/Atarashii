package net.somethingdreadful.MAL.api.ALModels.AnimeManga;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class BrowseMangaList {
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
    private ArrayList<Manga> data;

    public net.somethingdreadful.MAL.api.BaseModels.AnimeManga.BrowseList createBaseModel() {
        net.somethingdreadful.MAL.api.BaseModels.AnimeManga.BrowseList model = new net.somethingdreadful.MAL.api.BaseModels.AnimeManga.BrowseList();
        model.setTotal(getTotal());
        model.setPerPage(getPerPage());
        model.setCurrentPage(getCurrentPage());
        model.setLastPage(getLastPage());
        model.setFrom(getFrom());
        model.setTo(getTo());
        model.setManga(convertBaseArray(getData()));
        return model;
    }

    public static ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga> convertBaseArray(ArrayList<Manga> ALArray) {
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga> base = new ArrayList<>();
        if (ALArray != null) {
            for (Manga manga : ALArray) {
                base.add(manga.createBaseModel());
            }
        }
        return base;
    }
}
