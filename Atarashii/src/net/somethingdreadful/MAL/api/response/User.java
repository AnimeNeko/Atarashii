package net.somethingdreadful.MAL.api.response;

import java.util.Arrays;
import java.util.List;

import android.database.Cursor;

public class User {
    String name;
    Profile profile;
    
    public static User fromCursor(Cursor c) {
        return fromCursor(c, false);
    }
    
    public static User fromCursor(Cursor c, boolean friendDetails) {
        User result = new User();

        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setName(c.getString(columnNames.indexOf("username")));
        
        result.setProfile(Profile.fromCursor(c));
        return result;
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
    
    public static boolean isDeveloperRecord(String name){ 
    if (name.equals("Ratan12") || name.equals("ratan12") || 
                name.equals("AnimaSA") || name.equals("animaSA") || 
                name.equals("Motokochan") || name.equals("motokochan") ||
                name.equals("Apkawa") ||  name.equals("apkawa")) {
            return true;
        }else{
            return false;
        }
    }
}
