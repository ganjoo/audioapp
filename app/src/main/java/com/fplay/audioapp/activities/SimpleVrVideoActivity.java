/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fplay.audioapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fplay.audioapp.R;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.google.vr.sdk.widgets.video.VrVideoView.Options;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A test activity that renders a 360 video using {@link VrVideoView}.
 * It loads the congo video from app's assets by default. User can use it to load any video files
 * using adb shell commands such as:
 *
 * <p>Video from asset folder
 *   adb shell am start -a android.intent.action.VIEW \
 *     -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 *     -d "file:///android_asset/congo.mp4"
 *
 * <p>Video located on the phone's SD card.
 *   adb shell am start -a android.intent.action.VIEW \
 *     -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 *     -d /sdcard/FILENAME.MP4
 *
 * <p>Video hosted on a website:
 *   adb shell am start -a android.intent.action.VIEW \
 *     -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 *     -d "https://EXAMPLE.COM/FILENAME.MP4"
 *
 * <p>To load HLS files add "--ei inputFormat 2" to pass in an integer extra which will set
 * VrVideoView.Options.inputFormat to FORMAT_HLS. e.g.
 *   adb shell am start -a android.intent.action.VIEW \
 *     -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 *     -d "file:///android_asset/hls/iceland.m3u8" \
 *     --ei inputFormat 2 --ei inputType 2
 *
 * <p>To load MPEG-DASH files add "--ei inputFormat 3" to pass in an integer extra which will set
 * VrVideoView.Options.inputFormat to FORMAT_DASH. e.g.
 *   adb shell am start -a android.intent.action.VIEW \
 *     -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 *     -d "file:///android_asset/dash/congo_dash.mpd" \
 *     --ei inputFormat 3 --ei inputType 2
 *
 * <p>To specify that the video is of type stereo over under (has images for left and right eyes),
 * add "--ei inputType 2" to pass in an integer extra which will set VrVideoView.Options.inputType
 * to TYPE_STEREO_OVER_UNDER. This can be combined with other extras, e.g:
 *   adb shell am start -a android.intent.action.VIEW \
 *     -n com.google.vr.sdk.samples.simplevideowidget/.SimpleVrVideoActivity \
 *     -d "https://EXAMPLE.COM/FILENAME.MP4" \
 *     --ei inputType 2
 */
public class SimpleVrVideoActivity extends Activity {
    private static final String TAG = SimpleVrVideoActivity.class.getSimpleName();

    /**
     * Preserve the video's state when rotating the phone.
     */
    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_PROGRESS_TIME = "progressTime";
    /**
     * The video duration doesn't need to be preserved, but it is saved in this example. This allows
     * the seekBar to be configured during {@link #onRestoreInstanceState(Bundle)} rather than waiting
     * for the video to be reloaded and analyzed. This avoid UI jank.
     */
    private static final String STATE_VIDEO_DURATION = "videoDuration";

    /**
     * Arbitrary constants and variable to track load status. In this example, this variable should
     * only be accessed on the UI thread. In a real app, this variable would be code that performs
     * some UI actions when the video is fully loaded.
     */
    public static final int LOAD_VIDEO_STATUS_UNKNOWN = 0;
    public static final int LOAD_VIDEO_STATUS_SUCCESS = 1;
    public static final int LOAD_VIDEO_STATUS_ERROR = 2;

    private int loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

    /** Tracks the file to be loaded across the lifetime of this app. **/
    private String fileUri;

    /** Configuration information for the video. **/
    private Options videoOptions = new Options();

    private VideoLoaderTask backgroundVideoLoaderTask;

    private DownloadVideoTask downloadVideoTask;
    /**
     * The video view and its custom UI elements.
     */
    protected VrVideoView videoWidgetView;

    /**
     * Seeking UI & progress indicator. The seekBar's progress value represents milliseconds in the
     * video.
     */
    private TextView title;
    private SeekBar seekBar;
    private TextView statusText;
    private ProgressBar progress_bar;


    private ImageButton volumeToggle;
    private boolean isMuted;

    /**
     * By default, the video will start playing as soon as it is loaded. This can be changed by using
     * {@link VrVideoView#pauseVideo()} after loading the video.
     */
    private boolean isPaused = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        title = (TextView)findViewById(R.id.title);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBarListener());
        statusText = (TextView) findViewById(R.id.status_text);
        progress_bar = (ProgressBar)findViewById(R.id.progressbar);

        // Make the source link clickable.
        TextView sourceText = (TextView) findViewById(R.id.source);
        sourceText.setText(Html.fromHtml(getString(R.string.source)));
        sourceText.setMovementMethod(LinkMovementMethod.getInstance());

        // Bind input and output objects for the view.
        videoWidgetView = (VrVideoView) findViewById(R.id.video_view);
        videoWidgetView.setEventListener(new ActivityEventListener());

        volumeToggle = (ImageButton) findViewById(R.id.volume_toggle);
        volumeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIsMuted(!isMuted);
            }
        });

        loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;


        // Initial launch of the app or an Activity recreation due to rotation.
        handleIntent(getIntent());






    }

    /**
     * Called when the Activity is already running and it's given a new intent.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, this.hashCode() + ".onNewIntent()");
        // Save the intent. This allows the getIntent() call in onCreate() to use this new Intent during
        // future invocations.
        setIntent(intent);
        // Load the new video.
        handleIntent(intent);
    }

    public int getLoadVideoStatus() {
        return loadVideoStatus;
    }

    private void setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
        volumeToggle.setImageResource(isMuted ? R.drawable.volume_off : R.drawable.volume_on);
        videoWidgetView.setVolume(isMuted ? 0.0f : 1.0f);
    }

    public boolean isMuted() {
        return isMuted;
    }

    @Override
    public void onBackPressed() {
        if(downloadVideoTask.isRunning == true){
            Toast.makeText(this, "Download in Progress.. Please wait",Toast.LENGTH_LONG).show();
            return;
        }
        super.onBackPressed();

    }
    /**
     * Load custom videos based on the Intent or load the default video. See the Javadoc for this
     * class for information on generating a custom intent via adb.
     */
    private void handleIntent(Intent intent) {
        // Determine if the Intent contains a file to load.
//        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
//            Log.i(TAG, "ACTION_VIEW Intent received");
//
//            fileUri = intent.getData().toString();
//            if (fileUri == null) {
//                Log.w(TAG, "No data uri specified. Use \"-d /path/filename\".");
//            } else {
//                Log.i(TAG, "Using file " + fileUri);
//            }
//
//            videoOptions.inputFormat = intent.getIntExtra("inputFormat", Options.FORMAT_DEFAULT);
//            videoOptions.inputType = intent.getIntExtra("inputType", Options.TYPE_MONO);
//        } else {
//            Log.i(TAG, "Intent is not ACTION_VIEW. Using the default video.");
//            fileUri = null;
//        }
        String fileUri = intent.getStringExtra("downloadurl");
        String filetitle = intent.getStringExtra("title");
        // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
        // take 100s of milliseconds.
        if (backgroundVideoLoaderTask != null) {
            // Cancel any task from a previous intent sent to this activity.
            backgroundVideoLoaderTask.cancel(true);
        }
//        backgroundVideoLoaderTask = new VideoLoaderTask();
  //      backgroundVideoLoaderTask.execute(Pair.create(fileUri, videoOptions));

        if(downloadVideoTask != null)
            downloadVideoTask.cancel(true);
        downloadVideoTask = new DownloadVideoTask(videoWidgetView,this);
        downloadVideoTask .execute(fileUri,filetitle);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(STATE_PROGRESS_TIME, videoWidgetView.getCurrentPosition());
        savedInstanceState.putLong(STATE_VIDEO_DURATION, videoWidgetView.getDuration());
        savedInstanceState.putBoolean(STATE_IS_PAUSED, isPaused);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
        videoWidgetView.seekTo(progressTime);
        seekBar.setMax((int) savedInstanceState.getLong(STATE_VIDEO_DURATION));
        seekBar.setProgress((int) progressTime);

        isPaused = savedInstanceState.getBoolean(STATE_IS_PAUSED);
        if (isPaused) {
            videoWidgetView.pauseVideo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Prevent the view from rendering continuously when in the background.
        videoWidgetView.pauseRendering();
        // If the video is playing when onPause() is called, the default behavior will be to pause
        // the video and keep it paused when onResume() is called.
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the 3D rendering.
        videoWidgetView.resumeRendering();
        // Update the text to account for the paused video in onPause().
        updateStatusText();
    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        videoWidgetView.shutdown();
        super.onDestroy();
    }

    private void togglePause() {
        if (isPaused) {
            videoWidgetView.playVideo();
        } else {
            videoWidgetView.pauseVideo();
        }
        isPaused = !isPaused;
        updateStatusText();
    }

    private void updateStatusText() {
        StringBuilder status = new StringBuilder();
        status.append(isPaused ? "Paused: " : "Playing: ");
        status.append(String.format("%.2f", videoWidgetView.getCurrentPosition() / 1000f));
        status.append(" / ");
        status.append(videoWidgetView.getDuration() / 1000f);
        status.append(" seconds.");
        statusText.setText(status.toString());
    }

    /**
     * When the user manipulates the seek bar, update the video position.
     */
    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoWidgetView.seekTo(progress);
                updateStatusText();
            } // else this was from the ActivityEventHandler.onNewFrame()'s seekBar.setProgress update.
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrVideoEventListener  {
        /**
         * Called by video widget on the UI thread when it's done loading the video.
         */
        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "Successfully loaded video " + videoWidgetView.getDuration());
            loadVideoStatus = LOAD_VIDEO_STATUS_SUCCESS;
            seekBar.setMax((int) videoWidgetView.getDuration());
            updateStatusText();
        }

        /**
         * Called by video widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            // An error here is normally due to being unable to decode the video format.
            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
            Toast.makeText(
                    SimpleVrVideoActivity.this, "Error loading video: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Error loading video: " + errorMessage);
        }

        @Override
        public void onClick() {
            togglePause();
        }

        /**
         * Update the UI every frame.
         */
        @Override
        public void onNewFrame() {
            updateStatusText();
            seekBar.setProgress((int) videoWidgetView.getCurrentPosition());
        }

        /**
         * Make the video play in a loop. This method could also be used to move to the next video in
         * a playlist.
         */
        @Override
        public void onCompletion() {
            videoWidgetView.seekTo(0);
//            if (backgroundVideoLoaderTask != null) {
//                // Cancel any task from a previous intent sent to this activity.
//                backgroundVideoLoaderTask.cancel(true);
//            }
//            Toast.makeText(getApplicationContext(),"Completed",Toast.LENGTH_LONG);
//            backgroundVideoLoaderTask = new VideoLoaderTask();
//            backgroundVideoLoaderTask.execute(Pair.create("whitetiger_h264_short.mp4", videoOptions));



        }
    }

    /**
     * Helper class to manage threading.
     */
    class VideoLoaderTask extends AsyncTask<Pair<String, Options>, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Pair<String, Options>... fileInformation) {
            try {
                if (fileInformation == null || fileInformation.length < 1
                        || fileInformation[0] == null || fileInformation[0].first == null) {
                    // No intent was specified, so we default to playing the local stereo-over-under video.
                    Options options = new Options();
                    options.inputType = Options.TYPE_MONO;
                    videoWidgetView.loadVideoFromAsset("rhino_h264_short.mp4", options);
                } else {
                    videoWidgetView.loadVideoFromAsset(fileInformation[0].first, fileInformation[0].second);
                    videoWidgetView.seekTo(0);
                }
            } catch (IOException e) {
                // An error here is normally due to being unable to locate the file.
                loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
                // Since this is a background thread, we need to switch to the main thread to show a toast.
                videoWidgetView.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast
                                .makeText(SimpleVrVideoActivity.this, "Error opening file. ", Toast.LENGTH_LONG)
                                .show();
                    }
                });
                Log.e(TAG, "Could not open video: " + e);
            }

            return true;
        }
    }

    public class DownloadVideoTask extends AsyncTask<String,Integer, Uri> {

        private VrVideoView vrvideoView;
        private Context context;
        public boolean isRunning = false;

        public DownloadVideoTask(VrVideoView vrvideoView, Context context) {
            this.vrvideoView = vrvideoView;
            this.context = context;
            isRunning = false;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            statusText.setText("Downloading progress.. " + values[0]/1000 + " Mbs");
            progress_bar.setProgress(values[0]);
        }

        protected void onPostExecute(Uri videoUri) {
            try {
                VrVideoView.Options options = new VrVideoView.Options();
                vrvideoView.loadVideo(videoUri,options);
                vrvideoView.playVideo();
                progress_bar.setVisibility(View.GONE);
                Toast.makeText(this.context,"Playing...",Toast.LENGTH_LONG);
            } catch (Exception ex) {
                String msg = ex.getMessage();
                Toast.makeText(this.context,"Test",Toast.LENGTH_LONG);
            }
            isRunning = false;

        }





        @Override
        protected Uri doInBackground(String... url) {
            isRunning = true;
            title.setText(url[1]);
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
            File file = getBaseContext().getFileStreamPath(fname);
            return file.exists();
        }
    }


}