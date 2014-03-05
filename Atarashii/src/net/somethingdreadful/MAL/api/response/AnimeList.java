package net.somethingdreadful.MAL.api.response;

import java.util.ArrayList;

public class AnimeList {
	private ArrayList<Anime> anime;
	private Statistics statistics;

	public ArrayList<Anime> getAnimes() {
		return anime;
	}

	public Statistics getStatistics() {
		return statistics;
	}
}
