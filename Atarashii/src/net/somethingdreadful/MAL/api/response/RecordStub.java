package net.somethingdreadful.MAL.api.response;

import java.io.Serializable;

/*
 * base stub class for relations returned by API like side stories, sequels etc
 */
public class RecordStub implements Serializable {
    private int anime_id = 0;
    private int manga_id = 0;
    private String title;
    private String url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setId(int id) {
        /* for setting id it doesn't matter if it is the anime_id or manga_id, this separation is only
         * for retrofits deserialization, just make sure that one is 0 (see getId() )
         */
        this.anime_id = id;
        this.manga_id = 0;
    }

    public int getId() {
        if (anime_id > 0)
            return anime_id;
        else
            return manga_id;
    }
}
