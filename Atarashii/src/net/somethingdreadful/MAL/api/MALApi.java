package net.somethingdreadful.MAL.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;

import net.somethingdreadful.MAL.PrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class MALApi extends BaseMALApi {
	private static final String TAG = MALApi.class.getSimpleName();
	private static String api_host = "http://newapi.atarashiiapp.com";

	public MALApi(String username, String password) {
		super(username, password);
	}

	public MALApi(Context context) {
		super(null, null);
		PrefManager prefManager = new PrefManager(context);
		setUsername(prefManager.getUser());
		setPassword(prefManager.getPass());
	}

	public JSONArray responseToJSONArray(RestResult<String> response) {
		JSONArray result = null;

		try {
			result = new JSONArray(response.result);
		} catch (JSONException e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}
		return result;

	}

	private static String getFullPath(String path) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return MALApi.api_host + path;
	}

	private String encodeAsFormPost(HashMap<String, String> data) {
		StringBuffer encodedData = new StringBuffer();
		if (data != null) {

			for (Entry<String, String> entry : data.entrySet()) {
				encodedData.append(String.format("%s=%s&", entry.getKey(),
						entry.getValue()));
			}

		}
		encodedData.deleteCharAt(encodedData.length() - 1);
		return encodedData.toString();
	}

	@Override
	public boolean isAuth() {
		URL url;
		try {
			url = new URL(getFullPath("account/verify_credentials"));

			RestResult<String> response = restHelper.get(url);
			return response != null && response.code == 200;
		} catch (MalformedURLException e) {
			Log.e(TAG, "", e);
			return false;
		}
	}

	@Override
	public JSONArray search(ListType listType, String query) {
		URL url;
		RestResult<String> response = null;
		try {
			url = new URL(getFullPath(getListTypeString(listType)
					+ String.format("/search?q=%s", query)));
			response = restHelper.get(url);
		} catch (MalformedURLException e) {
			Log.e(TAG, "Something went wrong, returning an empty list instead of null", e);
			response = new RestResult<String>();
		}
		return responseToJSONArray(response);
	}

	@Override
	public JSONArray getList(ListType listType) {
		JSONArray jsonArray = null;
		try {
			URL url = new URL(getFullPath(getListTypeString(listType) + "list/"
					+ getUsername()));
			RestResult<String> response = restHelper.get(url);

			if (response != null) {
				jsonArray = new JSONObject(response.result)
						.getJSONArray(getListTypeString(listType));
			}
		} catch (JSONException e) {
			Log.e(TAG, Log.getStackTraceString(e));

		} catch (MalformedURLException e) {
			Log.e(TAG, "", e);
		}
		return jsonArray;
	}

	@Override
	public JSONObject getDetail(Integer id, ListType listType) {
		JSONObject jsonObject = null;
		try {
			URL url = new URL(getFullPath(getListTypeString(listType) + "/"
					+ id));
			RestResult<String> response = restHelper.get(url);

			if (response != null) {
				jsonObject = new JSONObject(response.result);
			}
		} catch (JSONException e) {
			Log.e(TAG, Log.getStackTraceString(e));

		} catch (MalformedURLException e) {
			Log.e(TAG, "", e);
		}
		return jsonObject;
	}

	@Override
	public boolean addOrUpdateGenreInList(boolean hasCreate, ListType listType,
			String genre_id, HashMap<String, String> data) {
		String listPrefix = getListTypeString(listType);
		String uri = getFullPath(listPrefix + "list" + "/" + listPrefix);
		RestResult<String> response = null;
		try {
			if (!hasCreate) {
				uri += "/" + genre_id;
				response = restHelper.put(new URL(uri), encodeAsFormPost(data));

			} else {
				data = new HashMap<String, String>(data);
				data.put(listPrefix + "_id", genre_id);
				response = restHelper.post(new URL(uri), encodeAsFormPost(data));
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "", e);
		}

		return response.code == 200;
	}

	@Override
	public boolean deleteGenreFromList(ListType listType, String genre_id) {
		String listPrefix = getListTypeString(listType);
		URL url;
		RestResult<String> response = null;
		try {
			url = new URL(getFullPath(listPrefix + "list" + "/" + listPrefix
					+ "/" + genre_id));
			response = restHelper.delete(url);
		} catch (MalformedURLException e) {
			Log.e(TAG, "", e);
			return false;
		}
		return response.code == 200;
	}
	
	@Override
	public JSONArray getMostPopular(ListType listType, int page){
		URL url;
		RestResult<String> response = null;
		try {
			url = new URL(getFullPath(getListTypeString(listType) + "/popular?page="+page));
			response = restHelper.get(url);
		} catch (Exception e) {
			Log.e(TAG, "Something went wrong, returning an empty list instead of null", e);
			response = new RestResult<String>();
		}
		return responseToJSONArray(response);
	}
	
	@Override
	public JSONArray getTopRated(ListType listType, int page){
		URL url;
		RestResult<String> response = null;
		try {
			url = new URL(getFullPath(getListTypeString(listType) + "/top?page="+page));
			response = restHelper.get(url);
		} catch (Exception e) {
			Log.e(TAG, "Something went wrong, returning an empty list instead of null", e);
			response = new RestResult<String>();
		}
		return responseToJSONArray(response);
	}
	
	@Override
	public JSONArray getJustAdded(ListType listType, int page){
		URL url;
		RestResult<String> response = null;
		try {
			url = new URL(getFullPath(getListTypeString(listType) + "/just_added?page="+page));
			response = restHelper.get(url);
		} catch (Exception e) {
			Log.e(TAG, "Something went wrong, returning an empty list instead of null", e);
			response = new RestResult<String>();
		}
		return responseToJSONArray(response);
	}
	
	@Override
	public JSONArray getUpcoming(ListType listType, int page){
		URL url;
		RestResult<String> response = null;
		try {
			url = new URL(getFullPath(getListTypeString(listType) + "/upcoming?page="+page));
			response = restHelper.get(url);
		} catch (Exception e) {
			Log.e(TAG, "Something went wrong, returning an empty list instead of null", e);
			response = new RestResult<String>();
		}
		return responseToJSONArray(response);
	}
	

}
