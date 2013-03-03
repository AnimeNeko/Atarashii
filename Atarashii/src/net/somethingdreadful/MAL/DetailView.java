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
    AnimeRecord mAr;
    MangaRecord mMr;
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
    TextView MyScoreView;
    TextView MemberScoreView;
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
        if ("manga".equals(recordType)) {
            ProgressFragment.setArgsSensibly("PROGRESS", R.layout.card_layout_progress_manga, GenericCardFragment.CONTENT_TYPE_PROGRESS, true);
        } else {
            ProgressFragment.setArgsSensibly("PROGRESS", R.layout.card_layout_progress, GenericCardFragment.CONTENT_TYPE_PROGRESS, true);
        }
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
        //        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
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
                if (mAr.getDirty() == 1) {
                    writeDetails(mAr);
                }
            } else {
                if (mMr.getDirty() == 1) {
                    writeDetails(mMr);
                }
            }
        } catch (NullPointerException npe) {

        }
    }

    //Called after the basic fragment is finished it's setup, populate data into it
    @Override
    public void basicFragmentReady() {

        CoverImageView = (ImageView) bfrag.getView().findViewById(R.id.detailCoverImage);

        getDetails(recordID);
    }

    public void getDetails(int id) {
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

        int mID;
        MALManager mmManager;
        ActionBar bar;
        ImageDownloader imageDownloader = new ImageDownloader(context);
        String internalType;

        @Override
        protected void onPreExecute() {
            mID = recordID;
            mmManager = mManager;
            internalType = recordType;
        }

        @Override
        protected GenericMALRecord doInBackground(Void... arg0) {

            if ("anime".equals(internalType)) {
                mAr = mmManager.getAnimeRecordFromDB(mID);

                //Basically I just use publishProgress as an easy way to display info we already have loaded sooner
                //This way, I can let the database work happen on the background thread and then immediately display it while
                //the synopsis loads if it hasn't previously been downloaded.
                publishProgress(true);

                if ((mAr.getSynopsis() == null) || (mAr.getMemberScore() <= 0)) {
                    mAr = mmManager.updateWithDetails(mID, mAr);
                }

                return mAr;
            } else {
                mMr = mmManager.getMangaRecordFromDB(mID);

                //Basically I just use publishProgress as an easy way to display info we already have loaded sooner
                //This way, I can let the database work happen on the background thread and then immediately display it while
                //the synopsis loads if it hasn't previously been downloaded.
                publishProgress(true);

                if ((mMr.getSynopsis() == null) || (mMr.getMemberScore() <= 0)) {
                    mMr = mmManager.updateWithDetails(mID, mMr);
                }

                return mMr;
            }

        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);

            if ("anime".equals(internalType)) {
                actionBar.setTitle(mAr.getName());

                CoverImageView.setImageDrawable(new BitmapDrawable(imageDownloader.returnDrawable(context, mAr.getImageUrl())));
                //                RecordStatusView.setText(WordUtils.capitalize(mAr.getRecordStatus()));
                //                RecordTypeView.setText(mAr.getRecordType());
                //                MyStatusView.setText(WordUtils.capitalize(mAr.getMyStatus()));

                ProgressText = Integer.toString(mAr.getPersonalProgress());
                TotalProgressText = mAr.getTotal();
                MyStatusText = WordUtils.capitalize(mAr.getMyStatus());

                ProgressCurrentView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountCurrent);
                ProgressTotalView = (TextView) ProgressFragment.getView().findViewById(R.id.progressCountTotal);

                if (ProgressTotalView != null) {
                    ProgressCurrentView.setText(ProgressText);
                    ProgressTotalView.setText("/" + TotalProgressText);

                }

                RecordStatusText = WordUtils.capitalize(mAr.getRecordStatus());
                RecordTypeText = WordUtils.capitalize(mAr.getRecordType());
                MemberScore = mAr.getMemberScore();
                MyScore = mAr.getMyScore();

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
                actionBar.setTitle(mMr.getName());

                CoverImageView.setImageDrawable(new BitmapDrawable(imageDownloader.returnDrawable(context, mMr.getImageUrl())));
                //                RecordStatusView.setText(WordUtils.capitalize(mMr.getRecordStatus()));
                //                RecordTypeView.setText(mMr.getRecordType());
                //                MyStatusView.setText(WordUtils.capitalize(mMr.getMyStatus()));

                VolumeProgressText = Integer.toString(mMr.getVolumeProgress());
                VolumeTotalText = mMr.getVolumeTotal();
                ProgressText = Integer.toString(mMr.getPersonalProgress());
                TotalProgressText = mMr.getTotal();
                MyStatusText = WordUtils.capitalize(mMr.getMyStatus());


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


                RecordStatusText = WordUtils.capitalize(mMr.getRecordStatus());
                RecordTypeText = WordUtils.capitalize(mMr.getRecordType());
                MemberScore = mMr.getMemberScore();
                MyScore = mMr.getMyScore();

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
        GenericMALRecord internalGr;
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
                internalManager.deleteItemFromDatabase(internalType, Integer.parseInt(gr[0].getID()));
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
            if (newValue == mAr.getPersonalProgress()) {

            } else {
                if (Integer.parseInt(mAr.getTotal()) != 0) {
                    if (newValue == Integer.parseInt(mAr.getTotal())) {
                        mAr.setMyStatus(mAr.STATUS_COMPLETED);
                        MyStatusView.setText(WordUtils.capitalize(mAr.STATUS_COMPLETED));
                    }
                    if (newValue == 0) {
                        mAr.setMyStatus(mAr.STATUS_PLANTOWATCH);
                        MyStatusView.setText(WordUtils.capitalize(mAr.STATUS_PLANTOWATCH));
                    }

                }

                mAr.setEpisodesWatched(newValue);
                mAr.setDirty(mAr.DIRTY);


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
            prevStatus = mAr.getMyStatus();

            if (AnimeRecord.STATUS_WATCHING.equals(currentStatus)) {
                mAr.setMyStatus(AnimeRecord.STATUS_WATCHING);
            }
            if (GenericMALRecord.STATUS_COMPLETED.equals(currentStatus)) {
                mAr.setMyStatus(AnimeRecord.STATUS_COMPLETED);
            }
            if (GenericMALRecord.STATUS_ONHOLD.equals(currentStatus)) {
                mAr.setMyStatus(AnimeRecord.STATUS_ONHOLD);
            }
            if (GenericMALRecord.STATUS_DROPPED.equals(currentStatus)) {
                mAr.setMyStatus(AnimeRecord.STATUS_DROPPED);
            }
            if ((AnimeRecord.STATUS_PLANTOWATCH.equals(currentStatus))) {
                mAr.setMyStatus(AnimeRecord.STATUS_PLANTOWATCH);
            }

            if (!prevStatus.equals(currentStatus)) {
                mAr.setDirty(GenericMALRecord.DIRTY);
                MyStatusView.setText(WordUtils.capitalize(currentStatus));
            }
        } else {
            prevStatus = mMr.getMyStatus();

            if (MangaRecord.STATUS_WATCHING.equals(currentStatus)) {
                mMr.setMyStatus(MangaRecord.STATUS_WATCHING);
            }
            if (GenericMALRecord.STATUS_COMPLETED.equals(currentStatus)) {
                mMr.setMyStatus(MangaRecord.STATUS_COMPLETED);
            }
            if (GenericMALRecord.STATUS_ONHOLD.equals(currentStatus)) {
                mMr.setMyStatus(MangaRecord.STATUS_ONHOLD);
            }
            if (GenericMALRecord.STATUS_DROPPED.equals(currentStatus)) {
                mMr.setMyStatus(MangaRecord.STATUS_DROPPED);
            }
            if (MangaRecord.STATUS_PLANTOWATCH.equals(currentStatus)) {
                mMr.setMyStatus(MangaRecord.STATUS_PLANTOWATCH);
            }

            if (!prevStatus.equals(currentStatus)) {
                mMr.setDirty(GenericMALRecord.DIRTY);
                MyStatusView.setText(WordUtils.capitalize(currentStatus));
            }
        }
    }

    public void setRating(int rating) {
        Log.v("MALX", "setRating received rating: " + rating);

        if ("anime".equals(recordType)) {
            MyScoreBar.setRating((float) rating / 2);

            mAr.setMyScore(rating);
            mAr.setDirty(GenericMALRecord.DIRTY);
        } else {
            MyScoreBar.setRating((float) rating / 2);

            mMr.setMyScore(rating);
            mMr.setDirty(GenericMALRecord.DIRTY);
        }
    }

    public void setAnimeStatus(String status) {
        mAr.setMyStatus(status);
        mAr.setDirty(mAr.DIRTY);

        //        MyStatusView.setText(WordUtils.capitalize(status));
    }

    public void setMangaStatus(String status) {
        mMr.setMyStatus(status);
        mMr.setDirty(mAr.DIRTY);

        //        MyStatusView.setText(WordUtils.capitalize(status));
    }

    @Override
    public void onMangaDialogDismissed(int newChapterValue, int newVolumeValue) {

        if ("manga".equals(recordType)) {

            if (newChapterValue == mMr.getPersonalProgress()) {

            } else {
                if (Integer.parseInt(mMr.getTotal()) != 0) {
                    if (newChapterValue == Integer.parseInt(mMr.getTotal())) {
                        mMr.setMyStatus(mMr.STATUS_COMPLETED);
                    }
                    if (newChapterValue == 0) {
                        mMr.setMyStatus(mMr.STATUS_PLANTOWATCH);
                    }

                }

                mMr.setPersonalProgress(newChapterValue);
                mMr.setDirty(mMr.DIRTY);

                ProgressCurrentView.setText(Integer.toString(newChapterValue));


            }

            if (newVolumeValue == mMr.getVolumeProgress()) {

            } else {
                mMr.setVolumesRead(newVolumeValue);
                mMr.setDirty(mMr.DIRTY);
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
            mAr.markForDeletion(true);
            mAr.setDirty(GenericMALRecord.DIRTY);
        } else {
            mMr.markForDeletion(true);
            mMr.setDirty(GenericMALRecord.DIRTY);
        }

        finish();
    }
}