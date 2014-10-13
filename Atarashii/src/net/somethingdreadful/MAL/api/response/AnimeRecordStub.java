package net.somethingdreadful.MAL.api.response;

import java.io.Serializable;

public class AnimeRecordStub extends RecordStub implements Serializable {
    private int anime_id;

    public int getId() {
        return anime_id;
    }

    public void setId(int anime_id) {
        this.anime_id = anime_id;
    }
}
