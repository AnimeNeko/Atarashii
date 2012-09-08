package net.somethingdreadful.MAL;

public class MangaRecord extends GenericMALRecord {

	private int volumesTotal;
	private int chaptersTotal;
	private int volumesRead;
	private int chaptersRead;
	
	public static final String STATUS_WATCHING = "reading";
	public static final String STATUS_COMPLETED = "completed";
	public static final String STATUS_ONHOLD = "on-hold";
	public static final String STATUS_DROPPED = "dropped";
	public static final String STATUS_PLANTOWATCH = "plan to read";
	
	public MangaRecord(int id, String name, String type, String status, String myStatus,
			int readVolumes, int readChapters, int totalVolumes, int totalChapters,
			String memberScore, String myScore, String synopsis, String imageUrl, int dirty) {
		this.recordID = id;
		this.recordName = name;
		this.recordType = type;
		this.imageUrl = imageUrl;
		this.recordStatus = status;
		this.myStatus = myStatus;
		this.memberScore = memberScore;
		this.myScore = myScore;
		this.synopsis = synopsis;
		
		this.volumesTotal = totalVolumes;
		this.chaptersTotal = totalChapters;
		this.volumesRead = readVolumes;
		this.chaptersRead = readChapters;
		
		this.dirty = dirty;
	}
	
	public MangaRecord(int id, String name, String type, String status, String myStatus,
			int readVolumes, int readChapters, int totalVolumes, int totalChapters,
			String myScore, String imageUrl, int dirty) {
		this.recordID = id;
		this.recordName = name;
		this.recordType = type;
		this.imageUrl = imageUrl;
		this.recordStatus = status;
		this.myStatus = myStatus;
		this.myScore = myScore;
		
		this.volumesTotal = totalVolumes;
		this.chaptersTotal = totalChapters;
		this.volumesRead = readVolumes;
		this.chaptersRead = readChapters;
		
		this.dirty = dirty;
		
	}

	public String getVolumeTotal() {
		return Integer.toString(chaptersTotal);
	}
	public String getTotal() {
		return Integer.toString(chaptersTotal);
	}
	
	public void setVolumesRead(int read) {
		this.volumesRead = read;
	}
	
	public void setChaptersRead(int read) {
		this.chaptersRead = read;
	}

	public int getVolumeProgress() {
		return volumesRead;
	}

	@Override
	public int getPersonalProgress() {
		return chaptersRead;
	}
	
	@Override
	public void setPersonalProgress(int amount) {
		this.chaptersRead = amount;
	}

}