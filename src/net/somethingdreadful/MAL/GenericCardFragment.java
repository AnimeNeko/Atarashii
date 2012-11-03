package net.somethingdreadful.MAL;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class GenericCardFragment extends SherlockFragment {

    public static int CONTENT_TYPE_SYNOPSIS = 0;

    String title;
    int layoutResID;
    int contentType;
    boolean actionable;
    ViewStub contentStub;
    TextView CardTitle;
    Activity parent;

    public GenericCardFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View base = inflater.inflate(R.layout.card_layout_base, container);
        contentStub = (ViewStub) base.findViewById(R.id.contentStub);
        CardTitle = (TextView) base.findViewById(R.id.CardTitle);

        if (state != null)
        {
            this.title = state.getString("title");
            this.layoutResID = state.getInt("layoutResID");
            this.actionable = state.getBoolean("actionable");
            this.contentType = state.getInt("contentType");


            CardTitle.setText(title);
        }


        return base;

        //      return super.onCreateView(inflater, container, savedInstanceState);
    }


    public void setArgsSensibly(String title, int layoutResID, int contentType, boolean actionable) {
        Bundle args = new Bundle();

        this.title = title;
        this.layoutResID = layoutResID;
        this.actionable = actionable;
        this.contentType = contentType;

        CardTitle.setText(title);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("title", title);
        outState.putInt("layoutResID", layoutResID);
        outState.putBoolean("actionable", actionable);
        outState.putInt("contentType", contentType);


        super.onSaveInstanceState(outState);
    }

    public void inflateContentStub() {
        contentStub.setLayoutResource(layoutResID);
        contentStub.inflate();

        ((DetailView) parent).contentInflated(contentType);
    }

    @Override
    public void onAttach(Activity a) {

        parent = a;

        super.onAttach(a);
    }



}
