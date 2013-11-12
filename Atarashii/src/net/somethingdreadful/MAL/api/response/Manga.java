package net.somethingdreadful.MAL.api.response;

public class Manga extends GenericRecord {
	int chapters;
	int volumes;
	String status;
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
}
