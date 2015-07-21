package net.somethingdreadful.MAL.api.response;

import net.somethingdreadful.MAL.api.MALApi;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/*
 * Base stub class for relations returned by API like side stories, sequels etc.
 * It contains both manga_id and anime_id to make it usable as response class for deserialization
 * through retrofit. Only one of those variables is set to a valid value.
 */
public class RecordStub implements Serializable {
    private int anime_id = 0;
    private int manga_id = 0;
    @Getter @Setter private String title;
    @Getter @Setter private String url;

    public void setId(int id, MALApi.ListType type) {
        this.anime_id = type.equals(MALApi.ListType.ANIME) ? id : 0;
        this.manga_id = type.equals(MALApi.ListType.MANGA) ? id : 0;
    }

    public int getId() {
        if (anime_id > 0)
            return anime_id;
        else
            return manga_id;
    }

    public MALApi.ListType getType() {
        if (anime_id > 0)
            return MALApi.ListType.ANIME;
        if (manga_id > 0)
            return MALApi.ListType.MANGA;
        return null;
    }
}
