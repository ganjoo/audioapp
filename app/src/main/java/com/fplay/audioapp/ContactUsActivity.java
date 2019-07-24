package com.fplay.audioapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class ContactUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        Button btn = (Button)findViewById(R.id.submit_feedback);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validInput()) {
                    Toast.makeText(getApplicationContext(), "Thank you for submitting your feedback", Toast.LENGTH_LONG).show();
                    finish();
                }
                else{
                    //Toast.makeText(getApplicationContext(),"Please check the details before submitting...",Toast.LENGTH_LONG).show();
                }


            }
        });

        ImageButton btn_back = (ImageButton)findViewById(R.id.btn_back_contact_us);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private boolean validInput(){
         EditText emailEditText = (EditText)findViewById(R.id.email);
         EditText nameText= (EditText)findViewById(R.id.name);
        EditText phone= (EditText)findViewById(R.id.phone);
        EditText message= (EditText)findViewById(R.id.message1);

        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(emailEditText.getText().toString());
        if(matcher.matches() == false)
        {
            Toast.makeText(getApplicationContext(),"Please check your email and try again.",Toast.LENGTH_LONG).show();
            return false;
        }
        if(nameText.getText().toString().length() < 5) {
            Toast.makeText(getApplicationContext(),"Please enter a valid name.",Toast.LENGTH_LONG).show();
            return false;
        }
        if(false == isValidPhoneNumber(phone.getText().toString())) {
            Toast.makeText(getApplicationContext(),"Please enter a valid phone number.",Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    public static boolean isValidPhoneNumber(String phone)
    {
        if (!phone.trim().equals("") || phone.length() > 10)
        {
            return Patterns.PHONE.matcher(phone).matches();
        }

        return false;
    }


}
