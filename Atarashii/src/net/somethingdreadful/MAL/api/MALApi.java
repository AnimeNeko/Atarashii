package net.somethingdreadful.MAL.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;
import net.somethingdreadful.MAL.api.response.Profile;
import net.somethingdreadful.MAL.api.response.User;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;

import java.util.ArrayList;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;
import retrofit.client.Response;

public class MALApi {
    // Use version 1.0 of the API interface
    private static final String API_HOST = "https://api.atarashiiapp.com/1";
    private static final String USER_AGENT = "Atarashii! (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + " Build/" + Build.DISPLAY + ")";

    private MALInterface service;
    private String username;

    public MALApi(Context context) {
        PrefManager prefManager = new PrefManager(context);
        username = prefManager.getUser();
        setupRESTService(prefManager.getUser(), prefManager.getPass());
    }

    public MALApi(String username, String password) {
        this.username = username;
        setupRESTService(username, password);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static ListType getListTypeByString(String name) {
        return ListType.valueOf(name.toUpperCase());
    }

    public static String getListTypeString(ListType type) {
        return type.name().toLowerCase();
    }

    private void setupRESTService(String username, String password) {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpProtocolParams.setUserAgent(client.getParams(), USER_AGENT);
        client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(username, password));

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new ApacheClient(client))
                .setServer(API_HOST)
                .build();
        service = restAdapter.create(MALInterface.class);
    }

    public boolean isAuth() {
        try {
            Response response = service.verifyAuthentication();
            return response.getStatus() == 200;
        } catch (RetrofitError e) {
            if (e.getResponse() != null)
                Log.e("MALX", "caught retrofit error: " + e.getResponse().getStatus());
            else
                Log.e("MALX", "caught retrofit error: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<Anime> searchAnime(String query, int page) {
        return service.searchAnime(query, page);
    }

    public ArrayList<Manga> searchManga(String query, int page) {
        return service.searchManga(query, page);
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
        if (anime.getCreateFlag())
            result = service.addAnime(anime.getId(), anime.getWatchedStatus(), anime.getWatchedEpisodes(), anime.getScore()).getStatus() == 200;
        else
            result = service.updateAnime(anime.getId(), anime.getWatchedStatus(), anime.getWatchedEpisodes(), anime.getScore()).getStatus() == 200;
        return result;
    }

    public boolean addOrUpdateManga(Manga manga) {
        boolean result = false;
        if (manga.getCreateFlag())
            result = service.addManga(manga.getId(), manga.getReadStatus(), manga.getChaptersRead(), manga.getVolumesRead(), manga.getScore()).getStatus() == 200;
        else
            result = service.updateManga(manga.getId(), manga.getReadStatus(), manga.getChaptersRead(), manga.getVolumesRead(), manga.getScore()).getStatus() == 200;
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
        return service.getProfile(user);
    }

    public ArrayList<User> getFriends(String user) {
        return service.getFriends(user);
    }

    public enum ListType {
        ANIME,
        MANGA
    }
}
