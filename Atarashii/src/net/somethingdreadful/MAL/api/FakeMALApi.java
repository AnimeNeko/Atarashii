package net.somethingdreadful.MAL.api;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

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

    @Override
    public HttpResponse call_api(HTTP_METHOD http_method, String uri, HashMap<String, String> data, Boolean is_auth) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JSONObject getList(MALApiListType listType) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isAuth() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JSONArray search(MALApiListType listType, String query) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean updateGenreInList(MALApiListType listType, String genre_id, HashMap<String, String> data) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean addGenreToList(MALApiListType listType, String genre_id, HashMap<String, String> data) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean deleteGenreFromList(MALApiListType listType, String genre_id, HashMap<String, String> data) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


}
