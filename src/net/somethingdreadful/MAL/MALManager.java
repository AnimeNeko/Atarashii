package net.somethingdreadful.MAL;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Base64;

public class MALManager {
	
	final static String APIProvider = "http://mal-api.com/";
	final static String VerifyAPI = "account/verify_credentials";
	final static String readAnimeListAPI = "animelist/";
	final static String readAnimeDetailsAPI = "anime/";
	final static String writeAnimeDetailsAPI = "animelist/anime/";
	final static String readAnimeMineParam = "?mine=1";
	
	Context c;
	PrefManager prefManager;
	String malUser;
	String malPass;
	MALSqlHelper helper;
	SQLiteDatabase db;
	
	int c_ID;
	int c_Name;
	int c_type;
	int c_imageUrl;
	int c_recordStatus;
	int c_myStatus;
	int c_memberScore;
	int c_myScore;
	int c_synopsis;
	int c_episodesWatched;
	int c_episodesTotal;
	
	
	
	
	public MALManager(Context c)
	{
		this.c = c;
		prefManager = new PrefManager(c);
		
		malUser = prefManager.getUser();
		malPass = prefManager.getPass();
		
		helper = new MALSqlHelper(this.c);
		db = helper.getWritableDatabase();
	}
	
	static public boolean verifyAccount(String user, String pass)
	{
		HttpGet request = new HttpGet(APIProvider + VerifyAPI);
		HttpResponse response = null;
		request.setHeader("Authorization", "basic " + Base64.encodeToString((user + ":" + pass).getBytes(), Base64.NO_WRAP));
		
		try 
		{
			HttpClient client = new DefaultHttpClient();
			response = client.execute(request);
			
		} 
		catch (ClientProtocolException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		
		if (statusCode == 200)
		{
			return true;
		}
		
		else
		{
			return false;
		}
	}
	
	static String listSortFromInt(int i)
	{
		String r = "watching";
		
		switch (i)
		{
		case 0:
			r = "";
			break;
		case 1:
			r = "watching";
			break;
		case 2:
			r = "completed";
			break;
		case 3:
			r = "on-hold";
			break;
		case 4:
			r = "dropped";
			break;
		case 5: 
			r = "plan to watch";
			break;
		}
		
		return r;
	}
	
	public JSONObject getAnimeList()
	{
		String result = null;
		JSONObject jReturn = null;
		
		System.out.println("getAnimeList() called");
		
		HttpGet request;
		HttpResponse response;
		HttpClient client = new DefaultHttpClient();
		
		request = new HttpGet(APIProvider + readAnimeListAPI + malUser);
		request.setHeader("Authorization", "basic " + Base64.encodeToString((malUser + ":" + malPass).getBytes(), Base64.NO_WRAP));
		
		
		try 
		{
			response = client.execute(request);
			
			HttpEntity getResponseEntity = response.getEntity();
			
			if (getResponseEntity != null) 
			{
				result = EntityUtils.toString(getResponseEntity);
				jReturn = new JSONObject(result);
				
				System.out.println("got json response");
				
			}
			
		} 
		catch (ClientProtocolException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}

		
		return jReturn;
	}
	
	public JSONObject getAnimeDetails(int id) {
		
		String result = null;
		JSONObject jReturn = null;
		
		HttpGet request;
		HttpResponse response;
		HttpClient client = new DefaultHttpClient();
		
		request = new HttpGet(APIProvider + readAnimeDetailsAPI + id + readAnimeMineParam);
		request.setHeader("Authorization", "basic " + Base64.encodeToString((malUser + ":" + malPass).getBytes(), Base64.NO_WRAP));
		
		try 
		{
			response = client.execute(request);
			
			HttpEntity getResponseEntity = response.getEntity();
			
			if (getResponseEntity != null) 
			{
				result = EntityUtils.toString(getResponseEntity);
				jReturn = new JSONObject(result);
				
			}
			
		} 
		catch (ClientProtocolException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}

		
		return jReturn;
	}
	
	public void downloadAndStoreAnimeList()
	{
		JSONObject raw = getAnimeList();
		
		
		JSONArray jArray;
		try 
		{
			jArray = raw.getJSONArray("anime");
			
			for (int i = 0; i < jArray.length(); i++)
			{
				JSONObject a = jArray.getJSONObject(i);
				
				int id = a.getInt("id");
				String name = a.getString("title");
				int watched = a.getInt("watched_episodes");
				int totalEpisodes = a.getInt("episodes");
				String imageUrl = a.getString("image_url");
				String animeStatus = a.getString("status");
				String myStatus = a.getString("watched_status");
				String animeType = a.getString("type");
				String myScore = a.getString("score");
				
				
				AnimeRecord ar = new AnimeRecord(id, name, imageUrl, watched, totalEpisodes, 
						myStatus, animeStatus, animeType, myScore);
				
				insertOrUpdateAnime(ar);
				
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
	}

	public AnimeRecord updateAnimeWithDetails(int id, AnimeRecord ar)
	{
		JSONObject o = getAnimeDetails(id);
		
		ar.setSynopsis(getDataFromJSON(o, "synopsis")
				.replace("<br>", "\n").replace("&amp;", "&").replace("&rsquo;", "'")
				.replace("<strong>", "").replace("</strong>", ""));
		
		insertOrUpdateAnime(ar);
		
		return ar;
	}
	
	public String getDataFromJSON(JSONObject json, String get)
	{
		String sReturn = "";
		
		try 
		{
			sReturn = json.getString(get);
//			System.out.println(sReturn);
			
			if ("episodes".equals(get))
			{
				if ("null".equals(sReturn))
				{
					sReturn = "unknown";
				}
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		catch (NullPointerException e)
		{
//			e.printStackTrace();
			
			sReturn = "unknown";
		}
		
		return sReturn;
	}
	
	public ArrayList<AnimeRecord> getAnimeRecordsFromDB(int list)
	{
		
		ArrayList<AnimeRecord> al = new ArrayList();
		Cursor cu;
		

//		Cursor cu = db.query(MALSqlHelper.TABLE_ANIME, null, "myStatus='watching'", null, null, null, "recordName");
//		Cursor c = db.query(true, MALSqlHelper.TABLE_ANIME, null, null, null, null, null, "recordName", null);
		if (list == 0)
		{
			cu = db.rawQuery("SELECT * FROM 'anime' ORDER BY recordName", null);
		}
		else
		{
			cu = db.rawQuery("SELECT * FROM 'anime' WHERE myStatus='" + listSortFromInt(list) + "' ORDER BY recordName", null);
		}
		
		
		System.out.println(cu.getCount());
		cu.moveToFirst();
		getAnimeIndices(cu);
		
		while (cu.isAfterLast() == false)
		{	
			
			AnimeRecord ar = new AnimeRecord(cu.getInt(c_ID), cu.getString(c_Name), cu.getString(c_type), cu.getString(c_recordStatus), cu.getString(c_myStatus), cu.getInt(c_episodesWatched), cu.getInt(c_episodesTotal), cu.getString(c_memberScore), cu.getString(c_myScore), cu.getString(c_synopsis), cu.getString(c_imageUrl));
			al.add(ar);
			
			cu.moveToNext();
		}
		
		if (al.isEmpty())
		{
			return null;
		}
		
		cu.close();
		
		return al;
	}
	
	public void insertOrUpdateAnime(AnimeRecord ar)
	{
		
		ContentValues cv = new ContentValues();
		
		cv.put("recordID", ar.getID());
		cv.put("recordName", ar.getName());
		cv.put("recordType", ar.getRecordType());
		cv.put("imageUrl", ar.getImageUrl());
		cv.put("recordStatus", ar.getRecordStatus());
		cv.put("myStatus", ar.getMyStatus());
		cv.put("memberScore", ar.getMemberScore());
		cv.put("myScore", ar.getMyScore());
		cv.put("synopsis", ar.getSynopsis());
		cv.put("episodesWatched", ar.getWatched());
		cv.put("episodesTotal", ar.getTotal());
		
		if (animeExists(ar.getID()))
		{
			db.update(MALSqlHelper.TABLE_ANIME, cv, "recordID=?", new String[] {ar.getID()});
		}
		else
		{
			db.insert(MALSqlHelper.TABLE_ANIME, null, cv);
		}
		
		
		
	}
	
	public AnimeRecord getAnimeRecordFromDB(int recordID)
	{
		String[] id =  { Integer.toString(recordID) };
		
		Cursor cursor = db.rawQuery("select * from anime where recordID=?", id);
		cursor.moveToFirst();
		getAnimeIndices(cursor);
		
		AnimeRecord ar = new AnimeRecord(cursor.getInt(c_ID), cursor.getString(c_Name), cursor.getString(c_type), 
				cursor.getString(c_recordStatus), cursor.getString(c_myStatus), cursor.getInt(c_episodesWatched), 
				cursor.getInt(c_episodesTotal),	cursor.getString(c_memberScore), cursor.getString(c_myScore), 
				cursor.getString(c_synopsis), cursor.getString(c_imageUrl));
		
		cursor.close();
		
		return ar;
	}
	
	public boolean animeExists(String id) {
		   Cursor cursor = db.rawQuery("select 1 from anime where recordID=?", 
		        new String[] { id });
		   boolean exists = (cursor.getCount() > 0);
		   cursor.close();
		   return exists;
		}
	
	public boolean mangaExists(String id) {
		   Cursor cursor = db.rawQuery("select 1 from manga where recordID=%s", 
		        new String[] { id });
		   boolean exists = (cursor.getCount() > 0);
		   cursor.close();
		   return exists;
		}
	
	public void getAnimeIndices(Cursor cu)
	{
		c_ID = cu.getColumnIndex("recordID");
		c_Name = cu.getColumnIndex("recordName");
		c_type = cu.getColumnIndex("recordType");
		c_imageUrl = cu.getColumnIndex("imageUrl");
		c_recordStatus = cu.getColumnIndex("recordStatus");
		c_myStatus = cu.getColumnIndex("myStatus");
		c_memberScore = cu.getColumnIndex("memberScore");
		c_myScore = cu.getColumnIndex("myScore");
		c_synopsis = cu.getColumnIndex("synopsis");
		c_episodesWatched = cu.getColumnIndex("episodesWatched");
		c_episodesTotal = cu.getColumnIndex("episodesTotal");
	}
	
}
