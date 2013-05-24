package net.somethingdreadful.MAL.record;

import java.util.HashMap;

public class MangaRecord extends GenericMALRecord {

    public static final String STATUS_WATCHING = "reading";
    public static final String STATUS_PLANTOWATCH = "plan to read";

    public MangaRecord(HashMap<String, Object> record_data) {
        super(record_data);
    }

    public Integer getVolumesTotal() {
        return (Integer)this.recordData.get("volumesTotal");
    }

    @Override
    public String getTotal() {
        return Integer.toString(getChaptersTotal());
    }

    public Integer getVolumeProgress() {
        return getVolumesRead();
    }

    public Integer getVolumesRead() {
        return (Integer)this.recordData.get("volumesRead");
    }

    public void setVolumesRead(Integer read) {
        this.recordData.put("volumesRead", read);
    }

    public Integer getChaptersTotal() {
        return (Integer)this.recordData.get("chaptersTotal");
    }

    public Integer getChaptersRead() {
        return (Integer)this.recordData.get("chaptersRead");
    }

    public void setChaptersRead(Integer chaptersRead) {
        this.recordData.put("chaptersRead", chaptersRead);
    }

    @Override
    public Integer getPersonalProgress() {
        return getChaptersRead();
    }

    @Override
    public void setPersonalProgress(Integer amount) {
        setChaptersRead(amount);
    }

    public static HashMap<String, Class<?>> getTypeMap() {
        /* if (typeMap != null) {
            return typeMap;
        } */
        typeMap = GenericMALRecord.getTypeMap();
        typeMap.put("volumesTotal", Integer.class);
        typeMap.put("chaptersTotal", Integer.class);
        typeMap.put("volumesRead", Integer.class);
        typeMap.put("chaptersRead", Integer.class);
        return typeMap;
    }

}