package ufobeacon.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.fplay.audioapp.R;

/**
 * Created by KP Patel on 03-Feb-17.
 */

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, UFOBeaconActivity.class);
                startActivity(intent);
            }
        }, 2000);

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
