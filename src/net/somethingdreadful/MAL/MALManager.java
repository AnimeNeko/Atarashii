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

	public ArrayList<AnimeRecord> getAnimeRecordsFromDB(int list)
	{
		
		ArrayList<AnimeRecord> al = new ArrayList();
		

//		Cursor cu = db.query(MALSqlHelper.TABLE_ANIME, null, "myStatus='watching'", null, null, null, "recordName");
//		Cursor c = db.query(true, MALSqlHelper.TABLE_ANIME, null, null, null, null, null, "recordName", null);
		Cursor cu = db.rawQuery("SELECT * FROM 'anime' WHERE myStatus='watching' ORDER BY recordName", null);
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
	
	public void initialInsertAnime(AnimeRecord ar)
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
//		cv.put("synopsis", ar.getSynopsis());
		cv.put("episodesWatched", ar.getWatched());
		cv.put("episodesTotal", ar.getTotal());
		
		long returnedID = db.insert(MALSqlHelper.TABLE_ANIME, null, cv);
		System.out.println(returnedID);
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
