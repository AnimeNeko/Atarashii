package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.squareup.picasso.Picasso;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.GenericRecord;
import net.somethingdreadful.MAL.api.response.Manga;
import net.somethingdreadful.MAL.tasks.AnimeNetworkTask;
import net.somethingdreadful.MAL.tasks.MangaNetworkTask;
import net.somethingdreadful.MAL.tasks.NetworkTaskCallbackListener;
import net.somethingdreadful.MAL.tasks.TaskJob;
import net.somethingdreadful.MAL.tasks.WriteDetailTask;

import java.util.ArrayList;
import java.util.Collection;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class IGF extends Fragment implements OnScrollListener, OnItemLongClickListener, OnItemClickListener, NetworkTaskCallbackListener {

	Context context;
	Boolean isAnime;
	TaskJob taskjob;
	GridView Gridview;
	PrefManager pref;
	ViewFlipper viewflipper;
    SwipeRefreshLayout swipeRefresh;
    FragmentActivity activity;
    ArrayList<Anime> al = new ArrayList<Anime>();
    ArrayList<Manga> ml = new ArrayList<Manga>();
    ListViewAdapter<Anime> aa;
    ListViewAdapter<Manga> ma;
	
	int page = 1;
	int list = -1;
	int resource;
	boolean useSecondaryAmounts;
	boolean loading = true;
	boolean detail = false;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
    	setRetainInstance(true);
        View view = inflater.inflate(R.layout.record_igf_layout, container, false);
        viewflipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);
        Gridview = (GridView) view.findViewById(R.id.gridview);
        Gridview.setOnItemClickListener(this);
        Gridview.setOnItemLongClickListener(this);
        Gridview.setOnScrollListener(this);
        
        context = getActivity();
        activity = getActivity();
        pref = new PrefManager(context);
        useSecondaryAmounts = pref.getUseSecondaryAmountsEnabled();
        if (pref.getTraditionalListEnabled()){
	        Gridview.setColumnWidth((int) Math.pow(9999,9999)); //remain in the listview mode
        	resource = R.layout.record_igf_listview;
        } else {
        	resource = R.layout.record_igf_gridview;
        }

        if (isOnHomeActivity()) {
            swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
            swipeRefresh.setOnRefreshListener((Home)getActivity());
            swipeRefresh.setColorScheme(
                    R.color.holo_blue_bright,
                    R.color.holo_green_light,
                    R.color.holo_orange_light,
                    R.color.holo_red_light
            );
        }

        if (list == -1)
        	getRecords(true, null, pref.getDefaultList());
        else
        	refresh();
        
    	NfcHelper.disableBeam(activity);
        return view;
    }

    private boolean isOnHomeActivity() {
        if (getActivity() != null)
            return getActivity().getClass() == Home.class;
        return false;
    }

	/*
	 * get the ListType
	 */
	public ListType getListType(){
		if (isAnime)
			return ListType.ANIME;
		else 
			return ListType.MANGA;
	}

	/*
	 * add +1 episode/volume/chapters to the anime/manga.
	 */
	public void setProgressPlusOne(Anime anime, Manga manga) {
    	if (isAnime) {
    		anime.setWatchedEpisodes(anime.getWatchedEpisodes() + 1);
    		if (anime.getWatchedEpisodes() == anime.getEpisodes())
    			anime.setWatchedStatus(GenericRecord.STATUS_COMPLETED);
    		new WriteDetailTask(getListType(), TaskJob.UPDATE, context).execute(anime);
    	} else {
    		manga.setProgress(useSecondaryAmounts, manga.getProgress(useSecondaryAmounts) + 1);
    		if (manga.getProgress(useSecondaryAmounts) == manga.getTotal(useSecondaryAmounts))
    			manga.setReadStatus(GenericRecord.STATUS_COMPLETED);
    	}
   		refresh();
    }

	/*
	 * mark the anime/manga as completed.
	 */
    public void setMarkAsComplete(Anime anime, Manga manga) {
    	if (isAnime){
    		anime.setWatchedStatus(GenericRecord.STATUS_COMPLETED);
    		if (anime.getEpisodes() > 0)
    			anime.setWatchedEpisodes(anime.getEpisodes());
    		anime.setDirty(true);
	        al.remove(anime);
	        new WriteDetailTask(getListType(), TaskJob.UPDATE, context).execute(anime);
    	} else {
    		manga.setReadStatus(GenericRecord.STATUS_COMPLETED);
    		manga.setDirty(true);
	        ml.remove(manga);
	        new WriteDetailTask(getListType(), TaskJob.UPDATE, context).execute(manga);
    	}
        refresh();
    }
    
    /*
     * handle the loading indicator
     */
    private void toggleLoadingIndicator(boolean show) {
        if (viewflipper != null) {
        	viewflipper.setDisplayedChild(show ? 1 : 0);
        }
    }

    public void toggleSwipeRefreshAnimation(boolean show) {
        if(swipeRefresh != null) {
            swipeRefresh.setRefreshing(show);
        }
    }

    public void setSwipeRefreshEnabled(boolean enabled) {
        if(swipeRefresh != null) {
            swipeRefresh.setEnabled(enabled);
        }
    }

    /*
	 * get the anime/manga lists.
	 * (if clear is true the whole list will be cleared and loaded)
	 */
	public void getRecords(boolean clear, TaskJob task, int list){
		if (task != null){
			taskjob = task;
		}
		if (list != this.list){
			this.list = list;
		}
        /* only show loading indicator if
         * - is not own list and not page 1
         * - force sync and list is empty (only show swipe refresh animation if not empty)
         */
        boolean isEmpty = (isAnime ? al.isEmpty() : ml.isEmpty());
        toggleLoadingIndicator((page == 1 && !isList()) || (taskjob.equals(TaskJob.FORCESYNC) && isEmpty));
        /* show swipe refresh animation if
         * - loading more pages
         * - forced update
         */
        toggleSwipeRefreshAnimation((page > 1 && !isList()) || taskjob.equals(TaskJob.FORCESYNC));
		loading = true;
		try{
			if (isAnime){
				if (clear){
					al.clear();
					if (aa == null){
						setAdapter();
					}
					aa.clear();
					resetPage();
				}
				if (isList())
					new AnimeNetworkTask(taskjob, page, context, IGF.this).execute(MALManager.listSortFromInt(list, "anime"));
				else 
					new AnimeNetworkTask(taskjob, page, context, IGF.this).execute(SearchActivity.query);
			} else {
				if (clear){
					ml.clear();
					if (ma == null){
						setAdapter();	
					}
					ma.clear();
					resetPage();
				}
				if (isList())
					new MangaNetworkTask(taskjob, page, context, this).execute(MALManager.listSortFromInt(list, "manga"));
				else
					new MangaNetworkTask(taskjob, page, context, this).execute(SearchActivity.query);
			}
		}catch (Exception e){
               Log.e("MALX", "error getting records: " + e.getMessage());
		}
	}
	
	/*
	 * reset the page number of anime/manga lists.
	 */
	public void resetPage(){
		if (!isList()){
			page = 1;
			Gridview.setSelection(0);
		}
	}
	
	/*
	 * set the adapter anime/manga
	 */
	public void setAdapter(){
		if (isAnime){
			aa = new ListViewAdapter<Anime>(context, resource);
            aa.setNotifyOnChange(true);
		} else {
			ma = new ListViewAdapter<Manga>(context, resource);
			ma.setNotifyOnChange(true);
		}
	}
	
	/*
	 * set the watched/read count & status on the covers.
	 */
	public static void setStatus(String myStatus, TextView textview, TextView progressCount, ImageView actionButton){
        actionButton.setVisibility(View.GONE);
        progressCount.setVisibility(View.GONE);
		if (myStatus == null) {
            textview.setText("");
		}
		else if (myStatus.equals("watching")) {
	        textview.setText(R.string.cover_Watching);
	        progressCount.setVisibility(View.VISIBLE);
            actionButton.setVisibility(View.VISIBLE);
	    }
	    else if (myStatus.equals("reading")) {
	        textview.setText(R.string.cover_Reading);
	        progressCount.setVisibility(View.VISIBLE);
            actionButton.setVisibility(View.VISIBLE);
	    }
	    else if (myStatus.equals("completed")) {
	        textview.setText(R.string.cover_Completed);
	    }
	    else if (myStatus.equals("on-hold")) {
	        textview.setText(R.string.cover_OnHold);
	        progressCount.setVisibility(View.VISIBLE);
	    }
	    else if (myStatus.equals("dropped")) {
	        textview.setText(R.string.cover_Dropped);
	    }
	    else if (myStatus.equals("plan to watch")) {
	        textview.setText(R.string.cover_PlanningToWatch);
	    }
	    else if (myStatus.equals("plan to read")) {
	        textview.setText(R.string.cover_PlanningToRead);
	    }
	    else {
	        textview.setText("");
	    }
	}
	
	/*
	 * refresh the covers.
	 */
	public void refresh(){
		try{
			if (isAnime){
				if (aa == null){
					setAdapter();
				}
				aa.clear();
				aa.supportAddAll(al);
				if (Gridview.getAdapter() == null){
					Gridview.setAdapter(aa);
				}
			} else{
				if (ma == null){
					setAdapter();
				}
				ma.clear();
				ma.supportAddAll(ml);
				if (Gridview.getAdapter() == null){
					Gridview.setAdapter(ma);
				}
			}
		} catch (Exception e){
			if (MALApi.isNetworkAvailable(context)){
				e.printStackTrace();
				if (taskjob.equals(TaskJob.SEARCH)){
					Crouton.makeText(activity, R.string.crouton_error_Search, Style.ALERT).show();
    			} else {
    				if (isAnime){
    					Crouton.makeText(activity, R.string.crouton_error_Anime_Sync, Style.ALERT).show();
    				} else {
    					Crouton.makeText(activity, R.string.crouton_error_Manga_Sync, Style.ALERT).show();
    				}
    			}
    			Log.e("MALX", "error on refresh: " + e.getMessage());
    		}else{
    			Crouton.makeText(activity, R.string.crouton_error_noConnectivity, Style.ALERT).show();
    		}
		}
		loading = false;
    }
	
	/*
	 * check if the taskjob is my personal anime/manga list
	 */
	public boolean isList(){
        return taskjob.equals(TaskJob.GETLIST) || taskjob.equals(TaskJob.FORCESYNC);
	}

    /*
     * the custom adapter for the covers anime/manga.
     */
	public class ListViewAdapter<T> extends ArrayAdapter<T> {
        
		public ListViewAdapter(Context context, int resource) {
            super(context, resource);
        }
	    
		@SuppressWarnings("deprecation")
		public View getView(int position, View view, ViewGroup parent) {
			final GenericRecord record;
	        ViewHolder viewHolder;
	        
	        if (view == null) {
            	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            	view = inflater.inflate(resource, parent, false);
            	
            	viewHolder = new ViewHolder();
            	viewHolder.label = (TextView) view.findViewById(R.id.animeName);
            	viewHolder.progressCount = (TextView) view.findViewById(R.id.watchedCount);
            	viewHolder.cover = (ImageView) view.findViewById(R.id.coverImage);
            	viewHolder.bar = (ImageView) view.findViewById(R.id.textOverlayPanel);
            	viewHolder.actionButton = (ImageView) view.findViewById(R.id.popUpButton);
            	viewHolder.flavourText = (TextView) view.findViewById(R.id.stringWatched);
            	view.setTag(viewHolder);
            } else{
            	viewHolder = (ViewHolder) view.getTag();
            }
            try{
            	if (isAnime)
	            	record = al.get(position);
 	            else
	            	record = ml.get(position);
            	
            	if (taskjob.equals(TaskJob.GETMOSTPOPULAR) || taskjob.equals(TaskJob.GETTOPRATED)){
            		viewHolder.progressCount.setVisibility(View.VISIBLE);
            		viewHolder.progressCount.setText(Integer.toString(position + 1));
            		viewHolder.actionButton.setVisibility(View.GONE);
            		viewHolder.flavourText.setText(R.string.label_Number);
	            } else if (isAnime) {
	            	viewHolder.progressCount.setText(Integer.toString(((Anime)record).getWatchedEpisodes()));
	            	setStatus(((Anime)record).getWatchedStatus(), viewHolder.flavourText, viewHolder.progressCount, viewHolder.actionButton);
	            } else {
	            	if (useSecondaryAmounts)
	            		viewHolder.progressCount.setText(Integer.toString(((Manga)record).getVolumesRead()));
	            	else
	            		viewHolder.progressCount.setText(Integer.toString(((Manga)record).getChaptersRead()));
	            	setStatus(((Manga)record).getReadStatus(), viewHolder.flavourText, viewHolder.progressCount, viewHolder.actionButton);
	            }
            	viewHolder.label.setText(record.getTitle());

                Picasso.with(context)
	            	.load(record.getImageUrl())
	            	.error(R.drawable.cover_error)
	            	.placeholder(R.drawable.cover_loading)
	            	.into(viewHolder.cover);
	            
                if (viewHolder.actionButton.getVisibility() == View.VISIBLE){
                	viewHolder.actionButton.setOnClickListener(new OnClickListener(){
                		@Override
                		public void onClick(View v) {
                			PopupMenu popup = new PopupMenu(context, v);
                			popup.getMenuInflater().inflate(R.menu.record_popup, popup.getMenu());
                			if (!isAnime)
                				popup.getMenu().findItem(R.id.plusOne).setTitle(R.string.action_PlusOneRead);
                			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                				public boolean onMenuItemClick(MenuItem item) {
                					switch (item.getItemId()) {
                						case R.id.plusOne:
                							if (isAnime)
                								setProgressPlusOne((Anime)record, null);
                							else
                								setProgressPlusOne(null, (Manga)record);
                							break;
                						case R.id.markCompleted:
                							if (isAnime)
                								setMarkAsComplete((Anime)record, null);
                							else
                								setMarkAsComplete(null, (Manga)record);
                							break;
									}
                				return true;
								}
							});
                		popup.show();
                		}
                	});
                }
                viewHolder.bar.setAlpha(175);
            }catch (Exception e){
    			Log.e("MALX", "error on the ListViewAdapter: " + e.getMessage());
            }
            return view;
        }
		
	    public void supportAddAll(Collection<? extends T> collection) {
	        for (T record : collection) {
	        	this.add(record);
	        }
	    }
	}
	
	static class ViewHolder {
        TextView label;
        TextView progressCount;
        TextView flavourText;
        ImageView cover;
        ImageView bar;
        ImageView actionButton;
    }

    /*
     * set the list with the new page/list.
     */
    @SuppressWarnings("unchecked") // Don't panic, we handle possible class cast exceptions
    @Override
    public void onNetworkTaskFinished(Object result, TaskJob job, int page, ListType type) {
        if (result == null) {
            Crouton.makeText(activity, type == ListType.ANIME ? R.string.crouton_error_Anime_Sync : R.string.crouton_error_Manga_Sync, Style.ALERT).show();
        } else {
            ArrayList resultList;
                try {
                    if (type == ListType.ANIME) {
                        resultList = (ArrayList<Anime>) result;
                    } else {
                        resultList = (ArrayList<Manga>) result;
                    }
                } catch (ClassCastException e) {
                    Log.e("MALX", "error reading result because of invalid result class: " + result.getClass().toString());
                    resultList = null;
                }
                if (resultList != null) {
                if (resultList.size() == 0 && taskjob.equals(TaskJob.SEARCH)) {
                    if (this.page == 1)
                        SearchActivity.onError(type, true, (SearchActivity) getActivity(), job);
                } else {
                    if (job.equals(TaskJob.FORCESYNC))
                        SearchActivity.onError(type, false, (Home) getActivity(), job);
                    if (type == ListType.ANIME) {
                        if (detail || job.equals(TaskJob.FORCESYNC)) { // a forced sync always reloads all data, so clear the list
                            al.clear();
                            detail = false;
                        }
                        al.addAll(resultList);
                    } else {
                        if (detail || job.equals(TaskJob.FORCESYNC)) { // a forced sync always reloads all data, so clear the list
                            ml.clear();
                            detail = false;
                        }
                        ml.addAll(resultList);
                    }
                    refresh();
                }
            }
        }
        toggleSwipeRefreshAnimation(false);
        toggleLoadingIndicator(false);
    }

	/*
	 * handle the gridview click by navigating to the detailview.
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
		Intent startDetails = new Intent(getView().getContext(), DetailView.class);
		if (isAnime){
			startDetails.putExtra("net.somethingdreadful.MAL.recordID", aa.getItem(position).getId());
		} else {
			startDetails.putExtra("net.somethingdreadful.MAL.recordID", ma.getItem(position).getId());
		}
		startDetails.putExtra("net.somethingdreadful.MAL.recordType", getListType());
        startActivity(startDetails);
        if (isList() || taskjob.equals(TaskJob.SEARCH)){
        	Home.af.detail = true;
        	Home.mf.detail = true;
        }
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	/*
	 * load more pages if we are almost on the bottom.
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (totalItemCount - firstVisibleItem <= (visibleItemCount * 2) && !loading){
			loading = true;
			if (taskjob != TaskJob.GETLIST && taskjob != TaskJob.FORCESYNC){
				page = page + 1;
				getRecords(false, null, 0);
			}
		}
	}

	/*
	 * corpy the anime title to the clipboard on long click.
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Crouton.makeText(activity, R.string.crouton_info_Copied, Style.CONFIRM).show();
	    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
	        android.text.ClipboardManager c = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
	        if (isAnime) {
	        	c.setText(al.get(position).getTitle());
	        } else {
	        	c.setText(ml.get(position).getTitle());
	        }
	    } else {
	        android.content.ClipboardManager c1 = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
	        android.content.ClipData c2;
	        if (isAnime)
	        	c2 = android.content.ClipData.newPlainText("Atarashii", al.get(position).getTitle());
	        else 
	        	c2 = android.content.ClipData.newPlainText("Atarashii", ml.get(position).getTitle());
	        c1.setPrimaryClip(c2);
	    }
		return false;
	}
}