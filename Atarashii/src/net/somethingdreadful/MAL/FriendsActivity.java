package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.User;
import net.somethingdreadful.MAL.tasks.FriendsNetworkTask;
import net.somethingdreadful.MAL.tasks.FriendsNetworkTaskFinishedListener;

import org.apache.commons.lang3.text.WordUtils;
import org.holoeverywhere.app.Activity;

import java.util.ArrayList;
import java.util.Collection;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class FriendsActivity extends Activity implements FriendsNetworkTaskFinishedListener, SwipeRefreshLayout.OnRefreshListener {

    Context context;
    ArrayList<User> listarray = new ArrayList<User>();
    ListViewAdapter<User> listadapter;
    GridView Gridview;
    SwipeRefreshLayout swipeRefresh;
    boolean forcesync = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_friends);
        setTitle(R.string.title_activity_friends); //set title

        Gridview = (GridView) findViewById(R.id.listview);
        int recource = R.layout.record_friends_gridview;

        listadapter = new ListViewAdapter<User>(context, recource);

        new FriendsNetworkTask(context, forcesync, this).execute(AccountService.getAccount(context).name);

        refresh(false);

        Gridview.setOnItemClickListener(new OnItemClickListener() { //start the profile with your friend
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent profile = new Intent(context, net.somethingdreadful.MAL.ProfileActivity.class);
                profile.putExtra("username", listarray.get(position).getName());
                startActivity(profile);
            }
        });

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorScheme(R.color.holo_blue_bright, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_red_light);
        swipeRefresh.setEnabled(true);

        NfcHelper.disableBeam(this);
    }

    public void refresh(Boolean crouton) {
        if (crouton) {
            Crouton.makeText(this, R.string.crouton_info_SyncDone, Style.CONFIRM).show();
        }
        Gridview.setAdapter(listadapter);
        listadapter.clear();
        try {
            listadapter.supportAddAll(listarray);
        } catch (Exception e) {
            if (MALApi.isNetworkAvailable(context)) {
                Crouton.makeText(this, R.string.crouton_error_noFriends, Style.ALERT).show();
            } else {
                Crouton.makeText(this, R.string.crouton_error_noConnectivity, Style.ALERT).show();
            }
        }
        listadapter.notifyDataSetChanged();
    }

    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.activity_friends_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
            case R.id.forceSync:
                sync(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFriendsNetworkTaskFinished(ArrayList<User> result) {
        if (result != null) {
            swipeRefresh.setEnabled(true);
            listarray = result;
            if (listarray.size() == 0)
                Crouton.makeText(this, R.string.crouton_error_noConnectivity, Style.ALERT).show();
            refresh(forcesync); // show crouton only if sync was forced
        } else {
            Crouton.makeText(this, R.string.crouton_error_Friends, Style.ALERT).show();
        }
        swipeRefresh.setRefreshing(false);
    }

    public void sync(Boolean swipe) {
        swipeRefresh.setEnabled(false);
        if (MALApi.isNetworkAvailable(context)) {
            if (!swipe)
                Crouton.makeText(this, R.string.crouton_info_SyncMessage, Style.INFO).show();
            forcesync = true;
            new FriendsNetworkTask(context, true, this).execute(AccountService.getAccount(context).name);
        } else {
            swipeRefresh.setRefreshing(false);
            Crouton.makeText(this, R.string.crouton_error_noConnectivity, Style.ALERT).show();
        }
    }

    @Override
    public void onRefresh() {
        sync(true);
    }

    static class ViewHolder {
        TextView username;
        TextView last_online;
        ImageView avatar;
    }

    public class ListViewAdapter<T> extends ArrayAdapter<T> {

        public ListViewAdapter(Context context, int resource) {
            super(context, resource);
        }

        public View getView(int position, View view, ViewGroup parent) {
            final User record = (listarray.get(position));
            ViewHolder viewHolder;

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.record_friends_gridview, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.username = (TextView) view.findViewById(R.id.userName);
                viewHolder.last_online = (TextView) view.findViewById(R.id.lastonline);
                viewHolder.avatar = (ImageView) view.findViewById(R.id.profileImg);

                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            try {
                String username = record.getName();
                viewHolder.username.setText(WordUtils.capitalize(username));
                if (User.isDeveloperRecord(username))
                    viewHolder.username.setTextColor(Color.parseColor("#008583")); //Developer

                String last_online = record.getProfile().getDetails().getLastOnline();
                last_online = MALDateTools.formatDateString(last_online, context, true);
                viewHolder.last_online.setText(last_online.equals("") ? record.getProfile().getDetails().getLastOnline() : last_online);
                Picasso picasso = Picasso.with(context);
                picasso.load(record.getProfile().getAvatarUrl())
                        .error(R.drawable.cover_error)
                        .placeholder(R.drawable.cover_loading)
                        .into(viewHolder.avatar);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return view;
        }

        public void supportAddAll(Collection<? extends T> collection) {
            for (T record : collection) {
                this.add(record);
            }
        }
    }
}
