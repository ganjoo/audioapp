package com.fplay.audioapp.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fplay.audioapp.CircuitPhoto;
import com.fplay.audioapp.R;
import com.fplay.audioapp.activities.ImageTargets.ImageTargetOverlayRenderer;
import com.fplay.audioapp.activities.SampleApplication.SampleApplicationControl;
import com.fplay.audioapp.activities.SampleApplication.SampleApplicationException;
import com.fplay.audioapp.activities.SampleApplication.SampleApplicationSession;
import com.fplay.audioapp.activities.SampleApplication.utils.LoadingDialogHandler;
import com.fplay.audioapp.activities.SampleApplication.utils.SampleApplicationGLView;
import com.fplay.audioapp.activities.SampleApplication.utils.Texture;
import com.fplay.audioapp.activities.ui.SampleAppMenu.SampleAppMenu;
import com.fplay.audioapp.activities.ui.SampleAppMenu.SampleAppMenuGroup;
import com.fplay.audioapp.activities.ui.SampleAppMenu.SampleAppMenuInterface;
import com.fplay.audioapp.utils.Utils;
import com.bumptech.glide.Glide;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerInitListener;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


public class ARScanPage extends AppCompatActivity  implements SampleApplicationControl,
        SampleAppMenuInterface {

    private static final String LOGTAG = "ARScanPage";

    private static final String HINDI_FILM_YOUTUBE_ID = "sndobzzfcvo";
    private static final String ENGLISH_FILM_YOUTUBE_ID = "1BXGBo-AyeE";

    public static ArrayList<CircuitPhoto> CIRCUIT_PHOTOS;

    SampleApplicationSession vuforiaAppSession;

    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 2;
    private int mStartDatasetsIndex = 0;
    private int mDatasetsNumber = 0;
    boolean doubleBackToExitPressedOnce = false;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();

    // Our OpenGL view:
    private SampleApplicationGLView mGlView;

    // Our renderer:
    private ImageTargetOverlayRenderer mRenderer;

    private GestureDetector mGestureDetector;

    // The textures we will use for rendering:
    private Vector<Texture> mTextures;

    private boolean mSwitchDatasetAsap = false;
    private boolean mFlash = false;
    private boolean mContAutofocus = true;
    private boolean mExtendedTracking = false;

    private View mFocusOptionView;
    private View mFlashOptionView;

    private RelativeLayout mUILayout;

    private SampleAppMenu mSampleAppMenu;

    public LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;

    boolean mIsDroidDevice = false;

    AmazonS3 s3Client;
    String bucket = "lucknowzooaudiolowbit";
    File uploadToS3 = new File("/storage/sdcard0/Pictures/Screenshots/Screenshot.png");
    File downloadFromS3 = new File("/storage/sdcard0/Pictures/Screenshot.png");
    TransferUtility transferUtility;

    private List<String> mListAudio = new ArrayList<String>();
    private List<String> mFileList = new ArrayList<String>();
    MediaPlayer mPlayer = new MediaPlayer();

    ProgressBar pg;
    YouTubePlayer yt_player;
    LinearLayout view_ytb;
    YouTubePlayerView youtubePlayerView;
    RecyclerView recyclerView;
     Button btn_english_film;
    Button btn_hindi_film;
    Button exit;
    Button play_video;

    Button show_walkthrough;


    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);

        vuforiaAppSession = new SampleApplicationSession(this);

        startLoadingAnimation();
        mDatasetStrings.add("StonesAndChips.xml");
        mDatasetStrings.add("Tarmac.xml");
        mDatasetStrings.add("lucknow_zoo.xml");


        vuforiaAppSession
                .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mGestureDetector = new GestureDetector(this, new ARScanPage.GestureListener());

        // Load any sample specific textures:
        mTextures = new Vector<Texture>();
        loadTextures();

        mIsDroidDevice = Build.MODEL.toLowerCase().startsWith(
                "droid");

        // callback method to call credentialsProvider method.
        s3credentialsProvider();

        // callback method to call the setTransferUtility method
        setTransferUtility();

        fetchFileFromS3(new View(this));

        pg = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        pg.setElevation(99);
        pg.setForegroundGravity(Gravity.CENTER);
        addContentView(pg, new LinearLayout.LayoutParams(128, 128));
        pg.setVisibility(View.GONE);

        mFileList.clear();
        listAssetFilesNew("");

        //mFileList = getFilesList();
        //listAssetFiles("");

        BigImageViewer.initialize(GlideImageLoader.with(this));


    }

    private boolean listAssetFilesNew(String path) {
        String [] list;
        try {
            list = this.getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    if (!listAssetFilesNew(path + "/" + file))
                        return false;
                    else {
                        // This is a file
                        // TODO: add file name to an array list
                        mFileList.add(file);
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }
    public void showARinfo(String name) {

        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.ping);
        mp.start();
        if(mRenderer.stopTracking)
            return;

        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(this.getMainLooper());
        final String tracker_name = name;
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if(checkForFish(tracker_name)) {

                    final View view_1;
                    view_1 = inflater.inflate(R.layout.animal_details_ar_popup, null);
                    TextView desc = (TextView) view_1.findViewById(R.id.tv_animal_description);
                    //desc.setText(R.string.info_red_common_gold);
                    desc.setText(tracker_name);
                    desc.setMovementMethod(new ScrollingMovementMethod());
                    desc.setText(Utils.getTextfromFishTracker(tracker_name,getApplicationContext()));
                    //desc.setText("This is Hyenna. isko kuch to aur bhi kehte hai");
                    Button dismiss = (Button) view_1.findViewById(R.id.button_dismiss);
                    dismiss.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            view_1.setVisibility(View.INVISIBLE);
                            mRenderer.stopTracking = false;
                            mPlayer.stop();
                        }
                    });
                    addContentView(view_1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    Thread t = new Thread(new Runnable() {
                        public void run() {
                            playCageNumber(tracker_name);
                        }
                    });

                    t.start();



                }else if(tracker_name.equalsIgnoreCase("entrance_structure") == true){

                        //Entrance
                    Toast.makeText(getBaseContext(), tracker_name,Toast.LENGTH_LONG).show();
                    popUpAndShowARVideo("gq8Hyo4UP20", true);
                }
                else if(tracker_name.equalsIgnoreCase("entrance-lion-signage") == true){

                    //Entrance
                    Toast.makeText(getBaseContext(), tracker_name,Toast.LENGTH_LONG).show();
                    popUpAndShowARVideo("gq8Hyo4UP20", true);
                }
                else if(tracker_name.equalsIgnoreCase("butterfly-park-signage") == true){

                    //Entrance
                    Toast.makeText(getBaseContext(), tracker_name,Toast.LENGTH_LONG).show();
                    popUpAndShowARVideo("1iaJkzbQJbE", false);
                }
                else if(tracker_name.equalsIgnoreCase("butterfly-park") == true){

                    //Entrance
                    Toast.makeText(getBaseContext(), tracker_name,Toast.LENGTH_LONG).show();
                    popUpAndShowARVideo("1iaJkzbQJbE", false);

                }
                else if(tracker_name.equalsIgnoreCase("Entrance") == true){

                    //Entrance
                    Toast.makeText(getBaseContext(), tracker_name,Toast.LENGTH_LONG).show();
                    popUpAndShowARVideo("gq8Hyo4UP20", true);
                }


                    else if((tracker_name.split("-")[1].equalsIgnoreCase("eco") == false) &&
                        ((tracker_name.split("-")[1].equalsIgnoreCase("park") == false)))
                        {

                        int circuit_number = Integer.parseInt(tracker_name.split("-")[1]);
                        view_ytb = (LinearLayout) inflater.inflate(R.layout.ar_popup_video, null);
                        youtubePlayerView = view_ytb.findViewById(R.id.youtube_player_view);
                        getLifecycle().addObserver(youtubePlayerView);
                         addContentView(view_ytb, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                         exit = (Button)view_ytb.findViewById(R.id.btn_exit_video);
                         exit.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View view) {
                                 youtubePlayerView.release();
                                 recyclerView.setVisibility(View.INVISIBLE);
                                 youtubePlayerView.setVisibility(View.GONE);
                                 btn_hindi_film.setVisibility(View.INVISIBLE);
                                 btn_english_film.setVisibility(View.INVISIBLE);
                                 play_video.setVisibility(View.INVISIBLE);
                                 //view_ytb.setVisibility(View.GONE);

                                 mRenderer.stopTracking = false;
                                 exit.setVisibility(View.INVISIBLE);
                             }
                         });
                    youtubePlayerView.initialize(new YouTubePlayerInitListener() {
                            @Override
                            public void onInitSuccess(@NonNull final YouTubePlayer initializedYouTubePlayer) {
                                initializedYouTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                                    @Override
                                    public void onReady() {
                                        String videoId = "sndobzzfcvo";
                                        //initializedYouTubePlayer.loadVideo(videoId, 0);
                                        yt_player = initializedYouTubePlayer;
                                        play_video.setVisibility(View.VISIBLE);


                                    }
                                });
                            }
                        }, true);
                    LinearLayout row_language = (LinearLayout)view_ytb.findViewById(R.id.row_language);
                    row_language.setVisibility(View.INVISIBLE);
                    youtubePlayerView.setVisibility(View.GONE);
                    btn_english_film = (Button) view_ytb.findViewById(R.id.btn_english_film);
                    btn_hindi_film = (Button) view_ytb.findViewById(R.id.btn_hindi_film);
                    btn_english_film.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            yt_player.loadVideo(ENGLISH_FILM_YOUTUBE_ID,0);
                            btn_english_film.setPressed(true);
                            btn_hindi_film.setPressed(false);
                        }

                    });



                    btn_hindi_film.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            yt_player.loadVideo(HINDI_FILM_YOUTUBE_ID,0);
                            btn_english_film.setPressed(false);
                            btn_hindi_film.setPressed(true);
                        }
                    });

                    btn_hindi_film.setPressed(true);





                    final RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 3);
                    recyclerView = (RecyclerView) findViewById(R.id.rv_images);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(layoutManager);

                    ArrayList<CircuitPhoto> cp = new ArrayList<CircuitPhoto>();
                    for(CircuitPhoto c:CircuitPhoto.getSpacePhotos(circuit_number)){
                        cp.add(c);
                    }
                    ImageGalleryAdapter adapter = new ImageGalleryAdapter(getApplicationContext(), cp);
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(adapter);
                    recyclerView.setVisibility(View.VISIBLE);

                    play_video = (Button)view_ytb.findViewById(R.id.play_movie);
                    play_video.setVisibility(View.INVISIBLE);
                    play_video.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            youtubePlayerView.setVisibility(View.VISIBLE);
                            yt_player.loadVideo(HINDI_FILM_YOUTUBE_ID, 0);
                            LinearLayout row_language = (LinearLayout)view_ytb.findViewById(R.id.row_language);
                            row_language.setVisibility(View.VISIBLE);
                            play_video.setVisibility(View.INVISIBLE);
                            recyclerView.setVisibility(View.GONE);

                        }
                    });
                }else if(tracker_name.split("-")[1].equalsIgnoreCase("eco") == true){
                    //For Eco map
                    //Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                    //startActivity(intent);
//
//                    View webview = inflater.inflate(R.layout.activity_web_view, null);
//                    WebView wb_view = webview.findViewById(R.id.webview);
//                    wb_view.getSettings().setPluginState(WebSettings.PluginState.ON);
//                    wb_view.getSettings().setJavaScriptEnabled(true);
//                   // wb_view.getSettings().setPluginsEnabled(true);
//                    wb_view.getSettings().setAllowFileAccess(true);
//                    String url ="file:///android_asset/flash.html";
//                    //wb_view.loadUrl("https://s3.ap-south-1.amazonaws.com/lucknowzoofiles/Wild_Sanctuary.swf");
//                    wb_view.loadUrl("http://www.buzz4health.com");
//                    addContentView(webview, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
//                    webview.setVisibility(View.VISIBLE);
                }else if(tracker_name.split("-")[1].equalsIgnoreCase("park") == true){
                    //For Butterfly park
                    //Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                    //startActivity(intent);

//                    View webview = inflater.inflate(R.layout.activity_web_view, null);
//                    WebView wb_view = webview.findViewById(R.id.webview);
//                    wb_view.getSettings().setPluginState(WebSettings.PluginState.ON);
//                    wb_view.getSettings().setJavaScriptEnabled(true);
//                    // wb_view.getSettings().setPluginsEnabled(true);
//                    wb_view.getSettings().setAllowFileAccess(true);
//                    String url ="file:///android_asset/flash.html";
//                    //wb_view.loadUrl("https://s3.ap-south-1.amazonaws.com/lucknowzoofiles/Wild_Sanctuary.swf");
//                    wb_view.loadUrl("http://www.buzz4health.com");
//                    addContentView(webview, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
//                    webview.setVisibility(View.VISIBLE);
                }




                }
        };
        mainHandler.post(myRunnable);

    }

    private boolean checkForFish(String name) {
        return Character.isDigit(name.charAt(0));

    }

    private List<String> getFilesList()//Returns the files written earlier
    {
        mFileList.clear();
        String list_files[] = this.getFilesDir().list();
        for(String ele:list_files){
            mFileList.add(this.getFilesDir() + File.separator + ele);
        }
        return mFileList;
    }
    private boolean listAssetFiles(String path) {
        String [] list;
        try {
            list = this.getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    if (!listAssetFiles(path + "/" + file))
                        return false;
                    else {
                        // This is a file
                        // TODO: add file name to an array list
                        mFileList.add(file);
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }
    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener
    {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();


        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
            if (!result)
                Log.e("SingleTapUp", "Unable to trigger focus");

            // Generates a Handler to trigger continuous auto-focus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable()
            {
                public void run()
                {
                    if (mContAutofocus)
                    {
                        final boolean autofocusResult = CameraDevice.getInstance().setFocusMode(
                                CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

                        if (!autofocusResult)
                            Log.e("SingleTapUp", "Unable to re-enable continuous auto-focus");
                    }
                }
            }, 1000L);

            return true;
        }
    }


    // We want to load specific textures from the APK, which we will later use
    // for rendering.

    private void loadTextures()
    {
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBrass.png",
                getAssets()));
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBlue.png",
                getAssets()));
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotRed.png",
                getAssets()));
        mTextures.add(Texture.loadTextureFromApk("ImageTargets/Buildings.jpeg",
                getAssets()));
    }


    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();

        showProgressIndicator(true);

        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        vuforiaAppSession.onResume();
    }


    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }


    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        // Turn off the flash
        if (mFlashOptionView != null && mFlash)
        {
            // OnCheckedChangeListener is called upon changing the checked state
            setMenuToggle(mFlashOptionView, false);
        }

        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
    }


    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();

        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;

        System.gc();
        killMediaPlayer();
    }


    // Initializes AR application components.
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        mRenderer = new ImageTargetOverlayRenderer(this, vuforiaAppSession);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);
    }


    private void startLoadingAnimation()
    {
        mUILayout = (RelativeLayout) View.inflate(this, R.layout.camera_overlay,
                null);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_indicator);

        // Shows the loading indicator at start
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        // Adds the inflated audio_animal_popup to the view
        addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

    }


    // Methods to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (mCurrentDataset == null)
            mCurrentDataset = objectTracker.createDataSet();

        if (mCurrentDataset == null)
            return false;

        if (!mCurrentDataset.load(
                mDatasetStrings.get(mCurrentDatasetSelectionIndex),
                STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;

        if (!objectTracker.activateDataSet(mCurrentDataset))
            return false;

        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++)
        {
            Trackable trackable = mCurrentDataset.getTrackable(count);
            if(isExtendedTrackingActive())
            {
                trackable.startExtendedTracking();
            }

            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(LOGTAG, "UserData:Set the following user data "
                    + (String) trackable.getUserData());
        }

        return true;
    }


    @Override
    public boolean doUnloadTrackersData()
    {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (mCurrentDataset != null && mCurrentDataset.isActive())
        {
            if (objectTracker.getActiveDataSet(0).equals(mCurrentDataset)
                    && !objectTracker.deactivateDataSet(mCurrentDataset))
            {
                result = false;
            } else if (!objectTracker.destroyDataSet(mCurrentDataset))
            {
                result = false;
            }

            mCurrentDataset = null;
        }

        return result;
    }

    @Override
    public void onVuforiaResumed()
    {
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
    }

    @Override
    public void onVuforiaStarted()
    {
        mRenderer.updateConfiguration();

        if (mContAutofocus)
        {
            // Set camera focus mode
            if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO))
            {
                // If continuous autofocus mode fails, attempt to set to a different mode
                if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO))
                {
                    CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
                }

                // Update Toggle state
                setMenuToggle(mFocusOptionView, false);
            }
            else
            {
                // Update Toggle state
                setMenuToggle(mFocusOptionView, true);
            }
        }
        else
        {
            setMenuToggle(mFocusOptionView, false);
        }

        showProgressIndicator(false);
    }


    public void showProgressIndicator(boolean show)
    {
        if (loadingDialogHandler != null)
        {
            if (show)
            {
                loadingDialogHandler
                        .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
            }
            else
            {
                loadingDialogHandler
                        .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
            }
        }
    }


    @Override
    public void onInitARDone(SampleApplicationException exception)
    {

        if (exception == null)
        {
            initApplicationAR();

            mRenderer.setActive(true);

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();

            // Sets the audio_animal_popup background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

            vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);

            mSampleAppMenu = new SampleAppMenu(this, this, "Image Targets",
                    mGlView, mUILayout, null);
            setSampleAppMenuSettings();

        } else
        {
            Log.e(LOGTAG, exception.getString());
            showInitializationErrorMessage(exception.getString());
        }
    }


    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String message)
    {
        final String errorMessage = message;
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (mErrorDialog != null)
                {
                    mErrorDialog.dismiss();
                }

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ARScanPage.this);
                builder
                        .setMessage(errorMessage)
                        .setTitle(getString(R.string.INIT_ERROR))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton(getString(R.string.button_OK),
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        finish();
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }


    @Override
    public void onVuforiaUpdate(State state)
    {
        if (mSwitchDatasetAsap)
        {
            mSwitchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker
                    .getClassType());
            if (ot == null || mCurrentDataset == null
                    || ot.getActiveDataSet(0) == null)
            {
                Log.d(LOGTAG, "Failed to swap datasets");
                return;
            }

            doUnloadTrackersData();
            doLoadTrackersData();
        }
    }


    @Override
    public boolean doInitTrackers()
    {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                    LOGTAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }


    @Override
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();

        return result;
    }


    @Override
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();

        return result;
    }


    @Override
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // Process the Gestures
        if (mSampleAppMenu != null && mSampleAppMenu.processEvent(event))
            return true;

        return mGestureDetector.onTouchEvent(event);
    }


    public boolean isExtendedTrackingActive()
    {
        return mExtendedTracking;
    }

    final public static int CMD_BACK = -1;
    final public static int CMD_EXTENDED_TRACKING = 1;
    final public static int CMD_AUTOFOCUS = 2;
    final public static int CMD_FLASH = 3;
    final public static int CMD_CAMERA_FRONT = 4;
    final public static int CMD_CAMERA_REAR = 5;
    final public static int CMD_DATASET_START_INDEX = 6;


    // This method sets the menu's settings
    private void setSampleAppMenuSettings()
    {
        SampleAppMenuGroup group;

        group = mSampleAppMenu.addGroup("", false);
        group.addTextItem(getString(R.string.menu_back), -1);

        group = mSampleAppMenu.addGroup("", true);
        group.addSelectionItem(getString(R.string.menu_extended_tracking),
                CMD_EXTENDED_TRACKING, false);
        mFocusOptionView = group.addSelectionItem(getString(R.string.menu_contAutofocus),
                CMD_AUTOFOCUS, mContAutofocus);
        mFlashOptionView = group.addSelectionItem(
                getString(R.string.menu_flash), CMD_FLASH, false);

        Camera.CameraInfo ci = new Camera.CameraInfo();
        boolean deviceHasFrontCamera = false;
        boolean deviceHasBackCamera = false;
        for (int i = 0; i < Camera.getNumberOfCameras(); i++)
        {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                deviceHasFrontCamera = true;
            else if (ci.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
                deviceHasBackCamera = true;
        }

        if (deviceHasBackCamera && deviceHasFrontCamera)
        {
            group = mSampleAppMenu.addGroup(getString(R.string.menu_camera),
                    true);
            group.addRadioItem(getString(R.string.menu_camera_front),
                    CMD_CAMERA_FRONT, false);
            group.addRadioItem(getString(R.string.menu_camera_back),
                    CMD_CAMERA_REAR, true);
        }

        group = mSampleAppMenu
                .addGroup(getString(R.string.menu_datasets), true);
        mStartDatasetsIndex = CMD_DATASET_START_INDEX;
        mDatasetsNumber = mDatasetStrings.size();

        group.addRadioItem("Stones & Chips", mStartDatasetsIndex, true);
        group.addRadioItem("Tarmac", mStartDatasetsIndex + 1, false);

        mSampleAppMenu.attachMenu();
    }


    private void setMenuToggle(View view, boolean value)
    {
        // OnCheckedChangeListener is called upon changing the checked state
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            ((Switch) view).setChecked(value);
        } else
        {
            ((CheckBox) view).setChecked(value);
        }
    }


    @Override
    public boolean menuProcess(int command)
    {

        boolean result = true;

        switch (command)
        {
            case CMD_BACK:
                finish();
                break;

            case CMD_FLASH:
                result = CameraDevice.getInstance().setFlashTorchMode(!mFlash);

                if (result)
                {
                    mFlash = !mFlash;
                } else
                {
                    showToast(getString(mFlash ? R.string.menu_flash_error_off
                            : R.string.menu_flash_error_on));
                    Log.e(LOGTAG,
                            getString(mFlash ? R.string.menu_flash_error_off
                                    : R.string.menu_flash_error_on));
                }
                break;

            case CMD_AUTOFOCUS:

                if (mContAutofocus)
                {
                    result = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);

                    if (result)
                    {
                        mContAutofocus = false;
                    } else
                    {
                        showToast(getString(R.string.menu_contAutofocus_error_off));
                        Log.e(LOGTAG,
                                getString(R.string.menu_contAutofocus_error_off));
                    }
                } else
                {
                    result = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

                    if (result)
                    {
                        mContAutofocus = true;
                    } else
                    {
                        showToast(getString(R.string.menu_contAutofocus_error_on));
                        Log.e(LOGTAG,
                                getString(R.string.menu_contAutofocus_error_on));
                    }
                }

                break;

            case CMD_CAMERA_FRONT:
            case CMD_CAMERA_REAR:

                // Turn off the flash
                if (mFlashOptionView != null && mFlash)
                {
                    setMenuToggle(mFlashOptionView, false);
                }

                vuforiaAppSession.stopCamera();

                vuforiaAppSession
                        .startAR(command == CMD_CAMERA_FRONT ? CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_FRONT
                                : CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_BACK);

                break;

            case CMD_EXTENDED_TRACKING:
                for (int tIdx = 0; tIdx < mCurrentDataset.getNumTrackables(); tIdx++)
                {
                    Trackable trackable = mCurrentDataset.getTrackable(tIdx);

                    if (!mExtendedTracking)
                    {
                        if (!trackable.startExtendedTracking())
                        {
                            Log.e(LOGTAG,
                                    "Failed to start extended tracking target");
                            result = false;
                        } else
                        {
                            Log.d(LOGTAG,
                                    "Successfully started extended tracking target");
                        }
                    } else
                    {
                        if (!trackable.stopExtendedTracking())
                        {
                            Log.e(LOGTAG,
                                    "Failed to stop extended tracking target");
                            result = false;
                        } else
                        {
                            Log.d(LOGTAG,
                                    "Successfully started extended tracking target");
                        }
                    }
                }

                if (result)
                    mExtendedTracking = !mExtendedTracking;

                break;

            default:
                if (command >= mStartDatasetsIndex
                        && command < mStartDatasetsIndex + mDatasetsNumber)
                {
                    mSwitchDatasetAsap = true;
                    mCurrentDatasetSelectionIndex = command
                            - mStartDatasetsIndex;
                }
                break;
        }

        return result;
    }


    private void showToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void s3credentialsProvider(){

        // Initialize the AWS Credential
        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:dc45a7f4-f4d2-4577-9c5f-5cd1e23ec795", // Identity pool ID
                Regions.US_EAST_1 // Region
        );

        createAmazonS3Client(credentialsProvider);
    }

    /**
     *  Create a AmazonS3Client constructor and pass the credentialsProvider.
     * @param credentialsProvider
     */
    public void createAmazonS3Client(CognitoCachingCredentialsProvider
                                             credentialsProvider){

        // Create an S3 client
        s3Client = new AmazonS3Client(credentialsProvider);

        // Set the region of your S3 bucket
        s3Client.setRegion(Region.getRegion(Regions.US_EAST_1));
    }

    public void setTransferUtility(){

        transferUtility = new TransferUtility(s3Client, getApplicationContext());
    }

    /**
     * This method is used to upload the file to S3 by using TransferUtility class
     * @param view
     */
    public void uploadFileToS3(View view){

        TransferObserver transferObserver = transferUtility.upload(
                bucket,     /* The bucket to upload to */
                "Screenshot.png",    /* The key for the uploaded object */
                uploadToS3       /* The file where the data to upload exists */
        );

        transferObserverListener(transferObserver);
    }

    /**
     *  This method is used to Download the file to S3 by using transferUtility class
     * @param view
     **/
    public void downloadFileFromS3(View view){

        TransferObserver transferObserver = transferUtility.download(
                bucket,     /* The bucket to download from */
                "Screenshot.png",    /* The key for the object to download */
                downloadFromS3        /* The file to download the object to */
        );
        transferObserverListener(transferObserver);
    }

    public void fetchFileFromS3(View view){

        // Get List of files from S3 Bucket
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {

                try {
                    Looper.prepare();
                    mListAudio = getObjectNamesForBucket(bucket, s3Client);
//
//                    for (int i=0; i< mListAudio.size(); i++){
//                        Toast.makeText(ARScanPage.this, mListAudio.get(i),Toast.LENGTH_SHORT).show();
//                    }
                    Looper.loop();
                    // Log.e("tag", "listing "+ listing);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("tag", "Exception found while listing "+ e);
                }

            }
        });
        thread.start();
    }

    /**
     * @desc This method is used to return list of files name from S3 Bucket
     * @param bucket
     * @param s3Client
     * @return object with list of files
     */
    private List<String> getObjectNamesForBucket(String bucket, AmazonS3 s3Client) {
        ObjectListing objects=s3Client.listObjects(bucket);
        List<String> objectNames=new ArrayList<String>(objects.getObjectSummaries().size());
        Iterator<S3ObjectSummary> iterator=objects.getObjectSummaries().iterator();
        while (iterator.hasNext()) {
            objectNames.add(iterator.next().getKey());
        }
        while (objects.isTruncated()) {
            objects=s3Client.listNextBatchOfObjects(objects);
            iterator=objects.getObjectSummaries().iterator();
            while (iterator.hasNext()) {
                objectNames.add(iterator.next().getKey());
            }
        }
        return objectNames;
    }

    /**
     * This is listener method of the TransferObserver
     * Within this listener method, we get status of uploading and downloading file,
     * to display percentage of the part of file to be uploaded or downloaded to S3
     * It displays an error, when there is a problem in  uploading or downloading file to or from S3.
     * @param transferObserver
     */

    public void transferObserverListener(TransferObserver transferObserver){

        transferObserver.setTransferListener(new TransferListener(){

            @Override
            public void onStateChanged(int id, TransferState state) {
                Toast.makeText(getApplicationContext(), "State Change"
                        + state, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent/bytesTotal * 100);
                Toast.makeText(getApplicationContext(), "Progress in %"
                        + percentage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("error","error");
            }

        });
    }
    private void playAudio(String url) throws Exception
    {
        killMediaPlayer();

        mPlayer = new MediaPlayer();
        mPlayer.setDataSource(url);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(),"Loading Audio from the server...",Toast.LENGTH_LONG).show();

                pg.setVisibility(View.VISIBLE);
            }
        });
        mPlayer.prepare();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(),"Playing...",Toast.LENGTH_LONG).show();

                pg.setVisibility(View.GONE);
            }
        });
        mPlayer.start();
    }
    private void killMediaPlayer() {
        if(mPlayer!=null) {
            try {
                mPlayer.release();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void playCageNumber(String tracker_number) {
        String to_play = "";
        String tracker_id = tracker_number.split("_")[0];

        //First search for local assets:
        for(String file: mFileList){
            if(file.split("/")[file.split("/").length-1].startsWith(tracker_id + "_")){
                to_play = file;
                break;
            }
        }
        if(to_play.length() == 0){
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"Downloading file from server. Please make sure you have good internet.",Toast.LENGTH_LONG);
                }
            });

            for(String file:mListAudio){
                if(file.startsWith(tracker_id + "_")){
                    to_play = "https://s3.amazonaws.com/" + bucket + "/" + file;
                    break;
                }
            }

        }

        if(to_play.compareToIgnoreCase("")==0){
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"Please enter a valid cage number from 1 to 66 or 100-150",Toast.LENGTH_LONG);
                }
            });

            return;
        }

        try {

          //  playAudio(to_play);
            playAssetAudio(to_play);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void playAssetAudio(String assetFile) throws Exception
    {
        killMediaPlayer();

        mPlayer = new MediaPlayer();
        AssetFileDescriptor afd = this.getAssets().openFd(assetFile);
        mPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(),"Loading Audio from the server...",Toast.LENGTH_LONG).show();

                // pg.setVisibility(View.VISIBLE);
            }
        });
        mPlayer.prepare();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(),"Playing...",Toast.LENGTH_LONG).show();

                // pg.setVisibility(View.GONE);
            }
        });
        mPlayer.start();
    }

    private class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder>  {

        @Override
        public ImageGalleryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View photoView = inflater.inflate(R.layout.item_circuit_grid, parent, false);
            ImageGalleryAdapter.MyViewHolder viewHolder = new ImageGalleryAdapter.MyViewHolder(photoView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ImageGalleryAdapter.MyViewHolder holder, int position) {

            CircuitPhoto spacePhoto = mCircuitPhotos.get(position);
            ImageView imageView = holder.mPhotoImageView;
            Glide.with(mContext)
                    .load(spacePhoto.getUrl())
                    .into(imageView);

        }

        @Override
        public int getItemCount() {
            return (mCircuitPhotos.size());
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public ImageView mPhotoImageView;
            public TextView mCircuitName;

            public MyViewHolder(View itemView) {

                super(itemView);
                mPhotoImageView = (ImageView) itemView.findViewById(R.id.iv_photo);
                //mCircuitName = (TextView)itemView.findViewById(R.id.circuit_name);
                mPhotoImageView.setOnClickListener(this);
                //mCircuitName.setOnClickListener(this);
                //itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {

                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION) {
                    CircuitPhoto circuitPhoto = mCircuitPhotos.get(position);
                    Intent intent = new Intent(mContext, CircuitPhotoActivity.class);
                    //intent.putExtra(CircuitPhotoActivity.EXTRA_CIRCUIT_PHOTO, circuitPhoto);
                    intent.putParcelableArrayListExtra("circuit_list",mCircuitPhotos);
                    CIRCUIT_PHOTOS  = mCircuitPhotos;
                    startActivity(intent);
                }
            }
        }

        private ArrayList<CircuitPhoto> mCircuitPhotos;
        private Context mContext;

        public ImageGalleryAdapter(Context context, ArrayList<CircuitPhoto> spacePhotos) {
            mContext = context;
            mCircuitPhotos = spacePhotos;
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Are you sure you want to quit the AR tracking? Press back again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    void popUpAndShowARVideo(final String yt_id, boolean showWalthrough){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        view_ytb = (LinearLayout) inflater.inflate(R.layout.ar_popup_video, null);

        show_walkthrough = view_ytb.findViewById(R.id.play_walkthrough);
        if(showWalthrough)
            show_walkthrough.setVisibility(View.VISIBLE);
        show_walkthrough.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AudioMainPage.class));
            }
        });

        youtubePlayerView = view_ytb.findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(youtubePlayerView);
        addContentView(view_ytb, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        exit = (Button)view_ytb.findViewById(R.id.btn_exit_video);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                youtubePlayerView.release();
//                recyclerView.setVisibility(View.INVISIBLE);
                youtubePlayerView.setVisibility(View.GONE);
                btn_hindi_film.setVisibility(View.INVISIBLE);
                btn_english_film.setVisibility(View.INVISIBLE);
                play_video.setVisibility(View.INVISIBLE);
                //view_ytb.setVisibility(View.GONE);

                mRenderer.stopTracking = false;
                exit.setVisibility(View.INVISIBLE);
            }
        });
        youtubePlayerView.initialize(new YouTubePlayerInitListener() {
            @Override
            public void onInitSuccess(@NonNull final YouTubePlayer initializedYouTubePlayer) {
                initializedYouTubePlayer.addListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady() {
                        String videoId = yt_id;
                        //initializedYouTubePlayer.loadVideo(videoId, 0);
                        yt_player = initializedYouTubePlayer;
                        play_video.setVisibility(View.VISIBLE);


                    }
                });
            }
        }, true);

        play_video = (Button)view_ytb.findViewById(R.id.play_movie);
        play_video.setText("Play Video");
        play_video.setVisibility(View.INVISIBLE);
        play_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                youtubePlayerView.setVisibility(View.VISIBLE);
                yt_player.loadVideo(yt_id, 0);
                LinearLayout row_language = (LinearLayout)view_ytb.findViewById(R.id.row_language);
                row_language.setVisibility(View.VISIBLE);
                play_video.setVisibility(View.INVISIBLE);
//                recyclerView.setVisibility(View.GONE);

            }
        });
        LinearLayout row_language = (LinearLayout)view_ytb.findViewById(R.id.row_language);
        row_language.setVisibility(View.INVISIBLE);
        youtubePlayerView.setVisibility(View.GONE);
        btn_english_film = (Button) view_ytb.findViewById(R.id.btn_english_film);
        btn_hindi_film = (Button) view_ytb.findViewById(R.id.btn_hindi_film);
        btn_hindi_film.setVisibility(View.INVISIBLE);
        btn_english_film.setVisibility(View.INVISIBLE);




    }
}
