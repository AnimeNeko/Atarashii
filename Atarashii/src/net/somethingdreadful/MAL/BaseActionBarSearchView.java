package net.somethingdreadful.MAL;

import android.content.Intent;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.widget.SearchView;

import net.somethingdreadful.MAL.api.MALApi.ListType;

public abstract class BaseActionBarSearchView extends SherlockFragmentActivity
        implements SearchView.OnQueryTextListener {
	
    SearchView mSearchView;
    static String query = "";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setQueryHint(getString(R.string.search_prompt));
        mSearchView.setOnQueryTextListener(this);
        if (SearchActivity.class.isInstance(this)) {
            mSearchView.setIconifiedByDefault(false);
        }
        if (!query.equals("")) {
            mSearchView.setQuery(query, false);
        }
        return true;
    }

    public ListType getCurrentListType() {
        return ListType.ANIME;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (!query.equals("")) {
            if (SearchActivity.class.isInstance(this)) {
            	BaseActionBarSearchView.query = query;
                this.doSearch();
            } else {
                Intent startSearch = new Intent(this, SearchActivity.class);
                startSearch.putExtra("net.somethingdreadful.MAL.search_query", query);
                startSearch.putExtra("net.somethingdreadful.MAL.search_type", getCurrentListType().ordinal());
                startActivity(startSearch);
            }
        }
        return false;
    }

    public void doSearch() {
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
