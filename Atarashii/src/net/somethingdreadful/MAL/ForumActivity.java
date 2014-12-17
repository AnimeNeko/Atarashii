package net.somethingdreadful.MAL;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.ViewFlipper;

public class ForumActivity extends ActionBarActivity {

    ForumsMain main;
    ForumsTopics topics;
    ForumsPosts posts;
    FragmentManager manager;
    ViewFlipper viewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        manager = getFragmentManager();
        main = (ForumsMain) manager.findFragmentById(R.id.main);
        topics = (ForumsTopics) manager.findFragmentById(R.id.topics);
        posts = (ForumsPosts) manager.findFragmentById(R.id.posts);

        viewFlipper.setDisplayedChild(0);
    }

    public void getTopics(int id) {
        viewFlipper.setDisplayedChild(1);
        topics.setId(id);
    }

    public void getPosts(int id) {
        viewFlipper.setDisplayedChild(2);
        posts.setId(id);
    }

    private void back() {
        if (viewFlipper.getDisplayedChild() - 1 != -1)
            viewFlipper.setDisplayedChild(viewFlipper.getDisplayedChild() - 1);
        else
            finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                back();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
