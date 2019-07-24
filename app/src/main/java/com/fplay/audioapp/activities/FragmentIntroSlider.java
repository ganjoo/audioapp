package com.fplay.audioapp.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.fplay.audioapp.R;


public class FragmentIntroSlider extends Fragment {

    private  ViewGroup rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int img_count = getArguments().getInt("image_count", 0);
        ImageView imView;
        switch (img_count){
            case 0: rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_intro_slider_template, container, false);
                     imView = (ImageView)rootView.findViewById(R.id.bg_img);
                    Glide.with(this).load(R.drawable.chimp).into(imView);
            break;
            case 1: rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_intro_slider_template, container, false);
                     imView = (ImageView)rootView.findViewById(R.id.bg_img);
                    Glide.with(this).load(R.drawable.tiger).into(imView);
                break;
            case 2: rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_intro_slider_template, container, false);
                imView = (ImageView)rootView.findViewById(R.id.bg_img);
                Glide.with(this).load(R.drawable.duck).into(imView);
                break;
            case 3: rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_intro_slider_template, container, false);
                imView = (ImageView)rootView.findViewById(R.id.bg_img);
                Glide.with(this).load(R.drawable.leopard).into(imView);
                break;
                default:
                    rootView = (ViewGroup) inflater.inflate(
                            R.layout.fragment_intro_slider_template, container, false);
                    imView = (ImageView)rootView.findViewById(R.id.bg_img);
                    Glide.with(this).load(R.drawable.chimp).into(imView);
        }

        return rootView;
    }
    public  void setImage(int pos) {


    }


}

