package net.somethingdreadful.MAL.dialog;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import net.somethingdreadful.MAL.FirstTimeInit;
import net.somethingdreadful.MAL.IGF;
import net.somethingdreadful.MAL.NfcHelper;
import net.somethingdreadful.MAL.PrefManager;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.IGFPagerAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.database.DatabaseManager;
import net.somethingdreadful.MAL.tasks.APIAuthenticationErrorListener;
import net.somethingdreadful.MAL.tasks.TaskJob;
import net.somethingdreadful.MAL.widgets.Widget1;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RecordPickerDialog extends AppCompatActivity implements IGF.IGFCallbackListener, APIAuthenticationErrorListener, ViewPager.OnPageChangeListener {
    private IGF af;
    private IGF mf;
    private Menu menu;
    private Context context;
    private IGFPagerAdapter mIGFPagerAdapter;

    @Bind(R.id.pager)
    ViewPager mViewPager;

    private int callbackCounter = 0;
    private int widgetID;
    private int recordID;
    private MALApi.ListType type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        context = getApplicationContext();
        if (AccountService.getAccount() != null) {
            setContentView(R.layout.activity_home);

            // Creates the adapter to return the Animu and Mango fragments
            mIGFPagerAdapter = new IGFPagerAdapter(getFragmentManager(), false);
            ButterKnife.bind(this);

            // Set up the ViewPager with the sections adapter.
            mViewPager.setAdapter(mIGFPagerAdapter);
            mViewPager.setPageMargin(32);
            mViewPager.addOnPageChangeListener(this);

            widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            type = (MALApi.ListType) intent.getSerializableExtra("recordType");
            recordID = intent.getIntExtra("recordID", 0);
            if (extras != null) {
                widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }
            if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID && recordID == 0) {
                finish();
            }
        } else {
            Intent firstRunInit = new Intent(this, FirstTimeInit.class);
            startActivity(firstRunInit);
            finish();
        }
        NfcHelper.disableBeam(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        checkIGF();
        switch (item.getItemId()) {
            case R.id.listType_all:
                getRecords(true, TaskJob.GETLIST, 0);
                setChecked(item);
                break;
            case R.id.listType_inprogress:
                getRecords(true, TaskJob.GETLIST, 1);
                setChecked(item);
                break;
            case R.id.listType_completed:
                getRecords(true, TaskJob.GETLIST, 2);
                setChecked(item);
                break;
            case R.id.listType_onhold:
                getRecords(true, TaskJob.GETLIST, 3);
                setChecked(item);
                break;
            case R.id.listType_dropped:
                getRecords(true, TaskJob.GETLIST, 4);
                setChecked(item);
                break;
            case R.id.listType_planned:
                getRecords(true, TaskJob.GETLIST, 5);
                setChecked(item);
                break;
            case R.id.listType_rewatching:
                getRecords(true, TaskJob.GETLIST, 6);
                setChecked(item);
                break;
            case R.id.forceSync:
                synctask(true);
                break;
            case R.id.menu_inverse:
                if (af != null && mf != null) {
                    if (!AccountService.isMAL() && af.taskjob == TaskJob.GETMOSTPOPULAR) {
                        af.toggleAiringTime();
                    } else {
                        af.inverse();
                        mf.inverse();
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * On some devices the af & mf will change into null due inactivity.
     * This is a check to prevent any crashes and set it again.
     */
    private void checkIGF() {
        if (af == null || mf == null) {
            af = (IGF) mIGFPagerAdapter.getIGF(mViewPager, 0);
            mf = (IGF) mIGFPagerAdapter.getIGF(mViewPager, 1);
        }
    }

    private void getRecords(boolean clear, TaskJob task, int list) {
        checkIGF();
        if (af != null && mf != null) {
            af.getRecords(clear, task, list);
            mf.getRecords(clear, task, list);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @SuppressLint("NewApi")
    @Override
    public void onPause() {
        super.onPause();
    }

    private void synctask(boolean clear) {
        getRecords(clear, TaskJob.FORCESYNC, af.list);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        if (af != null) {
            //All this is handling the ticks in the switch list menu
            switch (af.list) {
                case 0:
                    setChecked(menu.findItem(R.id.listType_all));
                    break;
                case 1:
                    setChecked(menu.findItem(R.id.listType_inprogress));
                    break;
                case 2:
                    setChecked(menu.findItem(R.id.listType_completed));
                    break;
                case 3:
                    setChecked(menu.findItem(R.id.listType_onhold));
                    break;
                case 4:
                    setChecked(menu.findItem(R.id.listType_dropped));
                    break;
                case 5:
                    setChecked(menu.findItem(R.id.listType_planned));
                    break;
                case 6:
                    setChecked(menu.findItem(R.id.listType_rewatching));
                    break;
            }
            menu.findItem(R.id.action_search).setVisible(false);
        }
        return true;
    }

    private void setChecked(MenuItem item) {
        item.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }

    @Override
    public void onIGFReady(IGF igf) {
        igf.setUsername(AccountService.getUsername());
        if (igf.listType.equals(MALApi.ListType.ANIME))
            af = igf;
        else
            mf = igf;
        // do forced sync after FirstInit
        if (PrefManager.getForceSync()) {
            if (af != null && mf != null) {
                PrefManager.setForceSync(false);
                PrefManager.commitChanges();
                synctask(true);
            }
        } else {
            if (igf.taskjob == null) {
                igf.getRecords(true, TaskJob.GETLIST, PrefManager.getDefaultList());
            }
        }
    }

    @Override
    public void onRecordsLoadingFinished(MALApi.ListType type, TaskJob job, boolean error, boolean resultEmpty, boolean cancelled) {
        if (cancelled && !job.equals(TaskJob.FORCESYNC)) {
            return;
        }

        callbackCounter++;

        if (callbackCounter >= 2) {
            callbackCounter = 0;

            if (job.equals(TaskJob.FORCESYNC)) {
                NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(R.id.notification_sync);
            }
        }
    }

    @Override
    public void onItemClick(int id, MALApi.ListType listType, String username) {
        DatabaseManager db = new DatabaseManager(context);
        boolean succeeded;
        if (recordID != 0)
            succeeded = db.updateWidgetRecord(recordID, id, listType);
        else
            succeeded = db.addWidgetRecord(id, listType);

        if (succeeded) {
            Intent updateWidgetIntent = new Intent(context, Widget1.class);
            updateWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateWidgetIntent.putExtra("checkGhost", true);
            context.sendBroadcast(updateWidgetIntent);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
            setResult(RESULT_OK, resultValue);
            finish();
        } else {
            Theme.Snackbar(this, R.string.toast_info_widget_exists);
        }
    }

    @Override
    public void onAPIAuthenticationError(MALApi.ListType type, TaskJob job) {
        startActivity(new Intent(this, RecordPickerDialog.class).putExtra("updatePassword", true));
        finish();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (menu != null)
            menu.findItem(R.id.listType_rewatching).setTitle(getString(position == 0 ? R.string.listType_rewatching : R.string.listType_rereading));
    }

    @Override
    public void onPageSelected(int position) {}

    @Override
    public void onPageScrollStateChanged(int state) {}
}
