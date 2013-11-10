package net.somethingdreadful.MAL.api.response;

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
}
