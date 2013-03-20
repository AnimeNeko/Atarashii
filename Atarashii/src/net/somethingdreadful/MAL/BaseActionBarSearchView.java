package net.somethingdreadful.MAL;

import android.content.Intent;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import net.somethingdreadful.MAL.api.BaseMALApi;

public abstract class BaseActionBarSearchView extends SherlockFragmentActivity
        implements SearchView.OnQueryTextListener {
    SearchView mSearchView;

    String query;

    public String getQuery() {
        if (query == null) {
            return "";
        }
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint("Search in MAL"); // TODO use R.string
        mSearchView.setOnQueryTextListener(this);
        if (SearchActivity.class.isInstance(this)) {
            mSearchView.setIconifiedByDefault(false);
        }
        String query = getQuery();
        if (!query.equals("")) {
            mSearchView.setQuery(query, false);
        }
        return true;
    }

    public BaseMALApi.ListType getCurrentListType() {
        return BaseMALApi.ListType.ANIME;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (!query.equals("")) {
            if (SearchActivity.class.isInstance(this)) {
                this.doSearch(query, getCurrentListType());
            } else {
                Intent startSearch = new Intent(this, SearchActivity.class);
                startSearch.putExtra("net.somethingdreadful.MAL.search_query", query);
                startSearch.putExtra("net.somethingdreadful.MAL.search_type", getCurrentListType().ordinal());
                startActivity(startSearch);
            }
        }
        return false;
    }

    public void doSearch(String query, BaseMALApi.ListType listType) {
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
