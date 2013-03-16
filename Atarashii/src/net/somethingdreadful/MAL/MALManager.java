package net.somethingdreadful.MAL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import net.somethingdreadful.MAL.record.AnimeRecord;
import net.somethingdreadful.MAL.record.GenericMALRecord;
import net.somethingdreadful.MAL.record.MangaRecord;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MALManager {

    final static String APIProvider = "http://mal-api.com/";
    final static String VerifyAPI = "account/verify_credentials";
    final static String readAnimeListAPI = "animelist/";
    final static String readAnimeDetailsAPI = "anime/";
    final static String writeAnimeDetailsAPI = "animelist/anime";
    final static String readMangaListAPI = "mangalist/";
    final static String readMangaDetailsAPI = "manga/";
    final static String writeMangaDetailsAPI = "mangalist/manga";
    final static String readMineParam = "?mine=1";

    final static String TYPE_ANIME = "anime";
    final static String TYPE_MANGA = "manga";

    final static String USER_AGENT = "Atarashii! (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + " Build/" + Build.DISPLAY + ")";

    private String[] animeColumns = {"recordID", "recordName", "recordType", "recordStatus", "myStatus",
            "episodesWatched", "episodesTotal", "memberScore", "myScore", "synopsis", "imageUrl", "dirty", "lastUpdate"};

    private String[] mangaColumns = {"recordID", "recordName", "recordType", "recordStatus", "myStatus",
            "volumesRead", "chaptersRead", "volumesTotal", "chaptersTotal", "memberScore", "myScore", "synopsis",
            "imageUrl", "dirty", "lastUpdate"};

    Context c;
    PrefManager prefManager;
    String malUser;
    String malPass;
    MALSqlHelper helper;
    SQLiteDatabase db;

    public MALManager(Context c) {
        this.c = c;
        prefManager = new PrefManager(c);

        malUser = prefManager.getUser();
        malPass = prefManager.getPass();

        helper = new MALSqlHelper(this.c);
        db = helper.getWritableDatabase();
    }

    static public boolean verifyAccount(String user, String pass) {
        HttpGet request = new HttpGet(APIProvider + VerifyAPI);
        request.setHeader("Authorization", "basic " + Base64.encodeToString((user + ":" + pass).getBytes(), Base64.NO_WRAP));

        try {
            HttpClient client = new DefaultHttpClient();
            client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
            HttpResponse response = client.execute(request);
            int statusCode = 0;
            if (response != null) {
                StatusLine statusLine = response.getStatusLine();
                statusCode = statusLine.getStatusCode();
            }
            return statusCode == 200;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;


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

    public JSONObject getList(String type) {
        String readListAPI;
        String result;
        JSONObject jReturn = null;

        if (type.equals("anime")) {
            readListAPI = MALManager.readAnimeListAPI;
        } else if (type.equals("manga")) {
            readListAPI = MALManager.readMangaListAPI;
        } else {
            throw new RuntimeException("getList called with unknown list type.");
        }

        HttpGet request;
        HttpResponse response;
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);

        request = new HttpGet(APIProvider + readListAPI + malUser);
        request.setHeader("Authorization", "basic " + Base64.encodeToString((malUser + ":" + malPass).getBytes(), Base64.NO_WRAP));


        try {
            response = client.execute(request);

            HttpEntity getResponseEntity = response.getEntity();

            if (getResponseEntity != null) {
                result = EntityUtils.toString(getResponseEntity);
                jReturn = new JSONObject(result);

                Log.v("MALX", "Got JSON Response from the API");

            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }


        return jReturn;
    }

    public JSONObject getDetails(int id, String type) {

        String result;
        JSONObject jReturn = null;
        String readDetailsAPI;

        switch (type) {
            case "anime":
                readDetailsAPI = MALManager.readAnimeDetailsAPI;
                break;
            case "manga":
                readDetailsAPI = MALManager.readMangaDetailsAPI;
                break;
            default:
                throw new RuntimeException("getDetails called with unknown list type.");
        }

        HttpGet request;
        HttpResponse response;
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);

        request = new HttpGet(APIProvider + readDetailsAPI + id + readMineParam);
        request.setHeader("Authorization", "basic " + Base64.encodeToString((malUser + ":" + malPass).getBytes(), Base64.NO_WRAP));

        try {
            response = client.execute(request);

            HttpEntity getResponseEntity = response.getEntity();

            if (getResponseEntity != null) {
                result = EntityUtils.toString(getResponseEntity);
                jReturn = new JSONObject(result);

            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return jReturn;
    }

    public HashMap<String, Object> getRecordDataFromJSONObject(JSONObject jsonObject, String type) {
        HashMap<String, Object> recordData = new HashMap<>();
        try {
            recordData.put("recordID", jsonObject.getInt("id"));
            recordData.put("recordName", jsonObject.getString("title"));
            recordData.put("recordType", jsonObject.getString("type"));
            recordData.put("recordStatus", jsonObject.getString("status"));
            recordData.put("myScore", jsonObject.optInt("score"));
            recordData.put("memberScore", (float) jsonObject.optDouble("members_score", 0.0));
            recordData.put("imageUrl", jsonObject.getString("image_url").replaceFirst("t.jpg$", ".jpg"));
            if (type == TYPE_ANIME) {
                recordData.put("episodesTotal", jsonObject.optInt("episodes"));
                recordData.put("episodesWatched", jsonObject.optInt("watched_episodes"));
                recordData.put("myStatus", jsonObject.getString("watched_status"));
            } else if (type == TYPE_MANGA) {
                recordData.put("myStatus", jsonObject.getString("read_status"));
                recordData.put("volumesTotal", jsonObject.optInt("volumes"));
                recordData.put("chaptersTotal", jsonObject.optInt("chapters"));
                recordData.put("volumesRead", jsonObject.optInt("volumes_read"));
                recordData.put("chaptersRead", jsonObject.optInt("chapters_read"));
            }
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), Log.getStackTraceString(e));
        }
        return recordData;
    }

    public void downloadAndStoreList(String type) {
        JSONObject raw = getList(type);

        int currentTime = (int) new Date().getTime() / 1000;

        JSONArray jArray;
        try {
            if (type == TYPE_ANIME) {
                jArray = raw.getJSONArray(TYPE_ANIME);
                for (int i = 0; i < jArray.length(); i++) {
                    HashMap<String, Object> recordData = getRecordDataFromJSONObject(jArray.getJSONObject(i), TYPE_ANIME);
                    AnimeRecord ar = new AnimeRecord(recordData);
                    ar.setLastUpdate(currentTime);
                    saveItem(ar, true);
                }
            } else if (type == TYPE_MANGA) {
                jArray = raw.getJSONArray(TYPE_MANGA);
                for (int i = 0; i < jArray.length(); i++) {
                    HashMap<String, Object> recordData = getRecordDataFromJSONObject(jArray.getJSONObject(i), TYPE_ANIME);
                    MangaRecord mr = new MangaRecord(recordData);
                    mr.setLastUpdate(currentTime);
                    saveItem(mr, true);
                }
            }

            clearDeletedItems(type, currentTime);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public AnimeRecord getAnimeRecordFromMAL(int id) {
        AnimeRecord record;
        String type = TYPE_ANIME;
        JSONObject jObject = this.getDetails(id, type);
        HashMap<String, Object> recordData = getRecordDataFromJSONObject(jObject, type);
        record = new AnimeRecord(recordData);
        if (record.getMyStatus() == "null") {
            record.markForCreate(true);
        }
        return record;
    }

    public MangaRecord getMangaRecordFromMAL(int id) {
        MangaRecord record;
        String type = TYPE_MANGA;
        JSONObject jObject = this.getDetails(id, type);
        HashMap<String, Object> recordData = getRecordDataFromJSONObject(jObject, type);
        record = new MangaRecord(recordData);
        if (record.getMyStatus() == "null") {
            record.markForCreate(true);
        }
        return record;
    }

    public AnimeRecord updateWithDetails(int id, AnimeRecord animeRecord) {
        JSONObject jsonObject = getDetails(id, "anime");

        animeRecord.setSynopsis(getDataFromJSON(jsonObject, "synopsis"));
        animeRecord.setMemberScore(Float.parseFloat(getDataFromJSON(jsonObject, "members_score")));

        saveItem(animeRecord, false);

        return animeRecord;
    }

    public MangaRecord updateWithDetails(int id, MangaRecord mangaRecord) {
        JSONObject jsonObject = getDetails(id, "manga");
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

    public Object getObjectFromCursorColumn(Cursor cursor, int index) {
        int object_type = cursor.getType(index);
        if (object_type == Cursor.FIELD_TYPE_STRING) {
            return cursor.getString(index);
        }
        if (object_type == Cursor.FIELD_TYPE_FLOAT) {
            return cursor.getFloat(index);
        }
        if (object_type == Cursor.FIELD_TYPE_INTEGER) {
            return cursor.getInt(index);
        }
        return null;
    }

    public HashMap<String, Object> getRecordDataFromCursor(Cursor cursor) {
        HashMap<String, Object> record_data = new HashMap<>();
        String[] columns = cursor.getColumnNames();
        for (int i = 0; i < columns.length; i++) {
            record_data.put(columns[i], this.getObjectFromCursorColumn(cursor, i));
        }
        return record_data;
    }

    public ArrayList<AnimeRecord> getAnimeRecordsFromDB(int list) {
        Log.v("MALX", "getAnimeRecordsFromDB() has been invoked for list " + listSortFromInt(list, "anime"));

        ArrayList<AnimeRecord> animeRecordArrayList = new ArrayList<>();
        Cursor cursor;

        if (list == 0) {
            cursor = db.query("anime", this.animeColumns, "myStatus != 'null'", null, null, null, "recordName ASC");
        } else {
            cursor = db.query("anime", this.animeColumns, "myStatus = ?", new String[]{listSortFromInt(list, "anime")}, null, null, "recordName ASC");
        }


        Log.v("MALX", "Got " + cursor.getCount() + " records.");
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            animeRecordArrayList.add(new AnimeRecord(this.getRecordDataFromCursor(cursor)));
            cursor.moveToNext();
        }

        if (animeRecordArrayList.isEmpty()) {
            return null;
        }

        cursor.close();

        return animeRecordArrayList;
    }

    public ArrayList<MangaRecord> getMangaRecordsFromDB(int list) {
        Log.v("MALX", "getMangaRecordsFromDB() has been invoked for list " + listSortFromInt(list, "manga"));

        ArrayList<MangaRecord> mangaRecordArrayList = new ArrayList<>();
        Cursor cursor;

        if (list == 0) {
            cursor = db.query("manga", this.mangaColumns, "myStatus != 'null'", null, null, null, "recordName ASC");
        } else {
            cursor = db.query("manga", this.mangaColumns, "myStatus = ?", new String[]{listSortFromInt(list, "manga")}, null, null, "recordName ASC");
        }

        Log.v("MALX", "Got " + cursor.getCount() + " records.");
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            mangaRecordArrayList.add(new MangaRecord(this.getRecordDataFromCursor(cursor)));
            cursor.moveToNext();
        }

        if (mangaRecordArrayList.isEmpty()) {
            return null;
        }

        cursor.close();

        return mangaRecordArrayList;
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
        cv.put("chaptersRead", mr.getPersonalProgress());
        cv.put("volumesTotal", mr.getVolumesTotal());
        cv.put("chaptersTotal", mr.getTotal());
        cv.put("dirty", mr.getDirty());
        cv.put("lastUpdate", mr.getLastUpdate());

        if (!ignoreSynopsis) {
            cv.put("synopsis", mr.getSynopsis());
        }

        if (itemExists(mr.getID(), "manga")) {
            db.update(MALSqlHelper.TABLE_MANGA, cv, "recordID=?", new String[]{mr.getID().toString()});
        } else {
            db.insert(MALSqlHelper.TABLE_MANGA, null, cv);
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
        cv.put("episodesWatched", ar.getPersonalProgress());
        cv.put("episodesTotal", ar.getTotal());
        cv.put("dirty", ar.getDirty());
        cv.put("lastUpdate", ar.getLastUpdate());


        if (!ignoreSynopsis) {
            cv.put("synopsis", ar.getSynopsis());
        }

        if (itemExists(ar.getID(), "anime")) {
            db.update(MALSqlHelper.TABLE_ANIME, cv, "recordID=?", new String[]{ar.getID().toString()});
        } else {
            db.insert(MALSqlHelper.TABLE_ANIME, null, cv);
        }
    }

    public AnimeRecord getAnimeRecordFromDB(int id) {
        Log.v("MALX", "getAnimeRecordFromDB() has been invoked for id " + id);

        Cursor cu = db.query("anime", this.animeColumns, "recordID = ?", new String[]{Integer.toString(id)}, null, null, null);

        cu.moveToFirst();
        AnimeRecord ar = new AnimeRecord(this.getRecordDataFromCursor(cu));
        cu.close();

        return ar;
    }

    public MangaRecord getMangaRecordFromDB(int id) {
        Log.v("MALX", "getMangaRecordFromDB() has been invoked for id " + id);

        Cursor cu = db.query("manga", this.mangaColumns, "recordID = ?", new String[]{Integer.toString(id)}, null, null, null);

        cu.moveToFirst();

        MangaRecord mr = new MangaRecord(this.getRecordDataFromCursor(cu));

        cu.close();

        return mr;
    }

    public AnimeRecord getAnimeRecord(int recordID) {
        if (this.itemExists(recordID, "anime")) {
            return getAnimeRecordFromDB(recordID);
        }
        return getAnimeRecordFromMAL(recordID);
    }

    public MangaRecord getMangaRecord(int recordID) {
        if (this.itemExists(recordID, "anime")) {
            return this.getMangaRecordFromDB(recordID);
        }
        return getMangaRecordFromMAL(recordID);
    }

    public boolean itemExists(int id, String type) {
        return this.itemExists(Integer.toString(id), type);
    }

    public boolean itemExists(String id, String type) {
        if (type.equals("anime") || type.equals("manga")) {
            Cursor cursor = db.rawQuery("select 1 from " + type + " WHERE recordID=? LIMIT 1",
                    new String[]{id});
            boolean exists = (cursor.getCount() > 0);
            cursor.close();
            return exists;
        } else {
            throw new RuntimeException("itemExists called with unknown type.");
        }
    }

    public boolean writeDetailsToMAL(GenericMALRecord gr, String type) {
        // TODO refactoring
        boolean success = false;

        if (gr.hasDelete()) {
            HttpDelete deleteRequest;
            HttpResponse response;
            HttpClient client = new DefaultHttpClient();
            client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);

            if ("anime".equals(type)) {
                deleteRequest = new HttpDelete(APIProvider + writeAnimeDetailsAPI + gr.getID());
            } else {
                deleteRequest = new HttpDelete(APIProvider + writeMangaDetailsAPI + gr.getID());
            }

            deleteRequest.setHeader("Authorization", "basic " + Base64.encodeToString((malUser + ":" + malPass).getBytes(), Base64.NO_WRAP));

            try {
                response = client.execute(deleteRequest);

                if (200 == response.getStatusLine().getStatusCode()) {
                    success = true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            HttpResponse response;
            String uri = "";

            List<NameValuePair> putParams = new ArrayList<>();
            if (type.equals(TYPE_ANIME)) {
                uri = APIProvider + writeAnimeDetailsAPI;
                putParams.add(new BasicNameValuePair("status", gr.getMyStatus()));
                putParams.add(new BasicNameValuePair("episodes", Integer.toString(gr.getPersonalProgress())));
                putParams.add(new BasicNameValuePair("score", gr.getMyScoreString()));

            } else if (type.equals(TYPE_MANGA)) {
                uri = APIProvider + writeMangaDetailsAPI;
                putParams.add(new BasicNameValuePair("status", gr.getMyStatus()));
                putParams.add(new BasicNameValuePair("chapters", Integer.toString(gr.getPersonalProgress())));
                putParams.add(new BasicNameValuePair("volumes", Integer.toString(((MangaRecord) gr).getVolumeProgress())));
                putParams.add(new BasicNameValuePair("score", gr.getMyScoreString()));

            }

            HttpClient client = new DefaultHttpClient();
            client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
            HttpEntityEnclosingRequestBase writeRequest;
            if (gr.hasCreate()) {
                writeRequest = new HttpPost(uri);
                if (type.equals(TYPE_ANIME)) {
                    putParams.add(new BasicNameValuePair("anime_id", gr.getID().toString()));
                } else if (type.equals(TYPE_MANGA)) {
                    putParams.add(new BasicNameValuePair("manga_id", gr.getID().toString()));
                }
            } else {
                writeRequest = new HttpPut(uri + "/" + gr.getID());
            }
            writeRequest.setHeader("Authorization", "basic " + Base64.encodeToString((malUser + ":" + malPass).getBytes(), Base64.NO_WRAP));

            try {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(putParams);
                writeRequest.setEntity(entity);

                response = client.execute(writeRequest);

                if (200 == response.getStatusLine().getStatusCode()) {
                    success = true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    public void clearDeletedItems(String type, long currentTime) {
        Log.v("MALX", "Removing deleted items of type " + type + " older than " + DateFormat.getDateTimeInstance().format(currentTime * 1000));

        int recordsRemoved = db.delete(type, "lastUpdate < ?", new String[]{String.valueOf(currentTime)});

        Log.v("MALX", "Removed " + recordsRemoved + " " + type + " items");
    }

    public boolean deleteItemFromDatabase(String type, int recordID) {
        int deleted = db.delete(type, "recordID = ?", new String[]{String.valueOf(recordID)});

        return deleted == 1;
    }

}
