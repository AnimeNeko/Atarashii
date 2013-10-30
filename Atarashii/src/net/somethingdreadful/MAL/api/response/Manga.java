package net.somethingdreadful.MAL.api.response;

import java.util.List;

public class Manga {
	int id;
	String title;
	OtherTitles other_titles;
	int rank;
	int popularity_rank;
	String image_url;
	String type;
	int chapters;
	int volumes;
	String status;
	float members_score;
	int members_count;
	int favorited_count;
	String synopsis;
	List<String> genres;
	List<String> tags;
	List<RelatedAnime> anime_adaptions;
	List<RelatedManga> related_manga;
	List<RelatedManga> alternative_versions;
	String read_status;
	int chapters_read;
	int volumes_read;
	int score;
}
