package net.somethingdreadful.MAL;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.User;
import net.somethingdreadful.MAL.tasks.UserNetworkTask;
import net.somethingdreadful.MAL.tasks.UserNetworkTaskFinishedListener;

import org.apache.commons.lang3.text.WordUtils;

public class ProfileActivity extends ActionBarActivity implements UserNetworkTaskFinishedListener, SwipeRefreshLayout.OnRefreshListener {
    Context context;
    Card imagecard;
    Card animecard;
    Card mangacard;
    User record;
    ViewFlipper viewFlipper;
    SwipeRefreshLayout swipeRefresh;
    ProgressBar progressBar;
    Card networkCard;

    boolean forcesync = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        context = getApplicationContext();

        imagecard = ((Card) findViewById(R.id.name_card));
        imagecard.setContent(R.layout.card_image);
        ((Card) findViewById(R.id.details_card)).setContent(R.layout.card_profile_details);
        animecard = (Card) findViewById(R.id.Anime_card);
        animecard.setContent(R.layout.card_profile_anime);
        mangacard = (Card) findViewById(R.id.Manga_card);
        mangacard.setContent(R.layout.card_profile_manga);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        networkCard = (Card) findViewById(R.id.network_Card);
        setTitle(R.string.title_activity_profile); //set title


        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);

        if (getIntent().getExtras().containsKey("user")) {
            toggle(1);
            record = (User) getIntent().getExtras().get("user");
            refresh();
        } else {
            swipeRefresh.setEnabled(false);
            toggle(1);
            new UserNetworkTask(context, forcesync, this).execute(getIntent().getStringExtra("username"));
        }

        TextView tv25 = (TextView) findViewById(R.id.websitesmall);
        tv25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri webstiteclick = Uri.parse(record.getProfile().getDetails().getWebsite());
                startActivity(new Intent(Intent.ACTION_VIEW, webstiteclick));
            }
        });

        NfcHelper.disableBeam(this);
    }

    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.activity_profile_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.forceSync:
                if (MALApi.isNetworkAvailable(context)) {
                    swipeRefresh.setEnabled(false);
                    swipeRefresh.setRefreshing(true);
                    String username;
                    if (record != null)
                        username = record.getName();
                    else
                        username = getIntent().getStringExtra("username");
                    new UserNetworkTask(context, true, this).execute(username);
                } else {
                    Toast.makeText(context, R.string.toast_error_noConnectivity, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_ViewMALPage:
                if (record == null)
                    Toast.makeText(context, R.string.toast_info_hold_on, Toast.LENGTH_SHORT).show();
                else
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://myanimelist.net/profile/" + record.getName())));
                break;
            case R.id.View:
                choosedialog(false);
                break;
            case R.id.Share:
                choosedialog(true);
        }
        return true;
    }

    public void card() { //settings for hide a card and text userprofile
        if (PrefManager.getHideAnime()) {
            animecard.setVisibility(View.GONE);
        }
        if (PrefManager.getHideManga()) {
            mangacard.setVisibility(View.GONE);
        }
        if (record.getProfile().getMangaStats().getTotalEntries() < 1) { //if manga (total entry) is beneath the int then hide
            mangacard.setVisibility(View.GONE);
        }
        if (record.getProfile().getAnimeStats().getTotalEntries() < 1) { //if anime (total entry) is beneath the int then hide
            animecard.setVisibility(View.GONE);
        }
        Card namecard = (Card) findViewById(R.id.name_card);
        namecard.Header.setText(WordUtils.capitalize(record.getName()));
    }

    public void setcolor() {
        TextView tv8 = (TextView) findViewById(R.id.accessranksmall);
        String name = record.getName();
        String rank = record.getProfile().getDetails().getAccessRank() != null ? record.getProfile().getDetails().getAccessRank() : "";
        if (!PrefManager.getTextColor()) {
            setColor(true);
            setColor(false);
            if (rank.contains("Administrator")) {
                tv8.setTextColor(Color.parseColor("#850000"));
            } else if (rank.contains("Moderator")) {
                tv8.setTextColor(Color.parseColor("#003385"));
            } else if (User.isDeveloperRecord(name)) {
                tv8.setTextColor(Color.parseColor("#008583")); //Developer
            } else {
                tv8.setTextColor(Color.parseColor("#0D8500")); //normal user
            }
            TextView tv11 = (TextView) findViewById(R.id.websitesmall);
            tv11.setTextColor(Color.parseColor("#002EAB"));
        }
        if (User.isDeveloperRecord(name)) {
            tv8.setText(R.string.access_rank_atarashii_developer); //Developer
        }
    }

    public void setColor(boolean type) {
        int Hue;
        TextView textview;
        if (type) {
            textview = (TextView) findViewById(R.id.atimedayssmall); //anime
            Hue = (int) (record.getProfile().getAnimeStats().getTimeDays() * 2.5);
        } else {
            textview = (TextView) findViewById(R.id.mtimedayssmall); // manga
            Hue = (int) (record.getProfile().getMangaStats().getTimeDays() * 5);
        }
        if (Hue > 359) {
            Hue = 359;
        }
        textview.setTextColor(Color.HSVToColor(new float[]{Hue, 1, (float) 0.7}));
    }

    private String getStringFromResourceArray(int resArrayId, int notFoundStringId, int index) {
        Resources res = getResources();
        try {
            String[] types = res.getStringArray(resArrayId);
            if (index < 0 || index >= types.length) // make sure to have a valid array index
                return res.getString(notFoundStringId);
            else
                return types[index];
        } catch (Resources.NotFoundException e) {
            Crashlytics.logException(e);
            return res.getString(notFoundStringId);
        }
    }

    private void toggle(int number) {
        swipeRefresh.setVisibility(number == 0 ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(number == 1 ? View.VISIBLE : View.GONE);
        networkCard.setVisibility(number == 2 ? View.VISIBLE : View.GONE);
    }

    public void Settext() {
        TextView tv1 = (TextView) findViewById(R.id.birthdaysmall);
        if (record.getProfile().getDetails().getBirthday() == null) {
            tv1.setText(R.string.not_specified);
        } else {
            String birthday = MALDateTools.formatDateString(record.getProfile().getDetails().getBirthday(), this, false);
            tv1.setText(birthday.equals("") ? record.getProfile().getDetails().getBirthday() : birthday);
        }
        TextView tv2 = (TextView) findViewById(R.id.locationsmall);
        if (record.getProfile().getDetails().getLocation() == null) {
            tv2.setText(R.string.not_specified);
        } else {
            tv2.setText(record.getProfile().getDetails().getLocation());
        }
        TextView tv25 = (TextView) findViewById(R.id.websitesmall);
        TextView tv26 = (TextView) findViewById(R.id.websitefront);
        Card tv36 = (Card) findViewById(R.id.details_card);
        if (record.getProfile().getDetails().getWebsite() != null && record.getProfile().getDetails().getWebsite().contains("http://") && record.getProfile().getDetails().getWebsite().contains(".")) { // filter fake websites
            tv25.setText(record.getProfile().getDetails().getWebsite().replace("http://", ""));
        } else {
            tv25.setVisibility(View.GONE);
            tv26.setVisibility(View.GONE);
        }
        TextView tv3 = (TextView) findViewById(R.id.commentspostssmall);
        tv3.setText(String.valueOf(record.getProfile().getDetails().getComments()));
        TextView tv4 = (TextView) findViewById(R.id.forumpostssmall);
        tv4.setText(String.valueOf(record.getProfile().getDetails().getForumPosts()));
        TextView tv5 = (TextView) findViewById(R.id.lastonlinesmall);
        if (record.getProfile().getDetails().getLastOnline() != null) {
            String lastOnline = MALDateTools.formatDateString(record.getProfile().getDetails().getLastOnline(), this, true);
            tv5.setText(lastOnline.equals("") ? record.getProfile().getDetails().getLastOnline() : lastOnline);
        } else
            tv5.setText("-");
        TextView tv6 = (TextView) findViewById(R.id.gendersmall);
        tv6.setText(getStringFromResourceArray(R.array.gender, R.string.not_specified, record.getProfile().getDetails().getGenderInt()));
        TextView tv7 = (TextView) findViewById(R.id.joindatesmall);
        if (record.getProfile().getDetails().getJoinDate() != null) {
            String joinDate = MALDateTools.formatDateString(record.getProfile().getDetails().getJoinDate(), this, false);
            tv7.setText(joinDate.equals("") ? record.getProfile().getDetails().getJoinDate() : joinDate);
        } else
            tv7.setText("-");
        TextView tv8 = (TextView) findViewById(R.id.accessranksmall);
        tv8.setText(record.getProfile().getDetails().getAccessRank());
        TextView tv9 = (TextView) findViewById(R.id.animelistviewssmall);
        tv9.setText(String.valueOf(record.getProfile().getDetails().getAnimeListViews()));
        TextView tv10 = (TextView) findViewById(R.id.mangalistviewssmall);
        tv10.setText(String.valueOf(record.getProfile().getDetails().getMangaListViews()));

        TextView tv11 = (TextView) findViewById(R.id.atimedayssmall);
        tv11.setText(record.getProfile().getAnimeStats().getTimeDays().toString());
        TextView tv12 = (TextView) findViewById(R.id.awatchingsmall);
        tv12.setText(String.valueOf(record.getProfile().getAnimeStats().getWatching()));
        TextView tv13 = (TextView) findViewById(R.id.acompletedpostssmall);
        tv13.setText(String.valueOf(record.getProfile().getAnimeStats().getCompleted()));
        TextView tv14 = (TextView) findViewById(R.id.aonholdsmall);
        tv14.setText(String.valueOf(record.getProfile().getAnimeStats().getOnHold()));
        TextView tv15 = (TextView) findViewById(R.id.adroppedsmall);
        tv15.setText(String.valueOf(record.getProfile().getAnimeStats().getDropped()));
        TextView tv16 = (TextView) findViewById(R.id.aplantowatchsmall);
        tv16.setText(String.valueOf(record.getProfile().getAnimeStats().getPlanToWatch()));
        TextView tv17 = (TextView) findViewById(R.id.atotalentriessmall);
        tv17.setText(String.valueOf(record.getProfile().getAnimeStats().getTotalEntries()));

        TextView tv18 = (TextView) findViewById(R.id.mtimedayssmall);
        tv18.setText(record.getProfile().getMangaStats().getTimeDays().toString());
        TextView tv19 = (TextView) findViewById(R.id.mwatchingsmall);
        tv19.setText(String.valueOf(record.getProfile().getMangaStats().getReading()));
        TextView tv20 = (TextView) findViewById(R.id.mcompletedpostssmall);
        tv20.setText(String.valueOf(record.getProfile().getMangaStats().getCompleted()));
        TextView tv21 = (TextView) findViewById(R.id.monholdsmall);
        tv21.setText(String.valueOf(record.getProfile().getMangaStats().getOnHold()));
        TextView tv22 = (TextView) findViewById(R.id.mdroppedsmall);
        tv22.setText(String.valueOf(record.getProfile().getMangaStats().getDropped()));
        TextView tv23 = (TextView) findViewById(R.id.mplantowatchsmall);
        tv23.setText(String.valueOf(record.getProfile().getMangaStats().getPlanToRead()));
        TextView tv24 = (TextView) findViewById(R.id.mtotalentriessmall);
        tv24.setText(String.valueOf(record.getProfile().getMangaStats().getTotalEntries()));
    }

    public void refresh() {
        if (record == null) {
            if (MALApi.isNetworkAvailable(context)) {
                Toast.makeText(context, R.string.toast_error_UserRecord, Toast.LENGTH_SHORT).show();
            } else {
                toggle(2);
            }
        } else {
            card();
            Settext();
            setcolor();

            Picasso.with(context)
                    .load(record.getProfile()
                    .getAvatarUrl())
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            imagecard.wrapImage(bitmap.getWidth(), bitmap.getHeight());
                            ((ImageView) findViewById(R.id.Image)).setImageBitmap(bitmap);
                            toggle(0);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            toggle(0);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            toggle(0);
                        }
                    });
        }
    }

    void choosedialog(final boolean share) { //as the name says
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        if (share) {
            builder.setTitle(R.string.dialog_title_share);
            builder.setMessage(R.string.dialog_message_share);
            sharingIntent.setType("text/plain");
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        } else {
            builder.setTitle(R.string.dialog_title_view);
            builder.setMessage(R.string.dialog_message_view);
        }

        builder.setPositiveButton(R.string.dialog_label_animelist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (share) {
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_animelist)
                            .replace("$name;", record.getName())
                            .replace("$username;", AccountService.getUsername(context)));
                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.dialog_title_share_via)));
                } else {
                    Uri mallisturlanime = Uri.parse("http://myanimelist.net/animelist/" + record.getName());
                    startActivity(new Intent(Intent.ACTION_VIEW, mallisturlanime));
                }
            }
        });
        builder.setNeutralButton(R.string.dialog_label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(R.string.dialog_label_mangalist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (share) {
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_mangalist)
                            .replace("$name;", record.getName())
                            .replace("$username;", AccountService.getUsername(context)));
                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.dialog_title_share_via)));
                } else {
                    Uri mallisturlmanga = Uri.parse("http://myanimelist.net/mangalist/" + record.getName());
                    startActivity(new Intent(Intent.ACTION_VIEW, mallisturlmanga));
                }
            }
        });
        builder.show();
    }

    @Override
    public void onUserNetworkTaskFinished(User result) {
        record = result;
        refresh();
        swipeRefresh.setEnabled(true);
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        swipeRefresh.setEnabled(false);
        String username;
        if (record != null)
            username = record.getName();
        else
            username = getIntent().getStringExtra("username");
        new UserNetworkTask(context, true, this).execute(username);
    }
}
