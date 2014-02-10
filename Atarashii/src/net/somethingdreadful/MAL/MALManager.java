package net.somethingdreadful.MAL;

import java.util.ArrayList;
import java.util.Date;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.AnimeList;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.api.response.MangaList;
import net.somethingdreadful.MAL.api.response.Profile;
import net.somethingdreadful.MAL.api.response.User;
import net.somethingdreadful.MAL.sql.DatabaseManager;

import android.content.Context;
import android.util.Log;

public class MALManager {

    final static String TYPE_ANIME = "anime";
    final static String TYPE_MANGA = "manga";
    final static String TYPE_FRIENDS = "friends";
    final static String TYPE_PROFILE = "profile";

    MALApi malApi;
    DatabaseManager dbMan;

    public MALManager(Context context) {
        malApi = new MALApi(context);
        dbMan = new DatabaseManager(context);
    }

    public static String listSortFromInt(int i, String type) {
        String r = "";

        if (type.equals("anime")) {
            switch (i) {
                case 0:
                    r = "";
                    break;
                case 1:
                    r = Anime.STATUS_WATCHING;
                    break;
                case 2:
                    r = Anime.STATUS_COMPLETED;
                    break;
                case 3:
                    r = Anime.STATUS_ONHOLD;
                    break;
                case 4:
                    r = Anime.STATUS_DROPPED;
                    break;
                case 5:
                    r = Anime.STATUS_PLANTOWATCH;
                    break;
                default:
                    r = Anime.STATUS_WATCHING;
                    break;
            }
        } else if (type.equals("manga")) {
            switch (i) {
                case 0:
                    r = "";
                    break;
                case 1:
                    r = Manga.STATUS_READING;
                    break;
                case 2:
                    r = Manga.STATUS_COMPLETED;
                    break;
                case 3:
                    r = Manga.STATUS_ONHOLD;
                    break;
                case 4:
                    r = Manga.STATUS_DROPPED;
                    break;
                case 5:
                    r = Manga.STATUS_PLANTOREAD;
                    break;
                default:
                    r = Manga.STATUS_READING;
                    break;
            }
        }

        return r;
    }
    
    public MALApi getAPIObject() {
    	return malApi;
    }

    public Anime getAnimeRecordFromMAL(int id) {
        Anime anime = malApi.getAnime(id);
        if ( anime != null ) {
	        if (anime.getWatchedStatus() == null) {
	        	anime.setCreateFlag(true);
	        }
        }
        return anime;
    }

    public Manga getMangaRecordFromMAL(int id) {
    	Manga manga = malApi.getManga(id);
        if ( manga != null ) {
	        if (manga.getReadStatus() == null) {
	        	manga.setCreateFlag(true);
	        }
        }
        return manga;
    }
    
    public ArrayList<Anime> downloadAndStoreAnimeList() {
    	ArrayList<Anime> result = null;
    	AnimeList animeList = malApi.getAnimeList();
    	if (animeList != null) {
    		result = animeList.getAnimes();
			dbMan.saveAnimeList(result);
    	}
    	return result;
    }
    
    public ArrayList<Manga> downloadAndStoreMangaList() {
    	ArrayList<Manga> result = null;
    	MangaList mangaList = malApi.getMangaList();
		if (mangaList != null) {
			result = mangaList.getManga();
			dbMan.saveMangaList(result);
		}
		return result;
    }
    
    public ArrayList<Anime> getAnimeListFromDB() {
    	return dbMan.getAnimeList();
    }
    
    public ArrayList<Anime> getAnimeListFromDB(String ListType) {
    	return dbMan.getAnimeList(ListType);
    }
    
    public ArrayList<Manga> getMangaListFromDB() {
    	return dbMan.getMangaList();
    }
    
    public ArrayList<Manga> getMangaListFromDB(String ListType) {
    	return dbMan.getMangaList(ListType);
    }

    public Anime updateWithDetails(int id, Anime anime) {
    	Anime anime_api = malApi.getAnime(id);
    	if ( anime_api != null ) {
    		anime.setSynopsis(anime_api.getSynopsis());
    		anime.setMembersScore(anime_api.getMembersScore());
            dbMan.saveAnime(anime, false);
    	}
        
        return anime;
    }
    
    public ArrayList<User> downloadAndStoreFriendList(String user) {
        ArrayList<User> result = null;
        try {
            result = malApi.getFriends(user);
            if ( result.size() > 0 ) {
                dbMan.saveFriendList(result);
            }
        } catch (Exception e) {
            result = null;
            Log.e("MALX", "error downloading friendlist: " + e.getMessage());
        }
        return result;
    }
    
    public ArrayList<User> getFriendListFromDB() {
        return dbMan.getFriendList();
    }
    
    public User downloadAndStoreProfile(String name) {
        User result = null;
        try {
            Profile profile = malApi.getProfile(name);
            if ( profile != null ) {
                result = new User();
                result.setName(name);
                result.setProfile(profile);
                dbMan.saveUser(result);
            }
        } catch (Exception e) {
            Log.e("MALX", e.getMessage());
            result = null;
        }
        return result;
    }
    
    public User getProfileFromDB(String name) {
        return dbMan.getProfile(name);
    }

    public Manga updateWithDetails(int id, Manga manga) {
    	Manga manga_api = malApi.getManga(id);
    	if ( manga_api != null ) {
    		manga.setSynopsis(manga_api.getSynopsis());
    		manga.setMembersScore(manga_api.getMembersScore());
    		dbMan.saveManga(manga, false);
    	}
        
        return manga;
    }

    public Anime getAnimeRecord(int recordID) {
    	Anime result = dbMan.getAnime(recordID);
        if ( result == null )
        	result = getAnimeRecordFromMAL(recordID);
        return result;
    }

    public Manga getMangaRecord(int recordID) {
    	Manga result = dbMan.getManga(recordID);
        if ( result == null )
        	result = getMangaRecordFromMAL(recordID);
        return result;
    }
    
    public void saveAnimeToDatabase(Anime anime, boolean ignoreSynopsis) {
    	dbMan.saveAnime(anime, ignoreSynopsis);
    }
    
    public void saveMangaToDatabase(Manga manga, boolean ignoreSynopsis) {
    	dbMan.saveManga(manga, ignoreSynopsis);
    }
    
    public boolean deleteAnimeFromDatabase(Anime anime) {
    	return dbMan.deleteAnime(anime.getId());
    }
    
    public boolean deleteMangaFromDatabase(Manga manga) {
    	return dbMan.deleteManga(manga.getId());
    }
    
    /* only difference between old addItemToMAL and writeDetailsToMAL was that
     * addItemToMal sets score to 0... so do that and pass to writeAnimeDetailsToMAL
     */
    public boolean addAnimeToMAL(Anime anime) {
    	anime.setScore(0);
    	return writeAnimeDetailsToMAL(anime);
    }
    
    public boolean writeAnimeDetailsToMAL(Anime anime) {
    	boolean result;
    	if (anime.getDeleteFlag())
    		result = malApi.deleteAnimeFromList(anime.getId());
    	else
    		result = malApi.addOrUpdateAnime(anime);
    	return result;
    }

    /* only difference between old addItemToMAL and writeDetailsToMAL was that
     * addItemToMal sets score to 0... so do that and pass to writeAnimeDetailsToMAL
     */
    public boolean addMangaToMAL(Manga manga) {
    	manga.setScore(0);
    	return writeMangaDetailsToMAL(manga);
    }

    public boolean writeMangaDetailsToMAL(Manga manga) {
    	boolean result;
    	if (manga.getDeleteFlag())
    		result = malApi.deleteMangaFromList(manga.getId());
    	else
    		result = malApi.addOrUpdateManga(manga);
    	return result;
    }

    public void clearDeletedItems(String type, Date currentTime) {
        Log.v("MALX", "Removing deleted items of type " + type + " older than " + currentTime.toString());
        int recordsRemoved = 0;
        if (getListTypeFromString(type) == MALApi.ListType.ANIME)
        	recordsRemoved = dbMan.clearOldAnimeRecords(currentTime);
        else
        	recordsRemoved = dbMan.clearOldMangaRecords(currentTime);

        Log.v("MALX", "Removed " + recordsRemoved + " " + type + " items");
    }

    private MALApi.ListType getListTypeFromString(String type) {
        if (type.equals(TYPE_ANIME)) {

            return MALApi.ListType.ANIME;
        }

        else if (type.equals(TYPE_MANGA)) {
            return MALApi.ListType.MANGA;
        }

        else {
            return null;
        }
    }

    public boolean cleanDirtyAnimeRecords() {
        boolean totalSuccess = true;

        ArrayList<Anime> dirtyAnimes = dbMan.getDirtyAnimeList();
        
        if (dirtyAnimes != null) {
        	Log.v("MALX", "Got " + dirtyAnimes.size() + " dirty anime records. Cleaning..");

        	for(Anime anime : dirtyAnimes) {
        		totalSuccess = writeAnimeDetailsToMAL(anime);
        		if (totalSuccess) {
        			anime.setDirty(false);
        			saveAnimeToDatabase(anime, false);
        		}
        		
        		if (!totalSuccess)
        			break;
        	}
        	Log.v("MALX", "Cleaned dirty anime records, status: " + totalSuccess);
        }
        return totalSuccess;
    }

    public boolean cleanDirtyMangaRecords() {
        boolean totalSuccess = true;

        ArrayList<Manga> dirtyMangas = dbMan.getDirtyMangaList();
        
        if (dirtyMangas != null) {
        	Log.v("MALX", "Got " + dirtyMangas.size() + " dirty manga records. Cleaning..");

        	for(Manga manga : dirtyMangas) {
        		totalSuccess = writeMangaDetailsToMAL(manga);
        		if (totalSuccess) {
        			manga.setDirty(false);
        			saveMangaToDatabase(manga, false);
        		}
        		
        		if (!totalSuccess)
        			break;
        	}
        	Log.v("MALX", "Cleaned dirty manga records, status: " + totalSuccess);
        }
        return totalSuccess;
    }
}
