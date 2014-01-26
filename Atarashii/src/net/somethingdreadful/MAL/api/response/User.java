package net.somethingdreadful.MAL.api.response;

import java.util.Arrays;
import java.util.List;

import android.database.Cursor;

public class User {
    String name;
    String friend_since;
    Profile profile;
    
    public static User fromCursor(Cursor c) {
        return fromCursor(c, false);
    }
    
    public static User fromCursor(Cursor c, boolean friendDetails) {
        User result = new User();

        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setName(c.getString(columnNames.indexOf("username")));
        if ( friendDetails )
            result.setFriendSince(c.getString(columnNames.indexOf("friend_since")));
        
        result.setProfile(Profile.fromCursor(c, friendDetails));
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFriendSince() {
        return friend_since;
    }

    public void setFriendSince(String friend_since) {
        this.friend_since = friend_since;
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
