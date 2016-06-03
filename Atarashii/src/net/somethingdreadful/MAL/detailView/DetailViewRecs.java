package net.somethingdreadful.MAL.detailView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.RoundedTransformation;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.api.MALModels.Recommendations;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.TaskJob;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import lombok.Getter;

public class DetailViewRecs extends Fragment implements NetworkTask.NetworkTaskListener {
    public ArrayList<Recommendations> record = new ArrayList<>();
    DetailView activity;
    StaggeredGridLayoutManager SGLM;
    private recommendationAdapter ra;

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Getter
    @Bind(R.id.progressBar)
    ProgressBar progressBar;

    public int page = 0;

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putInt("page", page);
        state.putSerializable("list", record);
        super.onSaveInstanceState(state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_details_review, container, false);
        ButterKnife.bind(this, view);

        recyclerView.setHasFixedSize(true);
        SGLM = new StaggeredGridLayoutManager(getMaxColumns(), 1);

        recyclerView.setLayoutManager(SGLM);
        recyclerView.addItemDecoration(new SpacesItemDecoration());
        ra = new recommendationAdapter(activity);
        recyclerView.setAdapter(ra);

        if (state != null) {
            getProgressBar().setVisibility(View.GONE);
            page = state.getInt("page");
            record = (ArrayList<Recommendations>) state.getSerializable("list");
            ra.notifyDataSetChanged();
        }
        return view;
    }

    /**
     * Get the max amount of columns.
     *
     * @return int The amount of max columns
     */
    public int getMaxColumns() {
        int screen;
        if (Theme.isPortrait())
            screen = activity.getResources().getConfiguration().screenHeightDp;
        else
            screen = activity.getResources().getConfiguration().screenWidthDp;
        return screen > 970 ? 2 : 1;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = ((DetailView) activity);
        this.activity.setRecommendations(this);
    }

    /**
     * Set the list with the new page/list.
     */
    @SuppressWarnings("unchecked") // Don't panic, we handle possible class cast exceptions
    @Override
    public void onNetworkTaskFinished(Object result, TaskJob job, ListType type, Bundle data, boolean cancelled) {
        getProgressBar().setVisibility(View.GONE);
        try {
            ArrayList<Recommendations> records = (ArrayList<Recommendations>) result;
            // The activity could be destroyed when this is being loaded because the user pressed back
            if (activity != null && isAdded()) {
                if (result != null && records.size() > 0) {
                    record.addAll(records);
                    ra.notifyDataSetChanged();
                }
            }
        } catch (Exception e) {
            Theme.logTaskCrash(this.getClass().getSimpleName(), "onNetworkTaskFinished()", e);
        }
    }

    @Override
    public void onNetworkTaskError(TaskJob job, ListType type, Bundle data, boolean cancelled) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Theme.Snackbar(activity, R.string.toast_error_Records);
            }
        });
    }

    /**
     * Get the requested records.
     *
     * @param page The page number
     */
    public void getRecords(int page) {
        if (page != this.page)
            this.page = page;
        if (APIHelper.isNetworkAvailable(activity)) {
            Bundle bundle = new Bundle();
            bundle.putInt("page", page);
            int id = activity.isAnime() ? activity.animeRecord.getId() : activity.mangaRecord.getId();
            new NetworkTask(TaskJob.RECOMMENDATION, activity.type, activity, bundle, activity.recommendations).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(id));
        } else {
            Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
        }
    }

    /**
     * The custom adapter for recommendations.
     */
    public class recommendationAdapter extends RecyclerView.Adapter<recommendationAdapterHolder> {
        private Context context;
        private String image;
        private String title;
        private Recommendations recommendation;

        public recommendationAdapter(Context context) {
            this.context = context;
        }

        @Override
        public recommendationAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_details_review, null);
            return new recommendationAdapterHolder(layoutView);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(recommendationAdapterHolder holder, int position) {
            try {
                // Get the precommendation part for the recommendations
                recommendation = record.get(position);

                image = recommendation.getItem().getImageUrl();
                title = recommendation.getItem().getTitle();
                holder.title.setText(WordUtils.capitalize(title));

                holder.subTitle.setText(recommendation.getRecommendations().get(0).getUsername());
                holder.content.setText(Html.fromHtml(recommendation.getRecommendations().get(0).getInformation()));

                Picasso.with(context)
                        .load(image)
                        .transform(new RoundedTransformation(title)).fit()
                        .into(holder.imageView);

            } catch (Exception e) {
                Theme.logTaskCrash(this.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        @Override
        public int getItemCount() {
            return record.size();
        }
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration() {
            this.space = Theme.convert(8);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin for the first item to avoid multiple spaces
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.top = space;
            } else if (getMaxColumns() == 2 && parent.getChildLayoutPosition(view) == 1) {
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
        }
    }

    /**
     * The viewholder for performance.
     */
    public class recommendationAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public TextView subTitle;
        public TextView subTitle2;
        public TextView subTitle3;
        public TextView content;
        public ImageView imageView;
        public RelativeLayout header;

        public recommendationAdapterHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.CardTitle);
            subTitle = (TextView) itemView.findViewById(R.id.Cardsub);
            subTitle2 = (TextView) itemView.findViewById(R.id.Cardsub2);
            subTitle3 = (TextView) itemView.findViewById(R.id.Cardsub3);
            content = (TextView) itemView.findViewById(R.id.content);
            imageView = (ImageView) itemView.findViewById(R.id.coverImage);
            header = (RelativeLayout) itemView.findViewById(R.id.header);

            subTitle2.setVisibility(View.INVISIBLE);
            subTitle3.setVisibility(View.INVISIBLE);

            GradientDrawable shape = (GradientDrawable) header.getBackground();
            shape.setColor(ContextCompat.getColor(activity, R.color.card_green));

            imageView.setOnClickListener(this);
            content.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (APIHelper.isNetworkAvailable(activity)) {
                Intent startDetails = new Intent(activity, DetailView.class);
                startDetails.putExtra("recordID", record.get(getAdapterPosition()).getItem().getId());
                startDetails.putExtra("recordType", activity.type);
                startActivity(startDetails);
            } else {
                Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
            }
        }
    }
}