package net.somethingdreadful.MAL.api.response;

//Basic manga details used for related, alternative
public class RelatedManga {
	int manga_id;
	String title;
	String url;

	public int getMangaId() {
		return manga_id;
	}
	public String getTitle() {
		return title;
	}
	public String getUrl() {
		return url;
	}
}
