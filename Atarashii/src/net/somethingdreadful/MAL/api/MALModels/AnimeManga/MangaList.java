package net.somethingdreadful.MAL.api.MALModels.AnimeManga;

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

    public class Statistics implements Serializable {
        @Setter
        @Getter
        private float days;
    }

    public static UserList createBaseModel(MangaList MALArray) {
        UserList userList = new UserList();
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga> MangaList = new ArrayList<>();
        if (MALArray != null)
            MangaList = convertBaseArray(MALArray.getManga());
        userList.setMangaList(MangaList);
        return userList;
    }

    public static ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga> convertBaseArray(ArrayList<Manga> MALArray) {
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga> base = new ArrayList<>();
        if (MALArray != null)
            for (Manga manga : MALArray) {
                base.add(manga.createBaseModel());
            }
        return base;
    }
}
