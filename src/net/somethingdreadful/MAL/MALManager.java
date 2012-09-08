package net.somethingdreadful.MAL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.NetworkOnMainThreadException;
import android.util.Base64;

public class MALManager {
	
	final static String APIProvider = "http://mal-api.com/";
	final static String VerifyAPI = "account/verify_credentials";
	final static String readAnimeListAPI = "animelist/";
	final static String readAnimeDetailsAPI = "anime/";
	final static String writeAnimeDetailsAPI = "animelist/anime/";
	final static String readMangaListAPI = "mangalist/";
	final static String readMangaDetailsAPI = "manga/";
	final static String writeMangaDetailsAPI = "mangalist/anime/";
	final static String readMineParam = "?mine=1";

	
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
	int c_readVolumes;
	int c_readChapters;
	int c_totalVolumes;
	int c_totalChapters;
	int c_dirty;
	
	
	
	
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
	
	static String listSortFromInt(int i, String type)
	{
		String r = "";
		
		if(type == "anime") {
			switch (i)
			{
			case 0:
				r = "";
				break;
			case 1:
				r = AnimeRecord.STATUS_WATCHING;
				break;
			case 2:
				r = AnimeRecord.STATUS_COMPLETED;
				break;
			case 3:
				r = AnimeRecord.STATUS_ONHOLD;
				break;
			case 4:
				r = AnimeRecord.STATUS_DROPPED;
				break;
			case 5:
				r = AnimeRecord.STATUS_PLANTOWATCH;
				break;
			default:
				r = AnimeRecord.STATUS_WATCHING;
				break;
			}
		}
		else if (type == "manga") {
			switch (i)
			{
			case 0:
				r = "";
				break;
			case 1:
				r = MangaRecord.STATUS_WATCHING;
				break;
			case 2:
				r = MangaRecord.STATUS_COMPLETED;
				break;
			case 3:
				r = MangaRecord.STATUS_ONHOLD;
				break;
			case 4:
				r = MangaRecord.STATUS_DROPPED;
				break;
			case 5:
				r = MangaRecord.STATUS_PLANTOWATCH;
				break;
			default:
				r = MangaRecord.STATUS_WATCHING;
				break;
			}
		}

		return r;
	}
	
	public JSONObject getList(String type) {
		String readListAPI = null;
		String result = null;
		JSONObject jReturn = null;
		
		if (type == "anime") {
			readListAPI = MALManager.readAnimeListAPI;
		}
		else if (type == "manga") {
			readListAPI = MALManager.readMangaListAPI;
		}
		else
		{
			throw new RuntimeException("getList called with unknown list type.");
		}
		
		System.out.println("getList() called");
		
		HttpGet request;
		HttpResponse response;
		HttpClient client = new DefaultHttpClient();
		
		request = new HttpGet(APIProvider + readListAPI + malUser);
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
	
	public JSONObject getDetails(int id, String type) {

		String result = null;
		JSONObject jReturn = null;
		String readDetailsAPI = null;

		if (type == "anime") {
			readDetailsAPI = MALManager.readAnimeDetailsAPI;
		}
		else if (type == "manga") {
			readDetailsAPI = MALManager.readMangaDetailsAPI;
		}
		else
		{
			throw new RuntimeException("getDetails called with unknown list type.");
		}
		
		HttpGet request;
		HttpResponse response;
		HttpClient client = new DefaultHttpClient();
		
		request = new HttpGet(APIProvider + readDetailsAPI + id + readMineParam);
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
	
	public void downloadAndStoreList(String type)
	{
		JSONObject raw = getList(type);
		
		JSONArray jArray;
		try 
		{
			if(type == "anime") {
				jArray = raw.getJSONArray("anime");

				for (int i = 0; i < jArray.length(); i++) {
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
							myStatus, animeStatus, animeType, myScore, 0);

					saveItem(ar, true);
				}
			}
			else if(type == "manga") {
				jArray = raw.getJSONArray("manga");
				
				for (int i = 0; i < jArray.length(); i++)
				{
					try {
						JSONObject a = jArray.getJSONObject(i);

						int id = a.getInt("id");
						String name = a.getString("title");
						int readVolumes = a.getInt("volumes_read");
						int readChapters = a.getInt("chapters_read");
						int totalVolumes = a.getInt("volumes");
						int totalChapters = a.getInt("chapters");
						String imageUrl = a.getString("image_url");
						String mangaStatus = a.getString("status");
						String myStatus = a.getString("read_status");
						String mangaType = a.getString("type");
						String myScore = a.getString("score");

						MangaRecord mr = new MangaRecord(id, name, mangaType, mangaStatus, myStatus,
								readVolumes, readChapters, totalVolumes, totalChapters, myScore, imageUrl, 0);

						saveItem(mr, true);
					}
					catch (JSONException e) {
						throw e;
					}
					
					
					}
			}
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
	}

	public AnimeRecord updateWithDetails(int id, AnimeRecord ar)
	{
		JSONObject o = getDetails(id, "anime");
		
		ar.setSynopsis(getDataFromJSON(o, "synopsis"));
		
		saveItem(ar, false);
		
		return ar;
	}
	
	public MangaRecord updateWithDetails(int id, MangaRecord mr)
	{
		JSONObject o = getDetails(id, "manga");
		
		mr.setSynopsis(getDataFromJSON(o, "synopsis"));
		
		saveItem(mr, false);
		
		return mr;
	}

	public String getDataFromJSON(JSONObject json, String get)
	{
		String sReturn = "";
		
		try 
		{
			sReturn = json.getString(get);
			
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
			sReturn = "unknown";
		}
		
		return sReturn;
	}
	
	public ArrayList<AnimeRecord> getAnimeRecordsFromDB(int list)
	{
		
		ArrayList<AnimeRecord> al = new ArrayList();
		Cursor cu;
		
		if (list == 0)
		{
			cu = db.rawQuery("SELECT * FROM 'anime' ORDER BY recordName", null);
		}
		else
		{
			cu = db.rawQuery("SELECT * FROM 'anime' WHERE myStatus='" + listSortFromInt(list, "anime") + "' ORDER BY recordName", null);
		}
		
		
		System.out.println(cu.getCount());
		cu.moveToFirst();
		getIndices(cu);
		
		while (cu.isAfterLast() == false)
		{	
			
			AnimeRecord ar = new AnimeRecord(cu.getInt(c_ID), cu.getString(c_Name), cu.getString(c_type), 
					cu.getString(c_recordStatus), cu.getString(c_myStatus), cu.getInt(c_episodesWatched), 
					cu.getInt(c_episodesTotal), cu.getString(c_memberScore), cu.getString(c_myScore), 
					cu.getString(c_synopsis), cu.getString(c_imageUrl), cu.getInt(c_dirty));
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

	public ArrayList<MangaRecord> getMangaRecordsFromDB(int list)
	{
		ArrayList<MangaRecord> ml = new ArrayList();
		Cursor cu;
		
		String columnList = "recordID, recordName, recordType, imageUrl, " +
				"recordStatus, myStatus, memberScore, myScore, synopsis, " +
				"chaptersRead, chaptersTotal, volumesRead, volumesTotal, dirty";
		
		if (list == 0)
		{
			cu = db.rawQuery("SELECT " + columnList + " FROM 'manga' ORDER BY recordName", null);
		}
		else
		{
			cu = db.rawQuery("SELECT " + columnList + " FROM 'manga' WHERE myStatus='" + listSortFromInt(list, "manga") + "' ORDER BY recordName", null);
		}
		
		System.out.println(cu.getCount());
		cu.moveToFirst();
		
		while (cu.isAfterLast() == false)
		{	
			
			MangaRecord mr = new MangaRecord(cu.getInt(cu.getColumnIndex("recordID")), cu.getString(cu.getColumnIndex("recordName")),
					cu.getString(cu.getColumnIndex("recordType")),
					cu.getString(cu.getColumnIndex("recordStatus")), cu.getString(cu.getColumnIndex("myStatus")),
					cu.getInt(cu.getColumnIndex("volumesRead")), cu.getInt(cu.getColumnIndex("chaptersRead")),
					cu.getInt(cu.getColumnIndex("volumesTotal")), cu.getInt(cu.getColumnIndex("chaptersTotal")),
					cu.getString(cu.getColumnIndex("memberScore")), cu.getString(cu.getColumnIndex("myScore")),
					cu.getString(cu.getColumnIndex("synopsis")), cu.getString(cu.getColumnIndex("imageUrl")),
					cu.getInt(cu.getColumnIndex("dirty")));
			ml.add(mr);
			
			cu.moveToNext();
		}
		
		if (ml.isEmpty())
		{
			return null;
		}
		
		cu.close();
		
		return ml;
	}

	public void saveItem(MangaRecord mr, boolean ignoreSynopsis)
	{
		ContentValues cv = new ContentValues();
		
		cv.put("recordID", mr.getID());
		cv.put("recordName", mr.getName());
		cv.put("recordType", mr.getRecordType());
		cv.put("imageUrl", mr.getImageUrl());
		cv.put("recordStatus", mr.getRecordStatus());
		cv.put("myStatus", mr.getMyStatus());
		cv.put("memberScore", mr.getMemberScore());
		cv.put("myScore", mr.getMyScore());
		cv.put("volumesRead", mr.getVolumeProgress());
		cv.put("chaptersRead", mr.getPersonalProgress());
		cv.put("volumesTotal", mr.getVolumeTotal());
		cv.put("chaptersTotal", mr.getTotal());
		cv.put("dirty", mr.getDirty());
		
		if (ignoreSynopsis == false)
		{
			cv.put("synopsis", mr.getSynopsis());
		}
		
		if (itemExists(mr.getID(), "manga"))
		{
			db.update(MALSqlHelper.TABLE_MANGA, cv, "recordID=?", new String[] {mr.getID()});
		}
		else
		{
			db.insert(MALSqlHelper.TABLE_MANGA, null, cv);
		}
	}
	
	public void saveItem(AnimeRecord ar, boolean ignoreSynopsis)
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
		cv.put("episodesWatched", ar.getPersonalProgress());
		cv.put("episodesTotal", ar.getTotal());
		cv.put("dirty", ar.getDirty());
		
		if (ignoreSynopsis == false)
		{
			cv.put("synopsis", ar.getSynopsis());
		}
		
		if (itemExists(ar.getID(), "anime"))
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
		getIndices(cursor);
		
		AnimeRecord ar = new AnimeRecord(cursor.getInt(c_ID), cursor.getString(c_Name), cursor.getString(c_type), 
				cursor.getString(c_recordStatus), cursor.getString(c_myStatus), cursor.getInt(c_episodesWatched), 
				cursor.getInt(c_episodesTotal),	cursor.getString(c_memberScore), cursor.getString(c_myScore), 
				cursor.getString(c_synopsis), cursor.getString(c_imageUrl), cursor.getInt(c_dirty));
		
		cursor.close();
		
		return ar;
	}
	
	public MangaRecord getMangaRecordFromDB(int recordID)
	{
		String[] id =  { Integer.toString(recordID) };
		
		Cursor cursor = db.rawQuery("select * from manga where recordID=?", id);
		cursor.moveToFirst();
		getIndices(cursor);
		
		MangaRecord mr = new MangaRecord(cursor.getInt(c_ID), cursor.getString(c_Name), cursor.getString(c_type), 
				cursor.getString(c_recordStatus), cursor.getString(c_myStatus), cursor.getInt(c_readVolumes), 
				cursor.getInt(c_readChapters),	cursor.getInt(c_totalVolumes), cursor.getInt(c_totalChapters), 
				cursor.getString(c_memberScore), cursor.getString(c_myScore), cursor.getString(c_synopsis), 
				cursor.getString(c_imageUrl), cursor.getInt(c_dirty));
		
		cursor.close();
		
		return mr;
	}
	
	
	public boolean itemExists(String id, String type) {
		if (type == "anime" || type == "manga") {
			Cursor cursor = db.rawQuery("select 1 from " + type + " where recordID=?", 
			        new String[] { id });
			   boolean exists = (cursor.getCount() > 0);
			   cursor.close();
			   return exists;
		}
		else
		{
			throw new RuntimeException("itemExists called with unknown type.");
		}
	}
	
	public void getIndices(Cursor cu)
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
		c_readVolumes = cu.getColumnIndex("volumesRead");
		c_readChapters = cu.getColumnIndex("chaptersRead");
		c_totalVolumes = cu.getColumnIndex("volumesTotal");
		c_totalChapters = cu.getColumnIndex("chaptersTotal");
		c_dirty = cu.getColumnIndex("dirty");
	}
	
	public boolean writeAnimeDetailsToMAL(AnimeRecord ar)
	{
		boolean success = false;
		
		HttpPut writeRequest;
		HttpResponse response;
		HttpClient client = new DefaultHttpClient();
		
		writeRequest = new HttpPut(APIProvider + writeAnimeDetailsAPI + ar.getID());
		writeRequest.setHeader("Authorization", "basic " + Base64.encodeToString((malUser + ":" + malPass).getBytes(), Base64.NO_WRAP));
		
		List<NameValuePair> putParams = new ArrayList<NameValuePair>();
		putParams.add(new BasicNameValuePair("status", ar.getMyStatus()));
		putParams.add(new BasicNameValuePair("episodes", Integer.toString(ar.getPersonalProgress())));
		putParams.add(new BasicNameValuePair("score", ar.getMyScore()));
		
		try 
		{
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(putParams);
			writeRequest.setEntity(entity);
			
			response = client.execute(writeRequest);
			
			System.out.println(response.getStatusLine().toString());
			
			if (200 == response.getStatusLine().getStatusCode()) 
			{
				success = true;		
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
		catch (NetworkOnMainThreadException bullshit)
		{
			
		}
		
		return success;
	}

	public String watchedCounterBuilder(int watched, int total)
	{
		String built = "";
		
		if (total != 0)
		{
			built = watched + " / " + total;
		}
		else
		{
			built = watched + " / ?";
		}
		
		
		return built;
	}
}