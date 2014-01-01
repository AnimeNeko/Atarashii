package net.somethingdreadful.MAL.record;

import java.util.HashMap;

public class UserRecord extends GenericMALRecord {
	
    public UserRecord(HashMap<String, Object> record_data) {
        super(record_data);
    }
    
    public static String username;
	
	public String getUsername() {
        return (String) recordData.get("username");
    }
	public String getAvatar() {
        return (String) recordData.get("avatar_url");
    }
	public String getLast() {
        return (String) recordData.get("last_online");
    }
	public String getSince() {
        return (String) recordData.get("friend_since");
    }
    
    public static boolean developerRecord(String name){ 
    	if (name.equals("Ratan12") || name.equals("ratan12") || 
    			name.equals("AnimaSA") || name.equals("animaSA") || 
    			name.equals("Motokochan") || name.equals("motokochan") ||
    			name.equals("Apkawa") ||  name.equals("apkawa")) {
    		return true;
		}else{
			return false;
		}
    }
	
    public static HashMap<String, Class<?>> getTypeMapFriends() {
        typeMap = new HashMap<String, Class<?>>();

        typeMap.put("username", String.class);
        typeMap.put("avatar_url", String.class);
        typeMap.put("last_online", String.class);
        typeMap.put("friend_since", String.class);
        return typeMap;
    }
    
    public static HashMap<String, Class<?>> getTypeMapProfile() {
        typeMap = new HashMap<String, Class<?>>();

        typeMap.put("username", String.class);
        typeMap.put("avatar_url", String.class);
		typeMap.put("birthday", String.class); // get birthday for check
		typeMap.put("location", String.class);
		typeMap.put("website", String.class);
		typeMap.put("comments", Integer.class);
		typeMap.put("forum_posts", Integer.class);
		typeMap.put("last_online", String.class);
		typeMap.put("gender", String.class);
		typeMap.put("join_date", String.class);
		typeMap.put("access_rank", String.class);
		typeMap.put("anime_list_views", Integer.class);
		typeMap.put("manga_list_views", Integer.class);
		
		typeMap.put("anime_time_days_d", String.class);
		typeMap.put("anime_time_days", Integer.class); //get int for colors
		typeMap.put("anime_watching", Integer.class);
		typeMap.put("anime_completed", Integer.class);
		typeMap.put("anime_on_hold", Integer.class);
		typeMap.put("anime_dropped", Integer.class);
		typeMap.put("anime_plan_to_watch", Integer.class);
		typeMap.put("anime_total_entries", Integer.class);
		
		typeMap.put("manga_time_days_d", String.class);
		typeMap.put("manga_time_days", Integer.class); //get int for colors
		typeMap.put("manga_reading", Integer.class);
		typeMap.put("manga_completed", Integer.class);
		typeMap.put("manga_on_hold", Integer.class);
		typeMap.put("manga_dropped", Integer.class);
		typeMap.put("manga_plan_to_read", Integer.class);
		typeMap.put("manga_total_entries", Integer.class);
        return typeMap;
    }
    
	public String getBirthday() {
		return (String) recordData.get("birthday");
	}
	public String getLocation() {
		return (String) recordData.get("location");
	}
	public String getWebsite() {
		return (String) recordData.get("website");
	}
	public Integer getComments() {
		return (Integer) recordData.get("comments");
	}
	public Integer getForumposts() {
		return (Integer) recordData.get("forum_posts");
	}
	public String getGender() {
		return (String) recordData.get("gender");
	}
	public String getJoinDate() {
		return (String) recordData.get("join_date");
	}
	public String getAccessRank() {
		return (String) recordData.get("access_rank");
	}
	public Integer getAnimeListviews() {
		return (Integer) recordData.get("anime_list_views");
	}
	public Integer getMangaListviews() {
		return (Integer) recordData.get("manga_list_views");
	}

	public String getAnimeTimeDaysD() {
		return (String) recordData.get("anime_time_days_d").toString();
	}
	public Integer getAnimeTimedays() {
		return (Integer) recordData.get("anime_time_days");
	}
	public Integer getAnimeWatching() {
		return (Integer) recordData.get("anime_watching");
	}
	public Integer getAnimeCompleted() {
		return (Integer) recordData.get("anime_completed");
	}
	public Integer getAnimeOnHold() {
		return (Integer) recordData.get("anime_on_hold");
	}
	public Integer getAnimeDropped() {
		return (Integer) recordData.get("anime_dropped");
	}
	public Integer getAnimePlanToWatch() {
		return (Integer) recordData.get("anime_plan_to_watch");
	}
	public Integer getAnimeTotalEntries() {
		return (Integer) recordData.get("anime_total_entries");
	}

	public String getMangatimedaysD() {
		return (String) recordData.get("manga_time_days_d").toString();
	}
	public Integer getMangaTimedays() {
		return (Integer) recordData.get("manga_time_days");
	}
	public Integer getMangaReading() {
		return (Integer) recordData.get("manga_reading");
	}
	public Integer getMangaCompleted() {
		return (Integer) recordData.get("manga_completed");
	}
	public Integer getMangaOnHold() {
		return (Integer) recordData.get("manga_on_hold");
	}
	public Integer getMangaDropped() {
		return (Integer) recordData.get("manga_dropped");
	}
	public Integer getMangaPlanToRead() {
		return (Integer) recordData.get("manga_plan_to_read");
	}
	public Integer getMangaTotalEntries() {
		return (Integer) recordData.get("manga_total_entries");
	}

	@Override
	public Integer getPersonalProgress(boolean useSecondaryAmount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPersonalProgress(boolean useSecondaryAmount, Integer amount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTotal(boolean useSecondaryAmount) {
		// TODO Auto-generated method stub
		return null;
	}
}