package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import com.actionbarsherlock.app.SherlockFragment;
import net.somethingdreadful.MAL.record.AnimeRecord;
import net.somethingdreadful.MAL.record.MangaRecord;

import java.util.ArrayList;

public class BaseItemGridFragment extends SherlockFragment {

    // The pixel dimensions used by MAL images
    private static final double MAL_IMAGE_WIDTH = 225;
    private static final double MAL_IMAGE_HEIGHT = 320;

    public BaseItemGridFragment() {
    }

    GridView gridView;
    MALManager mManager;
    PrefManager mPrefManager;
    Context context;
    CoverAdapter<AnimeRecord> animeRecordCoverAdapter;
    CoverAdapter<MangaRecord> mangaRecordCoverAdapter;
    IBaseItemGridFragment Iready;
    boolean useTraditionalList = false;
    int currentList;
    int listColumns;
    int screenWidthDp;
    int gridCellWidth;
    int gridCellHeight;
    String recordType;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        if (state != null) {
            currentList = state.getInt("list", 1);
            useTraditionalList = state.getBoolean("traditionalList");
        }

    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        View layout = inflater.inflate(R.layout.fragment_animelist, null);
        context = layout.getContext();

        SearchActivity activity = (SearchActivity) getActivity();

        mManager = activity.mManager;
        mPrefManager = activity.mPrefManager;

        useTraditionalList = mPrefManager.getTraditionalListEnabled();

        final String recordType = args.getString("type");

        int orientation = layout.getContext().getResources().getConfiguration().orientation;

        gridView = (GridView) layout.findViewById(R.id.gridview);


        if ("anime".equals(recordType)) {
            gridView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent startDetails = new Intent(getView().getContext(), DetailView.class);
                    startDetails.putExtra("net.somethingdreadful.MAL.recordID", animeRecordCoverAdapter.getItem(position).getID());
                    startDetails.putExtra("net.somethingdreadful.MAL.recordType", recordType);
                    startActivity(startDetails);
                }
            });
        } else if ("manga".equals(recordType)) {
            gridView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent startDetails = new Intent(getView().getContext(), DetailView.class);
                    startDetails.putExtra("net.somethingdreadful.MAL.recordID", mangaRecordCoverAdapter.getItem(position).getID());
                    startDetails.putExtra("net.somethingdreadful.MAL.recordType", recordType);
                    startActivity(startDetails);
                }
            });
        }

        if (useTraditionalList) {
            listColumns = 1;
        } else {
            try {
                screenWidthDp = layout.getContext().getResources().getConfiguration().screenWidthDp;
            } catch (NoSuchFieldError e) {
                screenWidthDp = pxToDp(((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth());
            }

            listColumns = (int) Math.ceil(screenWidthDp / MAL_IMAGE_WIDTH);
            this.gridCellWidth = screenWidthDp / listColumns;
            this.gridCellHeight = (int) Math.ceil(gridCellWidth / (MAL_IMAGE_WIDTH / MAL_IMAGE_HEIGHT));
            Log.v("MALX", "Grid Cell Size for " + recordType + ": " + this.gridCellWidth + "x" + this.gridCellHeight);
        }

        gridView.setNumColumns(listColumns);

        gridView.setDrawSelectorOnTop(true);

        Iready.fragmentReady();

        return layout;

    }

    public void setAnimeRecords(ArrayList<AnimeRecord> objects) {
        CoverAdapter<AnimeRecord> adapter = animeRecordCoverAdapter;
        if (adapter == null) {
            int list_cover_item = R.layout.grid_cover_with_text_item;
            if (useTraditionalList) {
                list_cover_item = R.layout.list_cover_with_text_item;
            }
            adapter = new CoverAdapter<AnimeRecord>(context, list_cover_item, objects, mManager, recordType, this.gridCellHeight);
        }
        if (gridView.getAdapter() == null) {
            gridView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.supportAddAll(objects);
            adapter.notifyDataSetChanged();
        }
        animeRecordCoverAdapter = adapter;

    }

    public void setMangaRecords(ArrayList<MangaRecord> objects) {
        CoverAdapter<MangaRecord> adapter = mangaRecordCoverAdapter;
        if (adapter == null) {
            int list_cover_item = R.layout.grid_cover_with_text_item;
            if (useTraditionalList) {
                list_cover_item = R.layout.list_cover_with_text_item;
            }
            adapter = new CoverAdapter<MangaRecord>(context, list_cover_item, objects, mManager, recordType, this.gridCellHeight);
        }
        if (gridView.getAdapter() == null) {
            gridView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.supportAddAll(objects);
            adapter.notifyDataSetChanged();
        }
        mangaRecordCoverAdapter = adapter;

    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putInt("list", currentList);
        state.putBoolean("traditionalList", useTraditionalList);

        super.onSaveInstanceState(state);
    }

    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        Iready = (IBaseItemGridFragment) a;

    }

    public interface IBaseItemGridFragment {
        public void fragmentReady();
    }

    public int pxToDp(int px) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (px / (metrics.density));
    }
}