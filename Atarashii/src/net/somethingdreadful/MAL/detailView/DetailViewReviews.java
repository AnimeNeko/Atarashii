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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.DateTools;
import net.somethingdreadful.MAL.DetailView;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.RoundedTransformation;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.APIHelper;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.tasks.NetworkTask;
import net.somethingdreadful.MAL.tasks.TaskJob;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DetailViewReviews extends Fragment implements NetworkTask.NetworkTaskListener {
    public ArrayList<Reviews> record = new ArrayList<>();
    DetailView activity;
    StaggeredGridLayoutManager SGLM;
    private reviewAdapter ra;

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    public int page = 0;
    private boolean loading = false;
    private boolean hasmorepages = true;

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
        recyclerView.addOnScrollListener(new OnScrollListener(SGLM));
        recyclerView.addItemDecoration(new SpacesItemDecoration());
        ra = new reviewAdapter(activity);
        recyclerView.setAdapter(ra);

        if (state != null) {
            page = state.getInt("page");
            record = (ArrayList<Reviews>) state.getSerializable("list");
            ra.notifyDataSetChanged();
        } else if (page == 0 && !activity.isEmpty()) {
            getRecords(1);
        }
        return view;
    }

    /**
     * Get the max amount of columns.
     *
     * @return int The amount of max columns
     */
    public int getMaxColumns() {
        int screen = 0;
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
        this.activity.setReviews(this);
    }

    /**
     * Set the list with the new page/list.
     */
    @SuppressWarnings("unchecked") // Don't panic, we handle possible class cast exceptions
    @Override
    public void onNetworkTaskFinished(Object result, TaskJob job, ListType type, Bundle data, boolean cancelled) {
        try {
            // The activity could be destroyed when this is being loaded because the user pressed back
            if (activity != null && isAdded()) {
                if (result != null) {
                    record.addAll((Collection<? extends Reviews>) result);
                    loading = false;
                    ra.notifyDataSetChanged();
                } else {
                    hasmorepages = false;
                    Theme.Snackbar(activity, R.string.toast_error_reviews);
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
                Theme.Snackbar(activity, R.string.toast_error_reviews);
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
            //TODO toggle(true);
            this.page = page;
        loading = true;
        if (APIHelper.isNetworkAvailable(activity)) {
            Bundle bundle = new Bundle();
            bundle.putInt("page", page);
            int id = activity.isAnime() ? activity.animeRecord.getId() : activity.mangaRecord.getId();
            new NetworkTask(TaskJob.REVIEWS, activity.type, activity, bundle, activity.reviews, activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(id));
        } else {
            Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
        }
    }

    /**
     * The custom adapter for reviews.
     */
    public class reviewAdapter extends RecyclerView.Adapter<reviewAdapterHolder> {
        private Context context;
        private String rating;
        private String chapseen;
        private String episeen;
        private String image;
        private String title;
        private Reviews review;

        public reviewAdapter(Context context) {
            this.context = context;
            rating = context.getString(R.string.card_content_rating);
            chapseen = context.getString(R.string.card_content_chapseen);
            episeen = context.getString(R.string.card_content_episeen);
        }

        @Override
        public reviewAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_details_review, null);
            return new reviewAdapterHolder(layoutView);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(reviewAdapterHolder holder, int position) {
            try {
                // Get the preview part for the reviews
                review = record.get(position);

                image = review.getUser().getImageUrl();
                title = review.getUser().getUsername();
                holder.title.setText(WordUtils.capitalize(title));
                holder.subTitle.setText(DateTools.parseDate(review.getDate(), !AccountService.isMAL()));
                holder.subTitle2.setText(rating + " " + review.getRating() + (!AccountService.isMAL() ? "/100" : ""));
                holder.subTitle3.setText(activity.isAnime() ? review.getEpisodesSeen(episeen) : review.getChaptersRead(chapseen));
                holder.content.setText(Html.fromHtml(review.getShortReview()));

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

    /**
     * The infinite scrolling mechanism.
     */
    public class OnScrollListener extends RecyclerView.OnScrollListener {
        int firstVisibleItem, visibleItemCount, totalItemCount;
        private StaggeredGridLayoutManager layoutManager;

        public OnScrollListener(StaggeredGridLayoutManager layoutManager) {
            this.layoutManager = layoutManager;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            visibleItemCount = recyclerView.getChildCount();
            totalItemCount = layoutManager.getItemCount();
            int[] firstVisibleItems = null;
            firstVisibleItems = layoutManager.findFirstVisibleItemPositions(firstVisibleItems);
            if (firstVisibleItems != null && firstVisibleItems.length > 0)
                firstVisibleItem = firstVisibleItems[0];

            // don't do anything if there is nothing in the list
            if (firstVisibleItem == 0 && visibleItemCount == 0 && totalItemCount == 0)
                return;
            if (totalItemCount - firstVisibleItem <= (visibleItemCount * 2) && !loading && hasmorepages && AccountService.isMAL()) {
                loading = true;
                page++;
                getRecords(page);
            }
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
    public class reviewAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public TextView subTitle;
        public TextView subTitle2;
        public TextView subTitle3;
        public TextView content;
        public ImageView imageView;
        public RelativeLayout header;

        public reviewAdapterHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.CardTitle);
            subTitle = (TextView) itemView.findViewById(R.id.Cardsub);
            subTitle2 = (TextView) itemView.findViewById(R.id.Cardsub2);
            subTitle3 = (TextView) itemView.findViewById(R.id.Cardsub3);
            content = (TextView) itemView.findViewById(R.id.content);
            imageView = (ImageView) itemView.findViewById(R.id.coverImage);
            header = (RelativeLayout) itemView.findViewById(R.id.header);

            GradientDrawable shape = (GradientDrawable) header.getBackground();
            shape.setColor(ContextCompat.getColor(activity, R.color.card_green));

            imageView.setOnClickListener(this);
            content.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.coverImage: // Show the profile
                    if (APIHelper.isNetworkAvailable(activity)) {
                        Intent profile = new Intent(activity, net.somethingdreadful.MAL.ProfileActivity.class);
                        profile.putExtra("username", record.get(getAdapterPosition()).getUser().getUsername());
                        startActivity(profile);
                    } else {
                        Theme.Snackbar(activity, R.string.toast_error_noConnectivity);
                    }
                    break;
                case R.id.content: //Expand the content after click
                    TextView content = (TextView) view;
                    content.setText(Html.fromHtml(record.get(getAdapterPosition()).getReview()));
                    break;
            }
        }
    }
}