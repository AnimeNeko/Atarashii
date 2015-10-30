package net.somethingdreadful.MAL.api.BaseModels.AnimeManga;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class UserList implements Serializable {
    @Getter
    @Setter
    private ArrayList<Anime> animeList;
    @Getter
    @Setter
    private ArrayList<Manga> mangaList;
}
