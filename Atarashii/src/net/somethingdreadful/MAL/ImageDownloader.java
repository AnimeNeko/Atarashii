package net.somethingdreadful.MAL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

public class ImageDownloader {

    HashMap<String, WeakReference<Bitmap>> imageCache;
    LruCache betterImageCache;
    Context context;

    final int memoryClass;
    final int cacheSize;

    public ImageDownloader(Context c){
        context = c;

        // Get memory class of this device, exceeding this amount will throw a OutOfMemory exception.
        memoryClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

        // Use 1/8th of the available memory for this memory cache.
        cacheSize = 1024 * 1024 * memoryClass / 8;

        betterImageCache = new LruCache<String, Bitmap>(cacheSize) {
            @SuppressLint("NewApi")
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in bytes rather than number of items.
                if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) >= 12)
                    return bitmap.getByteCount();
                else
                    return (bitmap.getRowBytes() * bitmap.getHeight());
            }
        };
    }

    public ImageDownloader()
    {

        // Get memory class of this device, exceeding this amount will throw a OutOfMemory exception.
        memoryClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

        // Use 1/8th of the available memory for this memory cache.
        cacheSize = 1024 * 1024 * memoryClass / 8;

        betterImageCache = new LruCache<String, Bitmap>(cacheSize) {
            @SuppressLint("NewApi")
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in bytes rather than number of items.
                if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) >= 12)
                    return bitmap.getByteCount();
                else
                    return (bitmap.getRowBytes() * bitmap.getHeight());
            }
        };
    }

    //download function
    public void download(String url, ImageView imageView) {
        if (cancelPotentialDownload(url, imageView)) {

            //Caching code right here
            String filename = String.valueOf(url.hashCode());
            File f = new File(getCacheDirectory(context), filename);

            // Is the bitmap in our memory cache?
            Bitmap bitmap = null;

            try
            {
                bitmap = (Bitmap) betterImageCache.get(f.getPath());

            }
            catch (NullPointerException npe)
            {
                //bitmap still null!
            }

            if (bitmap == null)
            {
                imageView.setImageResource(R.drawable.panel);
                new DecodeFileTask(imageView, url).execute(f.getPath());
            }
            else
            {
                imageView.setImageBitmap(bitmap);
            }

            //	    	  if(bitmap == null){
            //
            //	    		  bitmap = BitmapFactory.decodeFile(f.getPath());
            //
            //	    		  if(bitmap != null){
            //	    			  betterImageCache.put(f.getPath(), bitmap);
            //	    		  }
            //
            //	    	  }
            //	    	  //No? download it
            //	    	  if(bitmap == null){
            //	    		  BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
            //	    		  DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
            //	    		  imageView.setImageDrawable(downloadedDrawable);
            //	    		  task.execute(url);
            //	    	  }else{
            //	    		  //Yes? set the image
            //	    		  imageView.setImageBitmap(bitmap);
            //	    	  }
        }
    }

    public Bitmap returnDrawable(Context c, String url)
    {
        //Caching code right here
        String filename = String.valueOf(url.hashCode());
        File f = new File(getCacheDirectory(c), filename);

        // Is the bitmap in our memory cache?
        Bitmap bitmap = null;

        bitmap = (Bitmap) betterImageCache.get(f.getPath());

        if(bitmap == null){

            bitmap = BitmapFactory.decodeFile(f.getPath());

            if(bitmap != null){
                betterImageCache.put(f.getPath(), bitmap);
            }
            else
            {
                bitmap = BitmapFactory.decodeResource(c.getResources(), R.drawable.panel);
            }

        }

        return bitmap;
    }

    //cancel a download (internal only)
    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    //gets an existing download if one exists for the imageview
    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    //our caching functions
    // Find the dir to save cached images
    private static File getCacheDirectory(Context context){
        String sdState = android.os.Environment.getExternalStorageState();
        File cacheDir;

        if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
            File sdDir = android.os.Environment.getExternalStorageDirectory();

            //TODO : Change your direcory here
            cacheDir = new File(sdDir,"data/net.somethingdreadful.MAL/images/");
        }
        else
            cacheDir = context.getCacheDir();

        if(!cacheDir.exists())
            cacheDir.mkdirs();
        return cacheDir;
    }

    private void writeFile(Bitmap bmp, File f) {
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try { if (out != null ) out.close(); }
            catch(Exception ex) {}
        }
    }
    ///////////////////////

    //download asynctask
    public class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        // Actual download method, run in the task thread
        protected Bitmap doInBackground(String... params) {
            // params comes from the execute() call: params[0] is the url.
            url = params[0];
            return downloadBitmap(params[0]);
        }

        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with it
                if (this == bitmapDownloaderTask) {
                    imageView.setImageBitmap(bitmap);

                    //cache the image


                    String filename = String.valueOf(url.hashCode());
                    File f = new File(getCacheDirectory(imageView.getContext()), filename);

                    betterImageCache.put(f.getPath(), bitmap);

                    writeFile(bitmap, f);
                }
            }
        }


    }

    public class DecodeFileTask extends AsyncTask<String, Void, Bitmap> {

        Bitmap bm;
        WeakReference<ImageView> cover;
        String url;

        public DecodeFileTask(ImageView imageView, String url) {
            cover = new WeakReference<ImageView>(imageView);
            this.url = url;
        }

        @Override
        // Decode bitmap in a separate thead
        protected Bitmap doInBackground(String... params) {

            bm = BitmapFactory.decodeFile(params[0]);

            if(bm != null){
                betterImageCache.put(params[0], bm);
            }


            return bm;
        }

        @Override
        // Finish the calling function, pretty much
        protected void onPostExecute(Bitmap bitmap) {
            if (cover.get() != null)
            {
                if (bitmap != null)
                {
                    try
                    {
                        cover.get().setImageBitmap(bitmap);
                    }
                    catch (NullPointerException npe)
                    {

                    }
                }
                else
                {
                    BitmapDownloaderTask task = new BitmapDownloaderTask(cover.get());
                    DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
                    cover.get().setImageDrawable(downloadedDrawable);
                    task.execute(url);
                }
            }
        }

    }


    static class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
            super(Color.BLACK);
            bitmapDownloaderTaskReference =
                    new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    //the actual download code
    static Bitmap downloadBitmap(String url) {
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpClient client = new DefaultHttpClient(params);
        final HttpGet getRequest = new HttpGet(url);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // Could provide a more explicit error message for IOException or IllegalStateException
            getRequest.abort();
            Log.w("ImageDownloader", "Error while retrieving bitmap from " + url + e.toString());
        } finally {
            if (client != null) {
                //client.close();
            }
        }
        return null;
    }

    public void wipeCache() {
        File file = new File(getCacheDirectory(context), "");
        if (file != null && file.isDirectory()) {
            File[] files = file.listFiles();
            if(files != null) {
                for(File f : files) {
                    f.delete();

                }
            }
        }
    }
}

