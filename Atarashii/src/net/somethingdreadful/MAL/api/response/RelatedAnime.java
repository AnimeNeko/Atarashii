package net.somethingdreadful.MAL.api.response;

// Basic anime details used for prequels, sequels, sidestories etc.
public class RelatedAnime {
	int anime_id;
	String title;
	String url;

	public int getAnimeId() {
		return anime_id;
	}
	public String getTitle() {
		return title;
	}
	public String getUrl() {
		return url;
	}
}
