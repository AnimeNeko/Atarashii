package net.somethingdreadful.MAL.record;

import java.util.HashMap;

public class AnimeRecord extends GenericMALRecord {

    private int episodesWatched;
    private int episodesTotal;

    public static final String STATUS_WATCHING = "watching";
    public static final String STATUS_PLANTOWATCH = "plan to watch";

    public AnimeRecord(HashMap<String, Object> record_data) {
        super(record_data);
    }


    public AnimeRecord(int id, String name, String type, String status, String myStatus, int watched, int total,
                       float memberScore, int myScore, String synopsis, String imageUrl, int dirty, long lastUpdate) {
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

        this.dirty = dirty;
        this.lastUpdate = lastUpdate;

    }

    public AnimeRecord(int id, String name, String imageUrl, int watched, int totalEpisodes,
                       String myStatus, String animeStatus, String animeType, int myScore, int dirty, long lastUpdate) {
        this.recordID = id;
        this.recordName = name;
        this.episodesWatched = watched;
        this.imageUrl = imageUrl;
        this.myStatus = myStatus;
        this.episodesTotal = totalEpisodes;
        this.recordStatus = animeStatus;
        this.recordType = animeType;
        this.myScore = myScore;

        this.dirty = dirty;
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String getTotal() {
        return ((Integer) recordData.get("episodesTotal")).toString();
    }


    public void setEpisodesWatched(int watched) {
        this.episodesWatched = watched;
        recordData.put("episodesWatched", watched);
    }

    @Override
    public int getPersonalProgress() {
        return (int) recordData.get("episodesWatched");
    }

    @Override
    public void setPersonalProgress(int amount) {
        this.episodesWatched = amount;
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