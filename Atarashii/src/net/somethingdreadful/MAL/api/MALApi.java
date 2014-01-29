package net.somethingdreadful.MAL.api;

import java.util.ArrayList;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;

import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.Friend;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;
import net.somethingdreadful.MAL.api.response.Profile;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;
import retrofit.client.Response;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public class MALApi {
    private static final String API_HOST = "http://api.atarashiiapp.com";
    private static final String USER_AGENT = "Atarashii! (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + " Build/" + Build.DISPLAY + ")";
    private static final String FRIENDS_HOST = "http://newapi.atarashiiapp.com"; //TEMPORARY UNTIL NEW
    
    private MALInterface service;
    private FriendsInterface friends_service; //TEMPORARY UNTIL NEW
    private String username;
    
    public enum ListType {
    	ANIME, MANGA
    }

	public MALApi(Context context) {
		PrefManager prefManager = new PrefManager(context);
		username = prefManager.getUser();
		setupRESTService(prefManager.getUser(), prefManager.getPass());
		setupFriendRESTService();
	}
	
	public MALApi(String username, String password) {
		this.username = username;
		setupRESTService(username, password);
		setupFriendRESTService();
    }
	
	private void setupRESTService(String username, String password) {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpProtocolParams.setUserAgent(client.getParams(), USER_AGENT);
		client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
				new UsernamePasswordCredentials(username,password));
		
		RestAdapter restAdapter = new RestAdapter.Builder()
			.setClient(new ApacheClient(client))
			.setServer(API_HOST)
			.build();
		service = restAdapter.create(MALInterface.class);
	}
	
	private void setupFriendRESTService() {
	    DefaultHttpClient client = new DefaultHttpClient();
        HttpProtocolParams.setUserAgent(client.getParams(), USER_AGENT);
        
        RestAdapter restAdapter = new RestAdapter.Builder()
            .setClient(new ApacheClient(client))
            .setServer(FRIENDS_HOST)
            .build();
        friends_service = restAdapter.create(FriendsInterface.class);
	}

	public boolean isAuth() {
		try {
			Response response = service.verifyAuthentication();
			return response.getStatus() == 200;
		} catch (RetrofitError e) {
			Log.e("MALX", "caught retrofit error: " + e.getResponse().getStatus());
			return false;
		}
	}
	
	public static ListType getListTypeByString(String name) {
		return ListType.valueOf(name.toUpperCase());
	}
	
	public static String getListTypeString(ListType type) {
		return type.name().toLowerCase();
	}

	public ArrayList<Anime> searchAnime(String query) {
		return service.searchAnime(query);
	}
	
	public ArrayList<Manga> searchManga(String query) {
		return service.searchManga(query);
	}

	public AnimeList getAnimeList() {
		return service.getAnimeList(username);
	}
	
	public MangaList getMangaList() {
		return service.getMangaList(username);
	}

	public Anime getAnime(int id) {
		return service.getAnime(id);
	}
	
	public Manga getManga(int id) {
		return service.getManga(id);
	}

	public boolean addOrUpdateAnime(Anime anime) {
		boolean result = false;
		if ( anime.getCreateFlag() )
			result = service.addAnime(anime.getId(), anime.getWatchedStatus(), anime.getWatchedEpisodes(), anime.getScore()).getStatus() == 200;
		else
			result = service.updateAnime(anime.getId(), anime.getWatchedStatus(), anime.getWatchedEpisodes(), anime.getScore()).getStatus() == 200;
		return result;
	}
	
	public boolean addOrUpdateManga(Manga manga) {
		boolean result = false;
		if ( manga.getCreateFlag() )
			result = service.addManga(manga.getId(), manga.getStatus(), manga.getChaptersRead(), manga.getVolumesRead(), manga.getScore()).getStatus() == 200;
		else
			result = service.updateManga(manga.getId(), manga.getStatus(), manga.getChaptersRead(), manga.getVolumesRead(), manga.getScore()).getStatus() == 200;
		return result;
	}
	
	public boolean deleteAnimeFromList(int id) {
		return service.deleteAnime(id).getStatus() == 200;
	}
	
	public boolean deleteMangaFromList(int id) {
		return service.deleteManga(id).getStatus() == 200;
	}
	
	public ArrayList<Anime> getMostPopularAnime(int page) {
		return service.getPopularAnime(page);
	}
	
	public ArrayList<Manga> getMostPopularManga(int page) {
		return service.getPopularManga(page);
	}
	
	public ArrayList<Anime> getTopRatedAnime(int page) {
		return service.getTopRatedAnime(page);
	}
	
	public ArrayList<Manga> getTopRatedManga(int page) {
		return service.getTopRatedManga(page);
	}
	
	public ArrayList<Anime> getJustAddedAnime(int page) {
		return service.getJustAddedAnime(page);
	}
	
	public ArrayList<Manga> getJustAddedManga(int page) {
		return service.getJustAddedManga(page);
	}
	
	public ArrayList<Anime> getUpcomingAnime(int page) {
		return service.getUpcomingAnime(page);
	}
	
	public ArrayList<Manga> getUpcomingManga(int page) {
		return service.getUpcomingManga(page);
	}
	
	public Profile getProfile(String user) {
	    return friends_service.getProfile(user);
	}

    public ArrayList<Friend> getFriends(String user) {
        return friends_service.getFriends(user);
    }
}
