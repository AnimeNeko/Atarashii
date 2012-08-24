package net.somethingdreadful.MAL;

import net.somethingdreadful.MAL.R;

public abstract class GenericMALRecord {
	public static final int CLEAN = 0;
	public static final int DIRTY = 1;
	
	protected int recordID;
	protected String recordName;
	protected String recordType;
	protected String imageUrl;
	protected String recordStatus;
	protected String myStatus;
	protected String memberScore;
	protected String myScore;
	protected String synopsis;
	protected int SYNC_STATUS;
	
	public abstract void pushtoDB();
	
	public abstract void pullFromDB();
	
	public GenericMALRecord()
	{
		
	}
	
	
}
