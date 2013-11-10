package net.somethingdreadful.MAL.api;

import java.util.ArrayList;

import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;

import retrofit.RestAdapter;
import retrofit.client.Response;

import android.content.Context;
import android.os.Build;

public class MALApi {
	private static final String API_HOST = "http://api.atarashiiapp.com";
	public final static String USER_AGENT = "Atarashii! (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + " Build/" + Build.DISPLAY + ")";
    
    private MALInterface service;
    private String username;
    
    public enum ListType {
    	ANIME, MANGA
    }

	public MALApi(Context context) {
		PrefManager prefManager = new PrefManager(context);
		username = prefManager.getUser();
		setupRESTService(prefManager.getUser(), prefManager.getPass());
	}
	
	public MALApi(String username, String password) {
		this.username = username;
		setupRESTService(username, password);
	}
	
	private void setupRESTService(String username, String password) {
		RestAdapter restAdapter = new RestAdapter.Builder()
			.setServer(API_HOST)
			.setRequestInterceptor(new MALRequestInterceptor(username, password))
			.build();
		service = restAdapter.create(MALInterface.class);
	}

	public boolean isAuth() {
		Response response = service.verifyAuthentication();
		return response.getStatus() == 200;
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

	public boolean addOrUpdateAnime(boolean hasCreate, String id, Anime data) {
		// TODO
		return false;
	}
	
	public boolean addOrUpdateManga(boolean hasCreate, String id, Manga data) {
		// TODO
		return false;
	}

	public boolean deleteGenreFromList(ListType listType, String genre_id) {
		// TODO
		return false;
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
}
