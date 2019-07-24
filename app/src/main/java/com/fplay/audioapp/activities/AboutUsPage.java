package com.fplay.audioapp.activities;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.fplay.audioapp.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;


@EActivity(R.layout.activity_about_us_page)
public class AboutUsPage extends AppCompatActivity {

    @AfterViews
    public  void afterViews() {
        ImageButton btn_back = (ImageButton)findViewById(R.id.btn_back_about_us);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}
