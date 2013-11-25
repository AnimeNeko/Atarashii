package net.somethingdreadful.MAL;

import java.util.ArrayList;
import java.util.Collection;

import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.GenericRecord;
import net.somethingdreadful.MAL.api.response.Manga;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class CoverAdapter<T> extends ArrayAdapter<T> {

    private ArrayList<T> objects;
    private Context c;
    private MALManager mManager;
    private String type;
    private int resource;
    private int imageCoverHeight = 0;
    private boolean useSecondaryAmounts;


    public CoverAdapter(Context context, int resource, ArrayList<T> objects, MALManager m, String type, int coverheight, boolean useSecondaryAmounts) {
        super(context, resource, objects);
        this.objects = objects;
        this.c = context;
        mManager = m;
        this.type = type;
        this.resource = resource;
        this.imageCoverHeight = coverheight;
        this.useSecondaryAmounts = useSecondaryAmounts;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder viewHolder;
        final GenericRecord a;
        final String myStatus;
        int progress;

        a = ((GenericRecord) objects.get(position));
        if (type.equals(MALManager.TYPE_ANIME)) {
        	myStatus = ((Anime) a).getWatchedStatus();
        	progress = ((Anime) a).getWatchedEpisodes();
        } else {
        	myStatus = ((Manga) a).getReadStatus();
        	progress = ((Manga) a).getProgress(useSecondaryAmounts);
        }

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(resource, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.label = (TextView) v.findViewById(R.id.animeName);
            viewHolder.progressCount = (TextView) v.findViewById(R.id.watchedCount);
            viewHolder.cover = (ImageView) v.findViewById(R.id.coverImage);
            viewHolder.actionButton = (ImageView) v.findViewById(R.id.popUpButton);
            viewHolder.flavourText = (TextView) v.findViewById(R.id.stringWatched);

            v.setTag(viewHolder);

            if (this.imageCoverHeight > 0) {
                v.getLayoutParams().height = dpToPx(this.imageCoverHeight);
            }
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }


        viewHolder.label.setText(a.getTitle());

        viewHolder.progressCount.setText(Integer.toString(progress));

        Picasso coverImage = Picasso.with(c);

        coverImage
        .load(a.getImageUrl())
        .error(R.drawable.cover_error)
        .placeholder(R.drawable.cover_loading)
        .fit()
        .into(viewHolder.cover);

        if (Build.VERSION.SDK_INT >= 11) {
            if ((myStatus.equals(Anime.STATUS_WATCHING)) || (myStatus.equals(Manga.STATUS_READING))) {
                viewHolder.actionButton.setVisibility(View.VISIBLE);
                viewHolder.actionButton.setOnClickListener(
                        new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                showPopupMenu(v);
                            }

                            @SuppressLint("NewApi")
                            private void showPopupMenu(View v) {
                                PopupMenu pm = new PopupMenu(c, v);

                                if (myStatus.equals(Anime.STATUS_WATCHING)) {
                                    pm.getMenuInflater().inflate(R.menu.cover_action_menu, pm.getMenu());
                                }
                                if (myStatus.equals(Manga.STATUS_READING)) {
                                    pm.getMenuInflater().inflate(R.menu.cover_action_menu_manga, pm.getMenu());
                                }

                                pm.setOnMenuItemClickListener(
                                        new OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {
                                                switch (item.getItemId()) {
                                                    case R.id.action_PlusOneWatched:
                                                        setProgressPlusOne(a);
                                                        break;

                                                    case R.id.action_MarkAsComplete:
                                                        setMarkAsComplete(a);
                                                        break;
                                                }

                                                return true;
                                            }
                                        });

                                pm.show();

                            }

                        });
            } else {
                viewHolder.actionButton.setVisibility(View.INVISIBLE);
            }
        } else {
            viewHolder.actionButton.setVisibility(View.INVISIBLE);

            //Compatibility with setting the overlay alpha pre-API 11
            ImageView overlayPanel = (ImageView) v.findViewById(R.id.textOverlayPanel);
            overlayPanel.setAlpha(175);
        }


        TextView flavourText = (TextView) v.findViewById(R.id.stringWatched);

        if (myStatus.equals("watching")) {
            flavourText.setText(R.string.cover_Watching);
            viewHolder.progressCount.setVisibility(View.VISIBLE);
        }
        else if (myStatus.equals("reading")) {
            flavourText.setText(R.string.cover_Reading);
        }
        else if (myStatus.equals("completed")) {
            flavourText.setText(R.string.cover_Completed);
            viewHolder.progressCount.setVisibility(View.GONE);
        }
        else if (myStatus.equals("on-hold")) {
            flavourText.setText(R.string.cover_OnHold);
            viewHolder.progressCount.setVisibility(View.VISIBLE);
        }
        else if (myStatus.equals("dropped")) {
            flavourText.setText(R.string.cover_Dropped);
            viewHolder.progressCount.setVisibility(View.GONE);
        }
        else if (myStatus.equals("plan to watch")) {
            flavourText.setText(R.string.cover_PlanningToWatch);
            viewHolder.progressCount.setVisibility(View.GONE);
        }
        else if (myStatus.equals("plan to read")) {
            flavourText.setText(R.string.cover_PlanningToRead);
            viewHolder.progressCount.setVisibility(View.GONE);
        }
        else {
            flavourText.setText("");
            viewHolder.progressCount.setVisibility(View.GONE);
        }



        return v;
    }

    public void setProgressPlusOne(GenericRecord gr) {
    	if (type.equals(MALManager.TYPE_ANIME)) {
    		((Anime) gr).setWatchedEpisodes(((Anime) gr).getWatchedEpisodes() + 1);
    		if (((Anime) gr).getWatchedEpisodes() == ((Anime) gr).getEpisodes())
    			((Anime) gr).setWatchedStatus(GenericRecord.STATUS_COMPLETED);
    	} else {
    		((Manga) gr).setProgress(useSecondaryAmounts, ((Manga) gr).getProgress(useSecondaryAmounts) + 1);
    		if (((Manga) gr).getProgress(useSecondaryAmounts) == ((Manga) gr).getTotal(useSecondaryAmounts))
    			((Manga) gr).setReadStatus(GenericRecord.STATUS_COMPLETED);
    	}
        gr.setDirty(true);

        notifyDataSetChanged();

        new writeDetailsTask().execute(gr);
    }

    public void setMarkAsComplete(GenericRecord gr) {
    	if (type.equals(MALManager.TYPE_ANIME))
    		((Anime) gr).setWatchedStatus(GenericRecord.STATUS_COMPLETED);
    	else
    		((Manga) gr).setReadStatus(GenericRecord.STATUS_COMPLETED);
        gr.setDirty(true);

        new writeDetailsTask().execute(gr);

        objects.remove(gr);

        notifyDataSetChanged();
    }

    public void supportAddAll(Collection<? extends T> collection) {
        for (T record : collection) {
            this.add(record);
        }
    }

    public class writeDetailsTask extends AsyncTask<GenericRecord, Void, Boolean> {

        MALManager internalManager;
        String internalType;

        @Override
        protected void onPreExecute() {
            internalManager = mManager;
            internalType = type;
        }

        @Override
        protected Boolean doInBackground(GenericRecord... gr) {

            boolean result;

            if (MALManager.TYPE_ANIME.equals(internalType)) {
                internalManager.saveAnimeToDatabase((Anime) gr[0], false);
            } else {
                internalManager.saveMangaToDatabase((Manga) gr[0], false);
            }

            if (isNetworkAvailable()) {
                if (MALManager.TYPE_ANIME.equals(internalType)) {
                    result = internalManager.writeAnimeDetailsToMAL((Anime) gr[0]);
                } else {
                    result = internalManager.writeMangaDetailsToMAL((Manga) gr[0]);
                }
            }
            else {
                result = false;
            }


            if (result) {
                gr[0].setDirty(false);

                if (MALManager.TYPE_ANIME.equals(internalType)) {
                    internalManager.saveAnimeToDatabase((Anime) gr[0], false);
                } else {
                    internalManager.saveMangaToDatabase((Manga) gr[0], false);
                }
            }
            return result;
        }

    }

    public int dpToPx(float dp) {
        Resources resources = c.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        else {
            return false;
        }

    }

    static class ViewHolder {
        TextView label;
        TextView progressCount;
        TextView flavourText;
        ImageView cover;
        ImageView actionButton;
    }

}
