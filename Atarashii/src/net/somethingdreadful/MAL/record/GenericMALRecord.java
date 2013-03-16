package net.somethingdreadful.MAL.record;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import java.util.HashMap;

public abstract class GenericMALRecord {
    public static final int CLEAN = 0;
    public static final int DIRTY = 1;

    //these are the same for both, so put them in here
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_ONHOLD = "on-hold";
    public static final String STATUS_DROPPED = "dropped";

    protected boolean FLAG_DELETE = false;


    protected boolean FLAG_CREATE = false;

    protected HashMap<String, Class<?>> typeMap;
    protected HashMap<String, Object> recordData;

    public abstract int getPersonalProgress();

    public abstract void setPersonalProgress(int amount);

    public abstract String getTotal();


    public GenericMALRecord(HashMap<String, Object> record_data) {
        this.recordData = record_data;
    }

    public Object getSafeValueOrDefault(String field) {
        try {
            Object value = recordData.get(field);
            if (value != null) {
                return value;
            }
        } catch (NullPointerException e) {
            Log.e("FAIL", Log.getStackTraceString(e));
            return null;
        }
        Class<?> cls = getTypeMap().get(field);
        if (cls == String.class) {
            return "";
        }
        if (cls == int.class) {
            return 0;
        }
        if (cls == float.class) {
            return 0.0;
        }
        return null;
    }

    public String getName() {
        return (String) recordData.get("recordName");
    }

    public String getImageUrl() {
        return (String) recordData.get("imageUrl");
    }

    public Integer getID() {
        return (int) recordData.get("recordID");
    }

    public String getRecordStatus() {
        return (String) recordData.get("recordStatus");
    }

    public float getMemberScore() {
        return (float) recordData.get("memberScore");
    }

    public void setMemberScore(float memberScore) {
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
        recordData.put("synopsis", newSynopsis);
    }

    public String getRecordType() {
        return (String) recordData.get("recordType");
    }

    public String getMyStatus() {
        return (String) this.getSafeValueOrDefault("myStatus");
    }

    public void setMyStatus(String status) {
        recordData.put("myStatus", status);
    }

    public int getMyScore() {
        return (int) this.getSafeValueOrDefault("myScore");
    }

    public void setMyScore(int myScore) {
        recordData.put("myScore", myScore);
    }

    public String getMyScoreString() {
        return Integer.toString(getMyScore());
    }

    public int getDirty() {
        return (int) this.getSafeValueOrDefault("dirty");
    }

    public void setDirty(int dirty) {
        recordData.put("dirty", dirty);
    }

    public int getLastUpdate() {
        return (int) this.getSafeValueOrDefault("lastUpdate");
    }

    public void setLastUpdate(int lastUpdate) {
        recordData.put("lastUpdate", lastUpdate);
    }

    public boolean hasCreate() {
        return FLAG_CREATE;
    }

    public void markForCreate(boolean FLAG_CREATE) {
        this.FLAG_CREATE = FLAG_CREATE;
    }

    public void markForDeletion(boolean mark) {
        FLAG_DELETE = mark;
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
        typeMap.put("lastUpdate", int.class);
        return typeMap;
    }
}