//package com.fplay.lucknowzoo.utils;
//
//import android.content.Context;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Environment;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.google.vr.sdk.widgets.video.VrVideoView;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//public class DownloadVideoTask extends AsyncTask<String,Integer, Uri> {
//
//    private VrVideoView vrvideoView;
//    private Context context;
//
//    public DownloadVideoTask(VrVideoView vrvideoView, Context context) {
//        this.vrvideoView = vrvideoView;
//        this.context = context;
//    }
//
//
//
//
//    protected void onPostExecute(Uri videoUri) {
//        try {
//            VrVideoView.Options options = new VrVideoView.Options();
//            vrvideoView.loadVideo(videoUri,options);
//            vrvideoView.playVideo();
//            Toast.makeText(this.context,"Playing...",Toast.LENGTH_LONG);
//        } catch (Exception ex) {
//            String msg = ex.getMessage();
//            Toast.makeText(this.context,"Test",Toast.LENGTH_LONG);
//        }
//
//    }
//
//
//
//    public static void copyFile(InputStream in, OutputStream out) throws IOException {
//        byte[] buffer = new byte[1024];
//        int read;
//        while ((read = in.read(buffer)) != -1) {
//            out.write(buffer, 0, read);
//        }
//    }
//
//    @Override
//    protected Uri doInBackground(String... url) {
//
//
//
//        String fileName = url[0].substring(url[0].lastIndexOf("/")+1, url[0].length());
//        Log.d("Lucknow ","Filename: " + fileName);
//
//        File externalStorageDir = Environment.getExternalStorageDirectory();
//
//        File myFile = new File(externalStorageDir , fileName);
//        //HttpsURLConnection.setDefaultSSLSocketFactory(new NoSSLv3Factory());
//        HttpURLConnection urlConnection = null;
//
//
//        //String filepath = context.getFilesDir() + File.separator + fileName;
//        File file = null;
//        try {
//            URL videoUri = new java.net.URL(url[0]);
//            urlConnection = (HttpURLConnection) videoUri.openConnection();
//            urlConnection.connect();
//            Log.d("Lucknow ","Connected: ");
//            InputStream in = urlConnection.getInputStream();
//            Log.d("Lucknow ","Fetched inputstream: ");
//            FileOutputStream output = context.openFileOutput(fileName, context.MODE_PRIVATE);
//
//            //FileOutputStream output = new FileOutputStream(myFile);
//            copyFile(in, output);
//            Log.d("Lucknow ","Copied file: ");
//        } catch (java.net.MalformedURLException mex) {
//            String test = mex.toString();
//            Log.d("Lucknow ","Exception " + mex.getMessage());
//        } catch (Exception ex) {
//            String test = ex.toString();
//            Log.d("Lucknow ","Exception: " + ex.getMessage());
//        } finally {
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
//        }
//        Log.d("Lucknow ","Returning URL: " + myFile.getAbsolutePath());
//        //return Uri.parse(myFile.getAbsolutePath());
//        return Uri.parse(context.getFilesDir() + File.separator + fileName);
//    }
//}