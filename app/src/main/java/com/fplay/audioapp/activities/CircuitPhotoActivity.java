package com.fplay.audioapp.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.fplay.audioapp.CircuitPhoto;
import com.fplay.audioapp.R;
import com.fplay.audioapp.SlidingImage_adapter;
import com.github.piasy.biv.view.BigImageView;

import java.util.ArrayList;

import static com.fplay.audioapp.activities.ARScanPage.CIRCUIT_PHOTOS;

public class CircuitPhotoActivity extends AppCompatActivity{

    public static final String EXTRA_CIRCUIT_PHOTO = "CircuitPhotoActivity.CIRCUIT_PHOTO";
    private BigImageView mImageView;
    private static ViewPager mPager;
    private static int currentPage = 0;
    private static int NUM_PAGES = 0;
    private static final String[] IMAGES= {};
    private ArrayList<String> ImagesArray = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circuit_photo);
        init();

//        mImageView = (BigImageView) findViewById(R.id.image_cir);
        //CircuitPhoto spacePhoto = getIntent().getParcelableExtra(EXTRA_CIRCUIT_PHOTO);

        //BigImageViewer.initialize(GlideImageLoader.with(this));
        //BigImageView bigImageView = (BigImageView) findViewById(R.id.image_cir);
        //bigImageView.showImage(Uri.parse(spacePhoto.getUrl()));
//        Glide.with(this)
//                .load(spacePhoto.getUrl())
//                .asBitmap()
//                .error(R.drawable.ic_launcher)
//                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                .into(mImageView);
    }

    private void init() {
        ArrayList<CircuitPhoto> circuit_list = CIRCUIT_PHOTOS;
        for(int i=0;i<circuit_list.size();i++)
            ImagesArray.add(circuit_list.get(i).getUrl());

        mPager = (ViewPager) findViewById(R.id.pager_circuit);

        mPager.setAdapter(new SlidingImage_adapter(CircuitPhotoActivity.this,ImagesArray));


        final float density = getResources().getDisplayMetrics().density;

//Set circle indicator radius


        NUM_PAGES =circuit_list.size();

        // Auto start of viewpager
//        final Handler handler = new Handler();
//        final Runnable Update = new Runnable() {
//            public void run() {
//                if (currentPage == NUM_PAGES) {
//                    currentPage = 0;
//                }
//                mPager.setCurrentItem(currentPage++, true);
//            }
//        };
//        Timer swipeTimer = new Timer();
//        swipeTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                handler.post(Update);
//            }
//        }, 3000, 3000);



    }


}