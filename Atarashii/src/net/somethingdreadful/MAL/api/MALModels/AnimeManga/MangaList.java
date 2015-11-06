package net.somethingdreadful.MAL.api.MALModels.AnimeManga;

import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.BrowseList;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class MangaList implements Serializable {
    @Setter
    @Getter
    private ArrayList<Manga> manga;

    @Setter
    @Getter
    private Statistics statistics;

    public class Statistics {
        @Setter
        @Getter
        private float days;
    }

    public static ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga> convertBaseArray(ArrayList<Manga> MALArray) {
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga> base = new ArrayList<>();
        for (Manga manga : MALArray) {
            base.add(manga.createBaseModel());
        }
        return base;
    }

    public static UserList createBaseModel(MangaList MALArray) {
        UserList userList = new UserList();
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga> MangaList = new ArrayList<>();
        for (Manga MALObject : MALArray.getManga()) {
            MangaList.add(MALObject.createBaseModel());
        }
        userList.setMangaList(MangaList);
        return userList;
    }

    public static BrowseList convertBaseBrowseList(ArrayList<net.somethingdreadful.MAL.api.MALModels.AnimeManga.Manga> MALArray) {
        BrowseList browseList = new BrowseList();
        browseList.setManga(convertBaseArray(MALArray));
        return browseList;
    }
}
