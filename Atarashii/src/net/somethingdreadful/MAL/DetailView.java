package net.somethingdreadful.MAL;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.DetailViewPagerAdapter;
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

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;

public class DetailView extends ActionBarActivity implements Serializable, NetworkTaskCallbackListener, APIAuthenticationErrorListener, SwipeRefreshLayout.OnRefreshListener {

    public ListType type;
    public Anime animeRecord;
    public Manga mangaRecord;
    public String username;
    public DetailViewGeneral general;
    public DetailViewDetails details;
    public DetailViewPersonal personal;
    DetailViewPagerAdapter PageAdapter;
    int recordID;
    private ActionBar actionBar;
    private ViewPager viewPager;
    private ViewFlipper viewFlipper;
    private Menu menu;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detailview);
        actionBar = getSupportActionBar();
        context = getApplicationContext();
        username = getIntent().getStringExtra("username");
        type = (ListType) getIntent().getSerializableExtra("recordType");
        recordID = getIntent().getIntExtra("recordID", -1);

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        PageAdapter = new DetailViewPagerAdapter(getFragmentManager(), this);
        viewPager.setAdapter(PageAdapter);

        if (savedInstanceState != null) {
            animeRecord = (Anime) savedInstanceState.getSerializable("anime");
            mangaRecord = (Manga) savedInstanceState.getSerializable("manga");
        }
    }

    /*
     * Set text in all fragments
     */
    public void setText() {
        try {
            actionBar.setTitle(type == ListType.ANIME ? animeRecord.getTitle() : mangaRecord.getTitle());
            if (general != null) {
                general.setText();
            }
            if (details != null && !isEmpty()) {
                details.setText();
            }
            if (personal != null && !isEmpty()) {
                personal.setText();
            }
            if (!isEmpty()) {
                setupBeam();
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DetailView.setText(): " + e.getMessage());
            if (!(e instanceof IllegalStateException))
                Crashlytics.logException(e);
        }
        setMenu();
    }

    /*
     * show or hide the personal card
     */
    public void hidePersonal(boolean hide) {
        PageAdapter.count = hide ? 2 : 3;
        PageAdapter.notifyDataSetChanged();
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
        if (general != null) {
            general.swipeRefresh.setRefreshing(show);
            general.swipeRefresh.setEnabled(!show);
        }
        if (details != null) {
            details.swipeRefresh.setRefreshing(show);
            details.swipeRefresh.setEnabled(!show);
        }
        if (personal != null) {
            personal.swipeRefresh.setRefreshing(show);
            personal.swipeRefresh.setEnabled(!show);
        }
    }

    /*
     * Show the dialog with the tag
     */
    public void showDialog(String tag, DialogFragment dialog) {
        FragmentManager fm = getFragmentManager();
        dialog.show(fm, "fragment_" + tag);
    }

    /*
     * Show the dialog with the tag
     */
    public void showDialog(String tag, DialogFragment dialog, Bundle args) {
        FragmentManager fm = getFragmentManager();
        dialog.setArguments(args);
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
        }
    }

    /*
     * Date picker dialog
     */
    public void onDialogDismissed(boolean startDate, int year, int month, int day) {
        String monthString = Integer.toString(month);
        if (monthString.length() == 1)
            monthString = "0" + monthString;

        String dayString = Integer.toString(day);
        if (dayString.length() == 1)
            dayString = "0" + dayString;
        if (type.equals(ListType.ANIME)) {
            if (startDate)
                animeRecord.setWatchingStart(Integer.toString(year) + "-" + monthString + "-" + dayString);
            else
                animeRecord.setWatchingEnd(Integer.toString(year) + "-" + monthString + "-" + dayString);
            animeRecord.setDirty(true);
        } else {
            if (startDate)
                mangaRecord.setReadingStart(Integer.toString(year) + "-" + monthString + "-" + dayString);
            else
                mangaRecord.setReadingEnd(Integer.toString(year) + "-" + monthString + "-" + dayString);
            mangaRecord.setDirty(true);
        }
        setText();
    }

    /*
     * Set the right menu items.
     */
    public void setMenu() {
        if (menu != null) {
            if (isAdded()) {
                menu.findItem(R.id.action_Remove).setVisible(!isEmpty() && MALApi.isNetworkAvailable(this));
                menu.findItem(R.id.action_addToList).setVisible(false);
            } else {
                menu.findItem(R.id.action_Remove).setVisible(false);
                menu.findItem(R.id.action_addToList).setVisible(!isEmpty());
            }
            menu.findItem(R.id.action_Share).setVisible(!isEmpty());
            menu.findItem(R.id.action_ViewMALPage).setVisible(!isEmpty());
        }
    }

    /*
     * Add record to list
     */
    public void addToList() {
        if (!isEmpty()) {
            if (type.equals(ListType.ANIME)) {
                animeRecord.setCreateFlag(true);
                animeRecord.setWatchedStatus(Anime.STATUS_WATCHING);
                animeRecord.setDirty(true);
            } else {
                mangaRecord.setCreateFlag(true);
                mangaRecord.setReadStatus(Manga.STATUS_READING);
                mangaRecord.setDirty(true);
            }
            setText();
        }
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
        String shareText = PrefManager.getCustomShareText();
        shareText = shareText.replace("$title;", actionBar.getTitle());
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
            Crashlytics.logException(e);
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
        toggleLoadingIndicator(isEmpty());
        actionBar.setTitle(R.string.layout_card_loading);
        boolean loaded = false;
        if (!forceUpdate || !MALApi.isNetworkAvailable(this)) {
            if (getRecordFromDB()) {
                setText();
                setRefreshing(false);
                if (isDone()) {
                    loaded = true;
                    toggleLoadingIndicator(false);
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
            toggleLoadingIndicator(false);
            setRefreshing(false);
            if (isEmpty()) {
                actionBar.setTitle("");
                toggleNoNetworkCard(true);
            }
        }
    }

    public void onStatusDialogDismissed(String currentStatus) {
        if (type.equals(ListType.ANIME)) {
            animeRecord.setWatchedStatus(currentStatus);
            if (GenericRecord.STATUS_COMPLETED.equals(currentStatus)) {
                if (animeRecord.getEpisodes() != 0)
                    animeRecord.setWatchedEpisodes(animeRecord.getEpisodes());
            }
            animeRecord.setDirty(true);
        } else {
            mangaRecord.setReadStatus(currentStatus);
            if (GenericRecord.STATUS_COMPLETED.equals(currentStatus)) {
                if (mangaRecord.getChapters() != 0)
                    mangaRecord.setChaptersRead(mangaRecord.getChapters());
                if (mangaRecord.getVolumes() != 0)
                    mangaRecord.setVolumesRead(mangaRecord.getVolumes());
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
            case R.id.action_copy:
                if (animeRecord != null || mangaRecord != null) {
                    android.content.ClipboardManager clipBoard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clipData = android.content.ClipData.newPlainText("Atarashii", type == ListType.ANIME ? animeRecord.getTitle() : mangaRecord.getTitle());
                    clipBoard.setPrimaryClip(clipData);
                } else {
                    Toast.makeText(context, R.string.toast_info_hold_on, Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (animeRecord == null && mangaRecord == null)
            return; // nothing to do

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
            Crashlytics.log(Log.ERROR, "MALX", "DetailView.onPause(): " + e.getMessage());
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // received Android Beam?
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()))
            processIntent(getIntent());
    }

    private void processIntent(Intent intent) {
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
                Crashlytics.logException(e);
                finish();
            }
        }
    }

    private void setupBeam() {
        try {
            // setup beam functionality (if NFC is available)
            NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (mNfcAdapter == null) {
                Crashlytics.log(Log.INFO, "MALX", "DetailView.setupBeam(): NFC not available");
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
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DetailView.setupBeam(): " + e.getMessage());
            Crashlytics.logException(e);
        }
    }

    @SuppressWarnings("unchecked") // Don't panic, we handle possible class cast exceptions
    @Override
    public void onNetworkTaskFinished(Object result, TaskJob job, ListType type, Bundle data, boolean cancelled) {
        try {
            if (type == ListType.ANIME) {
                animeRecord = (Anime) result;
                if (isAdded() && AccountService.isMAL())
                    animeRecord.setDirty(true);
            } else {
                mangaRecord = (Manga) result;
                if (isAdded() && AccountService.isMAL())
                    mangaRecord.setDirty(true);
            }
            setRefreshing(false);
            toggleLoadingIndicator(false);

            setText();
        } catch (ClassCastException e) {
            Crashlytics.log(Log.ERROR, "MALX", "DetailView.onNetworkTaskFinished(): " + result.getClass().toString());
            Crashlytics.logException(e);
            Toast.makeText(context, R.string.toast_error_DetailsError, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNetworkTaskError(TaskJob job, ListType type, Bundle data, boolean cancelled) {
        Toast.makeText(context, R.string.toast_error_DetailsError, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAPIAuthenticationError(ListType type, TaskJob job) {
        showDialog("updatePassword", new UpdatePasswordDialogFragment());
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

    public void setPersonal(DetailViewPersonal personal) {
        this.personal = personal;
    }

    /*
     * handle the loading indicator
     */
    private void toggleLoadingIndicator(boolean show) {
        if (viewFlipper != null) {
            viewFlipper.setDisplayedChild(show ? 1 : 0);
        }
    }

    /*
     * handle the offline card
     */
    private void toggleNoNetworkCard(boolean show) {
        if (viewFlipper != null) {
            viewFlipper.setDisplayedChild(show ? 2 : 0);
        }
    }

    @Override
    public void onRefresh() {
        getRecord(true);
    }
}
