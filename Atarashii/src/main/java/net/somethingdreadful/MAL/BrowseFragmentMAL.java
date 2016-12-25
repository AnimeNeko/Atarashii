package net.somethingdreadful.MAL;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
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

public class BrowseFragmentMAL extends Fragment implements AdapterView.OnItemSelectedListener, NumberPickerDialogFragment.onUpdateClickListener, DatePickerDialogFragment.onDateSetListener, GenreDialogFragment.onUpdateClickListener, CompoundButton.OnCheckedChangeListener {
    BrowseActivity activity;
    HashMap<String, String> query;
    String startDate = "";
    String endDate = "";
    String minRating = "";
    ArrayList<String> genres = new ArrayList<>();

    // Default values
    String defaultStatus;

    @BindView(R.id.keyword) EditText keyword;
    @BindView(R.id.typeSwitch) Switch typeSwitch;
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
    @BindView(R.id.searchButton) TextView searchButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_browse_mal, container, false);
        Theme.setBackground(activity, view, Theme.darkTheme ? R.color.bg_dark : R.color.bg_light);
        ButterKnife.bind(this, view);

        initSpinner(sortSpinner, R.array.animeSort_MAL);
        initSpinner(statusSpinner, R.array.animeStatus_MAL);
        initSpinner(typeSpinner, R.array.animeType_MAL);
        initSpinner(ratingSpinner, R.array.classificationArray);
        initSpinner(genreSpinner, R.array.browse_genresArray);
        defaultStatus = getResources().getStringArray(R.array.animeStatus_AL)[0];
        typeSwitch.setOnCheckedChangeListener(this);

        if (Theme.darkTheme) {
            setBackground(view, R.color.bg_dark_card);
            setComponentBackground(R.color.bg_dark);
        } else {
            setBackground(view, R.color.bg_light);
            setComponentBackground(R.color.text_dark);
        }

        return view;
    }

    public void setComponentBackground(int color) {
        setBackground(keyword, color);
        setBackground(sortSpinner, color);
        setBackground(statusSpinner, color);
        setBackground(typeSwitch, color);
        setBackground(typeSpinner, color);
        setBackground(ratingSpinner, color);
        setBackground(genreSpinner, color);
        setBackground(startDateButton, color);
        setBackground(endDateButton, color);
        setBackground(minimumRatingButton, color);
        setBackground(genresButton, color);
        setBackground(inverseSwitch, color);
        setBackground(searchButton, color);
    }

    public void setBackground(View view, int colorID) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(activity, colorID)));
        } else {
            view.setBackground(new ColorDrawable(ContextCompat.getColor(activity, colorID)));
        }
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
        if (startDate.length() > 1)
            bundle.putString("current", startDate);
        activity.showDialog("startDate", new DatePickerDialogFragment().setCallback(this), bundle);
    }

    @OnClick(R.id.endDateButton)
    public void onEndDateButton() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("startDate", false);
        if (endDate.length() > 1)
            bundle.putString("current", endDate);
        activity.showDialog("endDate", new DatePickerDialogFragment().setCallback(this), bundle);
    }

    @OnClick(R.id.MinimumRatingButton)
    public void onMinimumRatingButton() {
        Bundle bundle = new Bundle();
        bundle.putInt("id", 1);
        bundle.putString("title", getString(R.string.card_content_minrating));
        bundle.putInt("max", 10);
        if (!minRating.equals(""))
            bundle.putInt("current", Integer.parseInt(minRating));
        activity.showDialog("storagevalue", new NumberPickerDialogFragment().setOnSendClickListener(this), bundle);
    }

    @OnClick(R.id.searchButton)
    public void onSearchButton() {
        reloadEntries();
        activity.viewPager.setCurrentItem(typeSwitch.isChecked() ? 2 : 1);
        activity.igf.getBrowse(query, true);
    }

    @OnClick(R.id.genresButton)
    public void onGenresButton() {
        Bundle bundle = new Bundle();
        bundle.putInt("id", R.id.genres);
        bundle.putStringArrayList("current", genres);
        bundle.putInt("arrayId", R.array.genresArray_MAL);
        activity.showDialog("storage", new GenreDialogFragment().setOnSendClickListener(this), bundle);
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

        // Get the chosen value and get the english API info
        if (!sortSpinner.getSelectedItem().toString().equals("Relevance")) {
            if (typeSwitch.isChecked())
                query.put("sort", activity.getAPIValue(sortSpinner.getSelectedItem().toString(), R.array.mangaSort_MAL, R.array.mangaFixedSort_MAL));
            else
                query.put("sort", activity.getAPIValue(sortSpinner.getSelectedItem().toString(), R.array.animeSort_MAL, R.array.animeFixedSort_MAL));
        }

        if (!sortSpinner.getSelectedItem().toString().equals("Relevance"))
            query.put("sort", sortSpinner.getSelectedItem().toString());

        // Get the chosen value and get the english API info
        if (!defaultStatus.equals(statusSpinner.getSelectedItem().toString())) {
            if (typeSwitch.isChecked())
                query.put("status", activity.getAPIValue(statusSpinner.getSelectedItem().toString(), R.array.mangaStatus_MAL, R.array.mangaFixedStatus_MAL));
            else
                query.put("status", activity.getAPIValue(statusSpinner.getSelectedItem().toString(), R.array.animeStatus_MAL, R.array.animeFixedStatus_MAL));
        }

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
            startDate = year == 0 ? "" : (year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day));
            startDateButton.setText(getString(R.string.card_content_start) + ": " + DateTools.parseDate(startDate, false));
        } else {
            endDate = year == 0 ? "" : (year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day));
            endDateButton.setText(getString(R.string.card_content_end) + ": " + DateTools.parseDate(endDate, false));
        }
    }

    @Override
    public void onUpdated(ArrayList<String> result, int id) {
        genres = result;
        genresButton.setText(getString(R.string.card_content_genres) + ": " + genres.toString().replace("[", "").replace("]", ""));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean manga) {
        if (manga) {
            initSpinner(sortSpinner, R.array.mangaSort_MAL);
            initSpinner(statusSpinner, R.array.mangaStatus_MAL);
            initSpinner(typeSpinner, R.array.mangaType_MAL);
            defaultStatus = getResources().getStringArray(R.array.mangaStatus_MAL)[0];
        } else {
            initSpinner(sortSpinner, R.array.animeSort_MAL);
            initSpinner(statusSpinner, R.array.animeStatus_MAL);
            initSpinner(typeSpinner, R.array.animeType_MAL);
            defaultStatus = getResources().getStringArray(R.array.animeStatus_MAL)[0];
        }
        activity.getBrowsePagerAdapter().isManga(manga);
    }
}
