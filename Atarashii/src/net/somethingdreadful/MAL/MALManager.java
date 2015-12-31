package net.somethingdreadful.MAL;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.ALApi;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.BrowseList;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.UserList;
import net.somethingdreadful.MAL.api.BaseModels.History;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALModels.ForumMain;
import net.somethingdreadful.MAL.database.DatabaseManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

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
                return type.equals(MALApi.ListType.ANIME) ? Anime.STATUS_REWATCHING : Manga.STATUS_REREADING;
            default:
                return type.equals(MALApi.ListType.ANIME) ? Anime.STATUS_WATCHING : Manga.STATUS_READING;
        }
    }

    public Anime getAnime(int id) {
        Crashlytics.log(Log.INFO, "MALX", "MALManager.getAnime() : Downloading " + id);
        return dbMan.getAnime(id);
    }

    public Manga getManga(int id) {
        Crashlytics.log(Log.INFO, "MALX", "MALManager.getManga() : Downloading " + id);
        return dbMan.getManga(id);
    }

    public ArrayList<Anime> downloadAndStoreAnimeList(String username) {
        Crashlytics.log(Log.INFO, "MALX", "MALManager.downloadAndStoreAnimeList() : Downloading " + username);
        ArrayList<Anime> result = null;
        UserList animeList = AccountService.isMAL() ? malApi.getAnimeList() : alApi.getAnimeList(username);

        if (animeList != null) {
            result = animeList.getAnimeList();
            dbMan.saveAnimeList(result);
            dbMan.cleanupAnimeTable();
        }
        return result;
    }

    public ArrayList<Manga> downloadAndStoreMangaList(String username) {
        Crashlytics.log(Log.INFO, "MALX", "MALManager.downloadAndStoreMangaList() : Downloading " + username);
        ArrayList<Manga> result = null;
        UserList mangaList = AccountService.isMAL() ? malApi.getMangaList() : alApi.getMangaList(username);

        if (mangaList != null) {
            result = mangaList.getMangaList();
            dbMan.saveMangaList(result);
            dbMan.cleanupMangaTable();
        }
        return result;
    }

    public ArrayList<Anime> getAnimeListFromDB(String ListType) {
        return dbMan.getAnimeList(ListType);
    }

    public ArrayList<Manga> getMangaListFromDB(String ListType) {
        return dbMan.getMangaList(ListType);
    }

    public Manga updateWithDetails(int id, Manga manga) {
        Crashlytics.log(Log.INFO, "MALX", "MALManager.updateWithDetails() : Downloading manga " + id);
        Manga manga_api = AccountService.isMAL() ? malApi.getManga(id) : alApi.getManga(id);

        if (manga_api != null) {
            dbMan.saveManga(manga_api);
            return AccountService.isMAL() ? manga_api : dbMan.getManga(id);
        }
        return manga;
    }

    public Anime updateWithDetails(int id, Anime anime) {
        Crashlytics.log(Log.INFO, "MALX", "MALManager.updateWithDetails() : Downloading anime " + id);
        Anime anime_api = AccountService.isMAL() ? malApi.getAnime(id) : alApi.getAnime(id);

        if (anime_api != null) {
            dbMan.saveAnime(anime_api);
            return AccountService.isMAL() ? anime_api : dbMan.getAnime(id);
        }
        return anime;
    }

    public ArrayList<Profile> downloadAndStoreFriendList(String user) {
        ArrayList<Profile> result =  new ArrayList<>();
        try {
            Crashlytics.log(Log.DEBUG, "MALX", "MALManager.downloadAndStoreFriendList(): Downloading friendlist of " + user);
            result = AccountService.isMAL() ? malApi.getFriends(user) : alApi.getFollowers(user);

            if (result != null && result.size() > 0 && AccountService.getUsername().equals(user))
                dbMan.saveFriendList(result);
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.downloadAndStoreFriendList(): " + e.getMessage());
            Crashlytics.logException(e);
        }

        return sortFriendlist(result);
    }

    private ArrayList<Profile> sortFriendlist(ArrayList<Profile> result){
        //sort friendlist
        Collections.sort(result != null ? result : new ArrayList<Profile>(), new Comparator<Profile>() {
            @Override
            public int compare(Profile profile1, Profile profile2)
            {
                return  profile1.getUsername().toLowerCase().compareTo(profile2.getUsername().toLowerCase());
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
            Crashlytics.log(Log.DEBUG, "MALX", "MALManager.getProfile(): Downloading profile of " + name);
            profile = AccountService.isMAL() ? malApi.getProfile(name) : alApi.getProfile(name);

            if (profile != null) {
                profile.setUsername(name);
                if (name.equalsIgnoreCase(AccountService.getUsername()))
                    dbMan.saveProfile(profile);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "MALManager.getProfile(): " + e.getMessage());
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

    public boolean cleanDirtyAnimeRecords() {
        return cleanDirtyAnimeRecords(true);
    }

    public boolean cleanDirtyMangaRecords() {
        return cleanDirtyMangaRecords(true);
    }

    public boolean cleanDirtyAnimeRecords(boolean dirtyOnly) {
        boolean totalSuccess = true;

        ArrayList<Anime> dirtyAnimes = dirtyOnly ? dbMan.getDirtyAnimeList() : getAnimeListFromDB(MALApi.ListType.ANIME.toString());

        if (dirtyAnimes != null) {
            Crashlytics.log(Log.VERBOSE, "MALX", "MALManager.cleanDirtyAnimeRecords(): Got " + dirtyAnimes.size() + " dirty anime records. Cleaning..");

            for (Anime anime : dirtyAnimes) {
                totalSuccess = writeAnimeDetails(anime);
                if (totalSuccess) {
                    anime.clearDirty();
                    saveAnimeToDatabase(anime);
                }

                if (!totalSuccess)
                    break;
            }
            Crashlytics.log(Log.VERBOSE, "MALX", "MALManager.cleanDirtyAnimeRecords(): Cleaned dirty anime records, status: " + totalSuccess);
        }
        return totalSuccess;
    }

    public boolean cleanDirtyMangaRecords(boolean dirtyOnly) {
        boolean totalSuccess = true;

        ArrayList<Manga> dirtyMangas = dirtyOnly ? dbMan.getDirtyMangaList() : getMangaListFromDB(MALApi.ListType.MANGA.toString());

        if (dirtyMangas != null) {
            Crashlytics.log(Log.VERBOSE, "MALX", "MALManager.cleanDirtyMangaRecords(): Got " + dirtyMangas.size() + " dirty manga records. Cleaning..");

            for (Manga manga : dirtyMangas) {
                totalSuccess = writeMangaDetails(manga);
                if (totalSuccess) {
                    manga.clearDirty();
                    saveMangaToDatabase(manga);
                }

                if (!totalSuccess)
                    break;
            }
            Crashlytics.log(Log.VERBOSE, "MALX", "MALManager.cleanDirtyMangaRecords(): Cleaned dirty manga records, status: " + totalSuccess);
        }
        return totalSuccess;
    }

    public ArrayList<History> getActivity(String username) {
        ArrayList<History> result = AccountService.isMAL() ? malApi.getActivity(username) : alApi.getActivity(username);
        Crashlytics.log(Log.INFO, "MALX", "MALManager.getActivity(): got " + String.valueOf(result != null ? result.size() : 0) + " records");
        return result;
    }

    /**
     * Api Requests
     * <p/>
     * All the methods below this block is used to determine and make request to the API.
     */

    public ForumMain search(String query) {
        return malApi.search(query);
    }

    public Anime getAnimeRecord(int id) {
        Crashlytics.log(Log.DEBUG, "MALX", "MALManager.getAnimeRecord(): Downloading " + id);
        return AccountService.isMAL() ? malApi.getAnime(id) : alApi.getAnime(id);
    }

    public Manga getMangaRecord(int id) {
        Crashlytics.log(Log.DEBUG, "MALX", "MALManager.getMangaRecord(): Downloading " + id);
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
        Crashlytics.log(Log.DEBUG, "MALX", "MALManager.writeAnimeDetails(): Updating " + anime.getId());
        boolean result;
        if (anime.getDeleteFlag())
            result = AccountService.isMAL() ? malApi.deleteAnimeFromList(anime.getId()) : alApi.deleteAnimeFromList(anime.getId());
        else
            result = AccountService.isMAL() ? malApi.addOrUpdateAnime(anime) : alApi.addOrUpdateAnime(anime);
        Crashlytics.log(Log.DEBUG, "MALX", "MALManager.writeAnimeDetails(): successfully updated: " + result);
        return result;
    }

    public boolean writeMangaDetails(Manga manga) {
        Crashlytics.log(Log.DEBUG, "MALX", "MALManager.writeMangaDetails(): Updating " + manga.getId());
        boolean result;
        if (manga.getDeleteFlag())
            result = AccountService.isMAL() ? malApi.deleteMangaFromList(manga.getId()) : alApi.deleteMangaFromList(manga.getId());
        else
            result = AccountService.isMAL() ? malApi.addOrUpdateManga(manga) : alApi.addOrUpdateManga(manga);

        return result;
    }

    public BrowseList getMostPopularAnime(int page) {
        return AccountService.isMAL() ? malApi.getMostPopularAnime(page) : alApi.getAiringAnime(page);
    }

    public BrowseList getMostPopularManga(int page) {
        return AccountService.isMAL() ? malApi.getMostPopularManga(page) : alApi.getPublishingManga(page);
    }

    public BrowseList getTopRatedAnime(int page) {
        return AccountService.isMAL() ? malApi.getTopRatedAnime(page) : alApi.getYearAnime(Calendar.getInstance().get(Calendar.YEAR), page);
    }

    public BrowseList getTopRatedManga(int page) {
        return AccountService.isMAL() ? malApi.getTopRatedManga(page) : alApi.getYearManga(Calendar.getInstance().get(Calendar.YEAR), page);
    }

    public BrowseList getJustAddedAnime(int page) {
        return AccountService.isMAL() ? malApi.getJustAddedAnime(page) : alApi.getJustAddedAnime(page);
    }

    public BrowseList getJustAddedManga(int page) {
        return AccountService.isMAL() ? malApi.getJustAddedManga(page) : alApi.getJustAddedManga(page);
    }

    public BrowseList getUpcomingAnime(int page) {
        return AccountService.isMAL() ? malApi.getUpcomingAnime(page) : alApi.getUpcomingAnime(page);
    }

    public BrowseList getUpcomingManga(int page) {
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

    public boolean deleteAnime(Anime anime) {
        return dbMan.deleteAnime(anime.getId());
    }

    public boolean deleteManga(Manga manga) {
        return dbMan.deleteManga(manga.getId());
    }
}
