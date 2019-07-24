package com.fplay.audioapp.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.fplay.audioapp.OcrCaptureActivity;
import com.fplay.audioapp.R;

public class SplashHelp extends AppCompatActivity {

    Intent m_intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_help);

        String next_screen = getIntent().getStringExtra("next_screen");

        ImageView imview_help = (ImageView) findViewById(R.id.help_image);



        if(next_screen.equals("VR")){
            imview_help.setImageResource(R.drawable.vr_help);
            m_intent = new Intent(getApplicationContext(), VRListPage_.class);
        }

        if(next_screen.equals("AR_OCR")){
            imview_help.setImageResource(R.drawable.ar_help);
            m_intent = new Intent(getApplicationContext(), OcrCaptureActivity.class);

        }
        if(next_screen.equals("AR_NATIVE")){
            imview_help.setImageResource(R.drawable.ar_help);
            m_intent = new Intent(getApplicationContext(), ARScanPage.class);

        }


        if(next_screen.equals("AT")){
            imview_help.setImageResource(R.drawable.smartaudio_help);
            m_intent = new Intent(getApplicationContext(), AudioMainPage.class);
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                finish();
                startActivity(m_intent);
            }
        }, 5000);


    }
}
