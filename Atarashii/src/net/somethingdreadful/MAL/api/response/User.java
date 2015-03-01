package net.somethingdreadful.MAL.api.response;

import android.database.Cursor;

import net.somethingdreadful.MAL.sql.MALSqlHelper;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;

public class User implements Serializable {
    @Getter @Setter private String name;
    @Getter @Setter private Profile profile;
    @Getter @Setter private Integer id;

    public static User fromCursor(Cursor c) {
        User result = new User();

        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setId(c.getInt(columnNames.indexOf(MALSqlHelper.COLUMN_ID)));
        result.setName(c.getString(columnNames.indexOf("username")));

        result.setProfile(Profile.fromCursor(c));
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
}
