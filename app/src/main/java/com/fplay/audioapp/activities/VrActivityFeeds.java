package com.fplay.audioapp.activities;

import com.fplay.audioapp.R;
import com.google.vr.sdk.widgets.video.VrVideoView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;


/**
 * Created by User on 06-11-2016.
 */
@EActivity(R.layout.activity_feeds)
public class VrActivityFeeds extends VrAppcompatActivityTrackable {

    @ViewById
    public  VrVideoView video_view;

    @AfterViews
    public  void afterViews() {


        VrVideoView.Options options = new VrVideoView.Options();
        options.inputType = VrVideoView.Options.TYPE_MONO;

        try {
            video_view.loadVideoFromAsset("", options);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
