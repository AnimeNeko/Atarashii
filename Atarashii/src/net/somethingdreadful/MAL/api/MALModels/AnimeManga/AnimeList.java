package net.somethingdreadful.MAL.api.MALModels.AnimeManga;

import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class AnimeList implements Serializable {
    @Setter
    @Getter
    private ArrayList<Anime> anime;

    @Setter
    @Getter
    private Statistics statistics;

    public class Statistics {
        @Setter
        @Getter
        private float days;
    }

    public static UserList createBaseModel(AnimeList MALArray) {
        UserList userList = new UserList();
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime> animeList = new ArrayList<>();
        for (Anime MALObject : MALArray.getAnime()) {
            animeList.add(MALObject.createBaseModel());
        }
        userList.setAnimeList(animeList);
        return userList;
    }

    public static ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime> convertBaseArray(ArrayList<Anime> MALArray) {
        ArrayList<net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime> base = new ArrayList<>();
        if (MALArray != null)
            for (Anime anime : MALArray) {
                base.add(anime.createBaseModel());
            }
        return base;
    }
}
