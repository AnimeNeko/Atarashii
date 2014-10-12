package net.somethingdreadful.MAL.api.response;

/*
 * base stub class for relations returned by API like side stories, sequels etc
 */
public class RecordStub {
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
}
