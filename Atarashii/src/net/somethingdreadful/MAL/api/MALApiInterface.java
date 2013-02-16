package net.somethingdreadful.MAL.api;

import org.json.JSONArray;

/**
 * Created with IntelliJ IDEA.
 * User: apkawa
 * Date: 16.02.13
 * Time: 23:56
 * To change this template use File | Settings | File Templates.
 */
public interface MALApiInterface {
    public JSONArray getList();

    public boolean isAuth();

    public void search();

    public boolean updateGenreInList();

    public boolean addGenreToList() ;

    public boolean deleteGenreFromList();



}
