package net.somethingdreadful.MAL.api;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created with IntelliJ IDEA.
 * User: apkawa
 * Date: 17.02.13
 * Time: 0:15
 * To change this template use File | Settings | File Templates.
 */
public class FakeMALApi extends BaseMALApi
{
    public FakeMALApi(String username, String password) {
        super(username, password);

    }

    public JSONArray getList() throws JSONException {
        return new JSONArray("");
    }

    public boolean isAuth() {
        return true;
    }

    public JSONArray search() throws JSONException {
        return new JSONArray("");
    }

    public boolean updateGenreInList() {
        return true;
    }

    public boolean addGenreToList() {
        return true;
    }

    public boolean deleteGenreFromList() {
        return true;
    }

}
