package net.somethingdreadful.MAL.api.response;

import java.util.Date;

public class Anime extends GenericRecord {
	int episodes;
	String status;
	Date start_date;
	Date end_date;
	String classification;
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
