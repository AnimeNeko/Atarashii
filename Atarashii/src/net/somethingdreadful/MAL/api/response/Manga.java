package net.somethingdreadful.MAL.api.response;

import java.util.ArrayList;

public class Manga extends GenericRecord {
	int chapters;
	int volumes;
	String status;
	ArrayList<RelatedAnime> anime_adaptions;
	ArrayList<RelatedManga> related_manga;
	ArrayList<RelatedManga> alternative_versions;
	String read_status;
	int chapters_read;
	int volumes_read;
}
