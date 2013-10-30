package net.somethingdreadful.MAL.api.response;

import java.util.Date;
import java.util.List;

public class Anime {
	int id;
	String title;
	OtherTitles other_titles;
	int rank;
	int popularity_rank;
	String image_url;
	String type;
	int episodes;
	String status;
	Date start_date;
	Date end_date;
	String classification;
	float members_score;
	int members_count;
	int favorited_count;
	String synopsis;
	List<String> genres;
	List<String> tags;
	List<RelatedManga> manga_adaptions;
	List<RelatedAnime> prequels;
	List<RelatedAnime> sequels;
	List<RelatedAnime> side_stories;
	List<RelatedAnime> parent_story;
	List<RelatedAnime> character_anime;
	List<RelatedAnime> spin_offs;
	List<RelatedAnime> alternative_versions;
	String watched_status;
	int watched_episodes;
	int score;
}
