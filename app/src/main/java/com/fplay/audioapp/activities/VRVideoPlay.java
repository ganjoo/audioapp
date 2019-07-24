package com.fplay.audioapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fplay.audioapp.R;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

public class VRVideoPlay extends AppCompatActivity {

    private static final String TAG = VRVideoPlay.class.getSimpleName();

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
    private VrVideoView.Options videoOptions = new VrVideoView.Options();

    //private VRVideoPlay.VideoLoaderTask backgroundVideoLoaderTask;

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
        setContentView(R.layout.activity_vrvideo_play);

        title = (TextView)findViewById(R.id.player_title);
        seekBar = (SeekBar) findViewById(R.id.player_seek_bar);
        seekBar.setOnSeekBarChangeListener(new VRVideoPlay.SeekBarListener());
        statusText = (TextView) findViewById(R.id.player_status_text);
        progress_bar = (ProgressBar)findViewById(R.id.player_progressbar);

        // Make the source link clickable.
        TextView sourceText = (TextView) findViewById(R.id.player_source);
        sourceText.setText(Html.fromHtml(getString(R.string.source)));
        sourceText.setMovementMethod(LinkMovementMethod.getInstance());

        // Bind input and output objects for the view.
        videoWidgetView = (VrVideoView) findViewById(R.id.player_video_view);
        videoWidgetView.setEventListener(new VRVideoPlay.ActivityEventListener());

        volumeToggle = (ImageButton) findViewById(R.id.player_volume_toggle);
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
        Uri videoUri = Uri.parse(intent.getStringExtra("downloadurl"));
        String filetitle = intent.getStringExtra("title");
        try {
            VrVideoView.Options options = new VrVideoView.Options();
            videoWidgetView.loadVideo(videoUri,options);
            videoWidgetView.playVideo();
            videoWidgetView.setDisplayMode(
                    VrVideoView.DisplayMode.FULLSCREEN_STEREO
            );
            progress_bar.setVisibility(View.GONE);
            Toast.makeText(this,"Playing...",Toast.LENGTH_LONG);
        } catch (Exception ex) {
            String msg = ex.getMessage();
            Toast.makeText(this,"Test",Toast.LENGTH_LONG);
        }
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
    private class ActivityEventListener extends VrVideoEventListener {
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
                    VRVideoPlay.this, "Error loading video: " + errorMessage, Toast.LENGTH_LONG)
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



}
