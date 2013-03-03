package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApiListType;
import net.somethingdreadful.MAL.record.AnimeRecord;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends SherlockFragmentActivity
        implements BaseItemGridFragment.IBaseItemGridFragment, ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SearchSectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    Context context;
    PrefManager mPrefManager;
    public MALManager mManager;
    BaseItemGridFragment animeItemGridFragment;
    BaseItemGridFragment mangaItemGridFragment;

    private TextView search_query_widget;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        mPrefManager = new PrefManager(context);


        setContentView(R.layout.activity_search);
        mManager = new MALManager(context);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);


        mSectionsPagerAdapter = new SearchSectionsPagerAdapter(
                getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.searchResult);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageMargin(32);
        // Add tabs for the animu and mango lists
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }

        search_query_widget = (EditText) findViewById(R.id.searchQuery);

        Button doSearchGoButton = (Button) findViewById(R.id.searchGo);
        doSearchGoButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new networkThread().execute();
                    }
                }
        );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, Settings.class));
                break;

            case R.id.menu_logout:
                //showLogoutDialog();
                break;

            case R.id.menu_about:
                startActivity(new Intent(this, About.class));
                break;

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (animeItemGridFragment != null) {
            //All this is handling the ticks in the switch list menu
            switch (animeItemGridFragment.currentList) {
                case 0:
                    menu.findItem(R.id.listType_all).setChecked(true);
                    break;
                case 1:
                    menu.findItem(R.id.listType_inprogress).setChecked(true);
                    break;
                case 2:
                    menu.findItem(R.id.listType_completed).setChecked(true);
                    break;
                case 3:
                    menu.findItem(R.id.listType_onhold).setChecked(true);
                    break;
                case 4:
                    menu.findItem(R.id.listType_dropped).setChecked(true);
                    break;
                case 5:
                    menu.findItem(R.id.listType_planned).setChecked(true);
            }
        }

        return true;
    }

    @Override
    public void fragmentReady() {
        //Interface implementation for knowing when the dynamically created fragment is finished loading

        //We use instantiateItem to return the fragment. Since the fragment IS instantiated, the method returns it.
        animeItemGridFragment = (BaseItemGridFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 0);
        mangaItemGridFragment = (BaseItemGridFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 1);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    public class networkThread extends AsyncTask<Void, Void, Void> {
        JSONArray _result;

        @Override
        protected Void doInBackground(Void... params) {
            //To change body of implemented methods use File | Settings | File Templates.
            String query = search_query_widget.getText().toString();
            MALApi api = new MALApi(null, null);
            _result = api.search(MALApiListType.ANIME, query);
            return null;
        }

        protected void onPostExecute(Void result) {
            ArrayList<AnimeRecord> list = new ArrayList<>();
            for (int i = 0; i < _result.length(); i++) {
                try {
                    JSONObject genre = (JSONObject) _result.get(i);

//                    String image_url = genre.getString("image_url").replaceFirst("t.jpg$", ".jpg");
                    HashMap<String, Object> record_data = new HashMap<>();
                    record_data.put("recordID", genre.getInt("id"));
                    record_data.put("recordName", genre.getString("title"));
                    record_data.put("recordType", genre.getString("type"));
                    record_data.put("recordStatus", "TODO: Status?");
                    record_data.put("memberScore", (float) genre.getDouble("members_score"));
                    record_data.put("imageUrl", genre.getString("image_url").replaceFirst("t.jpg$", ".jpg"));
                    record_data.put("episodesTotal", genre.getInt("episodes"));

                    AnimeRecord record = new AnimeRecord(record_data);


                    list.add(record);
                } catch (JSONException e) {
                    Log.e(SearchActivity.class.getName(), Log.getStackTraceString(e));

                }
            }
            animeItemGridFragment.setAnimeRecords(list);

        }
    }


}
