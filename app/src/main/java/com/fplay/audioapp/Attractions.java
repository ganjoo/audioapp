package com.fplay.audioapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class Attractions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attractions);
        ImageButton btn = (ImageButton) findViewById(R.id.btn_attractions_back);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



        Button btn1 = (Button) findViewById(R.id.btn_zoo_animals);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                intent.putExtra("url", "http://www.lucknowzoo.com/indian_and_exotic_animals"); //Optional parameters
                startActivity(intent);
            }
        });

        Button btn2 = (Button) findViewById(R.id.btn_zoo_birds);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                intent.putExtra("url", "http://www.lucknowzoo.com/indian_and_exotic_birds"); //Optional parameters
                startActivity(intent);
            }
        });

        Button btn3 = (Button) findViewById(R.id.btn_zoo_reptiles);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                intent.putExtra("url", "http://www.lucknowzoo.com/reptiles"); //Optional parameters
                startActivity(intent);
            }
        });
    }
}
