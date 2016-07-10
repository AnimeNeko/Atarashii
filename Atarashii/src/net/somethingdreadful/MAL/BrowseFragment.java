package net.somethingdreadful.MAL;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import net.somethingdreadful.MAL.dialog.DatePickerDialogFragment;
import net.somethingdreadful.MAL.dialog.GenreDialogFragment;
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BrowseFragment extends Fragment implements AdapterView.OnItemSelectedListener, NumberPickerDialogFragment.onUpdateClickListener, DatePickerDialogFragment.onDateSetListener, GenreDialogFragment.onUpdateClickListener {
    BrowseActivity activity;
    HashMap<String, String> query;
    String startDate = "";
    String endDate = "";
    String minRating = "";
    ArrayList<String> genres = new ArrayList<>();

    @BindView(R.id.keyword) EditText keyword;
    @BindView(R.id.sortSpinner) Spinner sortSpinner;
    @BindView(R.id.statusSpinner) Spinner statusSpinner;
    @BindView(R.id.typeSpinner) Spinner typeSpinner;
    @BindView(R.id.ratingSpinner) Spinner ratingSpinner;
    @BindView(R.id.genreSpinner) Spinner genreSpinner;
    @BindView(R.id.inverseSwitch) Switch inverseSwitch;
    @BindView(R.id.startDateButton) TextView startDateButton;
    @BindView(R.id.endDateButton) TextView endDateButton;
    @BindView(R.id.MinimumRatingButton) TextView minimumRatingButton;
    @BindView(R.id.genresButton) TextView genresButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_browse, container, false);
        Theme.setBackground(activity, view, Theme.darkTheme ? R.color.bg_dark : R.color.bg_light);
        ButterKnife.bind(this, view);

        initSpinner(sortSpinner, R.array.browse_sort_anime);
        initSpinner(statusSpinner, R.array.mediaStatus_Anime);
        initSpinner(typeSpinner, R.array.mediaType_Anime);
        initSpinner(ratingSpinner, R.array.classificationArray);
        initSpinner(genreSpinner, R.array.browse_genresArray);

        return view;
    }

    public void initSpinner(Spinner spinner, int array) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity, array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @OnClick(R.id.startDateButton)
    public void onStartDateButton() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("startDate", true);
        activity.showDialog("startDate", new DatePickerDialogFragment().setCallback(this), bundle);
    }

    @OnClick(R.id.endDateButton)
    public void onEndDateButton() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("startDate", false);
        activity.showDialog("endDate", new DatePickerDialogFragment().setCallback(this), bundle);
    }

    @OnClick(R.id.MinimumRatingButton)
    public void onMinimumRatingButton() {
        Bundle bundle = new Bundle();
        bundle.putInt("id", 1);
        bundle.putString("title", getString(R.string.card_content_minrating));
        bundle.putInt("max", 10);
        activity.showDialog("storagevalue", new NumberPickerDialogFragment().setOnSendClickListener(this), bundle);
    }

    @OnClick(R.id.searchButton)
    public void onSearchButton() {
        reloadEntries();
        activity.viewPager.setCurrentItem(1);
        activity.af.getBrowse(query, true);
        activity.mf.getBrowse(query, true);
    }

    @OnClick(R.id.genresButton)
    public void onGenresButton() {
        Bundle bundle = new Bundle();
        bundle.putInt("id", R.id.genres);
        activity.showDialog("storage", new GenreDialogFragment().setOnSendClickListener(this), bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (BrowseActivity) activity;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private void reloadEntries() {
        keyword.clearFocus();
        query = new HashMap<>();
        query.put("keyword", keyword.getText().toString());
        if (!sortSpinner.getSelectedItem().toString().equals("Relevance"))
            query.put("sort", sortSpinner.getSelectedItem().toString());
        query.put("status", statusSpinner.getSelectedItem().toString());
        query.put("type", typeSpinner.getSelectedItem().toString());
        query.put("rating", ratingSpinner.getSelectedItem().toString());
        query.put("genre_type", String.valueOf(genreSpinner.getSelectedItemPosition()));
        query.put("reverse", String.valueOf(inverseSwitch.isChecked() ? 0 : 1));
        query.put("start_date", startDate);
        query.put("end_date", endDate);
        query.put("score", minRating);
        query.put("genres", genres.toString().replace("[", "").replace("]", ""));
    }

    @Override
    public void onUpdated(int number, int id) {
        minRating = String.valueOf(number);
        minimumRatingButton.setText(getString(R.string.card_content_minrating) + ": " + number);
    }

    @Override
    public void onDateSet(Boolean start, int year, int month, int day) {
        if (start) {
            startDate = year == 0 ? "" : year + "-" + month + "-" + day;
            startDateButton.setText(getString(R.string.card_content_start) + ": " + DateTools.parseDate(startDate, false));
        } else {
            endDate = year == 0 ? "" : year + "-" + month + "-" + day;
            endDateButton.setText(getString(R.string.card_content_end) + ": " + DateTools.parseDate(endDate, false));
        }
    }

    @Override
    public void onUpdated(ArrayList<String> result, int id) {
        genres = result;
        genresButton.setText(getString(R.string.card_content_genres) + ": " + genres.toString().replace("[", "").replace("]", ""));
    }
}
