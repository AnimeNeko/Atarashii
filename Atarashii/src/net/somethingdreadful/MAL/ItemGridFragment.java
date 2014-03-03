package net.somethingdreadful.MAL;

import java.util.ArrayList;

import net.somethingdreadful.MAL.ItemGridFragmentScrollViewListener.RefreshList;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.tasks.AnimeNetworkTask;
import net.somethingdreadful.MAL.tasks.AnimeNetworkTaskFinishedListener;
import net.somethingdreadful.MAL.tasks.MangaNetworkTask;
import net.somethingdreadful.MAL.tasks.MangaNetworkTaskFinishedListener;
import net.somethingdreadful.MAL.tasks.TaskJob;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
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

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ItemGridFragment extends SherlockFragment implements AnimeNetworkTaskFinishedListener, MangaNetworkTaskFinishedListener {

    // The pixel dimensions used by MAL images
    private static final double MAL_IMAGE_WIDTH = 225;
    private static final double MAL_IMAGE_HEIGHT = 320;

    GridView gv;
    MALManager mManager;
    PrefManager mPrefManager;
    Context c;
    CoverAdapter<Anime> ca;
    CoverAdapter<Manga> cm;
    IItemGridFragment Iready;

    static boolean forceSyncBool = false;
    static boolean home = true;
    boolean useTraditionalList = false;
    boolean useSecondaryAmounts = false;
    int currentList;
    int listColumns;
    int screenWidthDp;
    int gridCellWidth;
    int gridCellHeight;
    String recordType;
    
    TaskJob mode;
    ItemGridFragmentScrollViewListener scrollListener;
    
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        if (state != null) {
            currentList = state.getInt("list", 1);
            useTraditionalList = state.getBoolean("traditionalList");
            useSecondaryAmounts = state.getBoolean("useSecondaryAmounts");
        }
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        Bundle args = getArguments();
        View layout = inflater.inflate(R.layout.fragment_animelist, null);
        c = layout.getContext();
        
        if (home){
        	mManager = ((Home) getActivity()).mManager;
        	mPrefManager = ((Home) getActivity()).mPrefManager;
        	if (!((Home) getActivity()).instanceExists) {
        		currentList = mPrefManager.getDefaultList();
        		useTraditionalList = mPrefManager.getTraditionalListEnabled();
        	}
        }else{
        	mPrefManager = ((SearchActivity) getActivity()).mPrefManager;
        	if (!((SearchActivity) getActivity()).instanceExists) {
        		currentList = mPrefManager.getDefaultList();
        		useTraditionalList = mPrefManager.getTraditionalListEnabled();
        	}
        }

        useSecondaryAmounts = mPrefManager.getUseSecondaryAmountsEnabled();
        final String recordType = args.getString("type");
        gv = (GridView) layout.findViewById(R.id.gridview);
        
        if ("anime".equals(recordType)) {
            gv.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent startDetails = new Intent(getView().getContext(), DetailView.class);
                    startDetails.putExtra("net.somethingdreadful.MAL.recordID", ca.getItem(position).getId());
                    startDetails.putExtra("net.somethingdreadful.MAL.recordType", recordType);

                    startActivity(startDetails);
                }
            });
        } else if ("manga".equals(recordType)) {
            gv.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent startDetails = new Intent(getView().getContext(), DetailView.class);
                    startDetails.putExtra("net.somethingdreadful.MAL.recordID", cm.getItem(position).getId());
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
                screenWidthDp = pxToDp(((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth());
            }

            listColumns = (int) Math.ceil(screenWidthDp / MAL_IMAGE_WIDTH);
            this.gridCellWidth = screenWidthDp / listColumns;
            this.gridCellHeight = (int) Math.ceil(gridCellWidth / (MAL_IMAGE_WIDTH / MAL_IMAGE_HEIGHT));
            Log.v("MALX", "Grid Cell Size for " + recordType + ": " + this.gridCellWidth + "x" + this.gridCellHeight);
        }

        gv.setNumColumns(listColumns);

        gv.setDrawSelectorOnTop(true);

        getRecords(currentList, recordType, false, this.c);
        
        scrollListener = new ItemGridFragmentScrollViewListener(gv,new RefreshList(){
        	@Override
            public void onRefresh(int pageNumber, ListType listType) {
                try{
                    // not all jobs return paged results
                    if ( mode != null && mode != TaskJob.GETLIST ) {
                        switch (listType) {
                            case ANIME:
                                AnimeNetworkTask animetask = new AnimeNetworkTask(mode,pageNumber, c, ItemGridFragment.this);
                                animetask.execute(BaseActionBarSearchView.query);
                                break;
                            case MANGA:
                                MangaNetworkTask mangatask = new MangaNetworkTask(mode,pageNumber, c, ItemGridFragment.this);
                                mangatask.execute(BaseActionBarSearchView.query);
                                break;
                            default:
                                Log.e("MALX", "invalid list type: " + listType.name());
                        }
                    }
                } catch (Exception e){
                    Log.e("MALX", "onRefresh() error: " + e.getMessage());
                }
            }
        });
        gv.setOnScrollListener(scrollListener);

        Iready.fragmentReady();

        return layout;
    }

    public void getRecords(int listint, String mediaType, boolean forceSync, Context c) {
        currentList = listint;
        recordType = mediaType;

        // Don't use forceSyncBool = forceSync! We don't wan't to set this to false here!
        if  (forceSync)
            forceSyncBool = true;

        if (recordType.equals("anime")) {
        	new AnimeNetworkTask(forceSync ? TaskJob.FORCESYNC : TaskJob.GETLIST, c, this).execute(MALManager.listSortFromInt(listint, "anime"));
        } else if (recordType.equals("manga")) {
        	new MangaNetworkTask(forceSync ? TaskJob.FORCESYNC : TaskJob.GETLIST, c, this).execute(MALManager.listSortFromInt(listint, "manga"));
        }
    }

    public void setAnimeRecords(ArrayList<Anime> objects){
    	CoverAdapter<Anime> adapter = ca;
    	if (adapter == null){
    		int list_cover_item = R.layout.grid_cover_with_text_item;
    		if (useTraditionalList){
    			list_cover_item = R.layout.list_cover_with_text_item;
    		}
    		adapter = new CoverAdapter<Anime>(c,list_cover_item,objects,mManager, MALManager.TYPE_ANIME,this.gridCellHeight,useSecondaryAmounts);
    		
    	}
    	if (gv.getAdapter() == null){
    		gv.setAdapter(adapter);
    	}else{
    	    scrollToTop();
    	    adapter.clear();
    		adapter.supportAddAll(objects);
    		adapter.notifyDataSetChanged();
    		scrollListener.resetPageNumber();
    	}
    	ca = adapter;
    }
    
    public void setMangaRecords(ArrayList<Manga> objects) {
        CoverAdapter<Manga> adapter = cm;
        if (adapter == null) {
            int list_cover_item = R.layout.grid_cover_with_text_item;
            if (useTraditionalList) {
                list_cover_item = R.layout.list_cover_with_text_item;
            }
            adapter = new CoverAdapter<Manga>(c, list_cover_item, objects, mManager, MALManager.TYPE_MANGA, this.gridCellHeight, useSecondaryAmounts);
        }
        if (gv.getAdapter() == null) {
            gv.setAdapter(adapter);
        } else {
            scrollToTop();
            adapter.clear();
            adapter.supportAddAll(objects);
            adapter.notifyDataSetChanged();
            scrollListener.resetPageNumber();
        }
        cm = adapter;
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
    
    public void setMode(TaskJob mode){
    	this.mode = mode;
    }
    
    public TaskJob getMode(){
    	return(this.mode);
    }
    
    public void scrollToTop(){
        // smoothScrollToPosition and scrollBy() are not working very well with lazy loading
    	gv.setSelection(0);
    }

	@Override
	public void onMangaNetworkTaskFinished(ArrayList<Manga> result, TaskJob job, int page) {

	    if (result != null) {
			if (result.size() == 0) {
				Log.w("MALX", "No manga records returned.");
			} else if ( job != null && job != TaskJob.GETLIST && job != TaskJob.FORCESYNC ){
			    // not all jobs return paged results
                scrollListener.notifyMorePages(ListType.MANGA);
            }
	        
			if (cm == null) {
	            if (useTraditionalList) {
	                cm = new CoverAdapter<Manga>(c, R.layout.list_cover_with_text_item, result, mManager, MALManager.TYPE_MANGA, this.gridCellHeight, useSecondaryAmounts);
	            } else {
	                cm = new CoverAdapter<Manga>(c, R.layout.grid_cover_with_text_item, result, mManager, MALManager.TYPE_MANGA, this.gridCellHeight, useSecondaryAmounts);
	            }
	        }
	
	        if (gv.getAdapter() == null) {
	            gv.setAdapter(cm);
	        } else {
	            if ( page == 1 )
	                cm.clear();
	            cm.supportAddAll(result);
	            cm.notifyDataSetChanged();
	        }

	        if (forceSyncBool)
	            Crouton.makeText((Activity)c, R.string.toast_SyncDone, Style.CONFIRM).show();
		} else {
			if (forceSyncBool)
				Crouton.makeText(this.getActivity(), R.string.crouton_Manga_Sync_error, Style.ALERT).show();
        }

	    if (job != null && job == TaskJob.FORCESYNC) {
            NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(R.id.notification_sync);
	        forceSyncBool = false;
        }
	}

	@Override
	public void onAnimeNetworkTaskFinished(ArrayList<Anime> result, TaskJob job, int page) {

		if (result != null) {
			if (result.size() == 0) {
				Log.w("MALX", "No anime records returned.");
	        } else if ( job != null && job != TaskJob.GETLIST && job != TaskJob.FORCESYNC ) {
	            // not all jobs return paged results
	            scrollListener.notifyMorePages(ListType.ANIME);
	        }

			if (ca == null) {
	            if (useTraditionalList) {
	                ca = new CoverAdapter<Anime>(c, R.layout.list_cover_with_text_item, result, mManager, MALManager.TYPE_ANIME, this.gridCellHeight, useSecondaryAmounts);
	            } else {
	                ca = new CoverAdapter<Anime>(c, R.layout.grid_cover_with_text_item, result, mManager, MALManager.TYPE_ANIME, this.gridCellHeight, useSecondaryAmounts);
	            }
	        }
	
	        if (gv.getAdapter() == null) {
	            gv.setAdapter(ca);
	        } else {
	            if ( page == 1 )
	                ca.clear();
	            ca.supportAddAll(result);
	            ca.notifyDataSetChanged();
	        }

	        if (forceSyncBool)
	        	Crouton.makeText((Activity)c, R.string.toast_SyncDone, Style.CONFIRM).show();
    	} else {
    		if (forceSyncBool)
    			Crouton.makeText(this.getActivity(), R.string.crouton_Anime_Sync_error, Style.ALERT).show();
        }

		if (job != null && job == TaskJob.FORCESYNC) {
    		NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(R.id.notification_sync);
            forceSyncBool = false;
		}
	}
}
