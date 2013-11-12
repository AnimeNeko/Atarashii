package net.somethingdreadful.MAL.api.response;

import java.util.Date;

public class GenericRecord {
	int id;
	String title;
	int rank;
	int popularity_rank;
	String image_url;
	String type;
	int score;
	float members_score;
	int members_count;
	int favorited_count;
	String synopsis;
	boolean dirty;
	Date lastUpdate;
	boolean flag_create;
	boolean flag_delete;
	
	public int getId() {
		return id;
	}
	public String getTitle() {
		return title;
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
	public boolean getCreateFlag() {
		return flag_create;
	}
	public void setCreateFlag(boolean flag_create) {
		this.flag_create = flag_create;
	}
	public boolean getDeleteFlag() {
		return flag_delete;
	}
	public void setDeleteFlag(boolean flag_delete) {
		this.flag_delete = flag_delete;
	}
}
