package net.somethingdreadful.MAL.api.response;

import android.database.Cursor;

import net.somethingdreadful.MAL.sql.MALSqlHelper;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class User implements Serializable {
    private String name;
    private Profile profile;
    private Integer id;

    public static User fromCursor(Cursor c) {
        return fromCursor(c, false);
    }

    public static User fromCursor(Cursor c, boolean friendDetails) {
        User result = new User();

        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setId(c.getInt(columnNames.indexOf(MALSqlHelper.COLUMN_ID)));
        result.setName(c.getString(columnNames.indexOf("username")));

        result.setProfile(Profile.fromCursor(c, friendDetails));
        return result;
    }

    public static boolean isDeveloperRecord(String name) {
        String[] developers = {
                "ratan12",
                "animasa",
                "motokochan",
                "apkawa",
                "d-sko"
        };
        return Arrays.asList(developers).contains(name.toLowerCase(Locale.US));
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
