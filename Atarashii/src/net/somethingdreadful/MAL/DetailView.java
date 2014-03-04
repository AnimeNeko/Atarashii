package net.somethingdreadful.MAL;

import java.nio.charset.Charset;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.GenericRecord;
import net.somethingdreadful.MAL.api.response.Manga;

import org.apache.commons.lang3.text.WordUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.squareup.picasso.Picasso;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class DetailView extends SherlockFragmentActivity implements DetailsBasicFragment.IDetailsBasicAnimeFragment,
EpisodesPickerDialogFragment.DialogDismissedListener, MangaProgressDialogFragment.MangaDialogDismissedListener,
StatusPickerDialogFragment.StatusDialogDismissedListener, RatingPickerDialogFragment.RatingDialogDismissedListener,
RemoveConfirmationDialogFragment.RemoveConfirmationDialogListener {

    MALManager mManager;
    PrefManager pManager;
    Context context;
    int recordID;
    ActionBar actionBar;
    Anime animeRecord;
    Manga mangaRecord;
    String recordType;
    boolean isAdded = true;

    DetailsBasicFragment bfrag;
    GenericCardFragment SynopsisFragment;
    GenericCardFragment ProgressFragment;
    GenericCardFragment StatusFragment;
    GenericCardFragment ScoreFragment;
    GenericCardFragment WatchStatusFragment;
    FragmentManager fm;
    EpisodesPickerDialogFragment epd;
    MangaProgressDialogFragment mpdf;
    StatusPickerDialogFragment spdf;
    RatingPickerDialogFragment rpdf;
    RemoveConfirmationDialogFragment rcdf;

    TextView SynopsisView;
    TextView RecordTypeView;
    TextView RecordStatusView;
    TextView MyStatusView;
    TextView ProgressCurrentVolumeView;
    TextView ProgressTotalVolumeView;
    TextView ProgressCurrentView;
    TextView ProgressTotalView;
    ImageView CoverImageView;
    RatingBar MALScoreBar;
    RatingBar MyScoreBar;


    Spanned SynopsisText;
    String VolumeProgressText;
    String VolumeTotalText;
    String ProgressText;
    String TotalProgressText;
    String RecordStatusText;
    String RecordTypeText;
    String MyStatusText;
    int MyScore;
    float MemberScore;
    boolean useSecondaryAmounts;
    boolean networkAvailable = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        //Get the recordID, passed in from the calling activity
        recordID = getIntent().getIntExtra("net.somethingdreadful.MAL.recordID", 1);

        //Get the recordType, also passed from calling activity
        //Record type will determine how the detail view lays out itself
        recordType = getIntent().getStringExtra("net.somethingdreadful.MAL.recordType");

        context = getApplicationContext();
        mManager = new MALManager(context);
        pManager = new PrefManager(context);

        networkAvailable = MALApi.isNetworkAvailable(context);


        fm = getSupportFragmentManager();

        bfrag = (DetailsBasicFragment) fm.findFragmentById(R.id.DetailsFragment);

        SynopsisFragment = (GenericCardFragment) fm.findFragmentById(R.id.SynopsisFragment);
        SynopsisFragment.setArgsSensibly(getString(R.string.card_name_synopsis), R.layout.card_layout_content_synopsis, GenericCardFragment.CONTENT_TYPE_SYNOPSIS, false);
        SynopsisFragment.inflateContentStub();


        ProgressFragment = (GenericCardFragment) fm.findFragmentById(R.id.ProgressFragment);
        int card_layout_progress = R.layout.card_layout_progress;
        if ("manga".equals(recordType)) {
            card_layout_progress = R.layout.card_layout_progress_manga;
        }
        ProgressFragment.setArgsSensibly(getString(R.string.card_name_progress), card_layout_progress, GenericCardFragment.CONTENT_TYPE_PROGRESS, true);
        ProgressFragment.inflateContentStub();

        ProgressFragment.getView().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //TODO: Added some kind of clicking feedback, like highlight it or something
                showProgressDialog();
            }

        });

        StatusFragment = (GenericCardFragment) fm.findFragmentById(R.id.StatusFragment);
        StatusFragment.setArgsSensibly(getString(R.string.card_name_stats), R.layout.card_layout_status, GenericCardFragment.CONTENT_TYPE_INFO, false);
        StatusFragment.inflateContentStub();

        ScoreFragment = (GenericCardFragment) fm.findFragmentById(R.id.ScoreFragment);
        ScoreFragment.setArgsSensibly(getString(R.string.card_name_rating), R.layout.card_layout_score, GenericCardFragment.CONTENT_TYPE_SCORE, true);
        ScoreFragment.inflateContentStub();

        ScoreFragment.getView().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showRatingDialog();

            }


        });

        WatchStatusFragment = (GenericCardFragment) fm.findFragmentById(R.id.WatchStatusFragment);
        WatchStatusFragment.setArgsSensibly(getString(R.string.card_name_status), R.layout.card_layout_watchstatus, GenericCardFragment.CONTENT_TYPE_WATCHSTATUS, true);
        WatchStatusFragment.inflateContentStub();

        WatchStatusFragment.getView().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //TODO: Added some kind of clicking feedback, like highlight it or something
                showStatusDialog();
            }

        });

        useSecondaryAmounts = pManager.getUseSecondaryAmountsEnabled();

        // Set up the action bar.
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        setupBeam();
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
                String message_str = recordType + ":" + String.valueOf(recordID);
                NdefMessage message = new NdefMessage(new NdefRecord[] { 
                    new NdefRecord(
                        NdefRecord.TNF_MIME_MEDIA ,
                        "application/net.somethingdreadful.MAL".getBytes(Charset.forName("US-ASCII")),
                        new byte[0], message_str.getBytes(Charset.forName("US-ASCII"))
                    ),
                    NdefRecord.createApplicationRecord("net.somethingdreadful.MAL")
                });
                mNfcAdapter.setNdefPushMessage(message, this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isAdded) {
            getSupportMenuInflater().inflate(R.menu.activity_detail_view, menu);
        }
        else {
            getSupportMenuInflater().inflate(R.menu.activity_detail_view_unrecorded, menu);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if (isAdded) {
    		if (networkAvailable) {
        		menu.findItem(R.id.action_Remove).setVisible(true);
        	}
        	else {
        		menu.findItem(R.id.action_Remove).setVisible(false);
        	}
    	}
    	
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
                Uri malurl = Uri.parse("http://myanimelist.net/" + recordType + "/" + recordID + "/");
                startActivity(new Intent(Intent.ACTION_VIEW, malurl));
                break;
        }

        return true;
    }

    public void showRemoveDialog() {
        rcdf = new RemoveConfirmationDialogFragment();

        if (Build.VERSION.SDK_INT >= 11) {
            rcdf.setStyle(SherlockDialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
        } else {
            rcdf.setStyle(SherlockDialogFragment.STYLE_NORMAL, 0);
        }
        rcdf.show(fm, "fragment_RemoveConfirmationDialog");

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
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ) {
            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            // only one message sent during the beam
            NdefMessage msg = (NdefMessage) rawMsgs[0];
            String message = new String(msg.getRecords()[0].getPayload());
            String[] splitmessage = message.split(":", 2);
            if ( splitmessage.length == 2 ) {
                try {
                    recordType = splitmessage[0];
                    recordID = Integer.parseInt(splitmessage[1]);
                    getDetails();
                } catch (NumberFormatException e) {
                    finish();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            if ("anime".equals(recordType)) {
                if (animeRecord.getDirty()) {
                    writeDetails(animeRecord);
                }
            } else {
                if (mangaRecord.getDirty()) {
                    writeDetails(mangaRecord);
                }
            }
        } catch (NullPointerException ignored) {

        }
    }

    //Called after the basic fragment is finished it's setup, populate data into it
    @Override
    public void basicFragmentReady() {

        CoverImageView = (ImageView) bfrag.getView().findViewById(R.id.detailCoverImage);

        getDetails();
    }

    public void getDetails() {
        new getDetailsTask().execute();
    }

    public void showProgressDialog() // Just a function to keep logic out of the switch statement
    {
        if ("anime".equals(recordType)) {
            showEpisodesWatchedDialog();
        } else {
            showMangaProgressDialog();
        }
    }

    public void showStatusDialog() {
        spdf = new StatusPickerDialogFragment();

        if (Build.VERSION.SDK_INT >= 11) {
            spdf.setStyle(SherlockDialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
        } else {
            spdf.setStyle(SherlockDialogFragment.STYLE_NORMAL, 0);
        }
        spdf.show(fm, "fragment_EditStatusDialog");
    }

    public void showRatingDialog() {
        rpdf = new RatingPickerDialogFragment();

        if (Build.VERSION.SDK_INT >= 11) {
            rpdf.setStyle(SherlockDialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
        } else {
            rpdf.setStyle(SherlockDialogFragment.STYLE_NORMAL, 0);
        }
        rpdf.show(fm, "fragment_EditRatingDialog");
    }

    public void showEpisodesWatchedDialog() {
        //Standard code for setting up a dialog fragment
        //Note we use setStyle to change the theme, the default light styled dialog didn't look good so we use the dark dialog
        epd = new EpisodesPickerDialogFragment();

        if (Build.VERSION.SDK_INT >= 11) {
            epd.setStyle(SherlockDialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
        } else {
            epd.setStyle(SherlockDialogFragment.STYLE_NORMAL, 0);
        }
        epd.show(fm, "fragment_EditEpisodesWatchedDialog");
    }

    public void showMangaProgressDialog() {
        //Standard code for setting up a dialog fragment
        //        Toast.makeText(context, "TODO: Make a MangaProgressFragment", Toast.LENGTH_SHORT).show();
        mpdf = new MangaProgressDialogFragment();
        if (Build.VERSION.SDK_INT >= 11) {
            mpdf.setStyle(SherlockDialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
        } else {
            mpdf.setStyle(SherlockDialogFragment.STYLE_NORMAL, 0);
        }
        mpdf.show(fm, "fragment_EditMangaProgressDialog");
    }

    public class getDetailsTask extends AsyncTask<Void, Boolean, GenericRecord> {
        int mRecordID;
        MALManager mMalManager;
        String internalType;

        @Override
        protected void onPreExecute() {
            mRecordID = recordID;
            mMalManager = mManager;
            internalType = recordType;
        }

        @Override
        protected GenericRecord doInBackground(Void... arg0) {

            if ("anime".equals(internalType)) {
                animeRecord = mMalManager.getAnimeRecord(mRecordID);

                //Basically I just use publishProgress as an easy way to display info we already have loaded sooner
                //This way, I can let the database work happen on the background thread and then immediately display it while
                //the synopsis loads if it hasn't previously been downloaded.
                publishProgress(true);

                if(networkAvailable && animeRecord != null) {
                    if ((animeRecord.getSynopsis() == null) || (animeRecord.getMembersScore() <= 0)) {
                        animeRecord = mMalManager.updateWithDetails(mRecordID, animeRecord);
                    }
                }
                else {

                }



                return animeRecord;
            } else {
                mangaRecord = mMalManager.getMangaRecord(mRecordID);

                //Basically I just use publishProgress as an easy way to display info we already have loaded sooner
                //This way, I can let the database work happen on the background thread and then immediately display it while
                //the synopsis loads if it hasn't previously been downloaded.
                publishProgress(true);

                if(networkAvailable && mangaRecord != null) {
                    if ((mangaRecord.getSynopsis() == null) || (mangaRecord.getMembersScore() <= 0)) {
                        mangaRecord = mMalManager.updateWithDetails(mRecordID, mangaRecord);
                    }
                }

                return mangaRecord;
            }

        }

        @Override
        protected void onProgressUpdate(Boolean... progress) {
            if (animeRecord != null && MALManager.TYPE_ANIME.equals(internalType)) {
                actionBar.setTitle(animeRecord.getTitle());

                Picasso coverImage = Picasso.with(context);

                coverImage
                .load(animeRecord.getImageUrl())
                .error(R.drawable.cover_error)
                .placeholder(R.drawable.cover_loading)
                .fit()
                .into(CoverImageView);

                ProgressText = Integer.toString(animeRecord.getWatchedEpisodes());
                TotalProgressText = Integer.toString(animeRecord.getEpisodes());
                MyStatusText = WordUtils.capitalize(animeRecord.getWatchedStatus());

                ProgressCurrentView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountCurrent);
                ProgressTotalView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountTotal);

                if (ProgressTotalView != null) {
                    ProgressCurrentView.setText(ProgressText);
                    ProgressTotalView.setText("/" + TotalProgressText);

                }

                RecordStatusText = WordUtils.capitalize(animeRecord.getStatus());
                RecordTypeText = WordUtils.capitalize(animeRecord.getType());
                MemberScore = animeRecord.getMembersScore();
                MyScore = animeRecord.getScore();

                RecordTypeView = (TextView) StatusFragment.getView().findViewById(R.id.mediaType);
                RecordStatusView = (TextView) StatusFragment.getView().findViewById(R.id.mediaStatus);

                if (RecordStatusView != null) {
                    RecordTypeView.setText(RecordTypeText);
                    RecordStatusView.setText(RecordStatusText);
                }

                if (animeRecord.getWatchedStatus() == null) {
                    Log.v("MALX", "No status found; Record must have been searched for, therefore not added to list");
                    setAddToListUI(true);
                }
            }
            else if (mangaRecord != null ){
                actionBar.setTitle(mangaRecord.getTitle());

                Picasso coverImage = Picasso.with(context);

                coverImage
                .load(mangaRecord.getImageUrl())
                .error(R.drawable.cover_error)
                .placeholder(R.drawable.cover_loading)
                .fit()
                .into(CoverImageView);

                VolumeProgressText = Integer.toString(mangaRecord.getVolumesRead());
                VolumeTotalText = Integer.toString(mangaRecord.getVolumes());
                ProgressText = Integer.toString(mangaRecord.getChaptersRead());
                TotalProgressText = Integer.toString(mangaRecord.getChapters());
                MyStatusText = WordUtils.capitalize(mangaRecord.getReadStatus());


                ProgressCurrentVolumeView = (TextView) ProgressFragment.getView().findViewById(R.id.progressVolumesCountCurrent);
                ProgressTotalVolumeView = (TextView) ProgressFragment.getView().findViewById(R.id.progressVolumesCountTotal);
                ProgressCurrentView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountCurrent);
                ProgressTotalView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountTotal);
                MyStatusView = (TextView) WatchStatusFragment.getView().findViewById(R.id.cardStatusLabel);

                if (ProgressTotalVolumeView != null) {
                    ProgressCurrentVolumeView.setText(VolumeProgressText);
                    ProgressTotalVolumeView.setText("/" + VolumeTotalText);
                }
                if (ProgressTotalView != null) {
                    ProgressCurrentView.setText(ProgressText);
                    ProgressTotalView.setText("/" + TotalProgressText);
                }

                if (MyStatusView != null) {
                    MyStatusView.setText(MyStatusText);
                }


                RecordStatusText = WordUtils.capitalize(mangaRecord.getStatus());
                RecordTypeText = WordUtils.capitalize(mangaRecord.getType());

                RecordTypeView = (TextView) StatusFragment.getView().findViewById(R.id.mediaType);
                RecordStatusView = (TextView) StatusFragment.getView().findViewById(R.id.mediaStatus);
                if (RecordStatusView != null) {
                    RecordTypeView.setText(RecordTypeText);
                    RecordStatusView.setText(RecordStatusText);
                }

                if (mangaRecord.getReadStatus() == null) {
                    Log.v("MALX", "No status found; Record must have been searched for, therefore not added to list");
                    setAddToListUI(true);
                }

            }
        }

        @Override
        protected void onPostExecute(GenericRecord gr) {
            if (gr != null) {
                if (ProgressFragment.getView() == null) {
                    // Parent activity is destroy, skipping
                    return;
                }
                if ("anime".equals(internalType)) {
                    MALScoreBar = (RatingBar) ScoreFragment.getView().findViewById(R.id.MALScoreBar);
                    MyScoreBar = (RatingBar) ScoreFragment.getView().findViewById(R.id.MyScoreBar);
    
                    if (MALScoreBar != null) {
                        MALScoreBar.setRating(MemberScore / 2);
                        MyScoreBar.setRating(MyScore / 2);
                    }
    
                    MyStatusView = (TextView) WatchStatusFragment.getView().findViewById(R.id.cardStatusLabel);
    
                    if (MyStatusView != null) {
                        MyStatusView.setText(MyStatusText);
                    }
    
                } else {
    
                    MemberScore = mangaRecord.getMembersScore();
                    MyScore = mangaRecord.getScore();
    
                    MALScoreBar = (RatingBar) ScoreFragment.getView().findViewById(R.id.MALScoreBar);
                    MyScoreBar = (RatingBar) ScoreFragment.getView().findViewById(R.id.MyScoreBar);
    
                    if (MALScoreBar != null) {
                        MALScoreBar.setRating(MemberScore / 2);
                        MyScoreBar.setRating(MyScore / 2);
                    }
                }
    
                if (gr.getSpannedSynopsis() != null) {
                    SynopsisText = gr.getSpannedSynopsis();
                    MemberScore = gr.getMembersScore();
                }
                else {
                    SynopsisText = Html.fromHtml("<em>No data loaded.</em>");
                    MemberScore = 0.0f;
                }
    
                if (SynopsisFragment.getView() != null) {
                    SynopsisView = (TextView) SynopsisFragment.getView().findViewById(R.id.SynopsisContent);
    
                    if (SynopsisView != null) {
                        SynopsisView.setText(SynopsisText, TextView.BufferType.SPANNABLE);
    
                    }
                }
    
                if (MALScoreBar != null) {
                    MALScoreBar.setRating(MemberScore / 2);
                    MyScoreBar.setRating(MyScore / 2);
                }
            } else {
                // if gr is null then the anime/manga is not stored in the database and could not be loaded from the API (e. g. no network connection)
                Toast.makeText(context, R.string.crouton_error_DetailsError, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public class writeDetailsTask extends AsyncTask<GenericRecord, Void, Boolean> {
        MALManager internalManager;
        String internalType;
        boolean internalNetworkAvailable;

        @Override
        protected void onPreExecute() {
            internalManager = mManager;
            internalType = recordType;
            internalNetworkAvailable = networkAvailable;

        }


        @Override
        protected Boolean doInBackground(GenericRecord... gr) {

            boolean result;

            if ("anime".equals(internalType))
                internalManager.saveAnimeToDatabase((Anime) gr[0], false);
            else
                internalManager.saveMangaToDatabase((Manga) gr[0], false);

            if (gr[0].getDeleteFlag()) {
            	if ("anime".equals(internalType)) {
                    internalManager.deleteAnimeFromDatabase((Anime) gr[0]);
                    result = internalManager.writeAnimeDetailsToMAL((Anime) gr[0]);
            	} else {
                    internalManager.deleteMangaFromDatabase((Manga) gr[0]);
                    result = internalManager.writeMangaDetailsToMAL((Manga) gr[0]);
            	}
            }
            else {
                if (internalNetworkAvailable){
                    if ("anime".equals(internalType)) {
                        if (gr[0].getCreateFlag())
                        	result = internalManager.addAnimeToMAL((Anime) gr[0]);
                        else
                        	result = internalManager.writeAnimeDetailsToMAL((Anime) gr[0]);
                    } else {
                        if (gr[0].getCreateFlag()) {
                            result = internalManager.addMangaToMAL((Manga) gr[0]);
                        }
                        else {
                            result = internalManager.writeMangaDetailsToMAL((Manga) gr[0]);
                        }

                    }
                }
                else {
                    result = false;
                }


                if (result) {
                    gr[0].setDirty(false);

                    if ("anime".equals(internalType)) {
                        internalManager.saveAnimeToDatabase((Anime) gr[0], false);
                    } else {
                        internalManager.saveMangaToDatabase((Manga) gr[0], false);
                    }

                }
            }


            return result;

        }

    }

    //Dialog returns new value, do something with it
    @Override
    public void onDialogDismissed(int newValue) {
        if ("anime".equals(recordType)) {
            if (newValue == animeRecord.getWatchedEpisodes()) {

            } else {
                if (animeRecord.getEpisodes() != 0) {
                    if (newValue == animeRecord.getEpisodes()) {
                        animeRecord.setWatchedStatus(GenericRecord.STATUS_COMPLETED);
                        MyStatusView.setText(WordUtils.capitalize(GenericRecord.STATUS_COMPLETED));
                    }
                    if (newValue == 0) {
                        animeRecord.setWatchedStatus(Anime.STATUS_PLANTOWATCH);
                        MyStatusView.setText(WordUtils.capitalize(Anime.STATUS_PLANTOWATCH));
                    }

                }

                animeRecord.setWatchedEpisodes(newValue);
                animeRecord.setDirty(true);


                ProgressCurrentView.setText(Integer.toString(newValue));

            }
        }

    }

    public void setAddToListUI(boolean enabled) {
        if (enabled) { //Configure DetailView to show the add to list UI, hide extraneous elements
            isAdded = false;
            supportInvalidateOptionsMenu();

            FragmentTransaction ft = fm.beginTransaction();
            ft.hide(ScoreFragment);
            ft.hide(ProgressFragment);
            ft.hide(WatchStatusFragment);
            ft.commit();

        }
        else { //Record was added, revert UI changes.
            isAdded = true;
            supportInvalidateOptionsMenu();

            FragmentTransaction ft = fm.beginTransaction();
            ft.show(ScoreFragment);
            ft.show(ProgressFragment);
            ft.show(WatchStatusFragment);
            ft.commit();
        }

    }

    //Create new write task and run it
    public void writeDetails(GenericRecord gr) {
        new writeDetailsTask().execute(gr);
    }

    public void setStatus(String currentStatus) {
        String prevStatus;

        if ("anime".equals(recordType)) {
            prevStatus = animeRecord.getStatus();

            if (Anime.STATUS_WATCHING.equals(currentStatus)) {
                animeRecord.setWatchedStatus(Anime.STATUS_WATCHING);
            }
            if (GenericRecord.STATUS_COMPLETED.equals(currentStatus)) {
                animeRecord.setWatchedStatus(Anime.STATUS_COMPLETED);
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

            if (!prevStatus.equals(currentStatus)) {
                animeRecord.setDirty(true);
                MyStatusView.setText(WordUtils.capitalize(currentStatus));
            }
        } else {
            prevStatus = mangaRecord.getReadStatus();

            if (Manga.STATUS_READING.equals(currentStatus)) {
                mangaRecord.setReadStatus(Manga.STATUS_READING);
            }
            if (GenericRecord.STATUS_COMPLETED.equals(currentStatus)) {
                mangaRecord.setReadStatus(Manga.STATUS_COMPLETED);
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

            if (!prevStatus.equals(currentStatus)) {
                mangaRecord.setDirty(true);
                MyStatusView.setText(WordUtils.capitalize(currentStatus));
            }
        }
    }

    public void setRating(int rating) {
        Log.v("MALX", "setRating received rating: " + rating);

        if ("anime".equals(recordType)) {
            MyScoreBar.setRating((float) rating / 2);

            animeRecord.setScore(rating);
            animeRecord.setDirty(true);
        } else {
            MyScoreBar.setRating((float) rating / 2);

            mangaRecord.setScore(rating);
            mangaRecord.setDirty(true);
        }
    }

    @Override
    public void onMangaDialogDismissed(int newChapterValue, int newVolumeValue) {

        if ("manga".equals(recordType)) {

            if (newChapterValue == mangaRecord.getProgress(false)) {

            }
            else {
                if (mangaRecord.getTotal(false) != 0) {

                    if (newChapterValue == mangaRecord.getTotal(false)) {
                        mangaRecord.setReadStatus(GenericRecord.STATUS_COMPLETED);
                    }
                    if (newChapterValue == 0) {
                        mangaRecord.setReadStatus(Manga.STATUS_PLANTOREAD);
                    }

                }

                mangaRecord.setProgress(false, newChapterValue);
                mangaRecord.setDirty(true);

                ProgressCurrentView.setText(Integer.toString(newChapterValue));


            }

            if (newVolumeValue == mangaRecord.getVolumesRead()) {

            }
            else {
                mangaRecord.setVolumesRead(newVolumeValue);
                mangaRecord.setDirty(true);

                ProgressCurrentVolumeView.setText(Integer.toString(newVolumeValue));
            }

            if (newChapterValue == 9001 || newVolumeValue == 9001) {
                Crouton.makeText(this, "It's over 9000!!", Style.INFO).show();
            }
        }


    }

    public void contentInflated(int contentType) {
        switch (contentType) {
            case GenericCardFragment.CONTENT_TYPE_SYNOPSIS:
                SynopsisView = (TextView) SynopsisFragment.getView().findViewById(R.id.SynopsisContent);
                if (SynopsisText != null) {
                    SynopsisView.setText(SynopsisText, TextView.BufferType.SPANNABLE);
                }
                break;
            case GenericCardFragment.CONTENT_TYPE_PROGRESS:
                ProgressCurrentView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountCurrent);
                ProgressTotalView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountTotal);

                if (ProgressText != null) {
                    ProgressCurrentView.setText(ProgressText);
                    ProgressTotalView.setText("/" + TotalProgressText);

                }
                break;
            case GenericCardFragment.CONTENT_TYPE_INFO:
                RecordTypeView = (TextView) StatusFragment.getView().findViewById(R.id.mediaType);
                RecordStatusView = (TextView) StatusFragment.getView().findViewById(R.id.mediaStatus);
                if (RecordStatusText != null) {
                    RecordTypeView.setText(RecordTypeText);
                    RecordStatusView.setText(RecordStatusText);
                }

                break;

            case GenericCardFragment.CONTENT_TYPE_SCORE:
                MALScoreBar = (RatingBar) ScoreFragment.getView().findViewById(R.id.MALScoreBar);
                MyScoreBar = (RatingBar) ScoreFragment.getView().findViewById(R.id.MyScoreBar);
                if (MemberScore > 0) {
                    MALScoreBar.setRating(MemberScore / 2);
                    MyScoreBar.setRating(MyScore / 2);
                }

                break;

            case GenericCardFragment.CONTENT_TYPE_WATCHSTATUS:
                MyStatusView = (TextView) WatchStatusFragment.getView().findViewById(R.id.cardStatusLabel);
                if (MyStatusText != null) {
                    Log.v("MALX", "MyStatusText not null, setting");
                    MyStatusView.setText(MyStatusText);
                }
                break;
        }

    }

    public void Share() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);

        sharingIntent.setType("text/plain");
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        sharingIntent.putExtra(Intent.EXTRA_TEXT, makeShareText());

        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public String makeShareText() {
        String shareText = pManager.getCustomShareText();

        shareText = shareText.replace("$title;", actionBar.getTitle());
        shareText = shareText.replace("$link;", "http://myanimelist.net/" + recordType + "/" + Integer.toString(recordID));

        shareText = shareText + getResources().getString(R.string.customShareText_fromAtarashii);

        return shareText;
    }

    @Override
    public void onStatusDialogDismissed(String currentStatus) {
        setStatus(currentStatus);
    }

    @Override
    public void onRatingDialogDismissed(int rating) {
        setRating(rating);
        Log.v("MALX", "Listener recieved rating: " + rating);

    }

    @Override
    public void onRemoveConfirmed() {
        Log.v("MALX", "Item flagged for being removing.");
        if ("anime".equals(recordType)) {
            animeRecord.setDeleteFlag(true);
            animeRecord.setDirty(true);
        } else {
            mangaRecord.setDeleteFlag(true);
            mangaRecord.setDirty(true);
        }

        finish();
    }

    public void addToList() {
        if (recordType.equals("anime")) {
            animeRecord.setCreateFlag(true);
            animeRecord.setWatchedStatus(Anime.STATUS_WATCHING);
            MyStatusView.setText(WordUtils.capitalize(Anime.STATUS_WATCHING));

            animeRecord.setDirty(true);
        }
        else {
            mangaRecord.setCreateFlag(true);
            mangaRecord.setReadStatus(Manga.STATUS_READING);
            MyStatusView.setText(WordUtils.capitalize(Manga.STATUS_READING));

            mangaRecord.setDirty(true);
        }

        setAddToListUI(false);
    }
}