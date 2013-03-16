package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import net.somethingdreadful.MAL.record.AnimeRecord;
import net.somethingdreadful.MAL.record.GenericMALRecord;
import net.somethingdreadful.MAL.record.MangaRecord;
import org.apache.commons.lang3.text.WordUtils;

public class DetailView extends SherlockFragmentActivity implements DetailsBasicFragment.IDetailsBasicAnimeFragment,
        EpisodesPickerDialogFragment.DialogDismissedListener, MangaProgressDialogFragment.MangaDialogDismissedListener,
        StatusPickerDialogFragment.StatusDialogDismissedListener, RatingPickerDialogFragment.RatingDialogDismissedListener,
        RemoveConfirmationDialogFragment.RemoveConfirmationDialogListener {

    MALManager mManager;
    PrefManager pManager;
    Context context;
    int recordID;
    ActionBar actionBar;
    AnimeRecord animeRecord;
    MangaRecord mangaRecord;
    String recordType;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        //Get the recordID, passed in from the calling activity
        recordID = getIntent().getIntExtra("net.somethingdreadful.MAL.recordID", 1);

        //Get the recordType, also passed from calling activity
        //Record type will determine how the detail view lays out itself
        recordType = getIntent().getStringExtra("net.somethingdreadful.MAL.recordType");


        fm = getSupportFragmentManager();

        bfrag = (DetailsBasicFragment) fm.findFragmentById(R.id.DetailsFragment);

        SynopsisFragment = (GenericCardFragment) fm.findFragmentById(R.id.SynopsisFragment);
        SynopsisFragment.setArgsSensibly("SYNOPSIS", R.layout.card_layout_content_synopsis, GenericCardFragment.CONTENT_TYPE_SYNOPSIS, false);
        SynopsisFragment.inflateContentStub();


        ProgressFragment = (GenericCardFragment) fm.findFragmentById(R.id.ProgressFragment);
        int card_layout_progress = R.layout.card_layout_progress;
        if ("manga".equals(recordType)) {
            card_layout_progress = R.layout.card_layout_progress_manga;
        }
        ProgressFragment.setArgsSensibly("PROGRESS", card_layout_progress, GenericCardFragment.CONTENT_TYPE_PROGRESS, true);
        ProgressFragment.inflateContentStub();

        ProgressFragment.getView().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //TODO: Added some kind of clicking feedback, like highlight it or something
                showProgressDialog();
            }

        });

        StatusFragment = (GenericCardFragment) fm.findFragmentById(R.id.StatusFragment);
        StatusFragment.setArgsSensibly("MAL STATS", R.layout.card_layout_status, GenericCardFragment.CONTENT_TYPE_INFO, false);
        StatusFragment.inflateContentStub();

        ScoreFragment = (GenericCardFragment) fm.findFragmentById(R.id.ScoreFragment);
        ScoreFragment.setArgsSensibly("RATING", R.layout.card_layout_score, GenericCardFragment.CONTENT_TYPE_SCORE, true);
        ScoreFragment.inflateContentStub();

        ScoreFragment.getView().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showRatingDialog();

            }


        });

        WatchStatusFragment = (GenericCardFragment) fm.findFragmentById(R.id.WatchStatusFragment);
        WatchStatusFragment.setArgsSensibly("STATUS", R.layout.card_layout_watchstatus, GenericCardFragment.CONTENT_TYPE_WATCHSTATUS, true);
        WatchStatusFragment.inflateContentStub();

        WatchStatusFragment.getView().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //TODO: Added some kind of clicking feedback, like highlight it or something
                showStatusDialog();
            }

        });


        context = getApplicationContext();
        mManager = new MALManager(context);
        pManager = new PrefManager(context);

        // Set up the action bar.
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_detail_view, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
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

    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            if ("anime".equals(recordType)) {
                if (animeRecord.getDirty() == 1) {
                    writeDetails(animeRecord);
                }
            } else {
                if (mangaRecord.getDirty() == 1) {
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

        getDetails(recordID);
    }

    public void getDetails(int id) {
        //TODO: What?
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

    public class getDetailsTask extends AsyncTask<Void, Boolean, GenericMALRecord> {
        int mRecordID;
        MALManager mMalManager;
        ImageDownloader imageDownloader = new ImageDownloader(context);
        String internalType;

        @Override
        protected void onPreExecute() {
            mRecordID = recordID;
            mMalManager = mManager;
            internalType = recordType;
        }

        @Override
        protected GenericMALRecord doInBackground(Void... arg0) {

            if ("anime".equals(internalType)) {
                animeRecord = mMalManager.getAnimeRecord(mRecordID);

                //Basically I just use publishProgress as an easy way to display info we already have loaded sooner
                //This way, I can let the database work happen on the background thread and then immediately display it while
                //the synopsis loads if it hasn't previously been downloaded.
                publishProgress(true);

                if ((animeRecord.getSynopsis() == null) || (animeRecord.getMemberScore() <= 0)) {
                    animeRecord = mMalManager.updateWithDetails(mRecordID, animeRecord);
                }

                return animeRecord;
            } else {
                mangaRecord = mMalManager.getMangaRecord(mRecordID);

                //Basically I just use publishProgress as an easy way to display info we already have loaded sooner
                //This way, I can let the database work happen on the background thread and then immediately display it while
                //the synopsis loads if it hasn't previously been downloaded.
                publishProgress(true);

                if ((mangaRecord.getSynopsis() == null) || (mangaRecord.getMemberScore() <= 0)) {
                    mangaRecord = mMalManager.updateWithDetails(mRecordID, mangaRecord);
                }

                return mangaRecord;
            }

        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);

            if ("anime".equals(internalType)) {
                actionBar.setTitle(animeRecord.getName());

                CoverImageView.setImageDrawable(new BitmapDrawable(imageDownloader.returnDrawable(context, animeRecord.getImageUrl())));

                ProgressText = Integer.toString(animeRecord.getPersonalProgress());
                TotalProgressText = animeRecord.getTotal();
                MyStatusText = WordUtils.capitalize(animeRecord.getMyStatus());

                ProgressCurrentView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountCurrent);
                ProgressTotalView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountTotal);

                if (ProgressTotalView != null) {
                    ProgressCurrentView.setText(ProgressText);
                    ProgressTotalView.setText("/" + TotalProgressText);

                }

                RecordStatusText = WordUtils.capitalize(animeRecord.getRecordStatus());
                RecordTypeText = WordUtils.capitalize(animeRecord.getRecordType());
                MemberScore = animeRecord.getMemberScore();
                MyScore = animeRecord.getMyScore();

                RecordTypeView = (TextView) StatusFragment.getView().findViewById(R.id.mediaType);
                RecordStatusView = (TextView) StatusFragment.getView().findViewById(R.id.mediaStatus);

                if (RecordStatusView != null) {
                    RecordTypeView.setText(RecordTypeText);
                    RecordStatusView.setText(RecordStatusText);
                }

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
                actionBar.setTitle(mangaRecord.getName());

                CoverImageView.setImageDrawable(new BitmapDrawable(imageDownloader.returnDrawable(context, mangaRecord.getImageUrl())));

                VolumeProgressText = Integer.toString(mangaRecord.getVolumeProgress());
                VolumeTotalText = Integer.toString(mangaRecord.getVolumesTotal());
                ProgressText = Integer.toString(mangaRecord.getPersonalProgress());
                TotalProgressText = mangaRecord.getTotal();
                MyStatusText = WordUtils.capitalize(mangaRecord.getMyStatus());


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


                RecordStatusText = WordUtils.capitalize(mangaRecord.getRecordStatus());
                RecordTypeText = WordUtils.capitalize(mangaRecord.getRecordType());
                MemberScore = mangaRecord.getMemberScore();
                MyScore = mangaRecord.getMyScore();

                RecordTypeView = (TextView) StatusFragment.getView().findViewById(R.id.mediaType);
                RecordStatusView = (TextView) StatusFragment.getView().findViewById(R.id.mediaStatus);
                if (RecordStatusView != null) {
                    RecordTypeView.setText(RecordTypeText);
                    RecordStatusView.setText(RecordStatusText);
                }

                MALScoreBar = (RatingBar) ScoreFragment.getView().findViewById(R.id.MALScoreBar);
                MyScoreBar = (RatingBar) ScoreFragment.getView().findViewById(R.id.MyScoreBar);

                if (MALScoreBar != null) {
                    MALScoreBar.setRating(MemberScore / 2);
                    MyScoreBar.setRating(MyScore / 2);
                }
            }
        }

        @Override
        protected void onPostExecute(GenericMALRecord gr) {

            if (SynopsisFragment.getView() != null) {
                SynopsisView = (TextView) SynopsisFragment.getView().findViewById(R.id.SynopsisContent);

                if (SynopsisView != null) {
                    SynopsisView.setText(gr.getSpannedSynopsis(), TextView.BufferType.SPANNABLE);
                    SynopsisText = gr.getSpannedSynopsis();
                    MemberScore = gr.getMemberScore();

                } else {
                    SynopsisText = gr.getSpannedSynopsis();
                    MemberScore = gr.getMemberScore();
                }
            }

            if (MALScoreBar != null) {
                MALScoreBar.setRating(MemberScore / 2);
                MyScoreBar.setRating(MyScore / 2);
            }

        }
    }

    public class writeDetailsTask extends AsyncTask<GenericMALRecord, Void, Boolean> {
        MALManager internalManager;
        String internalType;

        @Override
        protected void onPreExecute() {
            internalManager = mManager;
            internalType = recordType;

        }


        @Override
        protected Boolean doInBackground(GenericMALRecord... gr) {

            boolean result;

            if (gr[0].hasDelete()) {
                internalManager.deleteItemFromDatabase(internalType, gr[0].getID());
                result = internalManager.writeDetailsToMAL(gr[0], internalType);
            } else {
                if ("anime".equals(internalType)) {
                    internalManager.saveItem((AnimeRecord) gr[0], false);
                    result = internalManager.writeDetailsToMAL(gr[0], internalManager.TYPE_ANIME);
                } else {
                    internalManager.saveItem((MangaRecord) gr[0], false);
                    result = internalManager.writeDetailsToMAL(gr[0], internalManager.TYPE_MANGA);
                }

                if (result == true) {
                    gr[0].setDirty(gr[0].CLEAN);

                    if ("anime".equals(internalType)) {
                        internalManager.saveItem((AnimeRecord) gr[0], false);
                    } else {
                        internalManager.saveItem((MangaRecord) gr[0], false);
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
            if (newValue == animeRecord.getPersonalProgress()) {

            } else {
                if (Integer.parseInt(animeRecord.getTotal()) != 0) {
                    if (newValue == Integer.parseInt(animeRecord.getTotal())) {
                        animeRecord.setMyStatus(animeRecord.STATUS_COMPLETED);
                        MyStatusView.setText(WordUtils.capitalize(animeRecord.STATUS_COMPLETED));
                    }
                    if (newValue == 0) {
                        animeRecord.setMyStatus(animeRecord.STATUS_PLANTOWATCH);
                        MyStatusView.setText(WordUtils.capitalize(animeRecord.STATUS_PLANTOWATCH));
                    }

                }

                animeRecord.setEpisodesWatched(newValue);
                animeRecord.setDirty(animeRecord.DIRTY);


                ProgressCurrentView.setText(Integer.toString(newValue));

            }
        }

    }

    //Create new write task and run it
    public void writeDetails(GenericMALRecord gr) {
        new writeDetailsTask().execute(gr);
    }

    public void setStatus(String currentStatus) {
        String prevStatus;

        if ("anime".equals(recordType)) {
            prevStatus = animeRecord.getMyStatus();

            if (AnimeRecord.STATUS_WATCHING.equals(currentStatus)) {
                animeRecord.setMyStatus(AnimeRecord.STATUS_WATCHING);
            }
            if (GenericMALRecord.STATUS_COMPLETED.equals(currentStatus)) {
                animeRecord.setMyStatus(AnimeRecord.STATUS_COMPLETED);
            }
            if (GenericMALRecord.STATUS_ONHOLD.equals(currentStatus)) {
                animeRecord.setMyStatus(AnimeRecord.STATUS_ONHOLD);
            }
            if (GenericMALRecord.STATUS_DROPPED.equals(currentStatus)) {
                animeRecord.setMyStatus(AnimeRecord.STATUS_DROPPED);
            }
            if ((AnimeRecord.STATUS_PLANTOWATCH.equals(currentStatus))) {
                animeRecord.setMyStatus(AnimeRecord.STATUS_PLANTOWATCH);
            }

            if (!prevStatus.equals(currentStatus)) {
                animeRecord.setDirty(GenericMALRecord.DIRTY);
                MyStatusView.setText(WordUtils.capitalize(currentStatus));
            }
        } else {
            prevStatus = mangaRecord.getMyStatus();

            if (MangaRecord.STATUS_WATCHING.equals(currentStatus)) {
                mangaRecord.setMyStatus(MangaRecord.STATUS_WATCHING);
            }
            if (GenericMALRecord.STATUS_COMPLETED.equals(currentStatus)) {
                mangaRecord.setMyStatus(MangaRecord.STATUS_COMPLETED);
            }
            if (GenericMALRecord.STATUS_ONHOLD.equals(currentStatus)) {
                mangaRecord.setMyStatus(MangaRecord.STATUS_ONHOLD);
            }
            if (GenericMALRecord.STATUS_DROPPED.equals(currentStatus)) {
                mangaRecord.setMyStatus(MangaRecord.STATUS_DROPPED);
            }
            if (MangaRecord.STATUS_PLANTOWATCH.equals(currentStatus)) {
                mangaRecord.setMyStatus(MangaRecord.STATUS_PLANTOWATCH);
            }

            if (!prevStatus.equals(currentStatus)) {
                mangaRecord.setDirty(GenericMALRecord.DIRTY);
                MyStatusView.setText(WordUtils.capitalize(currentStatus));
            }
        }
    }

    public void setRating(int rating) {
        Log.v("MALX", "setRating received rating: " + rating);

        if ("anime".equals(recordType)) {
            MyScoreBar.setRating((float) rating / 2);

            animeRecord.setMyScore(rating);
            animeRecord.setDirty(GenericMALRecord.DIRTY);
        } else {
            MyScoreBar.setRating((float) rating / 2);

            mangaRecord.setMyScore(rating);
            mangaRecord.setDirty(GenericMALRecord.DIRTY);
        }
    }

    @Override
    public void onMangaDialogDismissed(int newChapterValue, int newVolumeValue) {

        if ("manga".equals(recordType)) {

            if (newChapterValue == mangaRecord.getPersonalProgress()) {

            } else {
                if (Integer.parseInt(mangaRecord.getTotal()) != 0) {
                    if (newChapterValue == Integer.parseInt(mangaRecord.getTotal())) {
                        mangaRecord.setMyStatus(mangaRecord.STATUS_COMPLETED);
                    }
                    if (newChapterValue == 0) {
                        mangaRecord.setMyStatus(mangaRecord.STATUS_PLANTOWATCH);
                    }

                }

                mangaRecord.setPersonalProgress(newChapterValue);
                mangaRecord.setDirty(mangaRecord.DIRTY);

                ProgressCurrentView.setText(Integer.toString(newChapterValue));


            }

            if (newVolumeValue == mangaRecord.getVolumeProgress()) {

            } else {
                mangaRecord.setVolumesRead(newVolumeValue);
                mangaRecord.setDirty(mangaRecord.DIRTY);
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
            animeRecord.markForDeletion(true);
            animeRecord.setDirty(GenericMALRecord.DIRTY);
        } else {
            mangaRecord.markForDeletion(true);
            mangaRecord.setDirty(GenericMALRecord.DIRTY);
        }

        finish();
    }
}