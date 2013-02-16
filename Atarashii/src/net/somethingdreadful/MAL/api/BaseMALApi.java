package net.somethingdreadful.MAL.api;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created with IntelliJ IDEA.
 * User: apkawa
 * Date: 16.02.13
 * Time: 23:38
 *
 * http://mal-api.com/docs/
 */

public abstract class BaseMALApi {
    private String username;
    private String password;

    public BaseMALApi(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public abstract JSONArray getList()  throws JSONException;

    public abstract boolean isAuth();

    public abstract JSONArray search() throws JSONException;

    public abstract boolean updateGenreInList();

    public abstract boolean addGenreToList() ;

    public abstract boolean deleteGenreFromList();
}
