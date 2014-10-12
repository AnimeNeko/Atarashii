package net.somethingdreadful.MAL.api.response;

import android.database.Cursor;

import net.somethingdreadful.MAL.sql.MALSqlHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Anime extends GenericRecord implements Serializable {

    public static final String STATUS_WATCHING = "watching";
    public static final String STATUS_PLANTOWATCH = "plan to watch";

    private int episodes;
    private String watched_status;
    private int watched_episodes;
    private String classification;
    private int listed_anime_id;
    /*private Date start_date;
    private Date end_date;*/
    private ArrayList<AnimeRecordStub> alternative_versions;
    private ArrayList<AnimeRecordStub> character_anime;
    private ArrayList<AnimeRecordStub> side_stories;
    private ArrayList<AnimeRecordStub> summaries;
    private ArrayList<AnimeRecordStub> spin_offs;
    private ArrayList<MangaRecordStub> manga_adaptations;

    public static Anime fromCursor(Cursor c) {
        Anime result = new Anime();
        result.setCreatedFromCursor(true);
        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setId(c.getInt(columnNames.indexOf(MALSqlHelper.COLUMN_ID)));
        result.setTitle(c.getString(columnNames.indexOf("recordName")));
        result.setType(c.getString(columnNames.indexOf("recordType")));
        result.setStatus(c.getString(columnNames.indexOf("recordStatus")));
        result.setWatchedStatus(c.getString(columnNames.indexOf("myStatus")));
        result.setWatchedEpisodes(c.getInt(columnNames.indexOf("episodesWatched")));
        result.setEpisodes(c.getInt(columnNames.indexOf("episodesTotal")));
        result.setMembersScore(c.getFloat(columnNames.indexOf("memberScore")));
        result.setScore(c.getInt(columnNames.indexOf("myScore")));
        result.setSynopsis(c.getString(columnNames.indexOf("synopsis")));
        result.setImageUrl(c.getString(columnNames.indexOf("imageUrl")));
        result.setDirty(c.getInt(columnNames.indexOf("dirty")) > 0);
        result.setClassification(c.getString(columnNames.indexOf("classification")));
        result.setMembersCount(c.getInt(columnNames.indexOf("membersCount")));
        result.setFavoritedCount(c.getInt(columnNames.indexOf("favoritedCount")));
        result.setPopularityRank(c.getInt(columnNames.indexOf("popularityRank")));
        result.setRank(c.getInt(columnNames.indexOf("rank")));
        result.setListedId(c.getInt(columnNames.indexOf("listedId")));
        Date lastUpdateDate;
        try {
            long lastUpdate = c.getLong(columnNames.indexOf("lastUpdate"));
            lastUpdateDate = new Date(lastUpdate);
        } catch (Exception e) { // database entry was null
            lastUpdateDate = null;
        }
        result.setLastUpdate(lastUpdateDate);
        return result;
    }

    public int getEpisodes() {
        return episodes;
    }

    public void setEpisodes(int episodes) {
        this.episodes = episodes;
    }

    public String getWatchedStatus() {
        return watched_status;
    }

    public void setWatchedStatus(String watched_status) {
        this.watched_status = watched_status;
    }

    public int getWatchedEpisodes() {
        return watched_episodes;
    }

    public void setWatchedEpisodes(int watched_episodes) {
        this.watched_episodes = watched_episodes;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public int getListedId() {
        return listed_anime_id;
    }

    public void setListedId(int listed_anime_id) {
        this.listed_anime_id = listed_anime_id;
    }

    /*public Date getStartDate() {
        return start_date;
    }

    public void setStartDate(Date start_data) {
        this.start_date = start_data;
    }

    public Date getEndDate() {
        return end_date;
    }

    public void setEndDate(Date end_date) {
        this.end_date = end_date;
    }*/

    public ArrayList<AnimeRecordStub> getAlternativeVersions() {
        return alternative_versions;
    }

    public void setAlternativeVersions(ArrayList<AnimeRecordStub> alternative_versions) {
        this.alternative_versions = alternative_versions;
    }

    public ArrayList<AnimeRecordStub> getCharacterAnime() {
        return character_anime;
    }

    public void setCharacterAnime(ArrayList<AnimeRecordStub> character_anime) {
        this.character_anime = character_anime;
    }

    public ArrayList<AnimeRecordStub> getSideStories() {
        return side_stories;
    }

    public void setSideStories(ArrayList<AnimeRecordStub> side_stories) {
        this.side_stories = side_stories;
    }

    public ArrayList<AnimeRecordStub> getSummaries() {
        return summaries;
    }

    public void setSummaries(ArrayList<AnimeRecordStub> summaries) {
        this.summaries = summaries;
    }

    public ArrayList<AnimeRecordStub> getSpinOffs() {
        return spin_offs;
    }

    public void setSpinOffs(ArrayList<AnimeRecordStub> spin_offs) {
        this.spin_offs = spin_offs;
    }

    public ArrayList<MangaRecordStub> getMangaAdaptions() {
        return manga_adaptations;
    }

    public void setMangaAdaptions(ArrayList<MangaRecordStub> manga_adaptations) {
        this.manga_adaptations = manga_adaptations;
    }

    public int getTypeInt() {
        String[] types = {
                "TV",
                "Movie",
                "OVA",
                "ONA",
                "Special",
                "Music"
        };
        return Arrays.asList(types).indexOf(getType());
    }

    public int getStatusInt() {
        String[] status = {
                "finished airing",
                "currently airing",
                "not yet aired"
        };
        return Arrays.asList(status).indexOf(getStatus());
    }

    public int getWatchedStatusInt() {
        return getUserStatusInt(getWatchedStatus());
    }
}
