package net.somethingdreadful.MAL;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.record.AnimeRecord;
import net.somethingdreadful.MAL.record.GenericMALRecord;
import net.somethingdreadful.MAL.record.MangaRecord;
import net.somethingdreadful.MAL.record.UserRecord;
import net.somethingdreadful.MAL.sql.MALSqlHelper;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

public class MALManager {

    final static String TYPE_ANIME = "anime";
    final static String TYPE_MANGA = "manga";
    final static String TYPE_FRIENDS = "friends";
    final static String TYPE_PROFILE = "profile";

    private String[] animeColumns = {"recordID", "recordName", "recordType", "recordStatus", "myStatus",
            "episodesWatched", "episodesTotal", "memberScore", "myScore", "synopsis", "imageUrl", "dirty", "lastUpdate"};

    private String[] mangaColumns = {"recordID", "recordName", "recordType", "recordStatus", "myStatus",
            "volumesRead", "chaptersRead", "volumesTotal", "chaptersTotal", "memberScore", "myScore", "synopsis",
            "imageUrl", "dirty", "lastUpdate"};
    
    private String[] friendsColumns = {"username", "avatar_url", "last_online", "friend_since"};
    
    private String[] profileColumns = {"username", "avatar_url", "birthday", "location", "website", "comments", "forum_posts",
    		"last_online", "gender", "join_date", "access_rank", "anime_list_views", "manga_list_views",
			"anime_time_days_d", "anime_time_days", "anime_watching", "anime_completed", "anime_on_hold", "anime_dropped",	//anime
			"anime_plan_to_watch", "anime_total_entries",
			"manga_time_days_d", "manga_time_days", "manga_reading", "manga_completed", "manga_on_hold", "manga_dropped", 	//manga
			"manga_plan_to_read",	"manga_total_entries"};

    static MALSqlHelper malSqlHelper;

    MALApi malApi;
    static SQLiteDatabase dbRead;

    public MALManager(Context context) {
        malApi = new MALApi(context);
        if (malSqlHelper == null) {
            malSqlHelper = MALSqlHelper.getHelper(context);
        }
    }

    public synchronized static SQLiteDatabase getDBWrite() {
        return malSqlHelper.getWritableDatabase();
    }

    public static SQLiteDatabase getDBRead() {
        if (dbRead == null) {
            dbRead = malSqlHelper.getReadableDatabase();
        }
        return dbRead;
    }

    static String listSortFromInt(int i, String type) {
        String r = "";

        if (type.equals("anime")) {
            switch (i) {
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
        } else if (type.equals("manga")) {
            switch (i) {
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

    public HashMap<String, Object> getRecordDataFromJSONObject(JSONObject jsonObject, String type) {
        HashMap<String, Object> recordData = new HashMap<String, Object>();

        try {
        	if (type.equals(TYPE_ANIME)|| type.equals(TYPE_MANGA)) {
        		recordData.put("recordID", jsonObject.getInt("id"));
        		recordData.put("recordName", StringEscapeUtils.unescapeHtml4(jsonObject.getString("title")));
        		recordData.put("recordType", jsonObject.getString("type"));
        		recordData.put("recordStatus", jsonObject.getString("status"));
        		recordData.put("myScore", jsonObject.optInt("score"));
        		recordData.put("memberScore", (float) jsonObject.optDouble("members_score", 0.0));
        		recordData.put("imageUrl", jsonObject.getString("image_url").replaceFirst("t.jpg$", ".jpg"));
        		if (type.equals(TYPE_ANIME)) {
        			recordData.put("episodesTotal", jsonObject.optInt("episodes"));
        			recordData.put("episodesWatched", jsonObject.optInt("watched_episodes"));
        			recordData.put("myStatus", jsonObject.getString("watched_status"));
        		} else if (type.equals(TYPE_MANGA)) {
        			recordData.put("myStatus", jsonObject.getString("read_status"));
        			recordData.put("volumesTotal", jsonObject.optInt("volumes"));
        			recordData.put("chaptersTotal", jsonObject.optInt("chapters"));
                	recordData.put("volumesRead", jsonObject.optInt("volumes_read"));
                	recordData.put("chaptersRead", jsonObject.optInt("chapters_read"));
        		}
        	}else if (type.equals(TYPE_PROFILE)){
        		JSONObject details = jsonObject.getJSONObject("details");
        		JSONObject anime = jsonObject.getJSONObject("anime_stats");
        		JSONObject manga = jsonObject.getJSONObject("manga_stats");
        		
        		recordData.put("username", UserRecord.username);
        		recordData.put("avatar_url", jsonObject.getString("avatar_url"));
    			recordData.put("birthday", details.getString("birthday"));
    			recordData.put("location", details.getString("location"));
    			recordData.put("website", details.getString("website"));
    			recordData.put("comments", details.getInt("comments"));
    			recordData.put("forum_posts", details.getInt("forum_posts"));
    			recordData.put("last_online", details.getString("last_online"));
    			recordData.put("gender", details.getString("gender"));
    			recordData.put("join_date", details.getString("join_date"));
    			recordData.put("access_rank", details.getString("access_rank"));
    			recordData.put("anime_list_views", details.getInt("anime_list_views"));
    			recordData.put("manga_list_views", details.getInt("manga_list_views"));
    			
    			recordData.put("anime_time_days_d", Double.toString(anime.getDouble("time_days")));
    			recordData.put("anime_time_days", anime.getInt("time_days"));
    			recordData.put("anime_watching", anime.getInt("watching"));
    			recordData.put("anime_completed", anime.getInt("completed"));
    			recordData.put("anime_on_hold", anime.getInt("on_hold"));
    			recordData.put("anime_dropped", anime.getInt("dropped"));
    			recordData.put("anime_plan_to_watch", anime.getInt("plan_to_watch"));
    			recordData.put("anime_total_entries", anime.getInt("total_entries"));
    			
    			recordData.put("manga_time_days_d", Double.toString(manga.getDouble("time_days")));
    			recordData.put("manga_time_days", manga.getInt("time_days"));
    			recordData.put("manga_reading", manga.getInt("reading"));
    			recordData.put("manga_completed", manga.getInt("completed"));
    			recordData.put("manga_on_hold", manga.getInt("on_hold"));
    			recordData.put("manga_dropped", manga.getInt("dropped"));
    			recordData.put("manga_plan_to_read", manga.getInt("plan_to_read"));
    			recordData.put("manga_total_entries", manga.getInt("total_entries"));
        	}else if (type.equals(TYPE_FRIENDS)){
        		String friend_since = jsonObject.getString("friend_since");
    			if (friend_since == "null"){
    				friend_since="Unknown";
    			}else{
    				try {
    			        Date frienddate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH).parse(friend_since);
    			        Calendar calendar = Calendar.getInstance();
    			        calendar.setTime(frienddate);
    			        String am_pm = "";
    			        if(calendar.get(Calendar.AM_PM) == 0){ am_pm = "AM"; }else{ am_pm = "PM";}
    			        String time = calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + " " + am_pm;
    			        String date = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    			        friend_since = date + ", " + time;
					} catch (Exception e) {
						e.printStackTrace();
					}
    			}
    			JSONObject profile = new JSONObject(jsonObject.getString("profile"));
    			JSONObject details = new JSONObject(profile.getString("details"));
    			
        		recordData.put("username", jsonObject.getString("name"));
        		recordData.put("avatar_url", profile.getString("avatar_url"));
        		recordData.put("last_online", details.getString("last_online"));
        		recordData.put("friend_since", friend_since);
        	}
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), Log.getStackTraceString(e));
        }
        return recordData;
    }
    
    public void downloadAndStoreProfile(String user) {

    	JSONObject jsonObject = malApi.getProfile(user);
    	UserRecord.username = user;
        try {
            getDBWrite().beginTransaction();
            
            HashMap<String, Object> recordData = getRecordDataFromJSONObject(jsonObject, TYPE_PROFILE);
            
            UserRecord pr = new UserRecord(recordData);
            saveItem(pr, TYPE_PROFILE);

            getDBWrite().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getDBWrite().endTransaction();
        }
    }
    
    public void downloadAndStoreFriends(String user) {

        JSONArray jArray = malApi.getFriends(user);
        try {
            getDBWrite().beginTransaction();
            
            for (int i = 0; i < jArray.length(); i++) {
            	HashMap<String, Object> recordData = getRecordDataFromJSONObject(jArray.getJSONObject(i), TYPE_FRIENDS);
            	UserRecord fr = new UserRecord(recordData);
            	saveItem(fr,TYPE_FRIENDS);
            }

            getDBWrite().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getDBWrite().endTransaction();
        }
    }

    public boolean downloadAndStoreList(String type) {
    	boolean result = false;
        ContentValues contentValues = new ContentValues();
        contentValues.put("lastUpdate", 0);
        getDBWrite().update(type, contentValues, null, null);

        int currentTime = (int) new Date().getTime() / 1000;
        JSONArray jArray = malApi.getList(getListTypeFromString(type));
        if ( jArray != null ) {
        	// we successfully downloaded the list
        	result = true;
	        try {
	            getDBWrite().beginTransaction();
	            if(type.equals(TYPE_ANIME)) {
	
	                for (int i = 0; i < jArray.length(); i++) {
	                    HashMap<String, Object> recordData = getRecordDataFromJSONObject(jArray.getJSONObject(i), type);
	                    AnimeRecord ar = new AnimeRecord(recordData);
	                    ar.setLastUpdate(currentTime);
	                    saveItem(ar, true);
	                }
	
	            }
	            else {
	                for (int i = 0; i < jArray.length(); i++) {
	                    HashMap<String, Object> recordData = getRecordDataFromJSONObject(jArray.getJSONObject(i), type);
	                    MangaRecord mr = new MangaRecord(recordData);
	                    mr.setLastUpdate(currentTime);
	                    saveItem(mr, true);
	                }
	            }
	
	            getDBWrite().setTransactionSuccessful();
	            clearDeletedItems(type, currentTime);
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            getDBWrite().endTransaction();
	        }
        }
        return result;
    }

    public AnimeRecord getAnimeRecordFromMAL(int id) {
        String type = TYPE_ANIME;
        JSONObject jsonObject = malApi.getDetail(id, getListTypeFromString(type));
        HashMap<String, Object> recordData = getRecordDataFromJSONObject(jsonObject, type);
        AnimeRecord record = new AnimeRecord(recordData);
        if (record.getMyStatus().equals("")) {
            record.markForCreate(true);
        }
        return record;
    }

    public MangaRecord getMangaRecordFromMAL(int id) {
        String type = TYPE_MANGA;
        JSONObject jsonObject = malApi.getDetail(id, getListTypeFromString(type));
        HashMap<String, Object> recordData = getRecordDataFromJSONObject(jsonObject, type);
        MangaRecord record = new MangaRecord(recordData);
        if (record.getMyStatus().equals("")) {
            record.markForCreate(true);
        }
        return record;
    }

    public AnimeRecord updateWithDetails(int id, AnimeRecord animeRecord) {
        JSONObject jsonObject = malApi.getDetail(id, getListTypeFromString(TYPE_ANIME));

        animeRecord.setSynopsis(getDataFromJSON(jsonObject, "synopsis"));
        animeRecord.setMemberScore(Float.parseFloat(getDataFromJSON(jsonObject, "members_score")));

        if (!getDBWrite().inTransaction()) {
            saveItem(animeRecord, false);
        }
        return animeRecord;
    }

    public MangaRecord updateWithDetails(int id, MangaRecord mangaRecord) {
        JSONObject jsonObject = malApi.getDetail(id, getListTypeFromString(TYPE_MANGA));
        mangaRecord.setSynopsis(getDataFromJSON(jsonObject, "synopsis"));
        mangaRecord.setMemberScore(Float.parseFloat(getDataFromJSON(jsonObject, "members_score")));
        saveItem(mangaRecord, false);
        return mangaRecord;
    }

    public String getDataFromJSON(JSONObject json, String get) {
        // TODO: replace to jsonObject.optString(get, fallback);
        String sReturn = "";

        try {
            sReturn = json.getString(get);

            if ("episodes".equals(get)) {
                if ("null".equals(sReturn)) {
                    sReturn = "unknown";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            sReturn = "unknown";
        }

        return sReturn;
    }

    @SuppressLint("NewApi")
    public Object getObjectFromCursorColumn(Cursor cursor, int index, HashMap<String, Class<?>> typeMap) {
        if (Build.VERSION.SDK_INT >= 11) {
            // TODO Maybe use switch? switch with int, char available >= 1.6
            int object_type = cursor.getType(index);
            if (object_type == Cursor.FIELD_TYPE_STRING) {
                return cursor.getString(index);
            }else if (object_type == Cursor.FIELD_TYPE_FLOAT) {
                return cursor.getFloat(index);
            }else if (object_type == Cursor.FIELD_TYPE_INTEGER) {
                return cursor.getInt(index);
            }
        }
        else {
            String object_name = cursor.getColumnName(index);
            Class<?> cls = typeMap.get(object_name);
            if (cls == String.class) {
                return cursor.getString(index);
            }
            if (cls == Float.class) {
                return cursor.getFloat(index);
            }
            if (cls == Integer.class) {
                return cursor.getInt(index);
            }
        }
        return null;
    }

    public HashMap<String, Object> getRecordDataFromCursor(Cursor cursor, HashMap<String, Class<?>> typeMap) {
        HashMap<String, Object> record_data = new HashMap<String, Object>();
        String[] columns = cursor.getColumnNames();
        for (int i = 0; i < columns.length; i++) {
            record_data.put(columns[i], this.getObjectFromCursorColumn(cursor, i, typeMap));
        }
        return record_data;
    }
    
    public UserRecord getProfileRecordsFromDB() {
    	Integer index = null;
        Log.v("MALX", "getProfileRecordsFromDB() has been invoked for list " + "profile");

        ArrayList<UserRecord> profileRecordArrayList = new ArrayList<UserRecord>();
        Cursor cursor = null;
        try {
        	cursor = getDBRead().query("profile", this.profileColumns, null, null, null, null, "username ASC");
        	Log.v("MALX", "Got " + cursor.getCount() + " records.");
        	cursor.moveToFirst();

        	while (!cursor.isAfterLast()) {
        		profileRecordArrayList.add(new UserRecord(this.getRecordDataFromCursor(cursor, UserRecord.getTypeMapProfile())));
        		cursor.moveToNext();
        	}
        } finally {
        	if (cursor != null) {
                cursor.close();
            }
        }
        for(int x = 0; x <  (profileRecordArrayList.size()); x++) {
        	if (profileRecordArrayList.get(x).getUsername() == null){
        	}else if (profileRecordArrayList.get(x).getUsername().equals(UserRecord.username)){
        		index = x;
        	}
        }
        
        if (profileRecordArrayList.isEmpty()) {
            return null;
        }
        
        try{
        	return profileRecordArrayList.get(index);
        }catch (Exception e){
        	return null;
        }
    }
    
    public ArrayList<UserRecord> getFriendsRecordsFromDB() {
        Log.v("MALX", "getFriendsRecordsFromDB() has been invoked for list " + "friends");

        ArrayList<UserRecord> friendsRecordArrayList = new ArrayList<UserRecord>();
        Cursor cursor = null;
        try{
        	cursor = getDBRead().query("friends", this.friendsColumns, null, null, null, null, "username ASC");
        	Log.v("MALX", "Got " + cursor.getCount() + " records.");
        	cursor.moveToFirst();

        	while (!cursor.isAfterLast()) {
        		friendsRecordArrayList.add(new UserRecord(this.getRecordDataFromCursor(cursor, UserRecord.getTypeMapFriends())));
        		cursor.moveToNext();
        	}
        } finally {
        	if (cursor != null) {
        		cursor.close();
        	}
        }

        if (friendsRecordArrayList.isEmpty()) {
            return null;
        }

        return friendsRecordArrayList;
    }

    public ArrayList<AnimeRecord> getAnimeRecordsFromDB(int list) {
        Log.v("MALX", "getAnimeRecordsFromDB() has been invoked for list " + listSortFromInt(list, "anime"));

        ArrayList<AnimeRecord> animeRecordArrayList = new ArrayList<AnimeRecord>();
        Cursor cursor = null;
        
        try{
        	if (list == 0) {
        		cursor = getDBRead().query("anime", this.animeColumns, "myStatus = 'watching' OR myStatus = 'completed' OR myStatus = 'plan to watch' OR myStatus = 'dropped' OR myStatus = 'on-hold'", null, null, null, "recordName ASC");
        	} else {
        		cursor = getDBRead().query("anime", this.animeColumns, "myStatus = ?", new String[]{listSortFromInt(list, "anime")}, null, null, "recordName ASC");
        	}

        	Log.v("MALX", "Got " + cursor.getCount() + " records.");
        	cursor.moveToFirst();

        	while (!cursor.isAfterLast()) {
        		animeRecordArrayList.add(new AnimeRecord(this.getRecordDataFromCursor(cursor, AnimeRecord.getTypeMap())));
        		cursor.moveToNext();
        	}
        } finally {
        	if (cursor != null) {
        		cursor.close();
        	}
        }

        if (animeRecordArrayList.isEmpty()) {
            return null;
        }

        return animeRecordArrayList;
    }

    public ArrayList<MangaRecord> getMangaRecordsFromDB(int list) {
        Log.v("MALX", "getMangaRecordsFromDB() has been invoked for list " + listSortFromInt(list, "manga"));

        ArrayList<MangaRecord> mangaRecordArrayList = new ArrayList<MangaRecord>();
        Cursor cursor = null;
        try{
        	if (list == 0) {
        		cursor = getDBRead().query("manga", this.mangaColumns, "myStatus = 'reading' OR myStatus = 'completed' OR myStatus = 'plan to read' OR myStatus = 'dropped' OR myStatus = 'on-hold'", null, null, null, "recordName ASC");
        	} else {
        		cursor = getDBRead().query("manga", this.mangaColumns, "myStatus = ?", new String[]{listSortFromInt(list, "manga")}, null, null, "recordName ASC");
        	}

        	Log.v("MALX", "Got " + cursor.getCount() + " records.");
        	cursor.moveToFirst();

        	while (!cursor.isAfterLast()) {
        		mangaRecordArrayList.add(new MangaRecord(this.getRecordDataFromCursor(cursor, MangaRecord.getTypeMap())));
        		cursor.moveToNext();
        	}
        } finally {
        	if (cursor != null) {
        		cursor.close();
        	}
        }
        if (mangaRecordArrayList.isEmpty()) {
            return null;
        }

        return mangaRecordArrayList;
    }
    
    public void saveItem(UserRecord ur, String type) {
        ContentValues cv = new ContentValues();
        
        if (type.equals(TYPE_FRIENDS)){
        	cv.put("username", ur.getUsername());
        	cv.put("avatar_url", ur.getAvatar());
        	cv.put("last_online", ur.getLast());
        	cv.put("friend_since", ur.getSince());

        	if (itemExists(ur.getUsername(), "friends")) {
        		getDBWrite().update(MALSqlHelper.TABLE_FRIENDS, cv, "username=?", new String[]{ur.getUsername()});
        	} else {
        		getDBWrite().insert(MALSqlHelper.TABLE_FRIENDS, null, cv);
        	}
        }else{
        	cv.put("username", ur.getUsername());
        	cv.put("avatar_url", ur.getAvatar());
			cv.put("birthday", ur.getBirthday());
			cv.put("location", ur.getLocation());
			cv.put("website", ur.getWebsite());
			cv.put("comments", ur.getComments());
			cv.put("forum_posts", ur.getForumposts());
			cv.put("last_online", ur.getLast());
			cv.put("gender", ur.getGender());
			cv.put("join_date", ur.getJoinDate());
			cv.put("access_rank", ur.getAccessRank());
			cv.put("anime_list_views", ur.getAnimeListviews());
			cv.put("manga_list_views", ur.getMangaListviews());
			
			cv.put("anime_time_days_d", ur.getAnimeTimeDaysD());
			cv.put("anime_time_days", ur.getAnimeTimedays());
			cv.put("anime_watching", ur.getAnimeWatching());
			cv.put("anime_completed", ur.getAnimeCompleted());
			cv.put("anime_on_hold", ur.getAnimeOnHold());
			cv.put("anime_dropped", ur.getAnimeDropped());
			cv.put("anime_plan_to_watch", ur.getAnimePlanToWatch());
			cv.put("anime_total_entries", ur.getAnimeTotalEntries());
			
			cv.put("manga_time_days_d", ur.getMangatimedaysD());
			cv.put("manga_time_days", ur.getMangaTimedays());
			cv.put("manga_reading", ur.getMangaReading());
			cv.put("manga_completed", ur.getMangaCompleted());
			cv.put("manga_on_hold", ur.getMangaOnHold());
			cv.put("manga_dropped", ur.getMangaDropped());
			cv.put("manga_plan_to_read", ur.getMangaPlanToRead());
			cv.put("manga_total_entries", ur.getMangaTotalEntries());
        	
        	if (itemExists(ur.getUsername(), "profile")) {
        		getDBWrite().update(MALSqlHelper.TABLE_PROFILE, cv, "username=?", new String[]{ur.getUsername()});
        	} else {
        		getDBWrite().insert(MALSqlHelper.TABLE_PROFILE, null, cv);
        	}
        }
    }

    public void saveItem(MangaRecord mr, boolean ignoreSynopsis) {
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
        cv.put("chaptersRead", mr.getPersonalProgress(false));
        cv.put("volumesTotal", mr.getVolumesTotal());
        cv.put("chaptersTotal", mr.getTotal(false));
        cv.put("dirty", mr.getDirty());
        cv.put("lastUpdate", mr.getLastUpdate());

        if (!ignoreSynopsis) {
            cv.put("synopsis", mr.getSynopsis());
        }

        if (itemExists(mr.getID(), "manga")) {
            getDBWrite().update(MALSqlHelper.TABLE_MANGA, cv, "recordID=?", new String[]{mr.getID().toString()});
        } else {
            getDBWrite().insert(MALSqlHelper.TABLE_MANGA, null, cv);
        }
    }

    public void saveItem(AnimeRecord ar, boolean ignoreSynopsis) {

        ContentValues cv = new ContentValues();

        cv.put("recordID", ar.getID());
        cv.put("recordName", ar.getName());
        cv.put("recordType", ar.getRecordType());
        cv.put("imageUrl", ar.getImageUrl());
        cv.put("recordStatus", ar.getRecordStatus());
        cv.put("myStatus", ar.getMyStatus());
        cv.put("memberScore", ar.getMemberScore());
        cv.put("myScore", ar.getMyScore());
        cv.put("episodesWatched", ar.getPersonalProgress(false));
        cv.put("episodesTotal", ar.getTotal(false));
        cv.put("dirty", ar.getDirty());
        cv.put("lastUpdate", ar.getLastUpdate());


        if (!ignoreSynopsis) {
            cv.put("synopsis", ar.getSynopsis());
        }

        if (itemExists(ar.getID(), "anime")) {
            getDBWrite().update(MALSqlHelper.TABLE_ANIME, cv, "recordID=?", new String[]{ar.getID().toString()});
        } else {
            getDBWrite().insert(MALSqlHelper.TABLE_ANIME, null, cv);
        }
    }

    public AnimeRecord getAnimeRecordFromDB(int id) {
        Log.v("MALX", "getAnimeRecordFromDB() has been invoked for id " + id);
        Cursor cursor = getDBRead().query("anime", this.animeColumns, "recordID = ?", new String[]{Integer.toString(id)}, null, null, null);
        cursor.moveToFirst();
        AnimeRecord ar = new AnimeRecord(this.getRecordDataFromCursor(cursor, AnimeRecord.getTypeMap()));
        cursor.close();
        return ar;
    }

    public MangaRecord getMangaRecordFromDB(int id) {
        Log.v("MALX", "getMangaRecordFromDB() has been invoked for id " + id);

        Cursor cursor = getDBRead().query("manga", this.mangaColumns, "recordID = ?", new String[]{Integer.toString(id)}, null, null, null);

        cursor.moveToFirst();

        MangaRecord mr = new MangaRecord(this.getRecordDataFromCursor(cursor, MangaRecord.getTypeMap()));

        cursor.close();

        return mr;
    }

    public AnimeRecord getAnimeRecord(int recordID) {
        if (this.itemExists(recordID, TYPE_ANIME)) {
            return getAnimeRecordFromDB(recordID);
        }
        return getAnimeRecordFromMAL(recordID);
    }

    public MangaRecord getMangaRecord(int recordID) {
        if (this.itemExists(recordID, TYPE_MANGA)) {
            return this.getMangaRecordFromDB(recordID);
        }
        return getMangaRecordFromMAL(recordID);
    }

    public boolean itemExists(int id, String type) {
        return this.itemExists(Integer.toString(id), type);
    }

    public boolean itemExists(String id, String type) {
        if (type.equals("anime") || type.equals("manga")) {
            Cursor cursor = getDBRead().rawQuery("select 1 from " + type + " WHERE recordID=? LIMIT 1",
                    new String[]{id});
            boolean exists = (cursor.getCount() > 0);
            cursor.close();
            return exists;
        }else if (type.equals("friends") || type.equals("profile")){
        	 Cursor cursor = getDBRead().rawQuery("select 1 from " + type + " WHERE username=? LIMIT 1",
                     new String[]{id});
             boolean exists = (cursor.getCount() > 0);
             cursor.close();
             return exists;
        } else {
            throw new RuntimeException("itemExists called with unknown type.");
        }
    }

    public boolean writeDetailsToMAL(GenericMALRecord gr, String type) {
        boolean success;
        MALApi.ListType listType = getListTypeFromString(type);

        if (gr.hasDelete()) {
            success = malApi.deleteGenreFromList(listType, gr.getID().toString());
        } else {
            HashMap<String, String> data = new HashMap<String, String>();
            data.put("status", gr.getMyStatus());
            data.put("score", gr.getMyScoreString());
            switch (listType) {
                case ANIME: {
                    data.put("episodes", Integer.toString(gr.getPersonalProgress(false)));
                    break;
                }
                case MANGA: {
                    data.put("chapters", Integer.toString(gr.getPersonalProgress(false)));
                    data.put("volumes", Integer.toString(((MangaRecord) gr).getVolumeProgress()));
                    break;
                }
            }
            success = malApi.addOrUpdateGenreInList(gr.hasCreate(), listType, gr.getID().toString(), data);
        }
        return success;
    }

    public void clearDeletedItems(String type, long currentTime) {
        Log.v("MALX", "Removing deleted items of type " + type + " older than " + DateFormat.getDateTimeInstance().format(currentTime * 1000));

        int recordsRemoved = getDBWrite().delete(type, "lastUpdate < ?", new String[]{String.valueOf(currentTime)});

        Log.v("MALX", "Removed " + recordsRemoved + " " + type + " items");
    }

    public boolean deleteItemFromDatabase(String type, int recordID) {
        int deleted = getDBWrite().delete(type, "recordID = ?", new String[]{String.valueOf(recordID)});

        return deleted == 1;
    }

    public boolean addItemToMAL(GenericMALRecord gr, String type) {
        boolean success;
        MALApi.ListType listType = getListTypeFromString(type);

        if (gr.hasDelete()) {
            success = malApi.deleteGenreFromList(listType, gr.getID().toString());
        } else {
            HashMap<String, String> data = new HashMap<String, String>();
            data.put("score", "0");
            data.put("status", gr.getMyStatus());
            switch (listType) {
                case ANIME: {
                    data.put("episodes", Integer.toString(gr.getPersonalProgress(false)));
                    break;
                }
                case MANGA: {
                    data.put("chapters", Integer.toString(gr.getPersonalProgress(false)));
                    data.put("volumes", Integer.toString(((MangaRecord) gr).getVolumeProgress()));
                    break;
                }
            }

            success = malApi.addOrUpdateGenreInList(true, listType, gr.getID().toString(), data);
        }
        return success;
    }

    private MALApi.ListType getListTypeFromString(String type) {
        if (type.equals(TYPE_ANIME)) {

            return MALApi.ListType.ANIME;
        }

        else if (type.equals(TYPE_MANGA)) {
            return MALApi.ListType.MANGA;
        }

        else {
            return null;
        }
    }

    public boolean cleanDirtyAnimeRecords() {
        Cursor animeCursor;
        boolean totalSuccess = true;

        animeCursor = getDBRead().query("anime", this.animeColumns, "dirty = 1", null, null, null, "recordName ASC");

        Log.v("MALX", "Got " + animeCursor.getCount() + " dirty anime records. Cleaning..");
        animeCursor.moveToFirst();

        while (!animeCursor.isAfterLast()) {
            AnimeRecord ar = new AnimeRecord(this.getRecordDataFromCursor(animeCursor, AnimeRecord.getTypeMap()));

            totalSuccess = writeDetailsToMAL(ar, "anime");

            if (totalSuccess) {
                ar.setDirty(GenericMALRecord.CLEAN);
                saveItem(ar, false);
            }

            if (!totalSuccess) {
                break;
            }

            animeCursor.moveToNext();
        }

        animeCursor.close();

        Log.v("MALX", "Cleaned dirty anime records, status: " + totalSuccess);

        return totalSuccess;
    }

    public boolean cleanDirtyMangaRecords() {
        Cursor mangaCursor;
        boolean totalSuccess = true;

        if (totalSuccess) {
            mangaCursor = getDBRead().query("manga", this.mangaColumns, "dirty = 1", null, null, null, "recordName ASC");

            Log.v("MALX", "Got " + mangaCursor.getCount() + " dirty manga records. Cleaning..");
            mangaCursor.moveToFirst();

            while (!mangaCursor.isAfterLast()) {
                MangaRecord mr = new MangaRecord(this.getRecordDataFromCursor(mangaCursor, MangaRecord.getTypeMap()));

                totalSuccess = writeDetailsToMAL(mr, "manga");

                if (totalSuccess) {
                    mr.setDirty(GenericMALRecord.CLEAN);
                    saveItem(mr, false);
                }

                if (!totalSuccess) {
                    break;
                }

                mangaCursor.moveToNext();
            }

            mangaCursor.close();
        }

        Log.v("MALX", "Cleaned dirty manga records, status: " + totalSuccess);

        return totalSuccess;
    }

}
