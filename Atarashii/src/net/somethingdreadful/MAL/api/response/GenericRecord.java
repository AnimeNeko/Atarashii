package net.somethingdreadful.MAL.api.response;

import java.util.Date;

import android.text.Html;
import android.text.Spanned;

public class GenericRecord {
	
	// these are the same for both, so put them in here
	public static final String STATUS_COMPLETED = "completed";
	public static final String STATUS_ONHOLD = "on-hold";
	public static final String STATUS_DROPPED = "dropped";
	
	private int id;
	private String title;
	private String image_url;
	private String type;
	private String status;
	private int score;
	private float members_score;
	private String synopsis;
	private boolean dirty;
	private Date lastUpdate;
	private boolean flag_create;
	private boolean flag_delete;
	
	private transient boolean from_cursor = false;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getImageUrl() {
	    // if not loaded from cursor the image might point to an thumbnail
	    if ( from_cursor )
	        return image_url;
	    else
	        return image_url.replaceFirst("t.jpg$", ".jpg");
	}
	public void setImageUrl(String image_url) {
		this.image_url = image_url;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public float getMembersScore() {
		return members_score;
	}
	public void setMembersScore(float members_score) {
		this.members_score = members_score;
	}
	public String getSynopsis() {
		return synopsis;
	}
	public void setSynopsis(String synopsis) {
		this.synopsis = synopsis;
	}
	public boolean getDirty() {
		return dirty;
	}
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public boolean getCreateFlag() {
		return flag_create;
	}
	public void setCreateFlag(boolean flag_create) {
		this.flag_create = flag_create;
	}
	public boolean getDeleteFlag() {
		return flag_delete;
	}
	public void setDeleteFlag(boolean flag_delete) {
		this.flag_delete = flag_delete;
	}
	
	// Use this to get a formatted version of the text suited for display in the application
    public Spanned getSpannedSynopsis() {

        try {
            return Html.fromHtml(getSynopsis());
        }
        catch (NullPointerException npe) {
            return null;
        }
    }
    
    public boolean getCreatedFromCursor() {
        return from_cursor;
    }
    
    public void setCreatedFromCursor(boolean from_cursor) {
        this.from_cursor = from_cursor;
    }
}
