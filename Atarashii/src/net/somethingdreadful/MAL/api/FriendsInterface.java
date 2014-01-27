package net.somethingdreadful.MAL.api;

import java.util.ArrayList;

import net.somethingdreadful.MAL.api.response.Friend;
import net.somethingdreadful.MAL.api.response.Profile;
import retrofit.http.GET;
import retrofit.http.Path;

//TEMPORARY UNTIL NEW
public interface FriendsInterface {
    @GET("/profile/{username}")
    Profile getProfile(@Path("username") String username);
    @GET("/friends/{username}")
    ArrayList<Friend> getFriends(@Path("username") String username);
}
