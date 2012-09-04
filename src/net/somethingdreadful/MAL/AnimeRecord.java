package net.somethingdreadful.MAL;

public class AnimeRecord extends GenericMALRecord {
	
//	private int recordID;
//	private String recordName;
//	private String recordType;
//	private String imageUrl;
//	private String recordStatus;
//	private String myStatus;
//	private String memberScore;
//	private String myScore;
//	private String synopsis;
//	private boolean dirty;
	
	private int episodesWatched;
	private int episodesTotal;
	
	public static final String STATUS_WATCHING = "watching";
	public static final String STATUS_COMPLETED = "completed";
	public static final String STATUS_ONHOLD = "on-hold";
	public static final String STATUS_DROPPED = "dropped";
	public static final String STATUS_PLANTOWATCH = "plan to watch";
	
	public AnimeRecord(int id, String name, String type, String status, String myStatus, int watched, int total, 
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
		
		this.episodesTotal = total;
		this.episodesWatched = watched;
		
		this.dirty = dirty;
		
	}
	
	public AnimeRecord(int id, String name, String imageUrl, int watched, int totalEpisodes, 
			String myStatus, String animeStatus, String animeType, String myScore, int dirty) {
		this.recordID = id;
		this.recordName = name;
		this.episodesWatched = watched;
		this.imageUrl = imageUrl;
		this.myStatus = myStatus;
		this.episodesTotal = totalEpisodes;
		this.recordStatus = animeStatus;
		this.recordType = animeType;
		this.myScore = myScore;
		
		this.dirty = dirty;
	}

	
	public String getWatched() {
		return Integer.toString(episodesWatched);
	}
	
	public String getTotal() {
		
		return Integer.toString(episodesTotal);
	}
	
	
	public void setEpisodesWatched(int watched) {
		this.episodesWatched = watched;
	}

	@Override
	public int getPersonalProgress() {
		return episodesWatched;
	}

}