package com.fplay.audioapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;

public class WebViewActivity extends AppCompatActivity {
    private WebView mWebView;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);


        mWebView = (WebView)findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        //mWebView.getSettings().setPluginsEnabled(true);


        String html = "<object width=\"768\" height=\"1024\"> <param name=\"movie\" value=\"file:///android_asset/Metronome.swf\"> <embed src=\"file:///android_asset/Metronome.swf\" width=\"768\" height=\"1024\"> </embed> </object>";
        String mimeType = "text/html";
        String encoding = "utf-8";
        //mWebView.loadDataWithBaseURL("null", html, mimeType, encoding, "");
        Bundle bundle = getIntent().getExtras();
        mWebView.loadUrl(bundle.get("url").toString());

        ImageButton btn = (ImageButton) findViewById(R.id.btn_webview_back);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}
