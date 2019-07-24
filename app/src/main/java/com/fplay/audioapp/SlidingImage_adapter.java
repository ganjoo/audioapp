package com.fplay.audioapp;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.github.piasy.biv.view.BigImageView;

import java.util.ArrayList;


public class SlidingImage_adapter extends PagerAdapter {


    private ArrayList<String> IMAGES;
    private LayoutInflater inflater;
    private Context context;


    public SlidingImage_adapter(Context context,ArrayList<String> IMAGES) {
        BigImageViewer.initialize(GlideImageLoader.with(context));
        this.context = context;
        this.IMAGES=IMAGES;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return IMAGES.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View imageLayout = inflater.inflate(R.layout.sliding_images_layout, view, false);

        assert imageLayout != null;
        final BigImageView imageView = (BigImageView) imageLayout
                .findViewById(R.id.image_sliding);
        imageView.showImage(Uri.parse(IMAGES.get(position)));

        //imageView.setImageResource(IMAGES.get(position));


        view.addView(imageLayout, 0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }


}