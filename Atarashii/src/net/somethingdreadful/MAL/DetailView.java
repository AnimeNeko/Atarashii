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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ViewFlipper;

import com.crashlytics.android.Crashlytics;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.DetailViewPagerAdapter;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Manga;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.database.DatabaseManager;
import net.somethingdreadful.MAL.detailView.DetailViewDetails;
import net.somethingdreadful.MAL.detailView.DetailViewGeneral;
import net.somethingdreadful.MAL.detailView.DetailViewPersonal;
import net.somethingdreadful.MAL.detailView.DetailViewReviews;
import net.somethingdreadful.MAL.dialog.ListDialogFragment;
import net.somethingdreadful.MAL.dialog.MessageDialogFragment;
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.RemoveConfirmationDialogFragment;
import net.somethingdreadful.MAL.tasks.APIAuthenticationErrorListener;
import net.somethingdreadful.MAL.tasks.ForumJob;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.TaskJob;
import net.somethingdreadful.MAL.tasks.WriteDetailTask;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;

public class DetailView extends AppCompatActivity implements Serializable, NetworkTask.NetworkTaskListener, APIAuthenticationErrorListener, SwipeRefreshLayout.OnRefreshListener, NumberPickerDialogFragment.onUpdateClickListener, ListDialogFragment.onUpdateClickListener, MessageDialogFragment.onSendClickListener {

    public ListType type;
    public Anime animeRecord;
    public Manga mangaRecord;
    public String username;
    public DetailViewGeneral general;
    public DetailViewDetails details;
    public DetailViewPersonal personal;
    public DetailViewReviews reviews;
    public DetailViewPagerAdapter PageAdapter;
    int recordID;
    private ActionBar actionBar;
    private ViewFlipper viewFlipper;
    private Menu menu;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Theme.setTheme(this, R.layout.activity_detailview, true);
        PageAdapter = (DetailViewPagerAdapter) Theme.setActionBar(this, new DetailViewPagerAdapter(getFragmentManager(), this));

        actionBar = getSupportActionBar();
        context = getApplicationContext();
        username = getIntent().getStringExtra("username");
        type = (ListType) getIntent().getSerializableExtra("recordType");
        recordID = getIntent().getIntExtra("recordID", -1);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);

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
            if (general != null)
                general.setText();
            if (details != null && !isEmpty())
                details.setText();
            if (personal != null && !isEmpty())
                personal.setText();
            if (reviews != null && !isEmpty() && reviews.page == 0)
                reviews.getRecords(1);
            if (!isEmpty()) setupBeam();
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DetailView.setText(): " + e.getMessage());
            if (!(e instanceof IllegalStateException))
                Crashlytics.logException(e);
        }
        setMenu();
    }

    public String nullCheck(String string) {
        return isEmpty(string) ? getString(R.string.unknown) : string;
    }

    public boolean isEmpty(String string) {
        return ((string == null || string.equals("") || string.equals("0-00-00")));
    }

    public String nullCheck(int number) {
        return (number == 0 ? "?" : String.valueOf(number));
    }

    public String getDate(String string) {
        return (isEmpty(string) ? getString(R.string.unknown) : DateTools.parseDate(string, false));
    }

    /*
     * show or hide the personal card
     */
    public void hidePersonal(boolean hide) {
        PageAdapter.hidePersonal(hide);
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
    @Override
    public void onUpdated(int number, int id) {
        switch (id) {
            case R.id.progress1:
                    animeRecord.setWatchedEpisodes(number);
                break;
            case R.id.scorePanel:
                if (isAnime())
                    animeRecord.setScore(number);
                else
                    mangaRecord.setScore(number);
                break;
            case R.id.priorityPanel:
                if (isAnime())
                    animeRecord.setPriority(number);
                else
                    mangaRecord.setPriority(number);
                break;
            case R.id.storagePanel:
                animeRecord.setStorage(number);
                break;
            case R.id.capacityPanel:
                animeRecord.setStorageValue(number);
                break;
            case R.id.downloadPanel:
                animeRecord.setEpsDownloaded(number);
                break;
            case R.id.rewatchPriorityPanel:
                if (isAnime())
                    animeRecord.setRewatchValue(number);
                else
                    mangaRecord.setRereadValue(number);
                break;
            case R.id.countPanel:
                if (isAnime())
                    animeRecord.setRewatchCount(number);
                else
                    mangaRecord.setRereadCount(number);
                break;
        }
        setText();
    }

    @Override
    public void onSendClicked(String message, String subject, ForumJob task, int id) {
        switch (id) {
            case R.id.tagsPanel:
                if (isAnime())
                    animeRecord.setPersonalTags(message);
                else
                    mangaRecord.setPersonalTags(message);
                break;
            case R.id.commentspanel:
                if (isAnime())
                    animeRecord.setNotes(message);
                else
                    mangaRecord.setNotes(message);
                break;
            case R.id.fansubPanel:
                animeRecord.setFansubGroup(message);
                break;
        }
        setText();
    }

    @Override
    public void onCloseClicked(String message) {
    }

    /*
     * Date picker dialog
     */
    public void onDialogDismissed(boolean startDate, int year, int month, int day) {
        String monthString = String.valueOf(month);
        if (monthString.length() == 1)
            monthString = "0" + monthString;

        String dayString = String.valueOf(day);
        if (dayString.length() == 1)
            dayString = "0" + dayString;
        if (type.equals(ListType.ANIME)) {
            if (startDate)
                animeRecord.setWatchingStart(String.valueOf(year) + "-" + monthString + "-" + dayString);
            else
                animeRecord.setWatchingEnd(String.valueOf(year) + "-" + monthString + "-" + dayString);
        } else {
            if (startDate)
                mangaRecord.setReadingStart(String.valueOf(year) + "-" + monthString + "-" + dayString);
            else
                mangaRecord.setReadingEnd(String.valueOf(year) + "-" + monthString + "-" + dayString);
        }
        setText();
    }

    public boolean isAnime() {
        return type.equals(MALApi.ListType.ANIME);
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
                animeRecord.setWatchedStatus(PrefManager.getAddList());
            } else {
                mangaRecord.setCreateFlag(true);
                mangaRecord.setReadStatus(PrefManager.getAddList());
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
        if (AccountService.isMAL())
            shareText = shareText.replace("$link;", "http://myanimelist.net/" + type.toString().toLowerCase(Locale.US) + "/" + String.valueOf(recordID));
        else
            shareText = shareText.replace("$link;", "http://anilist.co/" + type.toString().toLowerCase(Locale.US) + "/" + String.valueOf(recordID));
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
        if (type == null) {
            Crashlytics.log(Log.ERROR, "MALX", "DetailView.getRecordFromDB(): ");
            if (isAdded())
                Theme.Snackbar(this, R.string.toast_error_Records);
            return false;
        } else if (type.equals(ListType.ANIME)) {
            animeRecord = dbMan.getAnime(recordID);
            return animeRecord != null;
        } else {
            mangaRecord = dbMan.getManga(recordID);
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
        ArrayList<String> genres = new ArrayList<>();
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
                setRefreshing(false);
                if (isDone()) {
                    loaded = true;
                    toggleLoadingIndicator(false);
                }
                setText();
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
                new NetworkTask(saveDetails ? TaskJob.GETDETAILS : TaskJob.GET, type, this, data, this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        } else {
            mangaRecord.setReadStatus(currentStatus);
        }
        setText();
    }

    public void onMangaDialogDismissed(int value, int value2) {
        mangaRecord.setChaptersRead(value);
        mangaRecord.setVolumesRead(value2);

        setText();
        setMenu();
    }

    public void onRemoveConfirmed() {
        if (type.equals(ListType.ANIME))
            animeRecord.setDeleteFlag(true);
        else
            mangaRecord.setDeleteFlag(true);
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
            case R.id.action_viewTopic:
                if (MALApi.isNetworkAvailable(context)) {
                    Intent forumActivity = new Intent(this, ForumActivity.class);
                    forumActivity.putExtra("id", recordID);
                    forumActivity.putExtra("listType", type);
                    startActivity(forumActivity);
                } else {
                    Theme.Snackbar(this, R.string.toast_error_noConnectivity);
                }
                break;
            case R.id.action_ViewMALPage:
                Uri malurl;
                if (AccountService.isMAL())
                    malurl = Uri.parse("http://myanimelist.net/" + type.toString().toLowerCase(Locale.US) + "/" + recordID + "/");
                else
                    malurl = Uri.parse("http://anilist.co/" + type.toString().toLowerCase(Locale.US) + "/" + recordID + "/");
                startActivity(new Intent(Intent.ACTION_VIEW, malurl));
                break;
            case R.id.action_copy:
                if (animeRecord != null || mangaRecord != null) {
                    android.content.ClipboardManager clipBoard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clipData = android.content.ClipData.newPlainText("Atarashii", type == ListType.ANIME ? animeRecord.getTitle() : mangaRecord.getTitle());
                    clipBoard.setPrimaryClip(clipData);
                    Theme.Snackbar(this, R.string.toast_info_Copied);
                } else {
                    Theme.Snackbar(this, R.string.toast_info_hold_on);
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
                if (animeRecord.isDirty() && !animeRecord.getDeleteFlag())
                    new WriteDetailTask(type, TaskJob.UPDATE, this, this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, animeRecord);
                else if (animeRecord.getDeleteFlag())
                    new WriteDetailTask(type, TaskJob.FORCESYNC, this, this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, animeRecord);
            } else if (type.equals(ListType.MANGA)) {
                if (mangaRecord.isDirty() && !mangaRecord.getDeleteFlag())
                    new WriteDetailTask(type, TaskJob.UPDATE, this, this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mangaRecord);
                else if (mangaRecord.getDeleteFlag())
                    new WriteDetailTask(type, TaskJob.FORCESYNC, this, this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mangaRecord);
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
            if (type == ListType.ANIME)
                animeRecord = (Anime) result;
            else
                mangaRecord = (Manga) result;
            setRefreshing(false);
            toggleLoadingIndicator(false);

            setText();
        } catch (ClassCastException e) {
            Crashlytics.log(Log.ERROR, "MALX", "DetailView.onNetworkTaskFinished(): " + result.getClass().toString());
            Crashlytics.logException(e);
            Theme.Snackbar(this, R.string.toast_error_DetailsError);
        }
    }

    @Override
    public void onNetworkTaskError(TaskJob job, ListType type, Bundle data, boolean cancelled) {
    }

    @Override
    public void onAPIAuthenticationError(ListType type, TaskJob job) {
        startActivity(new Intent(this, Home.class).putExtra("updatePassword", true));
        finish();
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

    public void setReviews(DetailViewReviews reviews) {
        this.reviews = reviews;
    }

    /*
     * handle the loading indicator
     */
    private void toggleLoadingIndicator(boolean show) {
        if (viewFlipper != null)
            viewFlipper.setDisplayedChild(show ? 1 : 0);

    }

    /*
     * handle the offline card
     */
    private void toggleNoNetworkCard(boolean show) {
        if (viewFlipper != null)
            viewFlipper.setDisplayedChild(show ? 2 : 0);
    }

    @Override
    public void onRefresh() {
        getRecord(true);
    }
}
