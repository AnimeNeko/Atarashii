package net.somethingdreadful.MAL;

public abstract class GenericMALRecord {
	public static final int CLEAN = 0;
	public static final int DIRTY = 1;
	
	private int recordID;
	private String recordName;
	private String recordType;
	private String imageUrl;
	private String recordStatus;
	private String myStatus;
	private String memberScore;
	private String myScore;
	private String synopsis;
	private int SYNC_STATUS;
	
	public abstract void pushtoDB();
	
	public abstract void pullFromDB();
	
	public GenericMALRecord()
	{
		
	}
	
	
}
