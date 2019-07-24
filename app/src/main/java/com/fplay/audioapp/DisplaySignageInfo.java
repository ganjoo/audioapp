package com.fplay.audioapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DisplaySignageInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_signage_info);

        Bundle b = getIntent().getExtras();
        String animal_description = b.getString("animal_description");
        TextView desc = findViewById(R.id.tv_animal_description);
        desc.setMovementMethod(new ScrollingMovementMethod());
        desc.setText(animal_description);
        Button dismiss = findViewById(R.id.button_dismiss);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });

    }
}
