package net.somethingdreadful.MAL.api.response;

import java.util.Arrays;
import java.util.List;

import android.database.Cursor;

public class ProfileAnimeStats {
    int completed;
    int dropped;
    int on_hold;
    int plan_to_watch;
    Double time_days;
    int total_entries;
    int watching;
    
    public static ProfileAnimeStats fromCursor(Cursor c) {
        ProfileAnimeStats result = new ProfileAnimeStats();

        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setCompleted(c.getInt(columnNames.indexOf("anime_completed")));
        result.setDropped(c.getInt(columnNames.indexOf("anime_dropped")));
        result.setOnHold(c.getInt(columnNames.indexOf("anime_on_hold")));
        result.setPlanToWatch(c.getInt(columnNames.indexOf("anime_plan_to_watch")));
        result.setTimeDays(c.getDouble(columnNames.indexOf("anime_time_days")));
        result.setTotalEntries(c.getInt(columnNames.indexOf("anime_total_entries")));
        result.setWatching(c.getInt(columnNames.indexOf("anime_watching")));

        return result;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public int getDropped() {
        return dropped;
    }

    public void setDropped(int dropped) {
        this.dropped = dropped;
    }

    public int getOnHold() {
        return on_hold;
    }

    public void setOnHold(int on_hold) {
        this.on_hold = on_hold;
    }

    public int getPlanToWatch() {
        return plan_to_watch;
    }

    public void setPlanToWatch(int plan_to_watch) {
        this.plan_to_watch = plan_to_watch;
    }

    public Double getTimeDays() {
        return time_days;
    }

    public void setTimeDays(Double time_days) {
        this.time_days = time_days;
    }

    public int getTotalEntries() {
        return total_entries;
    }

    public void setTotalEntries(int total_entries) {
        this.total_entries = total_entries;
    }

    public int getWatching() {
        return watching;
    }

    public void setWatching(int watching) {
        this.watching = watching;
    }
}
