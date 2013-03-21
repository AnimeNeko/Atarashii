package net.somethingdreadful.MAL.record;

import java.util.HashMap;

public class AnimeRecord extends GenericMALRecord {

    public static final String STATUS_WATCHING = "watching";
    public static final String STATUS_PLANTOWATCH = "plan to watch";

    public AnimeRecord(HashMap<String, Object> record_data) {
        super(record_data);
    }

    @Override
    public String getTotal() {
        return recordData.get("episodesTotal").toString();
    }


    public void setEpisodesWatched(Integer watched) {
        recordData.put("episodesWatched", watched);
    }

    @Override
    public Integer getPersonalProgress() {
        return (Integer)recordData.get("episodesWatched");
    }

    @Override
    public void setPersonalProgress(Integer amount) {
        recordData.put("episodesWatched", amount);
    }

    public static HashMap<String, Class<?>> getTypeMap() {
        if (typeMap != null) {
            return typeMap;
        }
        typeMap = GenericMALRecord.getTypeMap();
        typeMap.put("episodesTotal", Integer.class);
        typeMap.put("episodesWatched", Integer.class);
        return typeMap;
    }

}