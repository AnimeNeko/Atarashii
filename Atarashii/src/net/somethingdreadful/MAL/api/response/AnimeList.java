package net.somethingdreadful.MAL.api.response;

import net.somethingdreadful.MAL.account.AccountService;

import java.util.ArrayList;

import lombok.Getter;

public class AnimeList {
    // MyAnimeList
    private ArrayList<Anime> anime;
    @Getter private Statistics statistics;

    // AniList
    private List lists;
    private ArrayList<Anime> data;

    public ArrayList<Anime> getData() {
        ArrayList<Anime> list = new ArrayList<Anime>();
        for (Anime anime : data) {
            anime.createBaseModel();
            list.add(anime);
        }
        return list;
    }

    public ArrayList<Anime> getAnimes() {
        if (AccountService.isMAL()) {
            return anime;
        } else {
            ArrayList<Anime> list = new ArrayList<Anime>();
            if (lists.completed != null)
                list.addAll(lists.completed);
            if (lists.plan_to_watch != null)
                list.addAll(lists.plan_to_watch);
            if (lists.dropped != null)
                list.addAll(lists.dropped);
            if (lists.watching != null)
                list.addAll(lists.watching);
            if (lists.on_hold != null)
                list.addAll(lists.on_hold);
            return list;
        }
    }

    class List {
        public ArrayList<Anime> completed;
        public ArrayList<Anime> plan_to_watch;
        public ArrayList<Anime> dropped;
        public ArrayList<Anime> watching;
        public ArrayList<Anime> on_hold;
    }
}
