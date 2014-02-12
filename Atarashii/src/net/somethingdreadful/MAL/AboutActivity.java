package net.somethingdreadful.MAL;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class AboutActivity extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setTitle(R.string.title_activity_about);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        TextView animaMalContent = (TextView) findViewById(R.id.contributor_anima_name);
        TextView motokochanMalContent = (TextView) findViewById(R.id.contributor_motokochan_name);
        TextView apkawaMalContent = (TextView) findViewById(R.id.contributor_apkawa_name);
        TextView dskoMalContent = (TextView) findViewById(R.id.contributor_dsko_name);
        TextView acknowledgementsContent = (TextView) findViewById(R.id.acknowledgements_card_content);
        TextView communityContent = (TextView) findViewById(R.id.community_card_content);

        animaMalContent.setMovementMethod(LinkMovementMethod.getInstance());
        motokochanMalContent.setMovementMethod(LinkMovementMethod.getInstance());
        apkawaMalContent.setMovementMethod(LinkMovementMethod.getInstance());
        dskoMalContent.setMovementMethod(LinkMovementMethod.getInstance());
        communityContent.setMovementMethod(LinkMovementMethod.getInstance());
        acknowledgementsContent.setMovementMethod(LinkMovementMethod.getInstance());
        
        NfcHelper.disableBeam(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }

        return true;
    }

}
