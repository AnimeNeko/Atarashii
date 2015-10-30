package net.somethingdreadful.MAL.api.BaseModels;

import net.somethingdreadful.MAL.account.AccountType;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Backup implements Serializable {
    @Setter @Getter private ArrayList<Anime> animeList;
    @Setter @Getter private ArrayList<Manga> mangaList;
    @Setter @Getter private String username;
    @Setter @Getter private AccountType accountType;
}
