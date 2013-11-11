package net.somethingdreadful.MAL.api.response;

import java.util.Date;
import java.util.List;

public class GenericRecord {
	int id;
	String title;
	OtherTitles other_titles;
	int rank;
	int popularity_rank;
	String image_url;
	String type;
	int score;
	float members_score;
	int members_count;
	int favorited_count;
	String synopsis;
	List<String> genres;
	List<String> tags;
	boolean dirty;
	Date lastUpdate;
	
	public int getId() {
		return id;
	}
	public String getTitle() {
		return title;
	}
	public OtherTitles getOtherTitles() {
		return other_titles;
	}
	public int getRank() {
		return rank;
	}
	public int getPopularityRank() {
		return popularity_rank;
	}
	public String getImageUrl() {
		return image_url;
	}
	public String getType() {
		return type;
	}
	public int getScore() {
		return score;
	}
	public float getMembersScore() {
		return members_score;
	}
	public int getMembersCount() {
		return members_count;
	}
	public int getFavoritedCount() {
		return favorited_count;
	}
	public String getSynopsis() {
		return synopsis;
	}
	public List<String> getGenres() {
		return genres;
	}
	public List<String> getTags() {
		return tags;
	}
	public boolean getDirty() {
		return dirty;
	}
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
}
