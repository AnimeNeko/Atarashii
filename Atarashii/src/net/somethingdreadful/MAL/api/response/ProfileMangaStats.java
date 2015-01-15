package net.somethingdreadful.MAL.api.response;

import android.database.Cursor;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class ProfileMangaStats implements Serializable {
    @Getter @Setter private int completed;
    @Getter @Setter private int dropped;
    @Getter @Setter @SerializedName("on_hold") private int onHold;
    @Getter @Setter @SerializedName("plan_to_read") private int planToRead;
    @Getter @Setter private int reading;
    @Getter @Setter @SerializedName("time_days") private Double timeDays;
    @Getter @Setter @SerializedName("total_entries") private int totalEntries;

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
}
