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


    public void setEpisodesWatched(int watched) {
        recordData.put("episodesWatched", watched);
    }

    @Override
    public int getPersonalProgress() {
        return (Integer)recordData.get("episodesWatched");
    }

    @Override
    public void setPersonalProgress(int amount) {
        recordData.put("episodesWatched", amount);
    }

    @Override
    protected HashMap<String, Class<?>> getTypeMap() {
        if (typeMap != null) {
            return typeMap;
        }
        typeMap = super.getTypeMap();
        typeMap.put("episodesTotal", int.class);
        typeMap.put("episodesWatched", int.class);
        return typeMap;
    }

}