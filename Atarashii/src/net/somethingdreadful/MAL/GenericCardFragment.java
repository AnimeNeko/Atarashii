package net.somethingdreadful.MAL;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class GenericCardFragment extends SherlockFragment {

	public static final int CONTENT_TYPE_SYNOPSIS = 0;
	public static final int CONTENT_TYPE_PROGRESS = 1;
	public static final int CONTENT_TYPE_INFO = 2;
	public static final int CONTENT_TYPE_SCORE = 3;
	public static final int CONTENT_TYPE_WATCHSTATUS = 4;

	String title;
	int layoutResID;
	int contentType;
	boolean actionable;
	ViewStub contentStub;
	TextView cardTitle;
	ImageView actionableIcon;
	Activity parent;

	public GenericCardFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle state) {
		View base = inflater.inflate(R.layout.card_layout_base, container);
		contentStub = (ViewStub) base.findViewById(R.id.contentStub);
		cardTitle = (TextView) base.findViewById(R.id.CardTitle);
		actionableIcon = (ImageView) base.findViewById(R.id.actionableIcon);

		if (state != null) {
			this.title = state.getString("title");
			this.layoutResID = state.getInt("layoutResID");
			this.actionable = state.getBoolean("actionable");
			this.contentType = state.getInt("contentType");

			cardTitle.setText(title);

			if (actionable) {
				actionableIcon.setVisibility(View.VISIBLE);
			} else {
				actionableIcon.setVisibility(View.INVISIBLE);
			}
		}

		return base;

		// return super.onCreateView(inflater, container, savedInstanceState);
	}

	public void setArgsSensibly(String title, int layoutResID, int contentType,
			boolean actionable) {

		this.title = title;
		this.layoutResID = layoutResID;
		this.actionable = actionable;
		this.contentType = contentType;

		cardTitle.setText(title);

		if (actionable) {
			actionableIcon.setVisibility(View.VISIBLE);
		} else {
			actionableIcon.setVisibility(View.INVISIBLE);
		}

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
