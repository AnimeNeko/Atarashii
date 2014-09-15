package net.somethingdreadful.MAL;

import android.annotation.TargetApi;
import android.content.Context;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.GenericRecord;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.dialog.EpisodesPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.MangaPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.RemoveConfirmationDialogFragment;
import net.somethingdreadful.MAL.dialog.StatusPickerDialogFragment;
import net.somethingdreadful.MAL.dialog.UpdatePasswordDialogFragment;
import net.somethingdreadful.MAL.tasks.APIAuthenticationErrorListener;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.NetworkTaskCallbackListener;
import net.somethingdreadful.MAL.tasks.TaskJob;
import net.somethingdreadful.MAL.tasks.WriteDetailTask;

import org.apache.commons.lang3.text.WordUtils;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.DialogFragment;
import org.holoeverywhere.widget.TextView;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Locale;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class DetailView extends Activity implements Serializable, OnRatingBarChangeListener, NetworkTaskCallbackListener, Card.onCardClickListener, APIAuthenticationErrorListener, SwipeRefreshLayout.OnRefreshListener {

    public ListType type;
    public Anime animeRecord;
    public Manga mangaRecord;
    SwipeRefreshLayout swipeRefresh;
    int recordID;
    Context context;
    PrefManager pref;
    Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detailview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((Card) findViewById(R.id.detailCoverImage)).setContent(R.layout.card_detailview_image);
        ((Card) findViewById(R.id.synopsis)).setContent(R.layout.card_detailview_synopsis);
        ((Card) findViewById(R.id.mediainfo)).setContent(R.layout.card_detailview_mediainfo);
        ((Card) findViewById(R.id.status)).setContent(R.layout.card_detailview_status);
        ((Card) findViewById(R.id.progress)).setContent(R.layout.card_detailview_progress);
        ((Card) findViewById(R.id.rating)).setContent(R.layout.card_detailview_rating);

        type = (ListType) getIntent().getSerializableExtra("recordType");
        if (type.equals(ListType.ANIME))
            animeRecord = (Anime) getIntent().getSerializableExtra("record");
        else
            mangaRecord = (Manga) getIntent().getSerializableExtra("record");
        recordID = (type.equals(ListType.ANIME) ? animeRecord.getId() : mangaRecord.getId());

        context = getApplicationContext();
        pref = new PrefManager(context);

        setCard();
        setListener();
        if (savedInstanceState == null) {
            setText();
        } else {
            animeRecord = (Anime) savedInstanceState.getSerializable("anime");
            mangaRecord = (Manga) savedInstanceState.getSerializable("manga");
            setText();
        }
    }

    @Override
    public void onRefresh() {
        getRecord();
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
        if (animeRecord != null || mangaRecord != null)
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
                showRemoveDialog();
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

    /*
     * set all the ClickListeners
     */
    public void setListener() {
        ((RatingBar) findViewById(R.id.MyScoreBar)).setOnRatingBarChangeListener(this);
        ((Card) findViewById(R.id.status)).setCardClickListener(this);
        ((Card) findViewById(R.id.progress)).setCardClickListener(this);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorScheme(R.color.holo_blue_bright, R.color.holo_green_light, R.color.holo_orange_light, R.color.holo_red_light);
        swipeRefresh.setEnabled(true);
    }

    /*
     * Manage the progress card
     */
    public void setCard() {
        if (type != null && type.equals(ListType.ANIME)) {
            TextView progresslabel1 = (TextView) findViewById(R.id.progresslabel1);
            progresslabel1.setText(getString(R.string.card_content_episodes));
            TextView progresslabel2 = (TextView) findViewById(R.id.progresslabel2);
            progresslabel2.setVisibility(View.GONE);
            TextView progresslabel2Current = (TextView) findViewById(R.id.progresslabel2Current);
            progresslabel2Current.setVisibility(View.GONE);
            TextView progresslabel2Total = (TextView) findViewById(R.id.progresslabel2Total);
            progresslabel2Total.setVisibility(View.GONE);
        }
    }

    /*
     * set the right menu items.
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
            if (MALApi.isNetworkAvailable(context) && menu.findItem(R.id.action_Remove).isVisible()) {
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
        setText();
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

    @Override
    public void onPause() {
        super.onPause();

        try {
            if (type.equals(ListType.ANIME)) {
                if (animeRecord.getDirty() && !animeRecord.getDeleteFlag()) {
                    new WriteDetailTask(type, TaskJob.UPDATE, context, this).execute(animeRecord);
                } else if (animeRecord.getDeleteFlag()) {
                    new WriteDetailTask(type, TaskJob.FORCESYNC, context, this).execute(animeRecord);
                }
            } else if (type.equals(ListType.MANGA)) {
                if (mangaRecord.getDirty() && !mangaRecord.getDeleteFlag()) {
                    new WriteDetailTask(type, TaskJob.UPDATE, context, this).execute(mangaRecord);
                } else if (mangaRecord.getDeleteFlag()) {
                    new WriteDetailTask(type, TaskJob.FORCESYNC, context, this).execute(mangaRecord);
                }
            }
        } catch (Exception e) {
            Log.e("MALX", "Error updating record: " + e.getMessage());
        }
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
     * Get the records (Anime/Manga)
     */
    public void getRecord() {
        if (MALApi.isNetworkAvailable(context)) {
            Bundle data = new Bundle();
            data.putInt("recordID", recordID);
            new NetworkTask(TaskJob.GET, type, context, data, this, this).execute();
        } else {
            setText();
            swipeRefresh.setRefreshing(false);
        }
    }

    /*
     * Checks if this record is in our list
     */
    public boolean isAdded() {
        if (ListType.ANIME.equals(type)) {
            return animeRecord.getWatchedStatus() != null;
        } else {
            return mangaRecord.getReadStatus() != null;
        }
    }

    /*
     * show the dialog with the tag
     */
    public void showDialog(String tag, DialogFragment dialog) {
        FragmentManager fm = getSupportFragmentManager();
        dialog.show(fm, "fragment_" + tag);
    }

    /*
     * Show the RecordRemovalDialog
     */
    public void showRemoveDialog() {
        FragmentManager fm = getSupportFragmentManager();
        RemoveConfirmationDialogFragment rcdf = new RemoveConfirmationDialogFragment();
        rcdf.show(fm, "fragment_RemoveConfirmation");
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
                    setCard();
                    getRecord();
                } catch (NumberFormatException e) {
                    finish();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setupBeam() {
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
            setText();
            swipeRefresh.setRefreshing(false);
        } catch (ClassCastException e) {
            Log.e("MALX", "error reading result because of invalid result class: " + result.getClass().toString());
            Crouton.makeText(this, R.string.crouton_error_DetailsError, Style.ALERT).show();
        }
    }

    @Override
    public void onNetworkTaskError(TaskJob job, ListType type, Bundle data, boolean cancelled) {
        Crouton.makeText(this, R.string.crouton_error_DetailsError, Style.ALERT).show();
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

    private String getAnimeTypeString(int typeInt) {
        return getStringFromResourceArray(R.array.mediaType_Anime, R.string.unknown, typeInt);
    }

    private String getAnimeStatusString(int statusInt) {
        return getStringFromResourceArray(R.array.mediaStatus_Anime, R.string.unknown, statusInt);
    }

    private String getMangaTypeString(int typeInt) {
        return getStringFromResourceArray(R.array.mediaType_Manga, R.string.unknown, typeInt);
    }

    private String getMangaStatusString(int statusInt) {
        return getStringFromResourceArray(R.array.mediaStatus_Manga, R.string.unknown, statusInt);
    }

    private String getUserStatusString(int statusInt) {
        return getStringFromResourceArray(R.array.mediaStatus_User, R.string.unknown, statusInt);
    }

    /*
     * Place all the text in the right textview
     */
    public void setText() {
        if (type == null || (animeRecord == null && mangaRecord == null)) // not enough data to do anything
            return;
        GenericRecord record;
        setMenu();
        TextView status = (TextView) findViewById(R.id.cardStatusLabel);
        TextView mediaType = (TextView) findViewById(R.id.mediaType);
        TextView mediaStatus = (TextView) findViewById(R.id.mediaStatus);
        if (type.equals(ListType.ANIME)) {
            record = animeRecord;
            Card statusCard = (Card) findViewById(R.id.status);
            if (animeRecord.getWatchedStatus() != null) {
                status.setText(WordUtils.capitalize(getUserStatusString(animeRecord.getWatchedStatusInt())));
                statusCard.setVisibility(View.VISIBLE);
            } else {
                statusCard.setVisibility(View.GONE);
            }
            mediaType.setText(getAnimeTypeString(animeRecord.getTypeInt()));
            mediaStatus.setText(getAnimeStatusString(animeRecord.getStatusInt()));
        } else {
            record = mangaRecord;
            Card statusCard = (Card) findViewById(R.id.status);
            if (mangaRecord.getReadStatus() != null) {
                status.setText(WordUtils.capitalize(getUserStatusString(mangaRecord.getReadStatusInt())));
                statusCard.setVisibility(View.VISIBLE);
            } else {
                statusCard.setVisibility(View.GONE);
            }
            mediaType.setText(getMangaTypeString(mangaRecord.getTypeInt()));
            mediaStatus.setText(getMangaStatusString(mangaRecord.getStatusInt()));
        }

        TextView synopsis = (TextView) findViewById(R.id.SynopsisContent);
        if (record.getSynopsis() == null) {
            if (MALApi.isNetworkAvailable(context)) {
                Bundle data = new Bundle();
                data.putSerializable("record", type.equals(ListType.ANIME) ? animeRecord : mangaRecord);
                new NetworkTask(TaskJob.GETDETAILS, type, context, data, this, this).execute();
            } else {
                synopsis.setText(getString(R.string.crouton_error_noConnectivity));
            }
        } else {
            synopsis.setMovementMethod(LinkMovementMethod.getInstance());
            synopsis.setText(Html.fromHtml(record.getSynopsis()));
        }

        Card progress = (Card) findViewById(R.id.progress);
        if (isAdded()) {
            progress.setVisibility(View.VISIBLE);
        } else {
            progress.setVisibility(View.GONE);
        }

        if (type.equals(ListType.ANIME)) {
            TextView progresslabel1Current = (TextView) findViewById(R.id.progresslabel1Current);
            progresslabel1Current.setText(Integer.toString(animeRecord.getWatchedEpisodes()));
            TextView progresslabel1Total = (TextView) findViewById(R.id.progresslabel1Total);
            if (animeRecord.getEpisodes() == 0)
                progresslabel1Total.setText("/?");
            else
                progresslabel1Total.setText("/" + Integer.toString(animeRecord.getEpisodes()));
        } else {
            TextView progresslabel1Current = (TextView) findViewById(R.id.progresslabel1Current);
            progresslabel1Current.setText(Integer.toString(mangaRecord.getVolumesRead()));
            TextView progresslabel1Total = (TextView) findViewById(R.id.progresslabel1Total);
            if (mangaRecord.getVolumes() == 0)
                progresslabel1Total.setText("/?");
            else
                progresslabel1Total.setText("/" + Integer.toString(mangaRecord.getVolumes()));

            TextView progresslabel2Current = (TextView) findViewById(R.id.progresslabel2Current);
            progresslabel2Current.setText(Integer.toString(mangaRecord.getChaptersRead()));
            TextView progresslabel2Total = (TextView) findViewById(R.id.progresslabel2Total);
            if (mangaRecord.getChapters() == 0)
                progresslabel2Total.setText("/?");
            else
                progresslabel2Total.setText("/" + Integer.toString(mangaRecord.getChapters()));
        }

        TextView MALScoreLabel = (TextView) findViewById(R.id.MALScoreLabel);
        RatingBar MALScoreBar = (RatingBar) findViewById(R.id.MALScoreBar);
        if (record.getMembersScore() == 0) {
            MALScoreBar.setVisibility(View.GONE);
            MALScoreLabel.setVisibility(View.GONE);
        } else {
            MALScoreBar.setVisibility(View.VISIBLE);
            MALScoreLabel.setVisibility(View.VISIBLE);
            MALScoreBar.setRating(record.getMembersScore() / 2);
        }

        RatingBar MyScoreBar = (RatingBar) findViewById(R.id.MyScoreBar);
        TextView MyScoreLabel = (TextView) findViewById(R.id.MyScoreLabel);
        if (isAdded()) {
            MyScoreLabel.setVisibility(View.VISIBLE);
            MyScoreBar.setVisibility(View.VISIBLE);
            MyScoreBar.setRating((float) record.getScore() / 2);
        } else {
            MyScoreLabel.setVisibility(View.GONE);
            MyScoreBar.setVisibility(View.GONE);
        }
        (findViewById(R.id.Image)).setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Picasso.with(context)
                .load(record.getImageUrl())
                .error(R.drawable.cover_error)
                .placeholder(R.drawable.cover_loading)
                .centerInside()
                .fit()
                .into((ImageView) findViewById(R.id.Image), new Callback() {
                    @Override
                    public void onSuccess() {
                        ((Card) findViewById(R.id.detailCoverImage)).wrapWidth(false);
                    }

                    @Override
                    public void onError() {
                    }
                });

        getSupportActionBar().setTitle(record.getTitle());
        ((Card) findViewById(R.id.detailCoverImage)).Header.setText(record.getTitle());
        setupBeam();
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        if (type.equals(ListType.ANIME)) {
            if (animeRecord != null) {
                animeRecord.setScore((int) (rating * 2));
                animeRecord.setDirty(true);
            }
        } else {
            if (animeRecord != null) {
                mangaRecord.setScore((int) (rating * 2));
                mangaRecord.setDirty(true);
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
    public void onCardClickListener(int res) {
        if (res == R.id.status) {
            showDialog("statusPicker", new StatusPickerDialogFragment());
        } else if (res == R.id.progress) {
            if (type.equals(ListType.ANIME)) {
                showDialog("episodes", new EpisodesPickerDialogFragment());
            } else {
                showDialog("manga", new MangaPickerDialogFragment());
            }
        }
    }

    @Override
    public void onAPIAuthenticationError(ListType type, TaskJob job) {
        showDialog("updatePassword", new UpdatePasswordDialogFragment());
    }
}