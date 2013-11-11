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

	public String getReadStatus() {
		return read_status;
	}
	public void setReadStatus(String read_status) {
		this.read_status = read_status;
	}
	public int getChaptersRead() {
		return chapters_read;
	}
	public void setChaptersRead(int chapters_read) {
		this.chapters_read = chapters_read;
	}
	public int getVolumesRead() {
		return volumes_read;
	}
	public void setVolumesRead(int volumes_read) {
		this.volumes_read = volumes_read;
	}
	public int getChapters() {
		return chapters;
	}
	public int getVolumes() {
		return volumes;
	}
	public String getStatus() {
		return status;
	}
	public ArrayList<RelatedAnime> getAnimeAdaptions() {
		return anime_adaptions;
	}
	public ArrayList<RelatedManga> getRelatedManga() {
		return related_manga;
	}
	public ArrayList<RelatedManga> getAlternativeVersions() {
		return alternative_versions;
	}
}
