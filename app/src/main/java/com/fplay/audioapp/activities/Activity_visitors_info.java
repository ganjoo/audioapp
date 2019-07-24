package com.fplay.audioapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Activity_visitors_info extends AppCompatActivity {

    ImageView androidImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visitor_information);

        androidImageButton = (ImageView) findViewById(R.id.zoo_map_google);

        androidImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goo.gl/maps/WU6CQ4rrr3m"));
                startActivity(browserIntent);
            }
        });
    }
}
