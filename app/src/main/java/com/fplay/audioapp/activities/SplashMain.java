package com.fplay.audioapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.fplay.audioapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SplashMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_main);


        String my_date = new String("31/07/2019");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date strDate = null;
        try {
            strDate = sdf.parse(my_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (System.currentTimeMillis() > strDate.getTime()) {
            Toast.makeText(getApplicationContext(), "Oh no, your licence may have expired or exceeded the count of demos. Please contact White Butter for further support", Toast.LENGTH_LONG).show();
        }else{
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashMain.this, AudioMainPage.class);
                    startActivity(intent);
                }
            }, 2000);
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
