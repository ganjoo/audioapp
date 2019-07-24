package com.fplay.audioapp.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.fplay.audioapp.activities.IntroPage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ThreadDownloader extends Thread{

    Context mctx;
    List<String> mfile_list;

    String[] data ={
            "https://s3.ap-south-1.amazonaws.com/zoopictures/zoo+videos/Emu_h264_short.mp4","https://s3.ap-south-1.amazonaws.com/zoopictures/zoo+videos/deer_h264_short.mp4",
            "https://s3.ap-south-1.amazonaws.com/zoopictures/zoo+videos/giraffe_h264_short.mp4","https://s3.ap-south-1.amazonaws.com/zoopictures/zoo+videos/rhino_h264_short.mp4"
    };
    public ThreadDownloader(Context ctx, List<String> file_list) {
        mctx = ctx;
        mfile_list = file_list;
    }


    public void run() {
        // compute primes larger than minPrime
         int audio_count = 0;
        for(String ele:mfile_list){
            downloadfileToAssets(ele);
            audio_count++;
            final int count = audio_count;
            ((IntroPage)mctx).runOnUiThread(new Runnable()
            {
                public void run()
                {
                    ((IntroPage)mctx).onProgressDownload(count* 100.0f/128.0f );
                }
            });

        }
        ((IntroPage)mctx).runOnUiThread(new Runnable()
        {
            public void run()
            {
                ((IntroPage)mctx).onAudioSetupCompleted();
            }
        });

        for(String ele:data){
            downloadfileToAssets(ele);
        }

        ((IntroPage)mctx).runOnUiThread(new Runnable()
        {
            public void run()
            {
                ((IntroPage)mctx).onVRSetupCompleted();
            }
        });
    }

    private Uri downloadfileToAssets(String url){
        String fileName = url.substring(url.lastIndexOf("/")+1, url.length());
        if(fileExist(fileName)){
            return Uri.parse(mctx.getFilesDir() + File.separator + fileName);
        }



        Log.d("Lucknow ","Filename: " + fileName);

        File externalStorageDir = Environment.getExternalStorageDirectory();

        File myFile = new File(externalStorageDir , fileName);
        //HttpsURLConnection.setDefaultSSLSocketFactory(new NoSSLv3Factory());
        HttpURLConnection urlConnection = null;


        //String filepath = context.getFilesDir() + File.separator + fileName;
        File file = null;
        try {
            URL videoUri = new java.net.URL(url);
            urlConnection = (HttpURLConnection) videoUri.openConnection();
            urlConnection.connect();
            Log.d("Lucknow ","Connected: ");
            InputStream in = urlConnection.getInputStream();
            Log.d("Lucknow ","Fetched inputstream: ");
            FileOutputStream output = mctx.openFileOutput(fileName, mctx.MODE_PRIVATE);

            //FileOutputStream output = new FileOutputStream(myFile);
            copyFile(in, output);
            Log.d("Lucknow ","Copied file: " + url);
        } catch (java.net.MalformedURLException mex) {
            String test = mex.toString();
            Log.d("Lucknow ","Exception " + mex.getMessage());
        } catch (Exception ex) {
            String test = ex.toString();
            Log.d("Lucknow ","Exception: " + ex.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        Log.d("Lucknow ","Returning URL: " + myFile.getAbsolutePath());
        //return Uri.parse(myFile.getAbsolutePath());

        return Uri.parse(mctx.getFilesDir() + File.separator + fileName);
    }
    public boolean fileExist(String fname){
        File file = mctx.getFileStreamPath(fname);
        return file.exists();
    }

    public  void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;

        int progress_count = 0;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            progress_count++;
            //Log.d("Lucknow", Integer.toString(progress_count));

        }
        progress_count = 100;
        //publishProgress(progress_count);
    }
}
