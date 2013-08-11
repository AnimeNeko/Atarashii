package net.somethingdreadful.MAL.api;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Build;

/**
 * Created with IntelliJ IDEA.
 * User: apkawa
 * Date: 16.02.13
 * Time: 23:38
 * <p/>
 * http://mal-api.com/docs/
 */

public abstract class BaseMALApi {

    public String username;
    public String password;
    
	protected RestHelper restHelper;

    final static String USER_AGENT = "Atarashii! (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + " Build/" + Build.DISPLAY + ")";

    public BaseMALApi(String username, String password) {
        this.username = username;
        this.password = password;
        
        restHelper = new RestHelper(username, password);
        restHelper.applyUserAgent(USER_AGENT);
        
    }

    public abstract JSONArray getList(ListType listType);

    public abstract JSONObject getDetail(Integer id, ListType listType);

    public abstract boolean isAuth();

    public abstract JSONArray search(ListType listType, String query);

    public abstract boolean addOrUpdateGenreInList(boolean hasCreate, ListType listType, String genre_id, HashMap<String, String> data);

    public abstract boolean deleteGenreFromList(ListType listType, String genre_id);
    
    public abstract JSONArray getMostPopular(ListType listType);
    public abstract JSONArray getTopRated(ListType listType);


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        restHelper.setCredentials(this.username, this.password);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        restHelper.setCredentials(this.username, this.password);
    }

    public enum ListType {
        ANIME, MANGA
    }

    public static String getListTypeString(ListType listType) {
        return listType.name().toLowerCase();
    }

    public static ListType getListTypeByString(String listTypeName) {
        return ListType.valueOf(listTypeName.toUpperCase());
    }
}
