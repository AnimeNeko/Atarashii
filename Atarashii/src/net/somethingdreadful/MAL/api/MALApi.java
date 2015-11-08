package net.somethingdreadful.MAL.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.BrowseList;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALModels.AnimeManga.AnimeList;
import net.somethingdreadful.MAL.api.MALModels.AnimeManga.MangaList;
import net.somethingdreadful.MAL.api.MALModels.ForumMain;
import net.somethingdreadful.MAL.api.MALModels.Friend;
import net.somethingdreadful.MAL.api.MALModels.History;

import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class MALApi {
    // Use version 2.0 of the API interface
    private static final String API_HOST = "https://api.atarashiiapp.com/2";

    //It's not best practice to use internals, but there is no other good way to get the OkHttp default UA
    private static final String okUa = com.squareup.okhttp.internal.Version.userAgent();
    private static final String USER_AGENT = "Atarashii! (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + " Build/" + Build.DISPLAY + ") " + okUa;

    private MALInterface service;
    private String username;

    public MALApi() {
        username = AccountService.getUsername();
        setupRESTService(username, AccountService.getPassword());
    }

    /*
     * Only use for verifying.
     */
    public MALApi(String username, String password) {
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
        OkHttpClient client = new OkHttpClient();

        client.interceptors().add(new UserAgentInterceptor(USER_AGENT));

        final String credential = Credentials.basic(username, password);

        client.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, com.squareup.okhttp.Response response) throws IOException {
                if (credential.equals(response.request().header("Authorization"))) {
                    return null; //If we already failed when trying the credentials, exit and don't retry
                }

                return response.request().newBuilder()
                        .header("Authorization", credential)
                        .build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, com.squareup.okhttp.Response response) throws IOException {
                return null;
            }
        });

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .setVersion(1)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(client))
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
                Crashlytics.log(Log.ERROR, "MALX", "MALApi.isAuth: " + e.getResponse().getStatus());
            else
                Crashlytics.log(Log.ERROR, "MALX", "MALApi.isAuth: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Response verifyAuthentication() {
        return service.verifyAuthentication();
    }

    public ArrayList<Anime> searchAnime(String query, int page) {
        return AnimeList.convertBaseArray(service.searchAnime(query, page));
    }

    public ArrayList<Manga> searchManga(String query, int page) {
        return MangaList.convertBaseArray(service.searchManga(query, page));
    }

    public UserList getAnimeList() {
        return AnimeList.createBaseModel(service.getAnimeList(username));
    }

    public UserList getMangaList() {
        return MangaList.createBaseModel(service.getMangaList(username));
    }

    public Anime getAnime(int id) {
        return service.getAnime(id).createBaseModel();
    }

    public Manga getManga(int id) {
        return service.getManga(id).createBaseModel();
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
                nameMap.put("priority", "priority");
                nameMap.put("personalTags", "tags");
                nameMap.put("personalComments", "comments");
                nameMap.put("fansubGroup", "fansubber");
                nameMap.put("storage", "storage_type");
                nameMap.put("storageValue", "storage_amt");
                nameMap.put("epsDownloaded", "downloaded_eps");
                nameMap.put("rewatchCount", "rewatch_count");
                nameMap.put("rewatchValue", "rewatch_value");
                HashMap<String, String> fieldMap = new HashMap<>();
                for (String dirtyField : anime.getDirty()) {
                    if (nameMap.containsKey(dirtyField)) {
                        if (anime.getPropertyType(dirtyField) == String.class) {
                            fieldMap.put(nameMap.get(dirtyField), anime.getStringPropertyValue(dirtyField));
                        } else if (anime.getPropertyType(dirtyField) == int.class) {
                            fieldMap.put(nameMap.get(dirtyField), anime.getIntegerPropertyValue(dirtyField).toString());
                        } else if (anime.getPropertyType(dirtyField) == ArrayList.class) {
                            fieldMap.put(nameMap.get(dirtyField), anime.getArrayPropertyValue(dirtyField));
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
                nameMap.put("priority", "priority");
                nameMap.put("personalTags", "tags");
                nameMap.put("rereadValue", "reread_value");
                nameMap.put("rereadCount", "reread_count");
                nameMap.put("personalComments", "comments");
                HashMap<String, String> fieldMap = new HashMap<>();
                for (String dirtyField : manga.getDirty()) {
                    if (nameMap.containsKey(dirtyField)) {
                        if (manga.getPropertyType(dirtyField) == String.class) {
                            fieldMap.put(nameMap.get(dirtyField), manga.getStringPropertyValue(dirtyField));
                        } else if (manga.getPropertyType(dirtyField) == int.class) {
                            fieldMap.put(nameMap.get(dirtyField), manga.getIntegerPropertyValue(dirtyField).toString());
                        } else if (manga.getPropertyType(dirtyField) == ArrayList.class) {
                            fieldMap.put(nameMap.get(dirtyField), manga.getArrayPropertyValue(dirtyField));
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

    public BrowseList getMostPopularAnime(int page) {
        return AnimeList.convertBaseBrowseList(service.getPopularAnime(page));
    }

    public BrowseList getMostPopularManga(int page) {
        return MangaList.convertBaseBrowseList(service.getPopularManga(page));
    }

    public BrowseList getTopRatedAnime(int page) {
        return AnimeList.convertBaseBrowseList(service.getTopRatedAnime(page));
    }

    public BrowseList getTopRatedManga(int page) {
        return MangaList.convertBaseBrowseList(service.getTopRatedManga(page));
    }

    public BrowseList getJustAddedAnime(int page) {
        return AnimeList.convertBaseBrowseList(service.getJustAddedAnime(page));
    }

    public BrowseList getJustAddedManga(int page) {
        return MangaList.convertBaseBrowseList(service.getJustAddedManga(page));
    }

    public BrowseList getUpcomingAnime(int page) {
        return AnimeList.convertBaseBrowseList(service.getUpcomingAnime(page));
    }

    public BrowseList getUpcomingManga(int page) {
        return MangaList.convertBaseBrowseList(service.getUpcomingManga(page));
    }

    public Profile getProfile(String user) {
        return service.getProfile(user).createBaseModel();
    }

    public ArrayList<Profile> getFriends(String user) {
        return Friend.convertBaseFriendList(service.getFriends(user));
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

    public ArrayList<Reviews> getAnimeReviews(int id, int page) {
        return net.somethingdreadful.MAL.api.MALModels.AnimeManga.Reviews.convertBaseArray(service.getAnimeReviews(id, page));
    }

    public ArrayList<Reviews> getMangaReviews(int id, int page) {
        return net.somethingdreadful.MAL.api.MALModels.AnimeManga.Reviews.convertBaseArray(service.getMangaReviews(id, page));
    }

    public ArrayList<net.somethingdreadful.MAL.api.BaseModels.History> getActivity(String username) {
        return History.convertBaseHistoryList(service.getActivity(username));
    }

    public enum ListType {
        ANIME,
        MANGA
    }
}
