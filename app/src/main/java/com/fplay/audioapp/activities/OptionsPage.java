package com.fplay.audioapp.activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.crashlytics.android.Crashlytics;
import com.fplay.audioapp.Activity_visitors_info;
import com.fplay.audioapp.BaseActivity;
import com.fplay.audioapp.R;

import facetracker.FaceTrackerActivity;


public class OptionsPage extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View root =  inflater.inflate(R.layout.activity_options_page, container, false);
        Glide.with(getActivity().getApplicationContext()).load(R.drawable.new_bg_main).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                root.setBackground(resource);
            }
        });
        ImageButton vr_button = (ImageButton)root.findViewById(R.id.vrzone_new);
       // Glide.with(getActivity().getApplicationContext()).load(R.drawable.img_btn_vr_states_new).into(vr_button);
        vr_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),  SplashHelp.class);
                intent.putExtra("next_screen", "VR");

                startActivity(intent);
            }
        });
        ImageButton ar_button = (ImageButton)root.findViewById(R.id.arzone_new);
        //Glide.with(getActivity().getApplicationContext()).load(R.drawable.img_btn_ar_states_new).into(ar_button);

        ar_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(getContext());
                }
                builder.setTitle("Select AR type")
                        .setMessage("Do you want to activate Animal Signages? Click No for Acquarium, Interpretation Center and Entrance.")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getContext(),  SplashHelp.class);
                                intent.putExtra("next_screen", "AR_OCR");
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getContext(),  SplashHelp.class);
                                intent.putExtra("next_screen", "AR_NATIVE");
                                startActivity(intent);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();


        }});
        ImageButton audio_button = (ImageButton)root.findViewById(R.id.audiotour_new);
        //Glide.with(getActivity().getApplicationContext()).load(R.drawable.img_btn_audio_states_new).into(audio_button);

        audio_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),  SplashHelp.class);
                intent.putExtra("next_screen", "AT");
                startActivity(intent);
            }
        });
        ImageButton photo_booth = (ImageButton)root.findViewById(R.id.photobooth_new);
       // Glide.with(getActivity().getApplicationContext()).load(R.drawable.img_btn_photo_booth_states_new).into(photo_booth);

        photo_booth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), FaceTrackerActivity.class));
            }
        });

//        ImageButton contact_us = (ImageButton)root.findViewById(R.id.contact_us);
//        Glide.with(getActivity().getApplicationContext()).load(R.drawable.img_button_contact_us).into(contact_us);
//
//        contact_us.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(getContext(), ContactUsActivity.class));
//            }
//        });

        ImageButton zoo_info = (ImageButton)root.findViewById(R.id.zooinfo_new);
        //Glide.with(getActivity().getApplicationContext()).load(R.drawable.zoo_information_button).into(zoo_info);

        zoo_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), Activity_visitors_info.class));
            }
        });



        ImageButton opendrawer = (ImageButton)root.findViewById(R.id.imageButton);
       // Glide.with(getActivity().getApplicationContext()).load(R.drawable.img_btn_vr_states).into(vr_button);

        opendrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity)getActivity()).openDrawer();
            }
        });

        Button cause_crash = (Button)root.findViewById(R.id.btn_cause_crash);
        cause_crash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Crashlytics.getInstance().crash();
            }
        });
        return root;


    }


    public  void afterViews() {


    }

}
