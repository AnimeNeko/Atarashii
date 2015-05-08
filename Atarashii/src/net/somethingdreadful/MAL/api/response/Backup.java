package net.somethingdreadful.MAL.api.response;

import net.somethingdreadful.MAL.account.AccountType;

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
