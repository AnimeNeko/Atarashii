package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Anime;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Schedule;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.tasks.ScheduleTask;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;

public class ScheduleActivity extends AppCompatActivity implements Serializable, ScheduleTask.ScheduleTaskListener, SwipeRefreshLayout.OnRefreshListener {
    @Getter
    Schedule schedule;
    @Getter
    ArrayList<Anime> records = new ArrayList<>();
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @Getter
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefresh;
    private GridLayoutManager GLM;
    private scheduleAdapter sa;
    private String[] weekdays;

    private final int mondayHeader = 0;
    private int tuesdayHeader;
    private int wednesdayHeader;
    private int thursdayHeader;
    private int fridayHeader;
    private int saturdayHeader;
    private int sundayHeader;
    private int totalRecords;
    private int recordheight;
    private int columns;
    private Activity activity;

    private MenuItem forceSync;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Theme.setTheme(this, R.layout.activity_schedule, true);
        Toolbar toolbar = (Toolbar) findViewById(R.id.actionbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
        activity = this;

        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefresh.setEnabled(true);

        setColumns();
        recyclerView.setHasFixedSize(true);
        weekdays = DateFormatSymbols.getInstance().getWeekdays();
        GLM = new GridLayoutManager(this, columns);
        GLM.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return isHeader(position) == 0 ? 1 : columns;
            }
        });

        recyclerView.setLayoutManager(GLM);
        recyclerView.addItemDecoration(new SpacesItemDecoration());
        sa = new scheduleAdapter();
        recyclerView.setAdapter(sa);

        if (state != null) {
            getProgressBar().setVisibility(View.GONE);
            schedule = (Schedule) state.getSerializable("schedule");
            records = (ArrayList<Anime>) state.getSerializable("records");
            tuesdayHeader = state.getInt("tuesdayHeader");
            wednesdayHeader = state.getInt("wednesdayHeader");
            thursdayHeader = state.getInt("thursdayHeader");
            fridayHeader = state.getInt("fridayHeader");
            saturdayHeader = state.getInt("saturdayHeader");
            sundayHeader = state.getInt("sundayHeader");
            totalRecords = state.getInt("totalRecords");
            recordheight = state.getInt("recordheight");
            sa.notifyDataSetChanged();
        } else {
            new ScheduleTask(this, false, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * set the numbers columns for the best overview.
     */
    @SuppressLint("InlinedApi")
    private void setColumns() {
        int screenWidth = Theme.convert(getResources().getConfiguration().screenWidthDp);
        columns = (int) Math.ceil(screenWidth / Theme.floatConvert(225));
        int recordWidth = screenWidth / columns;
        recordheight = (int) (recordWidth / 0.7);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_schedule, menu);
        forceSync = menu.findItem(R.id.forceSync);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.day_monday:
                GLM.scrollToPositionWithOffset(mondayHeader, 0);
                break;
            case R.id.day_tuesday:
                GLM.scrollToPositionWithOffset(tuesdayHeader, 0);
                break;
            case R.id.day_wednesday:
                GLM.scrollToPositionWithOffset(wednesdayHeader, 0);
                break;
            case R.id.day_thursday:
                GLM.scrollToPositionWithOffset(thursdayHeader, 0);
                break;
            case R.id.day_friday:
                GLM.scrollToPositionWithOffset(fridayHeader, 0);
                break;
            case R.id.day_saturday:
                GLM.scrollToPositionWithOffset(saturdayHeader, 0);
                break;
            case R.id.day_sunday:
                GLM.scrollToPositionWithOffset(sundayHeader, 0);
                break;
            case R.id.action_ViewMALPage:
                startActivity((new Intent(Intent.ACTION_VIEW)).setData(Uri.parse("https://myanimelist.net/anime/season/schedule")));
                break;
            case R.id.forceSync:
                new ScheduleTask(this, true, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                swipeRefresh.setEnabled(false);
                swipeRefresh.setRefreshing(true);
                item.setVisible(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle State) {
        super.onSaveInstanceState(State);
        State.putSerializable("schedule", schedule);
        State.putSerializable("records", records);
        State.putInt("tuesdayHeader", tuesdayHeader);
        State.putInt("wednesdayHeader", wednesdayHeader);
        State.putInt("thursdayHeader", thursdayHeader);
        State.putInt("fridayHeader", fridayHeader);
        State.putInt("saturdayHeader", saturdayHeader);
        State.putInt("sundayHeader", sundayHeader);
        State.putInt("totalRecords", totalRecords);
        State.putInt("recordheight", recordheight);
    }

    /**
     * Update the records for the RecyclerView.
     *
     * @param weekday The cristian weekday number
     * @param list    The schedule list which matches the weekday number
     */
    private void updateRecords(int weekday, ArrayList<Anime> list) {
        Anime dayname = new Anime();
        dayname.setTitle(StringUtils.capitalize(weekdays[weekday]));
        records.add(dayname);
        records.addAll(list);
    }

    @Override
    public void onScheduleTaskFinished(Schedule result) {
        getProgressBar().setVisibility(View.GONE);
        if (result != null && !result.isNull()) {
            schedule = result;
            if (records != null && records.size() != 0)
                records.clear();

            updateRecords(2, schedule.getMonday());
            updateRecords(3, schedule.getTuesday());
            updateRecords(4, schedule.getWednesday());
            updateRecords(5, schedule.getThursday());
            updateRecords(6, schedule.getFriday());
            updateRecords(7, schedule.getSaturday());
            updateRecords(1, schedule.getSunday());

            tuesdayHeader = getSchedule().getMonday().size() + mondayHeader + 1;
            wednesdayHeader = getSchedule().getTuesday().size() + tuesdayHeader + 1;
            thursdayHeader = getSchedule().getWednesday().size() + wednesdayHeader + 1;
            fridayHeader = getSchedule().getThursday().size() + thursdayHeader + 1;
            saturdayHeader = getSchedule().getFriday().size() + fridayHeader + 1;
            sundayHeader = getSchedule().getSaturday().size() + saturdayHeader + 1;
            totalRecords = getSchedule().getSunday().size() + sundayHeader + 1;
            sa.notifyDataSetChanged();
        }

        swipeRefresh.setRefreshing(false);
    }

    /**
     * Check if position is a header.
     *
     * @param position The position to check
     * @return int 0 if it is a record
     */
    private int isHeader(int position) {
        if (position == mondayHeader || position == tuesdayHeader || position == wednesdayHeader || position == thursdayHeader ||
                position == fridayHeader || position == saturdayHeader || position == sundayHeader) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void onRefresh() {
        new ScheduleTask(this, true, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setRefreshing(true);
        if (forceSync != null)
            forceSync.setVisible(false);
    }

    /**
     * The custom adapter for recommendations.
     */
    public class scheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final String episodes;
        private final String members;
        private final String unknown;
        private final boolean isMal;

        public scheduleAdapter() {
            this.episodes = getString(R.string.card_content_episodes) + ":";
            this.members = getString(R.string.card_content_members) + ":";
            this.unknown = getString(R.string.unknown);
            this.isMal = AccountService.isMAL();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                View v = LayoutInflater.from(activity).inflate(R.layout.record_igf_details, null);
                return new itemHolder(v);
            } else {
                View v = LayoutInflater.from(activity).inflate(R.layout.record_header, null);
                return new headerHolder(v);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return isHeader(position);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                Anime record = getRecords().get(position);
                if (holder instanceof headerHolder) {
                    headerHolder headerHolder = (headerHolder) holder;
                    headerHolder.header.setText(record.getTitle());
                } else {
                    itemHolder itemHolder = (itemHolder) holder;
                    itemHolder.label.setText(record.getTitle());
                    itemHolder.scoreCount.setText(record.getAverageScore());
                    itemHolder.typeCount.setText(record.getType());
                    itemHolder.stringStatus.setText(episodes);
                    itemHolder.statusCount.setText(record.getEpisodes() != 0 ? String.valueOf(record.getEpisodes()) : unknown);
                    if (isMal) {
                        itemHolder.flavourText.setText(members);
                        itemHolder.progressCount.setText(record.getAverageScoreCount());
                    } else {
                        itemHolder.flavourText.setText("");
                        itemHolder.progressCount.setText(record.getAiring().getNormaltime());
                    }

                    Picasso.with(activity)
                            .load(record.getImageUrl())
                            .error(R.drawable.cover_error)
                            .placeholder(R.drawable.cover_loading)
                            .into(itemHolder.cover);
                }
            } catch (Exception e) {
                Theme.logTaskCrash("ScheduleActivity", e.getMessage(), e);
            }
        }

        @Override
        public int getItemCount() {
            return totalRecords;
        }
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public SpacesItemDecoration() {
            this.space = Theme.convert(1);
        }

        private boolean isNotLastColumn(int position) {
            String math = String.valueOf(position / (double) columns);
            return !(math.length() < 5 && (math.contains(".0") || math.contains(",0")));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int pos = parent.getChildLayoutPosition(view);
            if (isHeader(pos) == 0) {
                if (pos < tuesdayHeader) {          // Monday
                    if (isNotLastColumn((pos - mondayHeader)))
                        outRect.right = space;
                } else if (pos < wednesdayHeader) { // Tuesday
                    if (isNotLastColumn((pos - tuesdayHeader)))
                        outRect.right = space;
                } else if (pos < thursdayHeader) {  // Wednesday
                    if (isNotLastColumn((pos - wednesdayHeader)))
                        outRect.right = space;
                } else if (pos < fridayHeader) {    // Thursday
                    if (isNotLastColumn((pos - thursdayHeader)))
                        outRect.right = space;
                } else if (pos < saturdayHeader) {  // Friday
                    if (isNotLastColumn((pos - fridayHeader)))
                        outRect.right = space;
                } else if (pos < sundayHeader) {    // Saturday
                    if (isNotLastColumn((pos - saturdayHeader)))
                        outRect.right = space;
                } else if (pos < totalRecords) {    // Sunday
                    if (isNotLastColumn((pos - sundayHeader)))
                        outRect.right = space;
                }
            }
            outRect.bottom = space;
        }
    }

    public class headerHolder extends RecyclerView.ViewHolder {
        final TextView header;

        public headerHolder(View itemView) {
            super(itemView);
            header = (TextView) itemView.findViewById(R.id.header);
        }
    }

    /**
     * The viewholder for performance.
     */
    public class itemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView label;
        final TextView progressCount;
        final TextView flavourText;
        final ImageView cover;
        final ImageView actionButton;
        final TextView scoreCount;
        final TextView typeCount;
        final TextView statusCount;
        final TextView stringStatus;

        public itemHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.animeName);
            progressCount = (TextView) itemView.findViewById(R.id.watchedCount);
            cover = (ImageView) itemView.findViewById(R.id.coverImage);
            actionButton = (ImageView) itemView.findViewById(R.id.popUpButton);
            flavourText = (TextView) itemView.findViewById(R.id.stringWatched);
            scoreCount = (TextView) itemView.findViewById(R.id.scoreCount);
            typeCount = (TextView) itemView.findViewById(R.id.typeCount);
            statusCount = (TextView) itemView.findViewById(R.id.statusCount);
            stringStatus = (TextView) itemView.findViewById(R.id.stringStatus);

            actionButton.setVisibility(View.GONE);
            cover.getLayoutParams().height = recordheight;
            cover.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (APIHelper.isNetworkAvailable(activity)) {
                Intent startDetails = new Intent(activity, DetailView.class);
                startDetails.putExtra("recordID", records.get(getAdapterPosition()).getId());
                startDetails.putExtra("recordType", MALApi.ListType.ANIME);
                startActivity(startDetails);
            } else {
                Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
            }
        }
    }
}
