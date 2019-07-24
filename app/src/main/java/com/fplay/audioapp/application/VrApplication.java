package com.fplay.audioapp.application;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.fplay.audioapp.R;
import com.squareup.leakcanary.LeakCanary;


import io.fabric.sdk.android.Fabric;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by User on 07-11-2016.
 */
public class VrApplication extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
       // LeakCanary.install(this);
        context = getApplicationContext() ;
        //calligraph api for including desired font .
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Lato-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );


    }



}
