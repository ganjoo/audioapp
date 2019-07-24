package com.fplay.audioapp.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.bumptech.glide.Glide;
import com.fplay.audioapp.BaseActivity;
import com.fplay.audioapp.R;
import com.fplay.audioapp.SlidingIntroImages_Adapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


@EActivity(R.layout.activity_intro_page)
public class IntroPage extends FragmentActivity {

    /**
     * The number of pages (wizard steps) to show in this demo.
     */

    AmazonS3 s3Client;
    String bucket = "lucknowzooaudiolowbit";
    private List<String> mListAudio = new ArrayList<String>();
    TransferUtility transferUtility;
    Snackbar snackbar;
    private static final int NUM_PAGES = 6;

    int currentPage = 0;
    Timer timer;
    final long DELAY_MS = 500;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 3000; // time in milliseconds between successive task executions.

    @ViewById
    public ViewPager pager;



    @ViewById
    public ImageView imageView3;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;


    private static final Integer[] IMAGES= {R.drawable.banner_general,R.drawable.banner_ar,R.drawable.chimp,R.drawable.tiger,R.drawable.duck,R.drawable.leopard};
    private ArrayList<Integer> ImagesArray = new ArrayList<Integer>();

    @AfterViews
    public  void afterViews() {

        Glide.with(this).load(R.drawable.animals_intro).into(imageView3);

        for(int i=0;i<IMAGES.length;i++)
            ImagesArray.add(IMAGES[i]);



        pager.setAdapter(new SlidingIntroImages_Adapter(IntroPage.this,ImagesArray));

       // mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());

        //pager.setPageTransformer(true, new ZoomOutPageTransformer());
        //pager.setAdapter(mPagerAdapter);

        /*After setting the adapter use the timer */
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                    timer.cancel();
                }
                pager.setCurrentItem(currentPage++, false);
            }
        };

        timer = new Timer(); // This will create a new Thread
        timer .schedule(new TimerTask() { // task to be scheduled

            @Override
            public void run() {
                handler.post(Update);
            }
        }, DELAY_MS, PERIOD_MS);

        ImageButton next = (ImageButton)findViewById(R.id.enter_btn);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(IntroPage.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(IntroPage.this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
                }
                else
                {
                    startActivity(new Intent(IntroPage.this, BaseActivity.class));
                }


            }
        });

        setUpAWSandLoadAudio(getApplicationContext());

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Begin monitoring for Aruba Beacon-based Campaign events
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(IntroPage.this, BaseActivity.class));
            }
        }


    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            FragmentIntroSlider fragment = new FragmentIntroSlider();
            Bundle args = new Bundle();
            args.putInt("image_count", position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }



    public  void setUpAWSandLoadAudio(Context ctx){
        // callback method to call credentialsProvider method.
        s3credentialsProvider();

        // callback method to call the setTransferUtility method
        setTransferUtility();

        fetchFileFromS3(new View(ctx));
    }
    public  void s3credentialsProvider(){

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
    public  void createAmazonS3Client(CognitoCachingCredentialsProvider
                                              credentialsProvider){

        // Create an S3 client
        s3Client = new AmazonS3Client(credentialsProvider);

        // Set the region of your S3 bucket
        s3Client.setRegion(Region.getRegion(Regions.US_EAST_1));
    }

    public void setTransferUtility(){

        transferUtility = new TransferUtility(s3Client, getApplicationContext());
    }

    private List<String> getObjectNamesForBucket(String bucket, AmazonS3 s3Client, String prefix) {
        ObjectListing objects=s3Client.listObjects(bucket);
        List<String> objectNames=new ArrayList<String>(objects.getObjectSummaries().size());
        Iterator<S3ObjectSummary> iterator=objects.getObjectSummaries().iterator();
        while (iterator.hasNext()) {
            objectNames.add(prefix + iterator.next().getKey());
        }
        while (objects.isTruncated()) {
            objects=s3Client.listNextBatchOfObjects(objects);
            iterator=objects.getObjectSummaries().iterator();
            while (iterator.hasNext()) {
                objectNames.add(prefix + iterator.next().getKey());
            }
        }
        return objectNames;
    }

    public void fetchFileFromS3(View view){

        // Get List of files from S3 Bucket


        final Context ctx = this;
        boolean audioExists = false;
            String list_files[] = this.getFilesDir().list();
            if(list_files.length>127){
                audioExists = true;
                final boolean downloadAudio = true;
            }

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {

                try {
                    Looper.prepare();
                    mListAudio = getObjectNamesForBucket(bucket, s3Client,"https://s3.amazonaws.com/" + bucket + "/");


                    String list_files[] = ((IntroPage_)ctx).getFilesDir().list();
                    boolean audioExists = true;

                    //Commenting the following block because asset file now contains files
//                    if(list_files.length<127){
//                        audioExists = false;
//                        final boolean downloadAudio = true;
//                        ThreadDownloader thread = new ThreadDownloader(ctx,mListAudio);
//                        thread.start();
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

    public void onAudioSetupCompleted(){

        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

// 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Audio Setup Completed.. Do you want to start the audio tour now?")
                .setTitle("Audio Tour Setup");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(getApplicationContext(), AudioMainPage.class));
            }
        });


// 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();

    }
    public void onVRSetupCompleted(){

    }
    public void onARSetupCompleted(){

    }

    public void onProgressDownload(float percentage){
        int display_percentage = (int)percentage;

        snackbar = Snackbar
                .make( getWindow().getDecorView().getRootView(), "Audio setup status: " + display_percentage + "% Completed", Snackbar.LENGTH_LONG)
                .setDuration(15000);
        snackbar.setActionTextColor(Color.RED);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }
}
