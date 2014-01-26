package net.somethingdreadful.MAL.api.response;

import java.util.Arrays;
import java.util.List;

import android.database.Cursor;

public class ProfileMangaStats {
    int completed;
    int dropped;
    int on_hold;
    int plan_to_read;
    int reading;
    Double time_days;
    int total_entries;
    
    public static ProfileMangaStats fromCursor(Cursor c) {
        ProfileMangaStats result = new ProfileMangaStats();

        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setCompleted(c.getInt(columnNames.indexOf("manga_completed")));
        result.setDropped(c.getInt(columnNames.indexOf("manga_dropped")));
        result.setOnHold(c.getInt(columnNames.indexOf("manga_on_hold")));
        result.setPlanToRead(c.getInt(columnNames.indexOf("manga_plan_to_read")));
        result.setTimeDays(c.getDouble(columnNames.indexOf("manga_time_days")));
        result.setTotalEntries(c.getInt(columnNames.indexOf("manga_total_entries")));
        result.setReading(c.getInt(columnNames.indexOf("manga_reading")));

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

    public int getPlanToRead() {
        return plan_to_read;
    }

    public void setPlanToRead(int plan_to_read) {
        this.plan_to_read = plan_to_read;
    }

    public int getReading() {
        return reading;
    }

    public void setReading(int reading) {
        this.reading = reading;
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
}
