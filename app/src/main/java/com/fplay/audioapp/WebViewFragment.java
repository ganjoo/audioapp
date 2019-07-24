package com.fplay.audioapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

public class WebViewFragment extends Fragment {

    public WebView mWebView;
    public String murl;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View v=inflater.inflate(R.layout.fragment_webview, container, false);
        mWebView = (WebView) v.findViewById(R.id.webview);
        Bundle bundle=getArguments();

        //here is your list array
        String url = bundle.getString("url");
        mWebView.loadUrl(url);

        // Enable Javascript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Force links and redirects to open in the WebView instead of in a browser
        mWebView.setWebViewClient(new WebViewClient());

        ImageButton btn_open_drawer = (ImageButton)v.findViewById(R.id.btn_open_drawer_web_view);
        btn_open_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity)getActivity()).openDrawer();
            }
        });
        return v;
    }
}