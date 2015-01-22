package net.somethingdreadful.MAL.api.response;

import net.somethingdreadful.MAL.account.AccountService;

import java.util.ArrayList;

import lombok.Setter;

public class MangaList {
    // MyAnimeList
    @Setter private ArrayList<Manga> manga;

    // AniList
    private List lists;
    private ArrayList<Manga> data;

    public ArrayList<Manga> getData() {
        ArrayList<Manga> list = new ArrayList<Manga>();
        for (Manga manga : data) {
            manga.createBaseModel();
            list.add(manga);
        }
        return list;
    }

    public ArrayList<Manga> getMangas() {
        if (AccountService.isMAL()) {
            return manga;
        } else {
            ArrayList<Manga> list = new ArrayList<Manga>();
            if (lists.completed != null)
                list.addAll(lists.completed);
            if (lists.plan_to_read != null)
                list.addAll(lists.plan_to_read);
            if (lists.dropped != null)
                list.addAll(lists.dropped);
            if (lists.reading != null)
                list.addAll(lists.reading);
            if (lists.on_hold != null)
                list.addAll(lists.on_hold);
            return list;
        }
    }

    class List {
        public ArrayList<Manga> completed;
        public ArrayList<Manga> plan_to_read;
        public ArrayList<Manga> dropped;
        public ArrayList<Manga> reading;
        public ArrayList<Manga> on_hold;
    }
}