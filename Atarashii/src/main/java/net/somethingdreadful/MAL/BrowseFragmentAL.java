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
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import net.somethingdreadful.MAL.dialog.GenreDialogFragment;
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BrowseFragmentAL extends Fragment implements CompoundButton.OnCheckedChangeListener, GenreDialogFragment.onUpdateClickListener, AdapterView.OnItemSelectedListener, NumberPickerDialogFragment.onUpdateClickListener {
    BrowseActivity activity;
    HashMap<String, String> query;
    String year = "";
    ArrayList<String> genres = new ArrayList<>();
    ArrayList<String> genresExclude = new ArrayList<>();

    // Default values
    String defaultSeason;
    String defaultStatus;
    String defaultType;

    @BindView(R.id.inverseSwitch) Switch inverseSwitch;
    @BindView(R.id.seasonSpinner) Spinner seasonSpinner;
    @BindView(R.id.typeSpinner) Spinner typeSpinner;
    @BindView(R.id.sortSpinner) Spinner sortSpinner;
    @BindView(R.id.typeSwitch) Switch typeSwitch;
    @BindView(R.id.statusSpinner) Spinner statusSpinner;
    @BindView(R.id.searchButton) TextView searchButton;
    @BindView(R.id.genresButton) TextView genresButton;
    @BindView(R.id.genresExcludeButton) TextView genresExcludeButton;
    @BindView(R.id.yearButton) TextView yearButton;

    @OnClick(R.id.genresButton)
    public void onGenresButton() {
        Bundle bundle = new Bundle();
        bundle.putInt("id", R.id.genresButton);
        bundle.putInt("arrayId", R.array.genresArray_AL);
        bundle.putStringArrayList("current", genres);
        activity.showDialog("genresButton", new GenreDialogFragment().setOnSendClickListener(this), bundle);
    }

    @OnClick(R.id.genresExcludeButton)
    public void onGenresExcludeButton() {
        Bundle bundle = new Bundle();
        bundle.putInt("id", R.id.genresExcludeButton);
        bundle.putInt("arrayId", R.array.genresArray_AL);
        bundle.putStringArrayList("current", genresExclude);
        activity.showDialog("genresExcludeButton", new GenreDialogFragment().setOnSendClickListener(this), bundle);
    }

    @OnClick(R.id.searchButton)
    public void onSearchButton() {
        reloadEntries();
        activity.viewPager.setCurrentItem(typeSwitch.isChecked() ? 2 : 1);
        activity.igf.getBrowse(query, true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (BrowseActivity) activity;
    }

    @OnClick(R.id.yearButton)
    public void onYearButton() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Bundle bundle = new Bundle();
        bundle.putInt("id", 1);
        bundle.putString("title", getString(R.string.card_content_year));
        bundle.putInt("max", year + 1);
        bundle.putInt("min", 1950);
        bundle.putInt("current", year);
        activity.showDialog("yearButton", new NumberPickerDialogFragment().setOnSendClickListener(this), bundle);
    }

    @Override
    public void onUpdated(ArrayList<String> result, int id) {
        if (id == R.id.genresButton) {
            genres = result;
            genresButton.setText(getString(R.string.card_content_genres) + ": " + genres.toString().replace("[", "").replace("]", ""));
            for (String genre : genres) { // resolve conflicts
                if (genresExclude.contains(genre)) {
                    genresExclude.remove(genre);
                    genresExclude = result;
                    genresExcludeButton.setText(getString(R.string.card_content_genresExc) + ": " + genresExclude.toString().replace("[", "").replace("]", ""));
                }
            }
        } else {
            genresExclude = result;
            genresExcludeButton.setText(getString(R.string.card_content_genresExc) + ": " + genresExclude.toString().replace("[", "").replace("]", ""));
            for (String genre : genresExclude) { // resolve conflicts
                if (genres.contains(genre)) {
                    genres.remove(genre);
                    genres = result;
                    genresButton.setText(getString(R.string.card_content_genres) + ": " + genres.toString().replace("[", "").replace("]", ""));
                }
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean manga) {
        if (manga) {
            initSpinner(statusSpinner, R.array.mangaStatus_AL);
            initSpinner(typeSpinner, R.array.mangaType_AL);
            defaultStatus = getResources().getStringArray(R.array.mangaStatus_AL)[0];
            defaultType = getResources().getStringArray(R.array.mangaType_AL)[0];
        } else {
            initSpinner(statusSpinner, R.array.animeStatus_AL);
            initSpinner(typeSpinner, R.array.animeType_AL);
            defaultStatus = getResources().getStringArray(R.array.animeStatus_AL)[0];
            defaultType = getResources().getStringArray(R.array.animeType_AL)[0];
        }
        activity.getBrowsePagerAdapter().isManga(manga);
    }

    @Override
    public void onUpdated(int number, int id) {
        year = String.valueOf(number);
        if (number == 1950)
            yearButton.setText(getString(R.string.card_content_year));
        else
            yearButton.setText(getString(R.string.card_content_year) + ": " + number);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_browse_al, container, false);
        Theme.setBackground(activity, view, Theme.darkTheme ? R.color.bg_dark : R.color.bg_light);
        ButterKnife.bind(this, view);

        initSpinner(seasonSpinner, R.array.seasonList);
        initSpinner(sortSpinner, R.array.genericSort_AL);
        initSpinner(statusSpinner, R.array.animeStatus_AL);
        initSpinner(typeSpinner, R.array.animeType_AL);
        defaultSeason = getResources().getStringArray(R.array.seasonList)[0];
        defaultStatus = getResources().getStringArray(R.array.animeStatus_AL)[0];
        defaultType = getResources().getStringArray(R.array.animeType_AL)[0];
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
        setBackground(inverseSwitch, color);
        setBackground(seasonSpinner, color);
        setBackground(sortSpinner, color);
        setBackground(statusSpinner, color);
        setBackground(typeSwitch, color);
        setBackground(typeSpinner, color);
        setBackground(yearButton, color);
        setBackground(genresButton, color);
        setBackground(genresExcludeButton, color);
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private void reloadEntries() {
        query = new HashMap<>();
        if (!year.equals("") && !year.equals("1950"))
            query.put("year", year);

        // Get the chosen value and get the english API info
        if (!defaultSeason.equals(seasonSpinner.getSelectedItem().toString()))
            query.put("season", activity.getAPIValue(seasonSpinner.getSelectedItem().toString(), R.array.seasonList, R.array.seasonFixedList));

        // Get the chosen value and get the english API info
        if (!defaultStatus.equals(statusSpinner.getSelectedItem().toString())) {
            if (typeSwitch.isChecked())
                query.put("status", activity.getAPIValue(statusSpinner.getSelectedItem().toString(), R.array.mangaStatus_AL, R.array.mangaFixedStatus_AL));
            else
                query.put("status", activity.getAPIValue(statusSpinner.getSelectedItem().toString(), R.array.animeStatus_AL, R.array.animeFixedStatus_AL));
        }

        // Type will never be translated and is disabled!
        if (!defaultType.equals(typeSpinner.getSelectedItem().toString()))
            query.put("type", typeSpinner.getSelectedItem().toString());

        // Get the chosen value and get the english API info
        query.put("sort", activity.getAPIValue(sortSpinner.getSelectedItem().toString(), R.array.genericSort_AL, R.array.genericFixedSort_AL) + (inverseSwitch.isChecked() ? "" : "-desc"));

        // Genres will never be translated and is disabled!
        query.put("genres", genres.toString().replace("[", "").replace("]", ""));
        query.put("genresExclude", genresExclude.toString().replace("[", "").replace("]", ""));
    }
}
