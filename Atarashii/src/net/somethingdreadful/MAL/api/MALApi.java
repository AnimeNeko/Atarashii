package net.somethingdreadful.MAL.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;
import net.somethingdreadful.MAL.api.response.Profile;
import net.somethingdreadful.MAL.api.response.User;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class MALApi {
    // Use version 1.0 of the API interface
    private static final String API_HOST = "https://api.atarashiiapp.com/2";
    private static final String USER_AGENT = "Atarashii! (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + " Build/" + Build.DISPLAY + ")";

    private MALInterface service;
    private String username;


    public MALApi() {
        username = AccountService.getUsername();
        setupRESTService(username, AccountService.getPassword());
    }

    public MALApi(String username, String password) {
        this.username = username;
        setupRESTService(username, password);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static String getListTypeString(ListType type) {
        return type.name().toLowerCase();
    }

    private void setupRESTService(String username, String password) {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpProtocolParams.setUserAgent(client.getParams(), USER_AGENT);
        client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(username, password));

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new ApacheClient(client))
                .setEndpoint(API_HOST)
                .setConverter(new GsonConverter(gson))
                .build();
        service = restAdapter.create(MALInterface.class);
    }

    public boolean isAuth() {
        try {
            Response response = verifyAuthentication();
            return response.getStatus() == 200;
        } catch (RetrofitError e) {
            if (e.getResponse() != null)
                Crashlytics.log(Log.ERROR, "MALX", "MALApi.getListTypeString: " + e.getResponse().getStatus());
            else
                Crashlytics.log(Log.ERROR, "MALX", "MALApi.getListTypeString: " + e.getMessage());
            return false;
        }
    }

    public Response verifyAuthentication() {
        return service.verifyAuthentication();
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
        boolean result;
        if (anime.getCreateFlag())
            result = service.addAnime(anime.getId(), anime.getWatchedStatus(), anime.getWatchedEpisodes(), anime.getScore()).getStatus() == 200;
        else {
            if (anime.isDirty()) {
                // map anime property names to api field names
                HashMap<String, String> nameMap = new HashMap<>();
                nameMap.put("watchedStatus", "status");
                nameMap.put("watchedEpisodes", "episodes");
                nameMap.put("score", "score");
                nameMap.put("watchingStart", "start");
                nameMap.put("watchingEnd", "end");
                HashMap<String, String> fieldMap = new HashMap<>();
                for (String dirtyField : anime.getDirty()) {
                    if (nameMap.containsKey(dirtyField)) {
                        if (anime.getPropertyType(dirtyField) == String.class) {
                            fieldMap.put(nameMap.get(dirtyField), anime.getStringPropertyValue(dirtyField));
                        } else if (anime.getPropertyType(dirtyField) == int.class) {
                            fieldMap.put(nameMap.get(dirtyField), anime.getIntegerPropertyValue(dirtyField).toString());
                        }
                    }
                }
                result = service.updateAnime(anime.getId(), fieldMap).getStatus() == 200;
            } else {
                result = false;
            }
        }
        return result;
    }

    public boolean addOrUpdateManga(Manga manga) {
        boolean result;
        if (manga.getCreateFlag())
            result = service.addManga(manga.getId(), manga.getReadStatus(), manga.getChaptersRead(), manga.getVolumesRead(), manga.getScore()).getStatus() == 200;
        else {
            if (manga.isDirty()) {
                // map manga property names to api field names
                HashMap<String, String> nameMap = new HashMap<>();
                nameMap.put("readStatus", "status");
                nameMap.put("chaptersRead", "chapters");
                nameMap.put("volumesRead", "volumes");
                nameMap.put("score", "score");
                nameMap.put("readingStart", "start");
                nameMap.put("readingEnd", "end");
                HashMap<String, String> fieldMap = new HashMap<>();
                for (String dirtyField : manga.getDirty()) {
                    if (nameMap.containsKey(dirtyField)) {
                        if (manga.getPropertyType(dirtyField) == String.class) {
                            fieldMap.put(nameMap.get(dirtyField), manga.getStringPropertyValue(dirtyField));
                        } else if (manga.getPropertyType(dirtyField) == int.class) {
                            fieldMap.put(nameMap.get(dirtyField), manga.getIntegerPropertyValue(dirtyField).toString());
                        }
                    }
                }
                result = service.updateManga(manga.getId(), fieldMap).getStatus() == 200;
            } else {
                result = false;
            }
        }
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

    public ForumMain getForum() {
        return service.getForum();
    }

    public ForumMain getTopics(int id, int page) {
        return service.getTopics(id, page);
    }

    public ForumMain getAnime(int id, int page) {
        return service.getAnime(id, page);
    }

    public ForumMain getManga(int id, int page) {
        return service.getManga(id, page);
    }

    public ForumMain getPosts(int id, int page) {
        return service.getPosts(id, page);
    }

    public ForumMain getSubBoards(int id, int page) {
        return service.getSubBoards(id, page);
    }

    public boolean addComment(int id, String message) {
        return service.addComment(id, message).getStatus() == 200;
    }

    public boolean updateComment(int id, String message) {
        return service.updateComment(id, message).getStatus() == 200;
    }

    public boolean addTopic(int id, String title, String message) {
        return service.addTopic(id, title, message).getStatus() == 200;
    }

    public ForumMain search(String query) {
        return service.search(query);
    }

    public enum ListType {
        ANIME,
        MANGA
    }
}
