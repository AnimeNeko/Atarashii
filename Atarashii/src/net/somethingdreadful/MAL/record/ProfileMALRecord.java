package net.somethingdreadful.MAL.record;

import net.somethingdreadful.MAL.FriendsActivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class ProfileMALRecord {
	public static Context context;
	public static String username = "";
	public static boolean parse = true; //if the boolean is false then it will stop the refresh at FriendsActivity
	public static String avatar_url_short = "";
	public static String since = "";
	FriendsActivity friends;
	
	//details
	public static String avatar_url = "";
	public static String birthday = ""; 
	public static String location = "";
	public static String website ="";
	public static Integer comments = 0;
	public static Integer forum_posts = 0;
	public static String last_online = "";
	public static String gender = "";
	public static String join_date = "";
	public static String access_rank = "";
	public static Integer anime_list_views = 0;
	public static Integer manga_list_views = 0;
	//anime details
	public static String A_time_days = "0";
	public static Integer A_time_daysint = 0;
	public static Integer A_watching = 0;
	public static Integer A_completed = 0;
	public static Integer A_on_hold = 0;
	public static Integer A_dropped = 0;
	public static Integer A_plan_to_watch = 0;
	public static Integer A_total_entries = 0;
	//manga details
	public static String M_time_days = "0";
	public static Integer M_time_daysint = 0;
	public static Integer M_reading = 0;
	public static Integer M_completed = 0;
	public static Integer M_on_hold = 0;
	public static Integer M_dropped = 0;
	public static Integer M_plan_to_read = 0;
	public static Integer M_total_entries = 0;
    
	public static void Loadrecord(){ //Load the data of an user
		SharedPreferences Read = PreferenceManager.getDefaultSharedPreferences(context);
		ProfileMALRecord.avatar_url = Read.getString("avatar_url" + ProfileMALRecord.username, avatar_url);
		ProfileMALRecord.birthday = Read.getString("birthday" + ProfileMALRecord.username, birthday);
		ProfileMALRecord.location = Read.getString("location" + ProfileMALRecord.username, location);
		ProfileMALRecord.website = Read.getString("website" + ProfileMALRecord.username, website);
		ProfileMALRecord.comments = Read.getInt("comments" + ProfileMALRecord.username, comments);
		ProfileMALRecord.forum_posts = Read.getInt("forum_posts" + ProfileMALRecord.username, forum_posts);
		ProfileMALRecord.last_online = Read.getString("last_online" + ProfileMALRecord.username, last_online);
		ProfileMALRecord.gender = Read.getString("gender" + ProfileMALRecord.username, gender);
		ProfileMALRecord.join_date = Read.getString("join_date" + ProfileMALRecord.username, join_date);
		ProfileMALRecord.access_rank = Read.getString("access_rank" + ProfileMALRecord.username, access_rank);
		ProfileMALRecord.anime_list_views = Read.getInt("anime_list_views" + ProfileMALRecord.username, anime_list_views);
		ProfileMALRecord.manga_list_views = Read.getInt("manga_list_views" + ProfileMALRecord.username, manga_list_views);
		
		ProfileMALRecord.A_time_days = Read.getString("A_time_days" + ProfileMALRecord.username, A_time_days);
		ProfileMALRecord.A_time_daysint = Read.getInt("A_time_daysint" + ProfileMALRecord.username, A_time_daysint);
		ProfileMALRecord.A_watching = Read.getInt("A_watching" + ProfileMALRecord.username, A_watching);
		ProfileMALRecord.A_completed = Read.getInt("A_completed" + ProfileMALRecord.username, A_completed);
		ProfileMALRecord.A_on_hold = Read.getInt("A_on_hold" + ProfileMALRecord.username, A_on_hold);
		ProfileMALRecord.A_dropped = Read.getInt("A_dropped" + ProfileMALRecord.username, A_dropped);
		ProfileMALRecord.A_plan_to_watch = Read.getInt("A_plan_to_watch" + ProfileMALRecord.username, A_plan_to_watch);
		ProfileMALRecord.A_total_entries = Read.getInt("A_total_entries" + ProfileMALRecord.username, A_total_entries);
		
		ProfileMALRecord.M_time_days = Read.getString("M_time_days" + ProfileMALRecord.username, M_time_days);
		ProfileMALRecord.M_time_daysint = Read.getInt("M_time_daysint" + ProfileMALRecord.username, M_time_daysint);
		ProfileMALRecord.M_reading = Read.getInt("M_reading" + ProfileMALRecord.username, M_reading);
		ProfileMALRecord.M_completed = Read.getInt("M_completed" + ProfileMALRecord.username, M_completed);
		ProfileMALRecord.M_on_hold = Read.getInt("M_on_hold" + ProfileMALRecord.username, M_on_hold);
		ProfileMALRecord.M_dropped = Read.getInt("M_dropped" + ProfileMALRecord.username, M_dropped);
		ProfileMALRecord.M_plan_to_read = Read.getInt("M_plan_to_watch" + ProfileMALRecord.username, M_plan_to_read);
		ProfileMALRecord.M_total_entries = Read.getInt("M_total_entries" + ProfileMALRecord.username, M_total_entries);
	}
    
    public static void Saverecord(){ //Save the data of an user
    	SharedPreferences.Editor Edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		Edit.putString("avatar_url" + ProfileMALRecord.username ,avatar_url).commit();
		Edit.putString("birthday" + ProfileMALRecord.username ,birthday).commit();
		Edit.putString("location" + ProfileMALRecord.username ,location).commit();
		Edit.putString("website" + ProfileMALRecord.username ,website).commit();
		Edit.putInt("comments" + ProfileMALRecord.username ,comments).commit();
		Edit.putInt("forum_posts" + ProfileMALRecord.username ,forum_posts).commit();
		Edit.putString("last_online" + ProfileMALRecord.username ,last_online).commit();
		Edit.putString("gender" + ProfileMALRecord.username ,gender).commit();
		Edit.putString("join_date" + ProfileMALRecord.username ,join_date).commit();
		Edit.putString("access_rank" + ProfileMALRecord.username ,access_rank).commit();
		Edit.putInt("anime_list_views" + ProfileMALRecord.username ,anime_list_views).commit();
		Edit.putInt("manga_list_views" + ProfileMALRecord.username ,manga_list_views).commit();
		
		Edit.putString("A_time_days" + ProfileMALRecord.username ,A_time_days).commit();
		Edit.putInt("A_time_daysint" + ProfileMALRecord.username ,A_time_daysint).commit();
		Edit.putInt("A_watching" + ProfileMALRecord.username ,A_watching).commit();
		Edit.putInt("A_completed" + ProfileMALRecord.username ,A_completed).commit();
		Edit.putInt("A_on_hold" + ProfileMALRecord.username ,A_on_hold).commit();
		Edit.putInt("A_dropped" + ProfileMALRecord.username ,A_dropped).commit();
		Edit.putInt("A_plan_to_watch" + ProfileMALRecord.username ,A_plan_to_watch).commit();
		Edit.putInt("A_total_entries" + ProfileMALRecord.username ,A_total_entries).commit();
		
		Edit.putString("M_time_days" + ProfileMALRecord.username ,M_time_days).commit();
		Edit.putInt("M_time_daysint" + ProfileMALRecord.username ,M_time_daysint).commit();
		Edit.putInt("M_reading" + ProfileMALRecord.username ,M_reading).commit();
		Edit.putInt("M_completed" + ProfileMALRecord.username ,M_completed).commit();
		Edit.putInt("M_on_hold" + ProfileMALRecord.username ,M_on_hold).commit();
		Edit.putInt("M_dropped" + ProfileMALRecord.username ,M_dropped).commit();
		Edit.putInt("M_plan_to_read" + ProfileMALRecord.username ,M_plan_to_read).commit();
		Edit.putInt("M_total_entries" + ProfileMALRecord.username ,M_total_entries).commit();
    }
    
    public static void Grabrecord(JSONObject jsonObject){ //Grab the data of an user
    	try {
			ProfileMALRecord.avatar_url = jsonObject.getString("avatar_url");
			ProfileMALRecord.birthday = jsonObject.getJSONObject("details").getString("birthday"); // get birthday for check
			ProfileMALRecord.location = jsonObject.getJSONObject("details").getString("location");
			ProfileMALRecord.website = jsonObject.getJSONObject("details").getString("website");
			ProfileMALRecord.comments = jsonObject.getJSONObject("details").getInt("comments");
			ProfileMALRecord.forum_posts = jsonObject.getJSONObject("details").getInt("forum_posts");
			ProfileMALRecord.last_online = jsonObject.getJSONObject("details").getString("last_online");
			ProfileMALRecord.gender = jsonObject.getJSONObject("details").getString("gender");
			ProfileMALRecord.join_date = jsonObject.getJSONObject("details").getString("join_date");
			ProfileMALRecord.access_rank = jsonObject.getJSONObject("details").getString("access_rank");
			ProfileMALRecord.anime_list_views = jsonObject.getJSONObject("details").getInt("anime_list_views");
			ProfileMALRecord.manga_list_views = jsonObject.getJSONObject("details").getInt("manga_list_views");
			
			ProfileMALRecord.A_time_days = Double.toString(jsonObject.getJSONObject("anime_stats").getDouble("time_days"));
			ProfileMALRecord.A_time_daysint = jsonObject.getJSONObject("anime_stats").getInt("time_days");//get int for colors
			ProfileMALRecord.A_watching = jsonObject.getJSONObject("anime_stats").getInt("watching");
			ProfileMALRecord.A_completed = jsonObject.getJSONObject("anime_stats").getInt("completed");
			ProfileMALRecord.A_on_hold = jsonObject.getJSONObject("anime_stats").getInt("on_hold");
			ProfileMALRecord.A_dropped = jsonObject.getJSONObject("anime_stats").getInt("dropped");
			ProfileMALRecord.A_plan_to_watch = jsonObject.getJSONObject("anime_stats").getInt("plan_to_watch");
			ProfileMALRecord.A_total_entries = jsonObject.getJSONObject("anime_stats").getInt("total_entries");
			
			ProfileMALRecord.M_time_days = Double.toString(jsonObject.getJSONObject("manga_stats").getDouble("time_days"));
			ProfileMALRecord.M_time_daysint = jsonObject.getJSONObject("manga_stats").getInt("time_days"); //get int for colors
			ProfileMALRecord.M_reading = jsonObject.getJSONObject("manga_stats").getInt("reading");
			ProfileMALRecord.M_completed = jsonObject.getJSONObject("manga_stats").getInt("completed");
			ProfileMALRecord.M_on_hold = jsonObject.getJSONObject("manga_stats").getInt("on_hold");
			ProfileMALRecord.M_dropped = jsonObject.getJSONObject("manga_stats").getInt("dropped");
			ProfileMALRecord.M_plan_to_read = jsonObject.getJSONObject("manga_stats").getInt("plan_to_read");
			ProfileMALRecord.M_total_entries = jsonObject.getJSONObject("manga_stats").getInt("total_entries");
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    public static void Clearrecord(boolean load){ //clear the record(saved items will not be lost)
    	ProfileMALRecord.avatar_url_short = "";
    	ProfileMALRecord.since = "";
    	
    	ProfileMALRecord.avatar_url = "";
    	ProfileMALRecord.birthday = ""; 
    	ProfileMALRecord.location = "";
    	ProfileMALRecord.website ="";
    	ProfileMALRecord.comments = 0;
    	ProfileMALRecord.forum_posts = 0;
    	ProfileMALRecord.last_online = "";
    	ProfileMALRecord.gender = "";
    	ProfileMALRecord.join_date = "";
    	ProfileMALRecord.access_rank = "";
    	ProfileMALRecord.anime_list_views = 0;
    	ProfileMALRecord.manga_list_views = 0;
    	
    	ProfileMALRecord.A_time_days = "0";
    	ProfileMALRecord.A_time_daysint = 0;
    	ProfileMALRecord.A_watching = 0;
    	ProfileMALRecord.A_completed = 0;
    	ProfileMALRecord.A_on_hold = 0;
    	ProfileMALRecord.A_dropped = 0;
    	ProfileMALRecord.A_plan_to_watch = 0;
    	ProfileMALRecord.A_total_entries = 0;
    	
    	ProfileMALRecord.M_time_days = "0";
    	ProfileMALRecord.M_time_daysint = 0;
    	ProfileMALRecord.M_reading = 0;
    	ProfileMALRecord.M_completed = 0;
    	ProfileMALRecord.M_on_hold = 0;
    	ProfileMALRecord.M_dropped = 0;
    	ProfileMALRecord.M_plan_to_read = 0;
    	ProfileMALRecord.M_total_entries = 0;
    	if (load){
    		ProfileMALRecord.Loadrecord();
    	}
    }
    public static boolean Developerrecord(String name){ 
    	if (ProfileMALRecord.username.equals("Ratan12") || ProfileMALRecord.username.equals("ratan12") || 
    			ProfileMALRecord.username.equals("AnimaSA") || ProfileMALRecord.username.equals("animaSA") || 
    			ProfileMALRecord.username.equals("Motokochan") || ProfileMALRecord.username.equals("motokochan") ||
    			ProfileMALRecord.username.equals("Apkawa") ||  ProfileMALRecord.username.equals("apkawa")) {
    		return true;
		}else{
			return false;
		}
    }
}
