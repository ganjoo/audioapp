package ufobeacon.main;

import android.app.Application;

import com.fplay.audioapp.R;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;


/**
 * Created by Dell on 07-03-2017.
 */
@ReportsCrashes(formKey = "", // will not be used
mailTo = "hiteshganjoo@gmail.com", mode = ReportingInteractionMode.TOAST, resToastText = R.string.app_name)

public class UFOBeaconApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}
