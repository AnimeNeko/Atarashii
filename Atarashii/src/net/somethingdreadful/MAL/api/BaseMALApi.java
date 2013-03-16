package net.somethingdreadful.MAL.api;

import android.os.Build;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: apkawa
 * Date: 16.02.13
 * Time: 23:38
 * <p/>
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

    public abstract HttpResponse call_api(HTTP_METHOD http_method, String uri, Boolean is_auth);

    public abstract HttpResponse call_api(HTTP_METHOD http_method, String uri, HashMap<String, String> data, Boolean is_auth);

    public abstract JSONArray getList(ListType listType);

    public abstract JSONObject getDetail(Integer id, ListType listType);

    public abstract boolean isAuth();

    public abstract JSONArray search(ListType listType, String query);

    public abstract boolean addOrUpdateGenreInList(boolean hasCreate, ListType listType, String genre_id, HashMap<String, String> data);

    public abstract boolean deleteGenreFromList(ListType listType, String genre_id);


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public enum ListType {
        ANIME, MANGA
    }

    public static String getListTypeString(ListType listType) {
        switch (listType) {
            case ANIME:
                return "anime";
            case MANGA:
                return "manga";
            default:
                return null;
        }
    }
}
