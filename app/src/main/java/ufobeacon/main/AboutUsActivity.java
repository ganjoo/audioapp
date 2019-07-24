package ufobeacon.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.fplay.audioapp.R;

/**
 * Created by Dell on 10-02-2017.
 */

public class AboutUsActivity extends Activity implements View.OnClickListener {

    private WebView webView;
    private TextView txtBack;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.aboutus);
        initScreen();
        setListener();
        setTypeface();
        startWebView("http://www.ufobeacon.com");
    }

    private void initScreen() {
        txtBack = (TextView) findViewById(R.id.aboutus_back);
        webView = (WebView) findViewById(R.id.webview);
    }

    private void setListener() {
        txtBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.aboutus_back:
                finish();
                break;
        }
    }

    private void setTypeface() {
        // TODO Auto-generated method stub
        Typeface font = Typeface.createFromAsset(getAssets(),
                "fonts/fontawesome-webfont.ttf");
        txtBack.setTypeface(font);
    }


    private void startWebView(String url) {

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        progressDialog = new ProgressDialog(AboutUsActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(AboutUsActivity.this, "Error:" + description, Toast.LENGTH_SHORT).show();

            }
        });
        webView.loadUrl(url);
    }
}
