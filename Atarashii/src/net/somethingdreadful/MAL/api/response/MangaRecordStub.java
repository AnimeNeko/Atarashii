package net.somethingdreadful.MAL.api.response;

import java.io.Serializable;

public class MangaRecordStub extends RecordStub implements Serializable {
    private int manga_id;

    public int getId() {
        return manga_id;
    }

    public void setId(int manga_id) {
        this.manga_id = manga_id;
    }
}
