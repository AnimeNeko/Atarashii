package net.somethingdreadful.MAL.api.response;

public class Anime extends GenericRecord {
	int episodes;
	String status;
	String watched_status;
	int watched_episodes;

	public int getEpisodes() {
		return episodes;
	}
	public String getStatus() {
		return status;
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
