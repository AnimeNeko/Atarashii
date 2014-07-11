package net.somethingdreadful.MAL.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import net.somethingdreadful.MAL.MALManager;
import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.MALApi.ListType;
import net.somethingdreadful.MAL.api.response.Anime;
import net.somethingdreadful.MAL.api.response.GenericRecord;
import net.somethingdreadful.MAL.api.response.Manga;

public class WriteDetailTask extends AsyncTask<GenericRecord, Void, Boolean> {
	MALManager manager;
	Context context;
	ListType type;
	TaskJob job;
	
	public WriteDetailTask(ListType type, TaskJob job, Context context) {
		this.context = context;
		this.job = job;
		this.type = type;
	}

	@Override
	protected Boolean doInBackground(GenericRecord... gr) {
	    manager = new MALManager(context);

	    try{
	    	if (MALApi.isNetworkAvailable(context)) {
	    		if (type.equals(ListType.ANIME)) {
	    			manager.writeAnimeDetailsToMAL((Anime) gr[0]);
	    		} else {
	    			manager.writeMangaDetailsToMAL((Manga) gr[0]);
	    		}
		    	gr[0].setDirty(false);
	    	}
	    }catch (Exception e){
			Log.e("MALX", "error on response WriteDetailTask: " + e.getMessage());
	    }
	    
	    if (!job.equals(TaskJob.UPDATE)){
	    	if (ListType.ANIME.equals(type)) {
	           	manager.deleteAnimeFromDatabase((Anime) gr[0]);
	        } else {
	          	manager.deleteMangaFromDatabase((Manga) gr[0]);
	        }
	    } else {
	        if (type.equals(ListType.ANIME)) {
	        	manager.saveAnimeToDatabase((Anime) gr[0], false);
	        } else {
	        	manager.saveMangaToDatabase((Manga) gr[0], false);
	        }
	    }

	    return null;
	}
}