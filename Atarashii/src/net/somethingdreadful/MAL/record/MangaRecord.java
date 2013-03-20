package net.somethingdreadful.MAL.record;

import java.util.HashMap;

public class MangaRecord extends GenericMALRecord {

    public static final String STATUS_WATCHING = "reading";
    public static final String STATUS_PLANTOWATCH = "plan to read";

    public MangaRecord(HashMap<String, Object> record_data) {
        super(record_data);
    }

    public int getVolumesTotal() {
        return (int) this.recordData.get("volumesTotal");
    }

    @Override
    public String getTotal() {
        return Integer.toString(getChaptersTotal());
    }

    public int getVolumeProgress() {
        return getVolumesRead();
    }

    public int getVolumesRead() {
        return (int) this.recordData.get("volumesRead");
    }

    public void setVolumesRead(int read) {
        this.recordData.put("volumesRead", read);
    }

    public int getChaptersTotal() {
        return (int) this.recordData.get("chaptersTotal");
    }

    public int getChaptersRead() {
        return (int) this.recordData.get("chaptersRead");
    }

    public void setChaptersRead(int chaptersRead) {
        this.recordData.put("chaptersRead", chaptersRead);
    }

    @Override
    public int getPersonalProgress() {
        return getChaptersRead();
    }

    @Override
    public void setPersonalProgress(int amount) {
        setChaptersRead(amount);
    }

    @Override
    protected HashMap<String, Class<?>> getTypeMap() {
        if (typeMap != null) {
            return typeMap;
        }
        typeMap = super.getTypeMap();
        typeMap.put("volumesTotal", int.class);
        typeMap.put("chaptersTotal", int.class);
        typeMap.put("volumesRead", int.class);
        typeMap.put("chaptersRead", int.class);
        return typeMap;
    }

}