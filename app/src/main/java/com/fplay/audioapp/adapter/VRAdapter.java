package com.fplay.audioapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fplay.audioapp.R;
import com.fplay.audioapp.activities.VRVideoPlay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class VRAdapter extends ArrayAdapter<String> {

        private Context mContext;
        private LayoutInflater inflater;
        private final String[] vrtitle;
        private final String[] vrdata;
        private final Integer[] vrthumbnail;


        public static class ViewHolder{
        public ImageView iv_patient_profile_pic ;
        public TextView tv_patient_name;
        public TextView tv_surgery_name;
        public LinearLayout lv_search_list_item;
      }



    public VRAdapter(Context context, String [] titles, String [] datas, Integer [] images){
        super(context, R.layout.activity_vrlist_page, titles);
        mContext = context;
        inflater =LayoutInflater.from(context);

        this.vrtitle=titles;
        this.vrdata=datas;
        this.vrthumbnail=images;
    }



    @Override
    public int getCount() {
        return vrtitle.length;
    }


    @Override
    public View getView(final int position, View convertView1, ViewGroup parent) {


            View convertView = inflater.inflate(R.layout.row_vr_list_item, null);
            ImageView thumbnail = (ImageView)convertView.findViewById(R.id.thumbnail);
            final TextView title  = (TextView)convertView.findViewById(R.id.vrtitle);
            final TextView status_text = (TextView)convertView.findViewById(R.id.vr_status_text);
            final ProgressBar pg  = (ProgressBar)convertView.findViewById(R.id.player_progressBar);
            ImageButton play = (ImageButton)convertView.findViewById(R.id.play_btn);
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Intent myIntent = new Intent(mContext, SimpleVrVideoActivity.class);
//                    myIntent.putExtra("downloadurl", vrdata[position]); //Optional parameters
//                    myIntent.putExtra("title", vrtitle[position]); //Optional parameters
//                    mContext.startActivity(myIntent);
                    handleClick(status_text,pg,vrdata[position], vrtitle[position],mContext);
                }
            });

            thumbnail.setImageResource(vrthumbnail[position]);
            title.setText(vrtitle[position]);
            convertView.setTag(vrdata[position]);
            return convertView;


    }

    private void handleClick(TextView status, ProgressBar pg, String link, String title, Context ctx) {
        VRAdapter.DownloadVideoTask download_and_play = new VRAdapter.DownloadVideoTask(status, pg, link,title, ctx);
        download_and_play.execute(link,title);

    }
    public class DownloadVideoTask extends AsyncTask<String,Integer, Uri> {


        private Context context;
        private TextView statusText;
        private ProgressBar progress_bar;
        private float file_size;

        public DownloadVideoTask(TextView tv, ProgressBar progress_bar, String link, String title, Context context) {

            this.statusText = tv;
            this.progress_bar = progress_bar;
            this.context = context;
            switch (link){
                case "https://s3.ap-south-1.amazonaws.com/zoopictures/zoo+videos/rhino_h264_short.mp4": file_size = 17.0f;break;
                case "https://s3.ap-south-1.amazonaws.com/zoopictures/zoo+videos/giraffe_h264_short.mp4": file_size = 10.0f;break;
                case "https://s3.ap-south-1.amazonaws.com/zoopictures/zoo+videos/Emu_h264_short.mp4": file_size = 15.0f;break;
                case "https://s3.ap-south-1.amazonaws.com/zoopictures/zoo+videos/deer_h264_short.mp4": file_size = 25.0f;break;


            }
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            statusText.setText("Downloading progress.. " + values[0]/1000 + " Mbs");
            progress_bar.setProgress((int)((values[0]/1000)/file_size * 100.0f));
        }

        protected void onPostExecute(Uri videoUri) {
            try {
                Intent myIntent = new Intent(mContext, VRVideoPlay.class);
                myIntent.putExtra("downloadurl", videoUri.toString());
                mContext.startActivity(myIntent);
                Toast.makeText(this.context,"Playing...",Toast.LENGTH_LONG);
            } catch (Exception ex) {
                String msg = ex.getMessage();
                Toast.makeText(this.context,"Test",Toast.LENGTH_LONG);
            }

        }





        @Override
        protected Uri doInBackground(String... url) {

            //title.setText(url[1]);
            String fileName = url[0].substring(url[0].lastIndexOf("/")+1, url[0].length());
            if(fileExist(fileName)){
                return Uri.parse(context.getFilesDir() + File.separator + fileName);
            }



            Log.d("Lucknow ","Filename: " + fileName);

            File externalStorageDir = Environment.getExternalStorageDirectory();

            File myFile = new File(externalStorageDir , fileName);
            //HttpsURLConnection.setDefaultSSLSocketFactory(new NoSSLv3Factory());
            HttpURLConnection urlConnection = null;


            //String filepath = context.getFilesDir() + File.separator + fileName;
            File file = null;
            try {
                URL videoUri = new java.net.URL(url[0]);
                urlConnection = (HttpURLConnection) videoUri.openConnection();
                urlConnection.connect();
                Log.d("Lucknow ","Connected: ");
                InputStream in = urlConnection.getInputStream();
                Log.d("Lucknow ","Fetched inputstream: ");
                FileOutputStream output = context.openFileOutput(fileName, context.MODE_PRIVATE);

                //FileOutputStream output = new FileOutputStream(myFile);
                copyFile(in, output);
                Log.d("Lucknow ","Copied file: ");
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
            publishProgress(100);
            return Uri.parse(context.getFilesDir() + File.separator + fileName);
        }
        public  void copyFile(InputStream in, OutputStream out) throws IOException {
            byte[] buffer = new byte[1024];
            int read;

            int progress_count = 0;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                progress_count++;
                publishProgress(progress_count);

            }
            progress_count = 100;
            publishProgress(progress_count);
        }
        public boolean fileExist(String fname){
            File file = context.getFileStreamPath(fname);
            if(file.exists()){
                //Check for file size
                // Get length of file in bytes
                long fileSizeInBytes = file.length();
                float fileSizeInKB = fileSizeInBytes / 1024.0f;
                float fileSizeInMB = fileSizeInKB / 1024.0f;
                if(fileSizeInMB < file_size){
                    file.delete();
                    return false;
                }
                return true;
            }
            return false;
        }
    }

}
