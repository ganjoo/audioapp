package com.fplay.audioapp;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DirectorsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directors);
        ImageButton btn_back = (ImageButton)findViewById(R.id.btn_back_zoo_directors);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView txt = (TextView)findViewById(R.id.tv_director_text);
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N)
        {
            txt.setText(Html.fromHtml("<tr><td colspan=\"2\"><font size=\"2\">\n" +
                    "      <strong> Nawab Wajid Ali Shah Zoological Garden</strong>,<br>\n" +
                    "      Hazratganj Road, Opp. to Civil Hospital, Lucknow,<br>\n" +
                    "\t  Uttar Pradesh 226001, India<br>\n" +
                    "<br><b>Telephone No. :</b> 0522-2239588 (O) , 2236538 (Fax) \n" +
                    "<br><b>E-mail address :</b> <a href=\"mailto:lucknowzoo@gmail.com\" class=\"text2\">lucknowzoo@gmail.com</a>\n" +
                    "<span class=\"style1\"><br>\n" +
                    "<strong>for any query contact Helpline No :  </strong>\n" +
                    "8005493617</span></font>       </td></tr>"));
        }
        else
        {
            txt.setText(Html.fromHtml("<tr><td colspan=\"2\"><font size=\"2\">\n" +
                    "      <strong> Nawab Wajid Ali Shah Zoological Garden</strong>,<br>\n" +
                    "      Hazratganj Road, Opp. to Civil Hospital, Lucknow,<br>\n" +
                    "\t  Uttar Pradesh 226001, India<br>\n" +
                    "<br><b>Telephone No. :</b> 0522-2239588 (O) , 2236538 (Fax) \n" +
                    "<br><b>E-mail address :</b> <a href=\"mailto:lucknowzoo@gmail.com\" class=\"text2\">lucknowzoo@gmail.com</a>\n" +
                    "<span class=\"style1\"><br>\n" +
                    "<strong>for any query contact Helpline No :  </strong>\n" +
                    "8005493617</span></font>       </td></tr>", Html.FROM_HTML_MODE_COMPACT));
        }

//        WebView wb = (WebView)findViewById(R.id.mapview);
//        wb.getSettings().setBuiltInZoomControls(true);
//        wb.loadUrl("https://goo.gl/maps/p6jKjp1xvmC2");
//        wb.clearCache(true);
//        wb.clearHistory();
//        wb.getSettings().setJavaScriptEnabled(true);
//        wb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        wb.setWebViewClient(new WebViewClient() {
//            public boolean shouldOverrideUrlLoading(WebView view, String url){
//                // do your handling codes here, which url is the requested url
//                // probably you need to open that url rather than redirect:
//                view.loadUrl(url);
//                return false; // then it is not handled by default action
//            }
//        });
        RelativeLayout layout1 = (RelativeLayout) findViewById(R.id.rlay_dir1); // id fetch from xml
        RelativeLayout layout2 = (RelativeLayout) findViewById(R.id.rlay_dir2); // id fetch from xml

        ShapeDrawable rectShapeDrawable = new ShapeDrawable(); // pre defined class

// get paint
        Paint paint = rectShapeDrawable.getPaint();

// set border color, stroke and stroke width
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5); // you can change the value of 5
       // layout1.setBackgroundDrawable(rectShapeDrawable);
        //layout2.setBackgroundDrawable(rectShapeDrawable);

        WebView WebView = (WebView)findViewById(R.id.news_webview);
        WebView.getSettings().setJavaScriptEnabled(true);
        //mWebView.getSettings().setPluginsEnabled(true);
        WebView.loadUrl("http://swan.lucknowzooweb.c66.me/news_and_pr");
    }
}
