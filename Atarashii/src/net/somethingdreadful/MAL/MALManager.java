package net.somethingdreadful.MAL;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.ALApi;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Activity;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;
import net.somethingdreadful.MAL.api.response.Profile;
import net.somethingdreadful.MAL.api.response.User;
import net.somethingdreadful.MAL.sql.DatabaseManager;

import java.util.ArrayList;
import java.util.Calendar;

public class MALManager {
    MALApi malApi;
    ALApi alApi;
    DatabaseManager dbMan;
    Context context;

    public MALManager(Context context) {
        if (AccountService.isMAL())
            malApi = new MALApi();
        else
            alApi = new ALApi();
        dbMan = new DatabaseManager(context);
        this.context = context;
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
                return Anime.STATUS_REWATCHING;
            default:
                return type.equals(MALApi.ListType.ANIME) ? Anime.STATUS_WATCHING : Manga.STATUS_READING;
        }
    }

    public Anime getAnime(int id, String username) {
        return dbMan.getAnime(id, username);
    }

    public Manga getManga(int id, String username) {
        return dbMan.getManga(id, username);
    }

    public ArrayList<Anime> downloadAndStoreAnimeList(String username) {
        ArrayList<Anime> result = null;
        AnimeList animeList = AccountService.isMAL() ? malApi.getAnimeList() : alApi.getAnimeList(username);

        if (animeList != null) {
            result = animeList.getAnimes();
            dbMan.saveAnimeList(result, username);
            dbMan.cleanupAnimeTable();
        }
        return result;
    }

    public ArrayList<Manga> downloadAndStoreMangaList(String username) {
        ArrayList<Manga> result = null;
        MangaList mangaList = AccountService.isMAL() ? malApi.getMangaList() : alApi.getMangaList(username);

        if (mangaList != null) {
            result = mangaList.getMangas();
            dbMan.saveMangaList(result, username);
            dbMan.cleanupMangaTable();
        }
        return result;
    }

    public ArrayList<Anime> getAnimeListFromDB(String ListType, String username) {
        return dbMan.getAnimeList(ListType, username);
    }

    public ArrayList<Manga> getMangaListFromDB(String ListType, String username) {
        return dbMan.getMangaList(ListType, username);
    }

    public Manga updateWithDetails(int id, Manga manga, String username) {
        Crashlytics.log(Log.INFO, "MALX", "MALManager.updateWithDetails(" + Integer.toString(id) + ", " + username + ")");
        Manga manga_api = AccountService.isMAL() ? malApi.getManga(id) : alApi.getManga(id).createBaseModel();

        if (manga_api != null) {
            dbMan.saveManga(manga_api, false, username);
            return AccountService.isMAL() ? manga_api : dbMan.getManga(id, AccountService.getUsername());
        }
        return manga;
    }

    public Anime updateWithDetails(int id, Anime anime, String username) {
        Crashlytics.log(Log.INFO, "MALX", "MALManager.updateWithDetails(" + Integer.toString(id) + ", " + username + ")");
        Anime anime_api = AccountService.isMAL() ? malApi.getAnime(id) : alApi.getAnime(id).createBaseModel();

        if (anime_api != null) {
            dbMan.saveAnime(anime_api, false, username);
            return AccountService.isMAL() ? anime_api : dbMan.getAnime(id, AccountService.getUsername());
        }
        return anime;
    }

    public ArrayList<User> downloadAndStoreFriendList(String user) {
        try {
            Crashlytics.log(Log.DEBUG, "MALX", "MALManager.downloadAndStoreFriendList(): Downloading friendlist of " + user);
            ArrayList<User> result = AccountService.isMAL() ? malApi.getFriends(user) : alApi.getFollowers(user);

            if (result != null && result.size() > 0)
                dbMan.saveFriendList(result, user);
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.downloadAndStoreFriendList(): " + e.getMessage());
            Crashlytics.logException(e);
        }
        return dbMan.getFriendList(user);
    }

    public ArrayList<User> getFriendListFromDB(String username) {
        return dbMan.getFriendList(username);
    }

    public User downloadAndStoreProfile(String name) {
        User result = new User();
        try {
            Crashlytics.log(Log.DEBUG, "MALX", "MALManager.downloadAndStoreProfile(): Downloading profile of " + name);
            Profile profile = AccountService.isMAL() ? malApi.getProfile(name) : alApi.getProfile(name);

            result.setName(name);
            result.setProfile(profile);

            if (profile != null && AccountService.isMAL()) {
                dbMan.saveUser(result, true);
            } else if (profile != null && !AccountService.isMAL()) {
                dbMan.saveProfile(profile);
            } else if (profile != null)
                result.getProfile().setAvatarUrl(profile.getImageUrl());
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.downloadAndStoreProfile(): " + e.getMessage());
            Crashlytics.logException(e);
            result = null;
        }
        return result;
    }

    public User getProfileFromDB(String name) {
        return dbMan.getProfile(name);
    }

    public void saveAnimeToDatabase(Anime anime, boolean ignoreSynopsis, String username) {
        dbMan.saveAnime(anime, ignoreSynopsis, username);
    }

    public void saveMangaToDatabase(Manga manga, boolean ignoreSynopsis, String username) {
        dbMan.saveManga(manga, ignoreSynopsis, username);
    }

    public boolean deleteAnimeFromAnimelist(Anime anime, String username) {
        return dbMan.deleteAnimeFromAnimelist(anime.getId(), username);
    }

    public boolean deleteMangaFromMangalist(Manga manga, String username) {
        return dbMan.deleteMangaFromMangalist(manga.getId(), username);
    }

    public boolean cleanDirtyAnimeRecords(String username) {
        boolean totalSuccess = true;

        ArrayList<Anime> dirtyAnimes = dbMan.getDirtyAnimeList(username);

        if (dirtyAnimes != null) {
            Crashlytics.log(Log.VERBOSE, "MALX", "MALManager.cleanDirtyAnimeRecords(): Got " + dirtyAnimes.size() + " dirty anime records. Cleaning..");

            for (Anime anime : dirtyAnimes) {
                totalSuccess = writeAnimeDetails(anime);
                if (totalSuccess) {
                    anime.clearDirty();
                    saveAnimeToDatabase(anime, false, username);
                }

                if (!totalSuccess)
                    break;
            }
            Crashlytics.log(Log.VERBOSE, "MALX", "MALManager.cleanDirtyAnimeRecords(): Cleaned dirty anime records, status: " + totalSuccess);
        }
        return totalSuccess;
    }

    public boolean cleanDirtyMangaRecords(String username) {
        boolean totalSuccess = true;

        ArrayList<Manga> dirtyMangas = dbMan.getDirtyMangaList(username);

        if (dirtyMangas != null) {
            Crashlytics.log(Log.VERBOSE, "MALX", "MALManager.cleanDirtyMangaRecords(): Got " + dirtyMangas.size() + " dirty manga records. Cleaning..");

            for (Manga manga : dirtyMangas) {
                totalSuccess = writeMangaDetails(manga);
                if (totalSuccess) {
                    manga.clearDirty();
                    saveMangaToDatabase(manga, false, username);
                }

                if (!totalSuccess)
                    break;
            }
            Crashlytics.log(Log.VERBOSE, "MALX", "MALManager.cleanDirtyMangaRecords(): Cleaned dirty manga records, status: " + totalSuccess);
        }
        return totalSuccess;
    }

    public ArrayList<Activity> getActivityFromDB(String username) {
        return dbMan.getActivity(username);
    }

    public ArrayList<Activity> downloadAndStoreActivity(String username) {
        ArrayList<Activity> result = alApi.getActivity(username);
        if (result != null && result.size() > 0) {
            dbMan.saveActivity(result, username);
        }
        return result;
    }

    /**
     * Api Requests
     *
     * All the methods below this block is used to determine and make request to the API.
     */

    public ForumMain search(String query) {
        return malApi.search(query);
    }

    public Anime getAnimeRecord(int id) {
        return AccountService.isMAL() ? malApi.getAnime(id) : alApi.getAnime(id);
    }

    public Manga getMangaRecord(int id) {
        return AccountService.isMAL() ? malApi.getManga(id) : alApi.getManga(id);
    }

    public ForumMain getForum() {
        return malApi.getForum();
    }

    public ForumMain getTopics(int id, int page) {
        return malApi.getTopics(id, page);
    }

    public ForumMain getDiscussion(int id, int page, MALApi.ListType type) {
        return type.equals(MALApi.ListType.ANIME) ? malApi.getAnime(id, page) : malApi.getManga(id, page);
    }

    public ForumMain getPosts(int id, int page) {
        return malApi.getPosts(id, page);
    }

    public ForumMain getSubBoards(int id, int page) {
        return malApi.getSubBoards(id, page);
    }

    public Boolean addComment(int id, String message) {
        return malApi.addComment(id, message);
    }

    public Boolean updateComment(int id, String message) {
        return malApi.updateComment(id, message);
    }

    public Boolean addTopic(int id, String title, String message) {
        return malApi.addTopic(id, title, message);
    }

    public void verifyAuthentication() {
        if (AccountService.isMAL())
            malApi.verifyAuthentication();
        else if (AccountService.getAccesToken() == null)
            alApi.getAccesToken();
    }

    public boolean writeAnimeDetails(Anime anime) {
        boolean result;
        if (anime.getDeleteFlag())
            result = AccountService.isMAL() ? malApi.deleteAnimeFromList(anime.getId()) : alApi.deleteAnimeFromList(anime.getId());
        else
            result = AccountService.isMAL() ? malApi.addOrUpdateAnime(anime) : alApi.addOrUpdateAnime(anime);
        return result;
    }

    public boolean writeMangaDetails(Manga manga) {
        boolean result;
        if (manga.getDeleteFlag())
            result = AccountService.isMAL() ? malApi.deleteMangaFromList(manga.getId()) : alApi.deleteMangaFromList(manga.getId());
        else
            result = AccountService.isMAL() ? malApi.addOrUpdateManga(manga) : alApi.addOrUpdateManga(manga);

        return result;
    }

    public ArrayList<Anime> getMostPopularAnime(int page) {
        return AccountService.isMAL() ? malApi.getMostPopularAnime(page) : alApi.getAiringAnime(page);
    }

    public ArrayList<Manga> getMostPopularManga(int page) {
        return AccountService.isMAL() ? malApi.getMostPopularManga(page) : alApi.getAiringManga(page);
    }

    public ArrayList<Anime> getTopRatedAnime(int page) {
        return AccountService.isMAL() ? malApi.getTopRatedAnime(page) : alApi.getYearAnime(Calendar.getInstance().get(Calendar.YEAR), page);
    }

    public ArrayList<Manga> getTopRatedManga(int page) {
        return AccountService.isMAL() ? malApi.getTopRatedManga(page) : alApi.getYearManga(Calendar.getInstance().get(Calendar.YEAR), page);
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
}
