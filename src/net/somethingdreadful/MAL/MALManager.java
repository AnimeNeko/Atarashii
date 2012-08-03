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
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
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
	
	
	
	public MALManager(Context c)
	{
		this.c = c;
		prefManager = new PrefManager(c);
		
		malUser = prefManager.getUser();
		malPass = prefManager.getPass();
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
		
		HttpGet request;
		HttpResponse response;
		HttpClient client = new DefaultHttpClient();
		
		request = new HttpGet(APIProvider + readAnimeListAPI + malUser);
		
		
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

	public ArrayList<AnimeRecord> getAnimeRecordsFromDB(int list)
	{
		
		ArrayList<AnimeRecord> al = new ArrayList();
		
		return null;
		
	}
	
	public class getAnimeRecords extends AsyncTask<Integer, Void, ArrayList<AnimeRecord>>
	{

		@Override
		protected ArrayList<AnimeRecord> doInBackground(Integer... list) {
			
			ArrayList<AnimeRecord> al = new ArrayList();
			
			int listint = 0;
			
			for(int i : list)
			{
				listint = i;
			}
			
			al = getAnimeRecordsFromDB(listint);
			
			if (al == null)
			{
				JSONObject raw = getAnimeList();
				
				
			}
			
			return al;
		}
		
	}
}
