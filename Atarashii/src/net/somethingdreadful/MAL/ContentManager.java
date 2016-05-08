package net.somethingdreadful.MAL;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.ALApi;
import net.somethingdreadful.MAL.api.ALModels.ForumAL;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList;
import net.somethingdreadful.MAL.api.BaseModels.Forum;
import net.somethingdreadful.MAL.api.BaseModels.History;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.database.DatabaseManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ContentManager {
    private MALApi malApi;
    private ALApi alApi;
    private final DatabaseManager dbMan;

    public ContentManager(Context context) {
        if (AccountService.isMAL())
            malApi = new MALApi();
        else
            alApi = new ALApi();
        dbMan = new DatabaseManager(context);
    }

    public ContentManager(Activity activity) {
        if (AccountService.isMAL())
            malApi = new MALApi(activity);
        else
            alApi = new ALApi(activity);
        dbMan = new DatabaseManager(activity);
    }

    public static String listSortFromInt(int i, MALApi.ListType type) {
        switch (i) {
            case 0:
                return "";
            case 1:
                return type.equals(MALApi.ListType.ANIME) ? Anime.STATUS_WATCHING : Manga.STATUS_READING;
            case 2:
                return Anime.STATUS_COMPLETED;
            case 3:
                return Anime.STATUS_ONHOLD;
            case 4:
                return Anime.STATUS_DROPPED;
            case 5:
                return type.equals(MALApi.ListType.ANIME) ? Anime.STATUS_PLANTOWATCH : Manga.STATUS_PLANTOREAD;
            case 6:
                return type.equals(MALApi.ListType.ANIME) ? Anime.STATUS_REWATCHING : Manga.STATUS_REREADING;
            default:
                return type.equals(MALApi.ListType.ANIME) ? Anime.STATUS_WATCHING : Manga.STATUS_READING;
        }
    }

    public Anime getAnime(int id) {
        Crashlytics.log(Log.INFO, "Atarashii", "ContentManager.getAnime() : Loading " + id);
        return dbMan.getAnime(id);
    }

    public Manga getManga(int id) {
        Crashlytics.log(Log.INFO, "Atarashii", "ContentManager.getManga() : Loading " + id);
        return dbMan.getManga(id);
    }

    public void downloadAnimeList(String username) {
        Crashlytics.log(Log.INFO, "Atarashii", "ContentManager.downloadAnimeList() : Downloading " + username);
        UserList animeList = AccountService.isMAL() ? malApi.getAnimeList() : alApi.getAnimeList(username);

        if (animeList != null) {
            dbMan.saveAnimeList(animeList.getAnimeList());
            dbMan.cleanupAnimeTable();
        }
    }

    public void downloadMangaList(String username) {
        Crashlytics.log(Log.INFO, "Atarashii", "ContentManager.downloadMangaList() : Downloading " + username);
        UserList mangaList = AccountService.isMAL() ? malApi.getMangaList() : alApi.getMangaList(username);

        if (mangaList != null) {
            dbMan.saveMangaList(mangaList.getMangaList());
            dbMan.cleanupMangaTable();
        }
    }

    public ArrayList<Anime> getAnimeListFromDB(String ListType, int sortType, String inverse) {
        return dbMan.getAnimeList(ListType, sortType, inverse.equals("false") ? 1 : 2);
    }

    public ArrayList<Manga> getMangaListFromDB(String ListType, int sortType, String inverse) {
        return dbMan.getMangaList(ListType, sortType, inverse.equals("false") ? 1 : 2);
    }

    public Manga updateWithDetails(int id, Manga manga) {
        Crashlytics.log(Log.INFO, "Atarashii", "ContentManager.updateWithDetails() : Downloading manga " + id);
        Manga manga_api = AccountService.isMAL() ? malApi.getManga(id, 1) : alApi.getManga(id);

        if (manga_api != null) {
            dbMan.saveManga(manga_api);
            return AccountService.isMAL() ? manga_api : dbMan.getManga(id);
        }
        return manga;
    }

    public Anime updateWithDetails(int id, Anime anime) {
        Crashlytics.log(Log.INFO, "Atarashii", "ContentManager.updateWithDetails() : Downloading anime " + id);
        Anime anime_api = AccountService.isMAL() ? malApi.getAnime(id, 1) : alApi.getAnime(id);

        if (anime_api != null) {
            dbMan.saveAnime(anime_api);
            return AccountService.isMAL() ? anime_api : dbMan.getAnime(id);
        }
        return anime;
    }

    public ArrayList<Profile> downloadAndStoreFriendList(String user) {
        ArrayList<Profile> result = new ArrayList<>();
        try {
            Crashlytics.log(Log.DEBUG, "Atarashii", "ContentManager.downloadAndStoreFriendList(): Downloading friendlist of " + user);
            result = AccountService.isMAL() ? malApi.getFriends(user) : alApi.getFollowing(user);

            if (result != null && result.size() > 0 && AccountService.getUsername().equals(user))
                dbMan.saveFriendList(result);
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "Atarashii", "ContentManager.downloadAndStoreFriendList(): " + e.getMessage());
            Crashlytics.logException(e);
        }

        return sortFriendlist(result);
    }

    public ArrayList<Profile> getFollowers(String user) {
        ArrayList<Profile> result = new ArrayList<>();
        try {
            Crashlytics.log(Log.DEBUG, "Atarashii", "ContentManager.getFollowers(): Downloading getFollowers of " + user);
            result = alApi.getFollowers(user);
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "Atarashii", "ContentManager.getFollowers(): " + e.getMessage());
            Crashlytics.logException(e);
        }

        return sortFriendlist(result);
    }

    private ArrayList<Profile> sortFriendlist(ArrayList<Profile> result) {
        //sort friendlist
        Collections.sort(result != null ? result : new ArrayList<Profile>(), new Comparator<Profile>() {
            @Override
            public int compare(Profile profile1, Profile profile2) {
                return profile1.getUsername().toLowerCase().compareTo(profile2.getUsername().toLowerCase());
            }
        });
        return result;
    }

    public ArrayList<Profile> getFriendListFromDB() {
        return dbMan.getFriendList();
    }

    public Profile getProfile(String name) {
        Profile profile = new Profile();
        try {
            Crashlytics.log(Log.DEBUG, "Atarashii", "ContentManager.getProfile(): Downloading profile of " + name);
            profile = AccountService.isMAL() ? malApi.getProfile(name) : alApi.getProfile(name);

            if (profile != null) {
                profile.setUsername(name);
                if (name.equalsIgnoreCase(AccountService.getUsername()))
                    dbMan.saveProfile(profile);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "Atarashii", "ContentManager.getProfile(): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return profile;
    }

    public Profile getProfileFromDB() {
        return dbMan.getProfile();
    }

    public void saveAnimeToDatabase(Anime anime) {
        dbMan.saveAnime(anime);
    }

    public void saveMangaToDatabase(Manga manga) {
        dbMan.saveManga(manga);
    }

    public void cleanDirtyAnimeRecords() {
        ArrayList<Anime> dirtyAnimes = dbMan.getDirtyAnimeList();

        if (dirtyAnimes != null) {
            Crashlytics.log(Log.VERBOSE, "Atarashii", "ContentManager.cleanDirtyAnimeRecords(): Got " + dirtyAnimes.size() + " dirty anime records. Cleaning..");

            for (Anime anime : dirtyAnimes) {
                if (writeAnimeDetails(anime)) {
                    anime.clearDirty();
                    saveAnimeToDatabase(anime);
                } else if (anime != null) {
                    Crashlytics.log(Log.ERROR, "Atarashii", "ContentManager.cleanDirtyAnimeRecords(): Failed to update " + anime.getId() + ".");
                }
            }
            Crashlytics.log(Log.VERBOSE, "Atarashii", "ContentManager.cleanDirtyAnimeRecords(): Cleaned dirty anime records.");
        }
    }

    public void cleanDirtyMangaRecords() {
        ArrayList<Manga> dirtyMangas = dbMan.getDirtyMangaList();

        if (dirtyMangas != null) {
            Crashlytics.log(Log.VERBOSE, "Atarashii", "ContentManager.cleanDirtyMangaRecords(): Got " + dirtyMangas.size() + " dirty manga records. Cleaning..");

            for (Manga manga : dirtyMangas) {
                if (writeMangaDetails(manga)) {
                    manga.clearDirty();
                    saveMangaToDatabase(manga);
                } else if (manga != null) {
                    Crashlytics.log(Log.ERROR, "Atarashii", "ContentManager.cleanDirtyMangaRecords(): Failed to update " + manga.getId() + ".");
                }
            }
            Crashlytics.log(Log.VERBOSE, "Atarashii", "ContentManager.cleanDirtyMangaRecords(): Cleaned dirty manga records.");
        }
    }

    public ArrayList<History> getActivity(String username, int page) {
        ArrayList<History> result = AccountService.isMAL() ? malApi.getActivity(username) : alApi.getActivity(username, page);
        Crashlytics.log(Log.INFO, "Atarashii", "ContentManager.getActivity(): got " + String.valueOf(result != null ? result.size() : 0) + " records on " + page);
        return result;
    }

    /**
     * Api Requests
     * <p/>
     * All the methods below this block is used to determine and make request to the API.
     */

    public Anime getAnimeRecord(int id) {
        Crashlytics.log(Log.DEBUG, "Atarashii", "ContentManager.getAnimeRecord(): Downloading " + id);
        return AccountService.isMAL() ? malApi.getAnime(id, 0) : alApi.getAnime(id);
    }

    public Manga getMangaRecord(int id) {
        Crashlytics.log(Log.DEBUG, "Atarashii", "ContentManager.getMangaRecord(): Downloading " + id);
        return AccountService.isMAL() ? malApi.getManga(id, 0) : alApi.getManga(id);
    }

    public void verifyAuthentication() {
        if (AccountService.isMAL())
            malApi.isAuth();
        else if (AccountService.getAccesToken() == null)
            alApi.getAccesToken();
    }

    public boolean writeAnimeDetails(Anime anime) {
        Crashlytics.log(Log.DEBUG, "Atarashii", "ContentManager.writeAnimeDetails(): Updating " + anime.getId());
        boolean result;
        if (anime.getDeleteFlag())
            result = AccountService.isMAL() ? malApi.deleteAnimeFromList(anime.getId()) : alApi.deleteAnimeFromList(anime.getId());
        else
            result = AccountService.isMAL() ? malApi.addOrUpdateAnime(anime) : alApi.addOrUpdateAnime(anime);
        Crashlytics.log(result ? Log.INFO : Log.ERROR, "Atarashii", "ContentManager.writeAnimeDetails(): successfully updated: " + result);
        return result;
    }

    public boolean writeMangaDetails(Manga manga) {
        Crashlytics.log(Log.DEBUG, "Atarashii", "ContentManager.writeMangaDetails(): Updating " + manga.getId());
        boolean result;
        if (manga.getDeleteFlag())
            result = AccountService.isMAL() ? malApi.deleteMangaFromList(manga.getId()) : alApi.deleteMangaFromList(manga.getId());
        else
            result = AccountService.isMAL() ? malApi.addOrUpdateManga(manga) : alApi.addOrUpdateManga(manga);
        Crashlytics.log(result ? Log.INFO : Log.ERROR, "Atarashii", "ContentManager.writeMangaDetails(): successfully updated: " + result);
        return result;
    }

    public ArrayList<Anime> getMostPopularAnime(int page) {
        return AccountService.isMAL() ? malApi.getMostPopularAnime(page) : alApi.getMostPopularAnime(page);
    }

    public ArrayList<Manga> getMostPopularManga(int page) {
        return AccountService.isMAL() ? malApi.getMostPopularManga(page) : alApi.getMostPopularManga(page);
    }

    public ArrayList<Anime> getTopRatedAnime(int page) {
        return AccountService.isMAL() ? malApi.getTopRatedAnime(page) : alApi.getTopRatedAnime(page);
    }

    public ArrayList<Manga> getTopRatedManga(int page) {
        return AccountService.isMAL() ? malApi.getTopRatedManga(page) : alApi.getTopRatedManga(page);
    }

    public ArrayList<Anime> getJustAddedAnime(int page) {
        return AccountService.isMAL() ? malApi.getJustAddedAnime(page) : alApi.getJustAddedAnime(page);
    }

    public ArrayList<Manga> getJustAddedManga(int page) {
        return AccountService.isMAL() ? malApi.getJustAddedManga(page) : alApi.getJustAddedManga(page);
    }

    public ArrayList<Anime> getUpcomingAnime(int page) {
        return AccountService.isMAL() ? malApi.getUpcomingAnime(page) : alApi.getUpcomingAnime(page);
    }

    public ArrayList<Manga> getUpcomingManga(int page) {
        return AccountService.isMAL() ? malApi.getUpcomingManga(page) : alApi.getUpcomingManga(page);
    }

    public ArrayList<Anime> searchAnime(String query, int page) {
        return AccountService.isMAL() ? malApi.searchAnime(query, page) : alApi.searchAnime(query, page);
    }

    public ArrayList<Manga> searchManga(String query, int page) {
        return AccountService.isMAL() ? malApi.searchManga(query, page) : alApi.searchManga(query, page);
    }

    public ArrayList<Reviews> getAnimeReviews(int id, int page) {
        return AccountService.isMAL() ? malApi.getAnimeReviews(id, page) : alApi.getAnimeReviews(id, page);
    }

    public ArrayList<Reviews> getMangaReviews(int id, int page) {
        return AccountService.isMAL() ? malApi.getMangaReviews(id, page) : alApi.getMangaReviews(id, page);
    }

    public ArrayList<Forum> getForumCategories() {
        return AccountService.isMAL() ? malApi.getForum().createBaseModel() : ForumAL.getForum();
    }

    public ArrayList<Forum> getCategoryTopics(int id, int page) {
        return AccountService.isMAL() ? malApi.getCategoryTopics(id, page).createBaseModel() : alApi.getTags(id, page).getForumListBase();
    }

    public ArrayList<Forum> getTopic(int id, int page) {
        return AccountService.isMAL() ? malApi.getPosts(id, page).createBaseModel() : alApi.getPosts(id, page).convertBaseModel();
    }

    public boolean deleteAnime(Anime anime) {
        return dbMan.deleteAnime(anime.getId());
    }

    public boolean deleteManga(Manga manga) {
        return dbMan.deleteManga(manga.getId());
    }

    public ArrayList<Forum> search(String query) {
        return AccountService.isMAL() ? malApi.search(query).createBaseModel() : alApi.search(query).getForumListBase();
    }

    public ArrayList<Forum> getSubCategory(int id, int page) {
        return malApi.getSubBoards(id, page).createBaseModel();
    }

    public boolean addComment(int id, String message) {
        return AccountService.isMAL() ? malApi.addComment(id, message) : alApi.addComment(id, message);
    }

    public boolean updateComment(int id, String message) {
        return AccountService.isMAL() ? malApi.updateComment(id, message) : alApi.updateComment(id, message);
    }
}
