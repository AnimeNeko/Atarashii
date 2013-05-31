package net.somethingdreadful.MAL;

import java.util.ArrayList;
import java.util.Collection;

import net.somethingdreadful.MAL.record.AnimeRecord;
import net.somethingdreadful.MAL.record.GenericMALRecord;
import net.somethingdreadful.MAL.record.MangaRecord;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
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

public class CoverAdapter<T> extends ArrayAdapter<T> {

    private ArrayList<T> objects;
    private ImageDownloader imageManager;
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
        imageManager = new ImageDownloader(c);
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
        final GenericMALRecord a;

        a = ((GenericMALRecord) objects.get(position));

        String myStatus = a.getMyStatus();

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


        viewHolder.label.setText(a.getName());

        viewHolder.progressCount.setText(Integer.toString(a.getPersonalProgress(useSecondaryAmounts)));

        imageManager.download(a.getImageUrl(), viewHolder.cover);

        if (Build.VERSION.SDK_INT >= 11) {
            if ((a.getMyStatus().equals(AnimeRecord.STATUS_WATCHING)) || (a.getMyStatus().equals(MangaRecord.STATUS_WATCHING))) {
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

                                if (a.getMyStatus().equals(AnimeRecord.STATUS_WATCHING)) {
                                    pm.getMenuInflater().inflate(R.menu.cover_action_menu, pm.getMenu());
                                }
                                if (a.getMyStatus().equals(MangaRecord.STATUS_WATCHING)) {
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

    public void setProgressPlusOne(GenericMALRecord gr) {
        gr.setPersonalProgress(useSecondaryAmounts, gr.getPersonalProgress(useSecondaryAmounts) + 1);

        if (gr.getPersonalProgress(useSecondaryAmounts) == Integer.parseInt(gr.getTotal(useSecondaryAmounts))) {
            gr.setMyStatus(GenericMALRecord.STATUS_COMPLETED);
        }

        notifyDataSetChanged();

        new writeDetailsTask().execute(gr);
    }

    public void setMarkAsComplete(GenericMALRecord gr) {
        gr.setMyStatus(GenericMALRecord.STATUS_COMPLETED);

        new writeDetailsTask().execute(gr);

        objects.remove(gr);

        notifyDataSetChanged();
    }

    public void supportAddAll(Collection<? extends T> collection) {
        for (T record : collection) {
            this.add(record);
        }
    }

    public class writeDetailsTask extends AsyncTask<GenericMALRecord, Void, Boolean> {

        MALManager internalManager;
        String internalType;

        @Override
        protected void onPreExecute() {
            internalManager = mManager;
            internalType = type;
        }

        @Override
        protected Boolean doInBackground(GenericMALRecord... gr) {

            boolean result;

            if ("anime".equals(internalType)) {
                internalManager.saveItem((AnimeRecord) gr[0], false);
                result = internalManager.writeDetailsToMAL(gr[0], MALManager.TYPE_ANIME);
            } else {
                internalManager.saveItem((MangaRecord) gr[0], false);
                result = internalManager.writeDetailsToMAL(gr[0], MALManager.TYPE_MANGA);
            }


            if (result) {
                gr[0].setDirty(GenericMALRecord.CLEAN);

                if ("anime".equals(internalType)) {
                    internalManager.saveItem((AnimeRecord) gr[0], false);
                } else {
                    internalManager.saveItem((MangaRecord) gr[0], false);
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

    static class ViewHolder {
        TextView label;
        TextView progressCount;
        TextView flavourText;
        ImageView cover;
        ImageView actionButton;
    }

}
