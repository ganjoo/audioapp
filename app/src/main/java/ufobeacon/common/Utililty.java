package ufobeacon.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Dell on 10-02-2017.
 */

public class Utililty {

    public static AlertDialog adialog;
    public static final int REQUEST_ENABLE_LOCATION = 1001;

    public static final String UIDMode = "00";
    public static final String URLMode = "10";
    public static final String TLMMode = "20";
    public static final String UID_TLM_Mode = "A0";
    public static final String URL_TLM_Mode = "B0";
    public static final String UID_URL_TLM_Mode = "C0";




    public static void ShowDialogToStartLocation(final Context context) {
        adialog = new AlertDialog.Builder(context)
                .setMessage(
                        "Location service is off, please enable it to find near by UFO beacons.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Intent settingIntent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        ((Activity) context).startActivityForResult(
                                settingIntent, REQUEST_ENABLE_LOCATION);
                    }
                }).show();
    }

    public static boolean validateURLByRegEx(String uri) {
        Pattern p = Pattern.compile("^(http://|https://)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}.?([a-z]+)?$");
        Matcher m;
        m = p.matcher(uri);
        boolean matches = m.matches();
        if (matches) {
            return true;
        } else {
            return false;
        }

    }
}
