package net.somethingdreadful.MAL;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApiListType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchActivity extends SherlockFragmentActivity
 {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    HomeSectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    Context context;
    PrefManager mPrefManager;
    public MALManager mManager;
    private boolean init = false;
    private boolean upgradeInit = false;
    ItemGridFragment af;
    ItemGridFragment mf;
    public boolean instanceExists;

    private ListView result_list_widget;
    private  ArrayAdapter<String> result_list_adapter;
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

        result_list_widget = (ListView) findViewById(R.id.searchResult);
        result_list_adapter = new ArrayAdapter<String>(this, R.layout.search_item, R.id.searchResultRow);
        result_list_widget.setAdapter(result_list_adapter);

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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.menu_settings:
                startActivity(new Intent (this, Settings.class));
                break;

            case R.id.menu_logout:
                //showLogoutDialog();
                break;

            case R.id.menu_about:
                startActivity(new Intent(this, About.class));
                break;

                //The following is the code that handles switching the list. It calls the fragment to update, then update the menu by invalidating
            case R.id.listType_all:
                if (af != null && mf != null)
                {
                    af.getRecords(0, "anime", false);
                    mf.getRecords(0, "manga", false);
                    invalidateOptionsMenu();
                }
                break;
            case R.id.listType_inprogress:
                if (af != null && mf != null)
                {
                    af.getRecords(1, "anime", false);
                    mf.getRecords(1, "manga", false);
                    invalidateOptionsMenu();
                }
                break;
            case R.id.listType_completed:
                if (af != null && mf != null)
                {
                    af.getRecords(2, "anime", false);
                    mf.getRecords(2, "manga", false);
                    invalidateOptionsMenu();
                }
                break;
            case R.id.listType_onhold:
                if (af != null && mf != null)
                {
                    af.getRecords(3, "anime", false);
                    mf.getRecords(3, "manga", false);
                    invalidateOptionsMenu();
                }
                break;
            case R.id.listType_dropped:
                if (af != null && mf != null)
                {
                    af.getRecords(4, "anime", false);
                    mf.getRecords(4, "manga", false);
                    invalidateOptionsMenu();
                }
                break;
            case R.id.listType_planned:
                if (af != null && mf != null)
                {
                    af.getRecords(5, "anime", false);
                    mf.getRecords(5, "manga", false);
                    invalidateOptionsMenu();
                }
                break;
            case R.id.forceSync:
                if (af != null && mf != null)
                {
                    af.getRecords(af.currentList, "anime", true);
                    mf.getRecords(af.currentList, "manga", true);
                    //syncNotify();
                }
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (instanceExists == true)
        {
            af.getRecords(af.currentList, "anime", false);
            mf.getRecords(af.currentList, "manga", false);
        }

    }

    @Override
    public void onPause()
    {
        super.onPause();

        instanceExists = true;
    }

    @Override
    public void onSaveInstanceState(Bundle state)
    {
        //This is telling out future selves that we already have some things and not to do them
        state.putBoolean("instanceExists", true);

        super.onSaveInstanceState(state);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if (af != null)
        {
            //All this is handling the ticks in the switch list menu
            switch (af.currentList)
            {
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

     public class networkThread extends AsyncTask<Void, Void, Void>
     {
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

             for (int i = 0; i < _result.length(); i++) {
                 try {
                     JSONObject genre = (JSONObject) _result.get(i);
                     result_list_adapter.add(genre.getString("title"));
                 } catch (JSONException e) {

                 }
             }

         }
     }


}
