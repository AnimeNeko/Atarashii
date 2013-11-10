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
}
