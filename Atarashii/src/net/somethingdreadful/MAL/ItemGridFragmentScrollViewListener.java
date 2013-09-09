package net.somethingdreadful.MAL;

import java.util.ArrayList;

import net.somethingdreadful.MAL.record.GenericMALRecord;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;

public class ItemGridFragmentScrollViewListener implements OnScrollListener{
	
	private GridView gridView;
    private boolean isLoading;
    private boolean hasMorePages;
    private int pageNumber=1;
    private boolean isRefreshing;
    //private ArrayList<GenericMALRecord> list;
    private RefreshList list;
    
    public ItemGridFragmentScrollViewListener(GridView gridview, RefreshList list){
    	this.gridView = gridview;
    	this.list = list;
    	this.isLoading = false;
    	this.hasMorePages = true;
    }
    
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		if (gridView.getLastVisiblePosition() +1 == totalItemCount && !isLoading){
			isLoading = true;
			if (hasMorePages && !isRefreshing){
				isRefreshing = true;
				//TODO: add more stuff
				list.onRefresh(pageNumber);
				
			}
		}else{
			isLoading = false;
		}
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}
	public void noMorePages() {
        this.hasMorePages = false;
    }

    public void notifyMorePages(){
        isRefreshing=false;
        pageNumber=pageNumber+1;
    }
    public interface RefreshList {
        public void onRefresh(int pageNumber);
    }
    public void resetPageNumber(){
    	pageNumber = 1;
    }

}
