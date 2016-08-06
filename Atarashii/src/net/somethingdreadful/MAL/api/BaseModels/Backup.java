package net.somethingdreadful.MAL.api.BaseModels;

import net.somethingdreadful.MAL.account.AccountType;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Backup implements Serializable {
    @Setter
    private ArrayList<Anime> animeList;
    @Setter
    private ArrayList<Manga> mangaList;
    @Setter
    @Getter
    private String username;
    @Setter
    @Getter
    private AccountType accountType;

    public ArrayList<Anime> getAnimeList() {
        ArrayList<Anime> result = new ArrayList<>();
        for (Anime anime : animeList) {
            anime.setAllDirty();
            result.add(anime);
        }
        return result;
    }

    public ArrayList<Manga> getMangaList() {
        ArrayList<Manga> result = new ArrayList<>();
        for (Manga manga : mangaList) {
            manga.setAllDirty();
            result.add(manga);
        }
        return result;
    }
}
