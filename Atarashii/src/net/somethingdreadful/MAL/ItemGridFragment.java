package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
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
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import net.somethingdreadful.MAL.record.AnimeRecord;
import net.somethingdreadful.MAL.record.MangaRecord;

import java.util.ArrayList;

public class ItemGridFragment extends SherlockFragment {

    // The pixel dimensions used by MAL images
    private static final double MAL_IMAGE_WIDTH = 225;
    private static final double MAL_IMAGE_HEIGHT = 320;

    public ItemGridFragment() {
    }

    ArrayList<AnimeRecord> al = new ArrayList<>();
    ArrayList<MangaRecord> ml = new ArrayList<>();
    GridView gv;
    MALManager mManager;
    PrefManager mPrefManager;
    Context c;
    CoverAdapter<AnimeRecord> ca;
    CoverAdapter<MangaRecord> cm;
    IItemGridFragment Iready;
    boolean forceSyncBool = false;
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
        c = layout.getContext();

        mManager = ((Home) getActivity()).mManager;
        mPrefManager = ((Home) getActivity()).mPrefManager;

        final String recordType = args.getString("type");

        if (!((Home) getActivity()).instanceExists) {
            currentList = mPrefManager.getDefaultList();
            useTraditionalList = mPrefManager.getTraditionalListEnabled();
        }


        int orientation = layout.getContext().getResources().getConfiguration().orientation;

        gv = (GridView) layout.findViewById(R.id.gridview);


        if ("anime".equals(recordType)) {
            gv.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent startDetails = new Intent(getView().getContext(), DetailView.class);
                    startDetails.putExtra("net.somethingdreadful.MAL.recordID", ca.getItem(position).getID());
                    startDetails.putExtra("net.somethingdreadful.MAL.recordType", recordType);

                    startActivity(startDetails);

                    //				Toast.makeText(context, animeRecordCoverAdapter.getItem(position).getID(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if ("manga".equals(recordType)) {
            gv.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent startDetails = new Intent(getView().getContext(), DetailView.class);
                    startDetails.putExtra("net.somethingdreadful.MAL.recordID", cm.getItem(position).getID());
                    startDetails.putExtra("net.somethingdreadful.MAL.recordType", recordType);

                    startActivity(startDetails);
                    //				Toast.makeText(context, animeRecordCoverAdapter.getItem(position).getID(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (useTraditionalList) {
            listColumns = 1;
        } else {
            try {
                screenWidthDp = layout.getContext().getResources().getConfiguration().screenWidthDp;
            } catch (NoSuchFieldError e) {
                screenWidthDp = pxToDp(((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth());
            }

            listColumns = (int) Math.ceil(screenWidthDp / MAL_IMAGE_WIDTH);
            this.gridCellWidth = screenWidthDp / listColumns;
            this.gridCellHeight = (int) Math.ceil(gridCellWidth / (MAL_IMAGE_WIDTH / MAL_IMAGE_HEIGHT));
            Log.v("MALX", "Grid Cell Size for " + recordType + ": " + this.gridCellWidth + "x" + this.gridCellHeight);
        }

        gv.setNumColumns(listColumns);

        gv.setDrawSelectorOnTop(true);

        getRecords(currentList, recordType, false);

        Iready.fragmentReady();

        return layout;

    }

    public void getRecords(int listint, String mediaType, boolean forceSync) {
        forceSyncBool = forceSync;
        currentList = listint;
        recordType = mediaType;

        if (recordType.equals("anime")) {
            new getAnimeRecordsTask(this.gridCellHeight).execute(currentList);
        } else if (recordType.equals("manga")) {
            new getMangaRecordsTask(this.gridCellHeight).execute(currentList);
        }
    }

    public class getAnimeRecordsTask extends AsyncTask<Integer, Void, ArrayList<AnimeRecord>> {

        boolean mForceSync = forceSyncBool;
        boolean mTraditionalList = useTraditionalList;
        String type = recordType;
        MALManager internalManager = mManager;
        int gridCellHeight;

        getAnimeRecordsTask(int imageHeight) {
            this.gridCellHeight = imageHeight;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected ArrayList<AnimeRecord> doInBackground(Integer... list) {

            int listint = 0;

            for (int i : list) {
                listint = i;
            }

            if (mForceSync) {
                al = new ArrayList();
                if (mManager == null) {
                    Log.w("MALX", "mManager is null. Attempting to re-create the object.");

                    try {
                        mManager = new MALManager(c);
                    } finally {
                        Log.v("MALX", "Successfully re-created mManager");
                    }
                }
                mManager.downloadAndStoreList(MALManager.TYPE_ANIME);

            }
            al = mManager.getAnimeRecordsFromDB(listint);

            return al;
        }

        @Override
        protected void onPostExecute(ArrayList<AnimeRecord> result) {

            if (result == null) {
                result = new ArrayList<>();
            }
            if (ca == null) {
                if (mTraditionalList) {
                    ca = new CoverAdapter<>(c, R.layout.list_cover_with_text_item, result, internalManager, type, this.gridCellHeight);
                } else {
                    ca = new CoverAdapter<>(c, R.layout.grid_cover_with_text_item, result, internalManager, type, this.gridCellHeight);

                }
            }

            if (gv.getAdapter() == null) {
                gv.setAdapter(ca);
            } else {
                ca.clear();
                ca.supportAddAll(result);
                ca.notifyDataSetChanged();
            }

            if (mForceSync) {
                Toast.makeText(c, R.string.toast_SyncDone, Toast.LENGTH_SHORT).show();
                NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(R.id.notification_sync);
            }

        }

    }

    public class getMangaRecordsTask extends AsyncTask<Integer, Void, ArrayList<MangaRecord>> {
        boolean mForceSync = forceSyncBool;
        boolean mTraditionalList = useTraditionalList;
        String type = recordType;
        MALManager internalManager = mManager;
        int gridCellHeight;

        getMangaRecordsTask(int imageHeight) {
            this.gridCellHeight = imageHeight;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        protected ArrayList<MangaRecord> doInBackground(Integer... list) {
            int listint = 0;
            for (int i : list) {
                listint = i;
            }
            if (mForceSync) {
                al = new ArrayList();
                if (mManager == null) {
                    Log.w("MALX", "mManager is null. Attempting to re-create the object.");

                    try {
                        mManager = new MALManager(c);
                    } finally {
                        Log.v("MALX", "Successfully re-created mManager");
                    }
                }
                mManager.downloadAndStoreList(MALManager.TYPE_MANGA);
            }

            ml = mManager.getMangaRecordsFromDB(listint);

            return ml;
        }

        @Override
        protected void onPostExecute(ArrayList<MangaRecord> result) {

            if (result == null) {
                result = new ArrayList<>();
            }
            if (cm == null) {
                if (mTraditionalList) {
                    cm = new CoverAdapter<>(c, R.layout.list_cover_with_text_item, result, internalManager, type, this.gridCellHeight);
                } else {
                    cm = new CoverAdapter<>(c, R.layout.grid_cover_with_text_item, result, internalManager, type, this.gridCellHeight);
                }
            }

            if (gv.getAdapter() == null) {
                gv.setAdapter(cm);
            } else {
                cm.clear();
                cm.supportAddAll(result);
                cm.notifyDataSetChanged();
            }

            if (mForceSync) {
                Toast.makeText(c, R.string.toast_SyncDone, Toast.LENGTH_SHORT).show();
                NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(R.id.notification_sync);
            }
        }

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
        Iready = (IItemGridFragment) a;

    }

    public interface IItemGridFragment {
        public void fragmentReady();
    }

    public int pxToDp(int px) {
        Resources resources = c.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (px / (metrics.density));
    }
}