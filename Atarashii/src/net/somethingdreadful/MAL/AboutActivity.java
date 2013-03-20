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
        TextView acknowledgementsContent = (TextView) findViewById(R.id.acknowledgements_card_content);

        animaMalContent.setMovementMethod(LinkMovementMethod.getInstance());
        motokochanMalContent.setMovementMethod(LinkMovementMethod.getInstance());
        acknowledgementsContent.setMovementMethod(LinkMovementMethod.getInstance());

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
