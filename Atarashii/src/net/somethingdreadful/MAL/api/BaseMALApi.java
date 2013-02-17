package net.somethingdreadful.MAL.api;

import org.apache.http.HttpResponse;
import org.json.JSONArray;

import java.util.HashMap;
import android.os.Build;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: apkawa
 * Date: 16.02.13
 * Time: 23:38
 *
 * http://mal-api.com/docs/
 */

enum HTTP_METHOD {
    GET, POST, PUT, DELETE
}

public abstract class BaseMALApi {
    public String username;
    public String password;

    final static String USER_AGENT = "Atarashii! (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + " Build/" + Build.DISPLAY + ")";

    public BaseMALApi(String username, String password) {
        this.username = username;
        this.password = password;

    }

    public abstract HttpResponse call_api(HTTP_METHOD http_method, String uri, HashMap<String, String> data, Boolean is_auth);

    public abstract JSONObject getList(MALApiListType listType);

    public abstract boolean isAuth();

    public abstract JSONArray search(MALApiListType listType, String query);

    public abstract boolean updateGenreInList(MALApiListType listType, String genre_id, HashMap<String, String> data);

    public abstract boolean addGenreToList(MALApiListType listType, String genre_id, HashMap<String, String> data) ;

    public abstract boolean deleteGenreFromList(MALApiListType listType, String genre_id, HashMap<String, String> data);
}
