package net.somethingdreadful.MAL.api.response;

import java.util.ArrayList;
import java.util.Date;

public class Anime extends GenericRecord {
	int episodes;
	String status;
	Date start_date;
	Date end_date;
	String classification;
	ArrayList<RelatedManga> manga_adaptions;
	ArrayList<RelatedAnime> prequels;
	ArrayList<RelatedAnime> sequels;
	ArrayList<RelatedAnime> side_stories;
	ArrayList<RelatedAnime> parent_story;
	ArrayList<RelatedAnime> character_anime;
	ArrayList<RelatedAnime> spin_offs;
	ArrayList<RelatedAnime> alternative_versions;
	String watched_status;
	int watched_episodes;

	public int getEpisodes() {
		return episodes;
	}
	public String getStatus() {
		return status;
	}
	public Date getStartDate() {
		return start_date;
	}
	public Date getEndDate() {
		return end_date;
	}
	public String getClassification() {
		return classification;
	}
	public ArrayList<RelatedManga> getMangaAdaptions() {
		return manga_adaptions;
	}
	public ArrayList<RelatedAnime> getPrequels() {
		return prequels;
	}
	public ArrayList<RelatedAnime> getSequels() {
		return sequels;
	}
	public ArrayList<RelatedAnime> getSideStories() {
		return side_stories;
	}
	public ArrayList<RelatedAnime> getParentStory() {
		return parent_story;
	}
	public ArrayList<RelatedAnime> getCharacterAnime() {
		return character_anime;
	}
	public ArrayList<RelatedAnime> getSpinOffs() {
		return spin_offs;
	}
	public ArrayList<RelatedAnime> getAlternativeVersions() {
		return alternative_versions;
	}
	public String getWatchedStatus() {
		return watched_status;
	}
	public void setWatchedStatus(String watched_status) {
		this.watched_status = watched_status;
	}
	public int getWatchedEpisodes() {
		return watched_episodes;
	}
	public void setWatchedEpisodes(int watched_episodes) {
		this.watched_episodes = watched_episodes;
	}
}
