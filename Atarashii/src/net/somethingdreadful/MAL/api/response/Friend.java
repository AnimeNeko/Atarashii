package net.somethingdreadful.MAL.api.response;

import java.util.Arrays;
import java.util.List;

import android.database.Cursor;

/*
 * Deserialization class for requests to /friends/{username}
 * see: FriendsInterface.java, MALApi.java
 * 
 * the friends API returns an different profile object than the profile API (anime-/mangastats as empty array, not object),
 * so we need to handle this separately :(
 */
public class Friend {
    private String name;
    private String friend_since;
    private BasicProfile profile;
    
    public static Friend fromCursor(Cursor c) {
        Friend result = new Friend();
        List<String> columnNames = Arrays.asList(c.getColumnNames());
        result.setName(c.getString(columnNames.indexOf("username")));
        
        result.setProfile(BasicProfile.fromCursor(c));
        result.setFriendSince(c.getString(columnNames.indexOf("friend_since")));
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
    
    public BasicProfile getProfile() {
        return profile;
    }
    
    public void setProfile(BasicProfile profile) {
        this.profile = profile;
    }
}
