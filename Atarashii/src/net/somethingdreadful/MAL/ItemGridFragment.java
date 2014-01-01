package net.somethingdreadful.MAL;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.somethingdreadful.MAL.ItemGridFragmentScrollViewListener.RefreshList;
import net.somethingdreadful.MAL.api.BaseMALApi;
import net.somethingdreadful.MAL.api.BaseMALApi.ListType;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.record.AnimeRecord;
import net.somethingdreadful.MAL.record.MangaRecord;
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

import com.actionbarsherlock.app.SherlockFragment;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ItemGridFragment extends SherlockFragment {

    // The pixel dimensions used by MAL images
    private static final double MAL_IMAGE_WIDTH = 225;
    private static final double MAL_IMAGE_HEIGHT = 320;

    public ItemGridFragment() {
    }

    ArrayList<AnimeRecord> al = new ArrayList<AnimeRecord>();
    ArrayList<MangaRecord> ml = new ArrayList<MangaRecord>();
    GridView gv;
    MALManager mManager;
    PrefManager mPrefManager;
    Context c;
    CoverAdapter<AnimeRecord> ca;
    CoverAdapter<MangaRecord> cm;
    IItemGridFragment Iready;
    boolean forceSyncBool = false;
    boolean useTraditionalList = false;
    boolean useSecondaryAmounts = false;
    int currentList;
    int listColumns;
    int screenWidthDp;
    int gridCellWidth;
    int gridCellHeight;
    String recordType;
    
    int mode = 0; //0 = home, 1 = top rated, 2 = most popular, so the endless gridview only applies to the popular/top rated mode
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

        mManager = ((Home) getActivity()).mManager;
        mPrefManager = ((Home) getActivity()).mPrefManager;

        useSecondaryAmounts = mPrefManager.getUseSecondaryAmountsEnabled();

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

        getRecords(currentList, recordType, false, this.c);
        
        scrollListener = new ItemGridFragmentScrollViewListener(gv,new RefreshList(){
        	@Override
        	public void onRefresh(int pageNumber, ListType listType) {
        		try{
        			switch (listType) {
        				case ANIME: {
        					if (mode == 1){
        						networkThread animethread = new networkThread(1,pageNumber);
        						animethread.setListType(BaseMALApi.ListType.ANIME);
        						animethread.execute();
        					} else if (mode == 2){
        						networkThread animethread = new networkThread(2,pageNumber);
        						animethread.setListType(BaseMALApi.ListType.ANIME);
                    			animethread.execute();
        					} else if (mode == 3){
        						networkThread animethread = new networkThread(3,pageNumber);
        						animethread.setListType(BaseMALApi.ListType.ANIME);
        						animethread.execute();   
        					} else if (mode == 4){
        						networkThread animethread = new networkThread(4,pageNumber);
        						animethread.setListType(BaseMALApi.ListType.ANIME);
        						animethread.execute();  
        					}
        					break;
        				}
        				case MANGA: {
        					if (mode == 1){
        						networkThread mangathread = new networkThread(1,pageNumber);
        						mangathread.setListType(BaseMALApi.ListType.MANGA);
        						mangathread.execute();
        					} else if (mode == 2){
        						networkThread mangathread = new networkThread(2,pageNumber);
        						mangathread.setListType(BaseMALApi.ListType.MANGA);
        						mangathread.execute();
        					} else if (mode == 3){
        						networkThread mangathread = new networkThread(3,pageNumber);
        						mangathread.setListType(BaseMALApi.ListType.MANGA);
        						mangathread.execute();   
        					} else if (mode == 4){
        						networkThread mangathread = new networkThread(4,pageNumber);
        						mangathread.setListType(BaseMALApi.ListType.MANGA);
        						mangathread.execute();  
        					}
        					break;
        				}
                    }
        		} catch (Exception e){
        			
        		}
            }
        });
        gv.setOnScrollListener(scrollListener);

        Iready.fragmentReady();

        return layout;
    }

    public void getRecords(int listint, String mediaType, boolean forceSync, Context c) {
        forceSyncBool = forceSync;
        currentList = listint;
        recordType = mediaType;
        Context context = c;

        if (recordType.equals("anime")) {
            new getAnimeRecordsTask(this.gridCellHeight, context).execute(currentList);
        } else if (recordType.equals("manga")) {
            new getMangaRecordsTask(this.gridCellHeight, context).execute(currentList);
        }
    }

    public void setAnimeRecords(ArrayList<AnimeRecord> objects){
    	CoverAdapter<AnimeRecord> adapter = ca;
    	if (adapter == null){
    		int list_cover_item = R.layout.grid_cover_with_text_item;
    		if (useTraditionalList){
    			list_cover_item = R.layout.list_cover_with_text_item;
    		}
    		adapter = new CoverAdapter<AnimeRecord>(c,list_cover_item,objects,mManager,recordType,this.gridCellHeight,useSecondaryAmounts);
    		
    	}
    	if (gv.getAdapter() == null){
    		gv.setAdapter(adapter);
    	}else{
    		adapter.clear();
    		adapter.supportAddAll(objects);
    		adapter.notifyDataSetChanged();
    	}
    	ca = adapter;
    }
    public void setMangaRecords(ArrayList<MangaRecord> objects) {
        CoverAdapter<MangaRecord> adapter = cm;
        if (adapter == null) {
            int list_cover_item = R.layout.grid_cover_with_text_item;
            if (useTraditionalList) {
                list_cover_item = R.layout.list_cover_with_text_item;
            }
            adapter = new CoverAdapter<MangaRecord>(c, list_cover_item, objects, mManager, recordType, this.gridCellHeight, useSecondaryAmounts);
        }
        if (gv.getAdapter() == null) {
            gv.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.supportAddAll(objects);
            adapter.notifyDataSetChanged();
        }
        cm = adapter;
    }
    
    
    public class getAnimeRecordsTask extends AsyncTask<Integer, Void, ArrayList<AnimeRecord>> {

        boolean mForceSync = forceSyncBool;
        boolean mTraditionalList = useTraditionalList;
        String type = recordType;
        MALManager internalManager = mManager;
        int gridCellHeight;
        Context context;

        getAnimeRecordsTask(int imageHeight, Context c) {
            this.gridCellHeight = imageHeight;
            this.context = c;
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
                        mManager = new MALManager(this.context);
                    } finally {
                        Log.v("MALX", "Successfully re-created mManager");
                    }
                }

                if (mManager.cleanDirtyAnimeRecords()) {
                    mManager.downloadAndStoreList(MALManager.TYPE_ANIME);
                }
            }
            al = mManager.getAnimeRecordsFromDB(listint);

            return al;
        }

        @Override
        protected void onPostExecute(ArrayList<AnimeRecord> result) {

            if (result == null) {
                result = new ArrayList<AnimeRecord>();
            }
            if (ca == null) {
                if (mTraditionalList) {
                    ca = new CoverAdapter<AnimeRecord>(c, R.layout.list_cover_with_text_item, result, internalManager, type, this.gridCellHeight, useSecondaryAmounts);
                } else {
                    ca = new CoverAdapter<AnimeRecord>(c, R.layout.grid_cover_with_text_item, result, internalManager, type, this.gridCellHeight, useSecondaryAmounts);

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
                NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(R.id.notification_sync);
            }

        }

    }

    public class getMangaRecordsTask extends AsyncTask<Integer, Void, ArrayList<MangaRecord>> {
        boolean mForceSync = forceSyncBool;
        boolean mTraditionalList = useTraditionalList;
        boolean mSyncSuccessful = false;
        String type = recordType;
        MALManager internalManager = mManager;
        int gridCellHeight;
        Context context;

        getMangaRecordsTask(int imageHeight, Context c) {
            this.gridCellHeight = imageHeight;
            this.context = c;
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
                        mManager = new MALManager(this.context);
                    } finally {
                        Log.v("MALX", "Successfully re-created mManager");
                    }
                }
                if (mManager.cleanDirtyMangaRecords()) {
                	mSyncSuccessful = mManager.downloadAndStoreList(MALManager.TYPE_MANGA);
                }

            }

            ml = mManager.getMangaRecordsFromDB(listint);

            return ml;
        }

        @Override
        protected void onPostExecute(ArrayList<MangaRecord> result) {

            if (result == null) {
                result = new ArrayList<MangaRecord>();
            }
            if (cm == null) {
                if (mTraditionalList) {
                    cm = new CoverAdapter<MangaRecord>(c, R.layout.list_cover_with_text_item, result, internalManager, type, this.gridCellHeight, useSecondaryAmounts);
                } else {
                    cm = new CoverAdapter<MangaRecord>(c, R.layout.grid_cover_with_text_item, result, internalManager, type, this.gridCellHeight, useSecondaryAmounts);
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
            	if ( mSyncSuccessful )
            		Crouton.makeText((Activity)c, R.string.toast_SyncDone, Style.CONFIRM).show();
            	else
            		Crouton.makeText((Activity)c, R.string.toast_SyncError, Style.ALERT).show();
                NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(R.id.notification_sync);
            }
        }
    }
    
    
    /*getting popular and top rated anime*/
    public class networkThread extends AsyncTask<String, Void, Void> {
        JSONArray _result;
        int job;
        int pagenumber;
        public networkThread(int job, int pagenumber){
        	this.job = job;
        	this.pagenumber = pagenumber;
        }

        public MALApi.ListType getListType() {
            return listType;
        }

        public void setListType(MALApi.ListType listType) {
            this.listType = listType;
        }

        MALApi.ListType listType;

        @Override
        protected Void doInBackground(String... params) {
        	try{
        		// String query = params[0];
        		MALApi api = new MALApi(c);
        		switch (job){
        		case 1:
        			_result = api.getTopRated(getListType(),pagenumber); //if job ==  1 then get the top rated
        			break;
        		case 2:
        			_result = api.getMostPopular(getListType(),pagenumber); //if job == 2 then get the most popular
        			break;
        		case 3:
        			_result = api.getJustAdded(getListType(),pagenumber); //if job == 3 then get the Just Added
        			break;
        		case 4:
        			_result = api.getUpcoming(getListType(),pagenumber); //if job == 4 then get the Upcoming
        			break;
        		}
        	}catch(Exception e){
        		
        	}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            String type = MALApi.getListTypeString(getListType());
            
            try {
                switch (listType) {
                    case ANIME: {
                        ArrayList<AnimeRecord> list = new ArrayList<AnimeRecord>();
                        
                        if (_result.length() == 0) {
							Log.w("MALX", "No anime records returned.");
                        }
                        else {
                        	for (int i = 0; i < _result.length(); i++) {
                                JSONObject genre = (JSONObject) _result.get(i);
                                AnimeRecord record = new AnimeRecord(mManager.getRecordDataFromJSONObject(genre, type));
                                list.add(record);
                            }
                        }
                        
                        if (ca == null) {
                            //if no coveradapter, but there should never be a time where there is no coveradapter
                        }

                        if (gv.getAdapter() == null) {
                            gv.setAdapter(ca);
                        } else {
                            ca.supportAddAll(list);
                            ca.notifyDataSetChanged();
                        }
                        break;
                    }
                    case MANGA: {
                        ArrayList<MangaRecord> list = new ArrayList<MangaRecord>();
                        
                        if (_result.length() == 0) {
							Log.w("MALX", "No manga records returned.");
                        }
                        else {
                        	for (int i = 0; i < _result.length(); i++) {
                                JSONObject genre =  (JSONObject) _result.get(i);
                                MangaRecord record = new MangaRecord(mManager.getRecordDataFromJSONObject(genre, type));
                                list.add(record);
                            }	
                        }
                        
                        if (cm == null) {
                            //if no coveradapter, but there should never be a time where there is no coveradapter
                        }

                        if (gv.getAdapter() == null) {
                            gv.setAdapter(cm);
                        } else {
                            cm.supportAddAll(list);
                            cm.notifyDataSetChanged();
                        }
                    }
                }
                
            } catch (JSONException e) {
                Log.e(SearchActivity.class.getName(), Log.getStackTraceString(e));
            }
            
            ItemGridFragment.this.scrollListener.notifyMorePages(listType);

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
    
    public void setMode(int mode){
    	this.mode = mode;
    }
    
    public int getMode(){
    	return(this.mode);
    }
    
    public void scrollToTop(){
    	gv.smoothScrollToPosition(0);
    }
}
