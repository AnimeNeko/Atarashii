package net.somethingdreadful.MAL.api;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import android.util.Base64;

/**
 * Created with IntelliJ IDEA.
 * User: apkawa
 * Date: 17.02.13
 * Time: 14:21
 * To change this template use File | Settings | File Templates.
 */
public class MALApi extends BaseMALApi {
    private String api_host = "http://mal-api.com";


    public MALApi(String username, String password) {
        super(username, password);
    }

    public JSONObject responseToJSONObject (HttpResponse response) throws IOException {
        HttpEntity getResponseEntity = response.getEntity();

        JSONObject result = null;
        if (response != null)
        {
            try {
                String raw_data = EntityUtils.toString(getResponseEntity);
                result = new JSONObject(raw_data);
            } catch (JSONException e) {
                // TODO logging

            } catch (IOException e) {
                // TODO logging
            }
        }
        return result;

    }

    public JSONArray responseToJSONArray(HttpResponse response) {
        HttpEntity getResponseEntity = response.getEntity();

        JSONArray result = null;
        if (response != null)
        {
            try {
                String raw_data = EntityUtils.toString(getResponseEntity);
                result = new JSONArray(raw_data);
            } catch (JSONException e) {
                // TODO logging

            } catch (IOException e) {
                // TODO logging
            }
        }
        return result;

    }

    private String getFullPath(String path) {
        return this.api_host + path;
    }

    private String getApiPrefixByMALListType(MALApiListType listType) {
        switch (listType){
            case ANIME:
                return new String("/anime/");
            default:
                return new String("/manga/");
        }
    }

    public HttpResponse call_api(HTTP_METHOD http_method, String uri, Boolean is_auth) {
        return this.call_api(http_method, uri, null, is_auth);
    }

    public String addGetQueryToURI(String uri, HashMap<String, String> query) {
        List<NameValuePair> putParams = new ArrayList<NameValuePair>();
        for (String key: query.keySet()) {
            putParams.add(new BasicNameValuePair(key, query.get(key)));
        }
        String get_query = URLEncodedUtils.format(putParams, "utf-8");

        String new_uri = new String(uri);
        if (! new_uri.endsWith("?")) {
            new_uri += "?";
        }

        new_uri += get_query;
        return new_uri;
    }

    @Override
    public HttpResponse call_api(HTTP_METHOD http_method, String uri, HashMap<String, String> data, Boolean is_auth) {

        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);

        HttpRequestBase writeRequest;

        uri = this.addGetQueryToURI(uri, data);
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
        try
        {

            if (http_method == HTTP_METHOD.POST || http_method == HTTP_METHOD.PUT) {
                List<NameValuePair> putParams = new ArrayList<NameValuePair>();
                for (String key: data.keySet()) {
                    putParams.add(new BasicNameValuePair(key, data.get(key)));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(putParams);
                HttpEntityEnclosingRequestBase t_writeRequest = (HttpEntityEnclosingRequestBase) writeRequest;
                t_writeRequest.setEntity(entity);
            }

            response = client.execute(writeRequest);
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public JSONObject getList(MALApiListType listType) {
        String uri = this.getFullPath(this.getApiPrefixByMALListType(listType) + "search?q=haruhi");
        return null;
    }

    @Override
    public boolean isAuth() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JSONArray search(MALApiListType listType, String query) {
        String uri = this.getFullPath(this.getApiPrefixByMALListType(listType) + "search");
        HashMap<String, String> data = new HashMap<>();
        data.put("q", query);

        boolean isAuth = false;
        HttpResponse response = call_api(HTTP_METHOD.GET, uri, data, isAuth);
        return this.responseToJSONArray(response);
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
