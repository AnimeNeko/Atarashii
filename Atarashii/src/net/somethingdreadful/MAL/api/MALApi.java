package net.somethingdreadful.MAL.api;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import net.somethingdreadful.MAL.PrefManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MALApi extends BaseMALApi {
    private static String api_host = "http://mal-api.com";


    public MALApi(String username, String password) {
        super(username, password);
    }

    public MALApi(Context context) {
        super(null, null);
        PrefManager prefManager = new PrefManager(context);
        setUsername(prefManager.getUser());
        setPassword(prefManager.getPass());
    }

    public JSONObject responseToJSONObject(HttpResponse response) throws IOException {
        HttpEntity getResponseEntity = response.getEntity();

        JSONObject result = null;
        try {
            String raw_data = EntityUtils.toString(getResponseEntity);
            result = new JSONObject(raw_data);
        } catch (JSONException | IOException e) {
            Log.e(this.getClass().getName(), Log.getStackTraceString(e));

        }
        return result;
    }

    public JSONArray responseToJSONArray(HttpResponse response) {
        HttpEntity getResponseEntity = response.getEntity();

        JSONArray result = null;
        try {
            String raw_data = EntityUtils.toString(getResponseEntity);
            result = new JSONArray(raw_data);
        } catch (JSONException | IOException e) {
            Log.e(this.getClass().getName(), Log.getStackTraceString(e));

        }
        return result;

    }

    private static String getFullPath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return MALApi.api_host + path;
    }

    public static String addGetQueryToURI(String uri, HashMap<String, String> query) {
        if (query == null || query.isEmpty()) {
            return uri;
        }
        List<NameValuePair> putParams = new ArrayList<>();
        for (String key : query.keySet()) {
            putParams.add(new BasicNameValuePair(key, query.get(key)));
        }
        String get_query = URLEncodedUtils.format(putParams, "utf-8");

        if (!uri.endsWith("?")) {
            uri += "?";
        }
        uri += get_query;
        return uri;
    }

    @Override
    public HttpResponse call_api(HTTP_METHOD http_method, String uri, Boolean is_auth) {
        return this.call_api(http_method, uri, null, is_auth);
    }

    @Override
    public HttpResponse call_api(HTTP_METHOD http_method, String uri, HashMap<String, String> data, Boolean is_auth) {
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);

        HttpRequestBase writeRequest;

        uri = addGetQueryToURI(uri, data);
        switch (http_method) {
            case POST:
                writeRequest = new HttpPost(uri);
                break;

            case PUT:
                writeRequest = new HttpPut(uri);
                break;

            case DELETE:
                writeRequest = new HttpDelete(uri);
                break;

            default:
                writeRequest = new HttpGet(uri);
                break;
        }

        if (is_auth) {
            writeRequest.setHeader("Authorization", "basic " + Base64.encodeToString((this.username + ":" + this.password).getBytes(), Base64.NO_WRAP));
        }

        HttpResponse response = null;
        try {

            if (http_method == HTTP_METHOD.POST || http_method == HTTP_METHOD.PUT) {
                List<NameValuePair> putParams = new ArrayList<>();
                for (String key : data.keySet()) {
                    putParams.add(new BasicNameValuePair(key, data.get(key)));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(putParams);
                HttpEntityEnclosingRequestBase t_writeRequest = (HttpEntityEnclosingRequestBase) writeRequest;
                t_writeRequest.setEntity(entity);
                writeRequest = t_writeRequest;
            }

            response = client.execute(writeRequest);
        } catch (IOException e) {
            Log.e(this.getClass().getName(), Log.getStackTraceString(e));
        }
        return response;
    }


    @Override
    public boolean isAuth() {
        String uri = getFullPath("account/verify_credentials");
        HttpResponse response = call_api(HTTP_METHOD.GET, uri, true);
        return response != null && response.getStatusLine().getStatusCode() == 200;
    }

    @Override
    public JSONArray search(ListType listType, String query) {
        String uri = getFullPath(getListTypeString(listType) + "/search");
        HashMap<String, String> data = new HashMap<>();
        data.put("q", query);
        boolean isAuth = true;
        HttpResponse response = call_api(HTTP_METHOD.GET, uri, data, isAuth);
        return this.responseToJSONArray(response);
    }

    @Override
    public JSONArray getList(ListType listType) {
        String uri = getFullPath(getListTypeString(listType) + "list/" + this.getUsername());
        JSONArray jsonArray = null;
        try {
            HttpResponse response = call_api(HTTP_METHOD.GET, uri, true);
            HttpEntity getResponseEntity = response.getEntity();

            if (getResponseEntity != null) {
                jsonArray = new JSONObject(EntityUtils.toString(getResponseEntity)).getJSONArray(getListTypeString(listType));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    @Override
    public JSONObject getDetail(Integer id, ListType listType) {
        JSONObject jsonObject = null;
        String uri = getFullPath(getListTypeString(listType) + "/" + id);
        try {
            HttpResponse response = call_api(HTTP_METHOD.GET, uri, true);
            HttpEntity getResponseEntity = response.getEntity();

            if (getResponseEntity != null) {
                jsonObject = new JSONObject(EntityUtils.toString(getResponseEntity));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public boolean addOrUpdateGenreInList(boolean hasCreate, ListType listType, String genre_id, HashMap<String, String> data) {
        String listPrefix = getListTypeString(listType);
        String uri = getFullPath(listPrefix + "list" + "/" + listPrefix);
        HTTP_METHOD methodType;
        if (!hasCreate) {
            uri += "/" + genre_id;
            methodType = HTTP_METHOD.PUT;
        } else {
            data = new HashMap<>(data);
            data.put(listPrefix + "_id", genre_id);
            methodType = HTTP_METHOD.POST;
        }
        HttpResponse response = call_api(methodType, uri, data, true);
        return response.getStatusLine().getStatusCode() == 200;
    }

    @Override
    public boolean deleteGenreFromList(ListType listType, String genre_id) {
        String listPrefix = getListTypeString(listType);
        String uri = getFullPath(listPrefix + "list" + "/" + listPrefix + "/" + genre_id);
        HttpResponse response = call_api(HTTP_METHOD.DELETE, uri, true);
        return response.getStatusLine().getStatusCode() == 200;
    }

}
