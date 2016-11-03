package net.somethingdreadful.MAL;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.adapters.BrowsePagerAdapter;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.tasks.TaskJob;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;

public class BrowseActivity extends AppCompatActivity implements IGF.IGFCallbackListener {
    public IGF igf;
    @Getter BrowsePagerAdapter browsePagerAdapter;
    @BindView(R.id.pager) ViewPager viewPager;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Theme.setTheme(this, R.layout.theme_viewpager, false);
        browsePagerAdapter = (BrowsePagerAdapter) Theme.setActionBar(this, new BrowsePagerAdapter(getFragmentManager(), this));
        ButterKnife.bind(this);

        NfcHelper.disableBeam(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show the dialog with the tag
     */
    public void showDialog(String tag, DialogFragment dialog, Bundle args) {
        FragmentManager fm = getFragmentManager();
        dialog.setArguments(args);
        dialog.show(fm, "fragment_" + tag);
    }

    @Override
    public void onIGFReady(IGF igf) {
        igf.setUsername(AccountService.getUsername());
        this.igf = igf;
    }

    @Override
    public void onRecordsLoadingFinished(TaskJob job) {

    }

    @Override
    public void onItemClick(int id, MALApi.ListType listType, String username) {
        Intent startDetails = new Intent(this, DetailView.class);
        startDetails.putExtra("recordID", id);
        startDetails.putExtra("recordType", listType);
        startDetails.putExtra("username", username);
        startActivity(startDetails);
    }
}
