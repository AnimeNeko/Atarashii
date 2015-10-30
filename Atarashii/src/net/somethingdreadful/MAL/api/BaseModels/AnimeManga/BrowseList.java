package net.somethingdreadful.MAL.api.BaseModels.AnimeManga;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class BrowseList {
    /**
     * The total amount of pages.
     */
    @Getter
    @Setter
    private int total;

    /**
     * Items amount per page.
     */
    @Getter
    @Setter
    private int perPage;

    /**
     * The number of the current item page.
     */
    @Getter
    @Setter
    private int currentPage;

    /**
     * The number of the last page.
     */
    @Getter
    @Setter
    private int lastPage;

    /**
     * The first item number on this page.
     */
    @Getter
    @Setter
    private int from;

    /**
     * The last item number on this page.
     */
    @Getter
    @Setter
    private int to;

    /**
     * The anime details.
     */
    @Getter
    @Setter
    private ArrayList<Anime> anime;

    /**
     * The manga details.
     */
    @Getter
    @Setter
    private ArrayList<Manga> manga;

    public ArrayList<GenericRecord> getList() {
        return (ArrayList<GenericRecord>) (anime == null ? manga : anime);
    }
}
