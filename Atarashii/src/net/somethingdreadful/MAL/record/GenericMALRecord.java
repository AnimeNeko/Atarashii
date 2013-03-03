package net.somethingdreadful.MAL.record;

import android.text.Html;
import android.text.Spanned;

import java.util.HashMap;

public abstract class GenericMALRecord {
    public static final int CLEAN = 0;
    public static final int DIRTY = 1;

    //these are the same for both, so put them in here
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_ONHOLD = "on-hold";
    public static final String STATUS_DROPPED = "dropped";

    protected int recordID;
    protected String recordName;
    protected String recordType;
    protected String imageUrl;
    protected String recordStatus;
    protected String myStatus;
    protected float memberScore;
    protected int myScore;
    protected String synopsis;
    protected int dirty;
    protected long lastUpdate;
    protected boolean FLAG_DELETE = false;
    protected boolean FLAG_CREATE = false;

    protected HashMap<String, Class<?>> typeMap;

    protected HashMap<String, Object> recordData;

    public abstract int getPersonalProgress();

    public abstract void setPersonalProgress(int amount);

    public abstract String getTotal();


    public GenericMALRecord() {

    }

    public GenericMALRecord(HashMap<String, Object> record_data) {
        this.recordData = record_data;
    }

    public GenericMALRecord(int id, String name, String type, String status,
                            float memberScore, String synopsis, String imageUrl) {
        this.recordID = id;
        this.recordName = name;
        this.recordType = type;
        this.imageUrl = imageUrl;
        this.recordStatus = status;
        this.memberScore = memberScore;
        this.synopsis = synopsis;
        this.myStatus = "";
    }

    public String getName() {
        return (String) recordData.get("recordName");
    }

    public String getImageUrl() {
        return (String) recordData.get("imageUrl");
    }

    public String getID() {
        return ((Integer) recordData.get("recordID")).toString();
    }

    public String getRecordStatus() {
        return (String) recordData.get("recordStatus");
    }

    public float getMemberScore() {
        return (float) recordData.get("memberScore");
    }

    public void setMemberScore(float memberScore) {
        this.memberScore = memberScore;
        recordData.put("memberScore", memberScore);
    }

    // Use this to get the raw HTML-formatted synopsis
    public String getSynopsis() {
        return (String) recordData.get("synopsis");
    }

    // Use this to get a formatted version of the text suited for display in the application
    public Spanned getSpannedSynopsis() {
        return Html.fromHtml(getSynopsis());
    }

    public void setSynopsis(String newSynopsis) {
        this.synopsis = newSynopsis;
        recordData.put("synopsis", newSynopsis);
    }

    public String getRecordType() {
        return (String) recordData.get("recordType");
    }

    public String getMyStatus() {
        return (String) recordData.get("myStatus");
    }

    public void setMyStatus(String status) {
        this.myStatus = status;
        recordData.put("myStatus", status);

    }

    public int getMyScore() {
        return (int) recordData.get("myScore");
    }

    public void setMyScore(int myScore) {
        this.myScore = myScore;
        recordData.put("myScore", myScore);
    }

    public String getMyScoreString() {
        return Integer.toString(getMyScore());
    }

    public int getDirty() {
        return (int) recordData.get("dirty");
    }

    public void setDirty(int dirty) {
        this.dirty = dirty;
        recordData.put("dirty", dirty);
    }

    public long getLastUpdate() {
        return (long) recordData.get("LastUpdate");
    }

    public void markForDeletion(boolean mark) {
        FLAG_DELETE = mark;
    }

    public boolean hasCreate() {
        return FLAG_CREATE;
    }

    public boolean hasDelete() {
        return FLAG_DELETE;

    }

    protected HashMap<String, Class<?>> getTypeMap() {
        if (typeMap != null) {
            return typeMap;
        }
        typeMap = new HashMap<>();

        typeMap.put("recordID", int.class);
        typeMap.put("recordName", String.class);
        typeMap.put("recordType", String.class);
        typeMap.put("recordStatus", String.class);
        typeMap.put("imageUrl", String.class);
        typeMap.put("memberScore", float.class);
        typeMap.put("synopsis", String.class);
        typeMap.put("myStatus", String.class);
        typeMap.put("myScore", int.class);
        typeMap.put("dirty", int.class);
        typeMap.put("lastUpdate", long.class);
        return typeMap;
    }
}