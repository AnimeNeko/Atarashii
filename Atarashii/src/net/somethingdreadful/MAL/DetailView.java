package net.somethingdreadful.MAL;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.GenericRecord;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.dialog.RemoveConfirmationDialogFragment;
import net.somethingdreadful.MAL.dialog.UpdatePasswordDialogFragment;
import net.somethingdreadful.MAL.sql.DatabaseManager;
import net.somethingdreadful.MAL.tasks.APIAuthenticationErrorListener;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.NetworkTaskCallbackListener;
import net.somethingdreadful.MAL.tasks.TaskJob;
import net.somethingdreadful.MAL.tasks.WriteDetailTask;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.DialogFragment;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class DetailView extends Activity implements Serializable, NetworkTaskCallbackListener, ViewPager.OnPageChangeListener, APIAuthenticationErrorListener, ActionBar.TabListener {

    public ListType type;
    public Anime animeRecord;
    public Manga mangaRecord;
    public String username;
    public PrefManager pref;
    public DetailViewGeneral general;
    public DetailViewDetails details;
    DetailViewPagerAdapter PageAdapter;
    int recordID;
    private ActionBar actionbar;
    private ViewPager viewPager;
    private Menu menu;
    private ArrayList<String> tabs = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detailview);
        actionbar = getSupportActionBar();
        username = getIntent().getStringExtra("username");
        pref = new PrefManager(this);
        type = (ListType) getIntent().getSerializableExtra("recordType");
        recordID = getIntent().getIntExtra("recordID", -1);

        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        viewPager = (ViewPager) findViewById(R.id.pager);
        PageAdapter = new DetailViewPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(PageAdapter);
        viewPager.setOnPageChangeListener(this);

        setTabs();

        if (savedInstanceState != null) {
            animeRecord = (Anime) savedInstanceState.getSerializable("anime");
            mangaRecord = (Manga) savedInstanceState.getSerializable("manga");
        }
    }

    /*
     * Set text in all fragments
     */
    public void setText() {
        general.setText();
        if (details != null)
            details.setText();
    }

    /*
     * Create tabs in the actionbar
     */
    public void setTabs() {
        for (int i = 0; i < PageAdapter.getCount(); i++) {
            tabs.add(PageAdapter.getPageTitle(i));
        }
        for (String tab : tabs) {
            actionbar.addTab(actionbar.newTab().setText(tab).setTabListener(this));
        }
    }

    /*
     * Checks if the records are null to prevent nullpointerexceptions
     */
    public boolean isEmpty() {
        return animeRecord == null && mangaRecord == null;
    }

    /*
     * Checks if this record is in our list
     */
    public boolean isAdded() {
        return !isEmpty() && (ListType.ANIME.equals(type) ? animeRecord.getWatchedStatus() != null : mangaRecord.getReadStatus() != null);
    }

    /*
     * Set refreshing on all SwipeRefreshViews
     */
    public void setRefreshing(Boolean show) {
        general.swipeRefresh.setRefreshing(show);
        general.swipeRefresh.setEnabled(!show);
        if (details != null) {
            details.swipeRefresh.setRefreshing(show);
            details.swipeRefresh.setEnabled(!show);
        }
    }

    /*
     * Show the dialog with the tag
     */
    public void showDialog(String tag, DialogFragment dialog) {
        FragmentManager fm = getSupportFragmentManager();
        dialog.show(fm, "fragment_" + tag);
    }

    /*
     * Episode picker dialog
     */
    public void onDialogDismissed(int newValue) {
        if (newValue != animeRecord.getWatchedEpisodes()) {
            if (newValue == animeRecord.getEpisodes()) {
                animeRecord.setWatchedStatus(GenericRecord.STATUS_COMPLETED);
            }
            if (newValue == 0) {
                animeRecord.setWatchedStatus(Anime.STATUS_PLANTOWATCH);
            }

            animeRecord.setWatchedEpisodes(newValue);
            animeRecord.setDirty(true);
            setText();
            setMenu();
        }
    }

    /*
     * Set the right menu items.
     */
    public void setMenu() {
        if (menu != null) {
            if (isAdded()) {
                menu.findItem(R.id.action_Remove).setVisible(true);
                menu.findItem(R.id.action_addToList).setVisible(false);
            } else {
                menu.findItem(R.id.action_Remove).setVisible(false);
                menu.findItem(R.id.action_addToList).setVisible(true);
            }
            if (MALApi.isNetworkAvailable(this) && menu.findItem(R.id.action_Remove).isVisible()) {
                menu.findItem(R.id.action_Remove).setVisible(true);
            } else {
                menu.findItem(R.id.action_Remove).setVisible(false);
            }
        }
    }

    /*
     * Add record to list
     */
    public void addToList() {
        if (type.equals(ListType.ANIME)) {
            animeRecord.setCreateFlag(true);
            animeRecord.setWatchedStatus(Anime.STATUS_WATCHING);
            animeRecord.setDirty(true);
        } else {
            mangaRecord.setCreateFlag(true);
            mangaRecord.setReadStatus(Manga.STATUS_READING);
            mangaRecord.setDirty(true);
        }
        setMenu();
    }

    /*
     * Open the share dialog
     */
    public void Share() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, makeShareText());
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    /*
     * Make the share text for the share dialog
     */
    public String makeShareText() {
        String shareText = pref.getCustomShareText();
        shareText = shareText.replace("$title;", getSupportActionBar().getTitle());
        shareText = shareText.replace("$link;", "http://myanimelist.net/" + type.toString().toLowerCase(Locale.US) + "/" + Integer.toString(recordID));
        shareText = shareText + getResources().getString(R.string.customShareText_fromAtarashii);
        return shareText;
    }

    /*
     * Check if the database contains the record.
     *
     * If it does contains the record it will set it.
     */
    private boolean getRecordFromDB() {
        DatabaseManager dbMan = new DatabaseManager(this);
        if (type.equals(ListType.ANIME)) {
            animeRecord = dbMan.getAnime(recordID, username);
            return animeRecord != null;
        } else {
            mangaRecord = dbMan.getManga(recordID, username);
            return mangaRecord != null;
        }
    }

    /*
     * Check if  the record contains all the details.
     *
     * Without this function the fragments will call setText while it isn't loaded.
     * This will cause a nullpointerexception.
     */
    public boolean isDone() {
        return (!isEmpty()) && (type.equals(ListType.ANIME) ? animeRecord.getSynopsis() != null : mangaRecord.getSynopsis() != null);
    }

    /*
     * Get the translation from strings.xml
     */
    private String getStringFromResourceArray(int resArrayId, int notFoundStringId, int index) {
        Resources res = getResources();
        try {
            String[] types = res.getStringArray(resArrayId);
            if (index < 0 || index >= types.length) // make sure to have a valid array index
                return res.getString(notFoundStringId);
            else
                return types[index];
        } catch (Resources.NotFoundException e) {
            return res.getString(notFoundStringId);
        }
    }

    /*
     * Get the anime or manga mediatype translations
     */
    public String getTypeString(int typesInt) {
        if (type.equals(ListType.ANIME))
            return getStringFromResourceArray(R.array.mediaType_Anime, R.string.unknown, typesInt);
        else
            return getStringFromResourceArray(R.array.mediaType_Manga, R.string.unknown, typesInt);
    }

    /*
     * Get the anime or manga status translations
     */
    public String getStatusString(int statusInt) {
        if (type.equals(ListType.ANIME))
            return getStringFromResourceArray(R.array.mediaStatus_Anime, R.string.unknown, statusInt);
        else
            return getStringFromResourceArray(R.array.mediaStatus_Manga, R.string.unknown, statusInt);
    }

    /*
     * Get the anime or manga genre translations
     */
    public ArrayList<String> getGenresString(ArrayList<Integer> genresInt) {
        ArrayList<String> genres = new ArrayList<String>();
        for (Integer genreInt : genresInt) {
            genres.add(getStringFromResourceArray(R.array.genresArray, R.string.unknown, genreInt));
        }
        return genres;
    }

    /*
     * Get the anime or manga classification translations
     */
    public String getClassificationString(Integer classificationInt) {
        return getStringFromResourceArray(R.array.classificationArray, R.string.unknown, classificationInt);
    }

    public String getUserStatusString(int statusInt) {
        return getStringFromResourceArray(R.array.mediaStatus_User, R.string.unknown, statusInt);
    }

    /*
     * Get the records (Anime/Manga)
     *
     * Try to fetch them from the Database first to get reading/watching details.
     * If the record doesn't contains a synopsis this method will get it.
     */
    public void getRecord(boolean forceUpdate) {
        setRefreshing(true);
        boolean loaded = false;
        if (!forceUpdate || !MALApi.isNetworkAvailable(this)) {
            if (getRecordFromDB()) {
                setText();
                if (isDone()) {
                    loaded = true;
                    setRefreshing(false);
                }
            }
        }
        if (MALApi.isNetworkAvailable(this)) {
            if (!loaded || forceUpdate) {
                Bundle data = new Bundle();
                boolean saveDetails = username != null && !username.equals("") && isAdded();
                if (saveDetails) {
                    data.putSerializable("record", type.equals(ListType.ANIME) ? animeRecord : mangaRecord);
                } else {
                    data.putInt("recordID", recordID);
                }
                new NetworkTask(saveDetails ? TaskJob.GETDETAILS : TaskJob.GET, type, this, data, this, this).execute();
            }
        } else {
            setRefreshing(false);
            if (!loaded) {
                Crouton.makeText(this, R.string.crouton_error_noConnectivity, Style.ALERT).show();
            }
        }
    }

    public void onStatusDialogDismissed(String currentStatus) {
        if (type.equals(ListType.ANIME)) {
            if (Anime.STATUS_WATCHING.equals(currentStatus)) {
                animeRecord.setWatchedStatus(Anime.STATUS_WATCHING);
            }
            if (GenericRecord.STATUS_COMPLETED.equals(currentStatus)) {
                animeRecord.setWatchedStatus(Anime.STATUS_COMPLETED);
                if (animeRecord.getEpisodes() != 0)
                    animeRecord.setWatchedEpisodes(animeRecord.getEpisodes());
            }
            if (GenericRecord.STATUS_ONHOLD.equals(currentStatus)) {
                animeRecord.setWatchedStatus(Anime.STATUS_ONHOLD);
            }
            if (GenericRecord.STATUS_DROPPED.equals(currentStatus)) {
                animeRecord.setWatchedStatus(Anime.STATUS_DROPPED);
            }
            if ((Anime.STATUS_PLANTOWATCH.equals(currentStatus))) {
                animeRecord.setWatchedStatus(Anime.STATUS_PLANTOWATCH);
            }
            animeRecord.setDirty(true);
        } else {
            if (Manga.STATUS_READING.equals(currentStatus)) {
                mangaRecord.setReadStatus(Manga.STATUS_READING);
            }
            if (GenericRecord.STATUS_COMPLETED.equals(currentStatus)) {
                mangaRecord.setReadStatus(Manga.STATUS_COMPLETED);
                if (mangaRecord.getChapters() != 0)
                    mangaRecord.setChaptersRead(mangaRecord.getChapters());
                if (mangaRecord.getVolumes() != 0)
                    mangaRecord.setVolumesRead(mangaRecord.getVolumes());
            }
            if (GenericRecord.STATUS_ONHOLD.equals(currentStatus)) {
                mangaRecord.setReadStatus(Manga.STATUS_ONHOLD);
            }
            if (GenericRecord.STATUS_DROPPED.equals(currentStatus)) {
                mangaRecord.setReadStatus(Manga.STATUS_DROPPED);
            }
            if (Manga.STATUS_PLANTOREAD.equals(currentStatus)) {
                mangaRecord.setReadStatus(Manga.STATUS_PLANTOREAD);
            }
            mangaRecord.setDirty(true);
        }
        setText();
    }


    public void onMangaDialogDismissed(int value, int value2) {
        if (value != mangaRecord.getChaptersRead()) {
            if (value == mangaRecord.getChapters() && mangaRecord.getChapters() != 0) {
                mangaRecord.setReadStatus(GenericRecord.STATUS_COMPLETED);
            }
            if (value == 0) {
                mangaRecord.setReadStatus(Manga.STATUS_PLANTOREAD);
            }
            mangaRecord.setChaptersRead(value);
            mangaRecord.setDirty(true);
        }

        if (value2 != mangaRecord.getVolumesRead()) {
            if (value2 == mangaRecord.getVolumes()) {
                mangaRecord.setReadStatus(GenericRecord.STATUS_COMPLETED);
            }
            if (value2 == 0) {
                mangaRecord.setReadStatus(Manga.STATUS_PLANTOREAD);
            }
            mangaRecord.setVolumesRead(value2);
            mangaRecord.setDirty(true);
        }

        if (value2 == 9001 || value == 9001) {
            Crouton.makeText(this, getString(R.string.crouton_info_Max_Counter), Style.INFO).show();
        }

        setText();
        setMenu();
    }

    public void onRemoveConfirmed() {
        if (type.equals(ListType.ANIME)) {
            animeRecord.setDirty(true);
            animeRecord.setDeleteFlag(true);
        } else {
            mangaRecord.setDirty(true);
            mangaRecord.setDeleteFlag(true);
        }
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle State) {
        super.onSaveInstanceState(State);
        State.putSerializable("anime", animeRecord);
        State.putSerializable("manga", mangaRecord);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail_view, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        setMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_Share:
                Share();
                break;
            case R.id.action_Remove:
                showDialog("removeConfirmation", new RemoveConfirmationDialogFragment());
                break;
            case R.id.action_addToList:
                addToList();
                break;
            case R.id.action_ViewMALPage:
                Uri malurl = Uri.parse("http://myanimelist.net/" + type.toString().toLowerCase(Locale.US) + "/" + recordID + "/");
                startActivity(new Intent(Intent.ACTION_VIEW, malurl));
                break;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            if (type.equals(ListType.ANIME)) {
                if (animeRecord.getDirty() && !animeRecord.getDeleteFlag()) {
                    new WriteDetailTask(type, TaskJob.UPDATE, this, this).execute(animeRecord);
                } else if (animeRecord.getDeleteFlag()) {
                    new WriteDetailTask(type, TaskJob.FORCESYNC, this, this).execute(animeRecord);
                }
            } else if (type.equals(ListType.MANGA)) {
                if (mangaRecord.getDirty() && !mangaRecord.getDeleteFlag()) {
                    new WriteDetailTask(type, TaskJob.UPDATE, this, this).execute(mangaRecord);
                } else if (mangaRecord.getDeleteFlag()) {
                    new WriteDetailTask(type, TaskJob.FORCESYNC, this, this).execute(mangaRecord);
                }
            }
        } catch (Exception e) {
            Log.e("MALX", "Error updating record: " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // received Android Beam?
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()))
            processIntent(getIntent());
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void processIntent(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            // only one message sent during the beam
            NdefMessage msg = (NdefMessage) rawMsgs[0];
            String message = new String(msg.getRecords()[0].getPayload());
            String[] splitmessage = message.split(":", 2);
            if (splitmessage.length == 2) {
                try {
                    type = ListType.valueOf(splitmessage[0].toUpperCase(Locale.US));
                    recordID = Integer.parseInt(splitmessage[1]);
                    getRecord(false);
                } catch (NumberFormatException e) {
                    finish();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setupBeam() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                // setup beam functionality (if NFC is available)
                NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
                if (mNfcAdapter == null) {
                    Log.i("MALX", "NFC not available");
                } else {
                    // Register NFC callback
                    String message_str = type.toString() + ":" + String.valueOf(recordID);
                    NdefMessage message = new NdefMessage(new NdefRecord[]{
                            new NdefRecord(
                                    NdefRecord.TNF_MIME_MEDIA,
                                    "application/net.somethingdreadful.MAL".getBytes(Charset.forName("US-ASCII")),
                                    new byte[0], message_str.getBytes(Charset.forName("US-ASCII"))),
                            NdefRecord.createApplicationRecord(getPackageName())
                    });
                    mNfcAdapter.setNdefPushMessage(message, this);
                }
            }
        } catch (Exception e) {
            Log.e("MALX", "error at setupBeam: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked") // Don't panic, we handle possible class cast exceptions
    @Override
    public void onNetworkTaskFinished(Object result, TaskJob job, ListType type, Bundle data, boolean cancelled) {
        try {
            if (type == ListType.ANIME) {
                animeRecord = (Anime) result;
                if (isAdded())
                    animeRecord.setDirty(true);
            } else {
                mangaRecord = (Manga) result;
                if (isAdded())
                    mangaRecord.setDirty(true);
            }
            setRefreshing(false);

            setText();
        } catch (ClassCastException e) {
            Log.e("MALX", "error reading result because of invalid result class: " + result.getClass().toString());
            Crouton.makeText(this, R.string.crouton_error_DetailsError, Style.ALERT).show();
        }
    }

    @Override
    public void onNetworkTaskError(TaskJob job, ListType type, Bundle data, boolean cancelled) {
        Crouton.makeText(this, R.string.crouton_error_DetailsError, Style.ALERT).show();
    }

    @Override
    public void onAPIAuthenticationError(ListType type, TaskJob job) {
        showDialog("updatePassword", new UpdatePasswordDialogFragment());
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        actionbar.setSelectedNavigationItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /*
     * Set the fragment to future use
     */
    public void setGeneral(DetailViewGeneral general) {
        this.general = general;
        if (isEmpty())
            getRecord(false);
        else
            setText();
    }

    /*
     * Set the fragment to future use
     */
    public void setDetails(DetailViewDetails details) {
        this.details = details;
    }
}
