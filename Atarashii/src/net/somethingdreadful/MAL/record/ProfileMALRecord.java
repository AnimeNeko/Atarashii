package net.somethingdreadful.MAL.record;

import java.util.ArrayList;
import net.somethingdreadful.MAL.FriendsActivity;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class ProfileMALRecord {
	public static Context context;
	public static String username = "";
	FriendsActivity friends;
	
	public static ArrayList<String> record = new ArrayList<String>();
	public static ArrayList<String> Friendrecord = new ArrayList<String>();
    
	public static boolean Loadrecord() { //Load the data of an user
		SharedPreferences Read = PreferenceManager.getDefaultSharedPreferences(context);
		for(int i=0;i < 28;i++){
			record.add(i,Read.getString(Integer.toString(i) + ProfileMALRecord.username, "No data"));
		}
		if (record.size()==0){
			return false;
		}else if (record.size()==28){
			return true;
		}else{
			return false;
		}
	}
    
    public static void Saverecord(){ //Save the data of an user
    	SharedPreferences.Editor Edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
    	for(int i=0;i < 28;i++){
			Edit.putString(Integer.toString(i) + ProfileMALRecord.username ,ProfileMALRecord.record.get(i)).commit();
		}
    }
    
    public static void Grabrecord(JSONObject jsonObject){ //Grab the data of an user
    	try {
    		for(int i=0;i < 28;i++){ //Fix bug (when user is scrolling onload)
    			record.add("");
    		}
    		record.add(0,jsonObject.getString("avatar_url").toString());
			record.add(1,jsonObject.getJSONObject("details").getString("birthday")); // get birthday for check
			record.add(2,jsonObject.getJSONObject("details").getString("location"));
			record.add(3,jsonObject.getJSONObject("details").getString("website"));
			record.add(4,Integer.toString(jsonObject.getJSONObject("details").getInt("comments")));
			record.add(5,Integer.toString(jsonObject.getJSONObject("details").getInt("forum_posts")));
			record.add(6,jsonObject.getJSONObject("details").getString("last_online"));
			record.add(7,jsonObject.getJSONObject("details").getString("gender"));
			record.add(8,jsonObject.getJSONObject("details").getString("join_date"));
			record.add(9,jsonObject.getJSONObject("details").getString("access_rank"));
			record.add(10,Integer.toString(jsonObject.getJSONObject("details").getInt("anime_list_views")));
			record.add(11,Integer.toString(jsonObject.getJSONObject("details").getInt("manga_list_views")));
			
			record.add(12,Double.toString(jsonObject.getJSONObject("anime_stats").getDouble("time_days")));
			record.add(13,Integer.toString(jsonObject.getJSONObject("anime_stats").getInt("time_days"))); //get int for colors
			record.add(14,Integer.toString(jsonObject.getJSONObject("anime_stats").getInt("watching")));
			record.add(15,Integer.toString(jsonObject.getJSONObject("anime_stats").getInt("completed")));
			record.add(16,Integer.toString(jsonObject.getJSONObject("anime_stats").getInt("on_hold")));
			record.add(17,Integer.toString(jsonObject.getJSONObject("anime_stats").getInt("dropped")));
			record.add(18,Integer.toString(jsonObject.getJSONObject("anime_stats").getInt("plan_to_watch")));
			record.add(19,Integer.toString(jsonObject.getJSONObject("anime_stats").getInt("total_entries")));
			
			record.add(20,Double.toString(jsonObject.getJSONObject("manga_stats").getDouble("time_days")));
			record.add(21,Integer.toString(jsonObject.getJSONObject("manga_stats").getInt("time_days"))); //get int for colors
			record.add(22,Integer.toString(jsonObject.getJSONObject("manga_stats").getInt("reading")));
			record.add(23,Integer.toString(jsonObject.getJSONObject("manga_stats").getInt("completed")));
			record.add(24,Integer.toString(jsonObject.getJSONObject("manga_stats").getInt("on_hold")));
			record.add(25,Integer.toString(jsonObject.getJSONObject("manga_stats").getInt("dropped")));
			record.add(26,Integer.toString(jsonObject.getJSONObject("manga_stats").getInt("plan_to_read")));
			record.add(27,Integer.toString(jsonObject.getJSONObject("manga_stats").getInt("total_entries")));
			Saverecord();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static void Clearrecord(boolean load){ //clear the record(saved items will not be lost)
    	for(int i=0;i < record.size();i++){
    		record.remove(i);
		}
    	if (load){
    		ProfileMALRecord.Loadrecord();
    	}
    }
    
    public static void LoadFriendrecord() { //Load the data of an user
    	try{
    		SharedPreferences Read = PreferenceManager.getDefaultSharedPreferences(context);
			int size = Read.getInt("Friendcount",0);
			Friendrecord.clear();
    		for(int i=0;i<size;i++){
    			Friendrecord.add(Read.getString(Integer.toString(i),"Error"));
    		}
    	}catch (Exception e){
    		e.printStackTrace();
    	}
	}
    
    public static void GrabFriendrecord(JSONObject jsonObject){ //Grab the data of an user
    	try {
    		SharedPreferences.Editor Edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
    			String name = jsonObject.getString("name");
    			String friend_since = jsonObject.getString("friend_since");
    				JSONObject profile = new JSONObject(jsonObject.getString("profile"));
    				if (friend_since == "null"){friend_since="Unknown";}
    			Edit.putString("avatar_url" + name , profile.getString("avatar_url")).commit();
    				JSONObject details = new JSONObject(profile.getString("details"));
    			Edit.putString("last_online" + name ,details.getString("last_online")).commit();
    			Edit.putString("since" + name , friend_since).commit();
    			Friendrecord.add(name);
			SaveFriendrecord();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static void SaveFriendrecord(){ //Save the data of an user
    	try{
    		SharedPreferences.Editor Edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
    		Edit.putInt("Friendcount", Friendrecord.size());
    		for(int i=0;i < Friendrecord.size();i++){
    			Edit.putString(Integer.toString(i) ,ProfileMALRecord.Friendrecord.get(i)).commit();
			}
    	}catch (Exception e){
    		e.printStackTrace();
    	}
    }
    
    public static boolean Developerrecord(String name){ 
    	if (name.equals("Ratan12") || name.equals("ratan12") || 
    			name.equals("AnimaSA") || name.equals("animaSA") || 
    			name.equals("Motokochan") || name.equals("motokochan") ||
    			name.equals("Apkawa") ||  name.equals("apkawa")) {
    		return true;
		}else{
			return false;
		}
    }
}
