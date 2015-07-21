package net.somethingdreadful.MAL.api.response;

import android.database.Cursor;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class ProfileAnimeStats implements Serializable {
    @Getter @Setter private int completed;
    @Getter @Setter private int dropped;
    @Getter @Setter @SerializedName("on_hold") private int onHold;
    @Getter @Setter @SerializedName("plan_to_watch") private int planToWatch;
    @Getter @Setter @SerializedName("time_days") private Double timeDays;
    @Getter @Setter @SerializedName("total_entries") private int totalEntries;
    @Getter @Setter private int watching;

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
}
