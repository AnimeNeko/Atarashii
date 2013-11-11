package net.somethingdreadful.MAL.api.response;

import java.util.ArrayList;

public class AnimeList {
	ArrayList<Anime> anime;
	Statistics statistics;
	
	public ArrayList<Anime> getAnimes() {
		return anime;
	}
	public Statistics getStatistics() {
		return statistics;
	}
}
