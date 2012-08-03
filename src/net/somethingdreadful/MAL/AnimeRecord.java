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
//	private int SYNC_STATUS;
	
	private int episodesWatched;
	private int episodesTotal;
	
	public AnimeRecord(int id, String name, String type, String status, String myStatus, int watched, int total, 
												String memberScore, String myScore, String synopsis, String imageUrl)
	{
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
		
	}
	
	public AnimeRecord(int id, String name, int watched)
	{
		this.recordID = id;
		this.recordName = name;
		this.episodesWatched = watched;
	}

	@Override
	public void pushtoDB() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pullFromDB() {
		// TODO Auto-generated method stub
		
	}
	
	public String getName()
	{
		return recordName;
	}
	
	public String getWatched()
	{
		return Integer.toString(episodesWatched);
	}

}
