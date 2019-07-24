package ufobeacon.main;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fplay.audioapp.R;


import java.text.SimpleDateFormat;

import ufobeaconsdk.callback.OnBeaconSuccessListener;
import ufobeaconsdk.callback.OnConnectSuccessListener;
import ufobeaconsdk.callback.OnFailureListener;
import ufobeaconsdk.callback.OnSuccessListener;
import ufobeaconsdk.main.EddystoneType;
import ufobeaconsdk.main.UFODevice;
import ufobeaconsdk.main.UFODeviceType;

/**
 * Created by KP Patel on 03-Feb-17.
 */

public class BeaconDetailActivity extends AppBaseActivity implements View.OnClickListener {

    private TextView tvMacid, tvUUID, tvTxpower, tvRSSI, tvMajor, tvMinor, tvDistance, tvBack, tvEdite;
    private Context context;
    private UFODevice ufodevice;
    private ProgressDialog progressdialog;
    private TextView tviBeacon;
    private LinearLayout lnrBeaconMain, lnrEddystoneMain;
    private LinearLayout lnrUID, lnrURL, lnrTLM;
    private TextView tvEddystoneUIDTitle, tvEddystoneURLTitle, tvEddystoneTLMTitle;
    private TextView tvNamespaceId, tvInstanceId;
    private TextView tvURL;
    private TextView tvBatteryVoltage, tvTemperature, tvPDUCount, tvBootTime;
    private HandlerThread connectionHandlerThread = null;
    private Handler connectionHandler;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private LinearLayout lnrBeaconTemp;
    private TextView txtiBeaconBatteryVoltage, txtiBeaconTemperature;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beacondetail_screen);

        ufodevice = SharedUFODevice.INSTANCE.getUfodevice();

        initScreen();
        listneScreen();

        if (ufodevice != null) {
            setValue();
        }
        registerBaseActivityReceiver();

    }

    @Override
    protected void onResume() {
        super.onResume();

        updateTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBaseActivityReceiver();
    }

    private void popupPasswordDialog() {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_password);

        TextView txtOk = (TextView) dialog.findViewById(R.id.dialog_password_txt_ok);
        TextView txtCancel = (TextView) dialog.findViewById(R.id.dialog_password_txt_cancel);
        final EditText edtValue = (EditText) dialog.findViewById(R.id.dialog_password_edt_value);

        txtOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                final String password = edtValue.getText().toString().trim();

                if (password != null && !password.equalsIgnoreCase("")) {

                    progressdialog = ProgressDialog.show(BeaconDetailActivity.this, "", "Please Wait");
                    progressdialog.setCancelable(false);

                    ufodevice.connect(new OnConnectSuccessListener() {
                        @Override
                        public void onSuccess(UFODevice ufoDevice) {
                            ufodevice = ufoDevice;
                            writePassword(password);
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(final int code, final String message) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    generateToast(code, message);

                                    if (progressdialog != null && progressdialog.isShowing())
                                        progressdialog.dismiss();

                                    if (code == 1009) {
                                        Intent intent = new Intent(context, EditBeaconActivity.class);
                                        intent.putExtra("password", password);
                                        startActivity(intent);
                                    }
                                }
                            });

                        }
                    });

                }

                dialog.dismiss();
            }
        });

        txtCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void writePassword(final String password) {
        ufodevice.setPassword(password, new OnBeaconSuccessListener() {
            @Override
            public void onSuccess(boolean isSuccess) {

                if (progressdialog != null && progressdialog.isShowing())
                    progressdialog.dismiss();

                Intent intent = new Intent(context, EditBeaconActivity.class);
                intent.putExtra("password", password);
                startActivity(intent);

            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(final int code, final String message) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        generateToast(code, message);
                        if (progressdialog != null && progressdialog.isShowing())
                            progressdialog.dismiss();
                    }
                });

            }
        });

    }

    // Print Toast message
    public void generateToast(int code, String message) {

        if (progressdialog != null && progressdialog.isShowing())
            progressdialog.dismiss();
        Log.e(TAG, "shoeToast");
        Toast.makeText(BeaconDetailActivity.this, "code:- " + code + " - " + message, Toast.LENGTH_SHORT).show();
    }

    private void setValue() {


        tvMacid.setText(ufodevice.getBtdevice().getAddress() + "");
        tvTxpower.setText(ufodevice.getRssiAt1meter() + " dBm");
        tvRSSI.setText(ufodevice.getRssi() + " dBm");
        tvDistance.setText(ufodevice.getDistance() + " mtr");

        if (ufodevice != null && ufodevice.getDeviceType() == UFODeviceType.IBEACON) {
            lnrBeaconMain.setVisibility(View.VISIBLE);
            lnrEddystoneMain.setVisibility(View.GONE);
            tviBeacon.setText("iBeacon");
            tvUUID.setText(ufodevice.getProximityUUID() + "");
            tvMajor.setText(ufodevice.getMajor() + "");
            tvMinor.setText(ufodevice.getMinor() + "");
            txtiBeaconBatteryVoltage.setText(ufodevice.getBatteryVoltage() + " V");
            txtiBeaconTemperature.setText(ufodevice.getTemperature() + getString(R.string.celsiusUnicode));

        } else if (ufodevice != null && ufodevice.getDeviceType() == UFODeviceType.EDDYSTONE) {
            lnrEddystoneMain.setVisibility(View.VISIBLE);
            lnrBeaconMain.setVisibility(View.GONE);
            if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID_URL_TLM) {
                lnrUID.setVisibility(View.VISIBLE);
                tvEddystoneUIDTitle.setVisibility(View.VISIBLE);
                tvNamespaceId.setText(ufodevice.getEddystoneNameSpace() + "");
                tvInstanceId.setText(ufodevice.getEddystoneInstance() + "");
                tviBeacon.setText(context.getString(R.string.eddystoneuid) + " + URL + TLM");
                lnrURL.setVisibility(View.VISIBLE);
                tvEddystoneURLTitle.setVisibility(View.VISIBLE);
                tvURL.setText(ufodevice.getEddystoneURL() + "");
                lnrTLM.setVisibility(View.VISIBLE);
                tvEddystoneTLMTitle.setVisibility(View.VISIBLE);
                tvBatteryVoltage.setText(ufodevice.getEddystoneTLMBatteryVoltage() + " V");
                tvTemperature.setText(ufodevice.getEddystoneTLMTemperature() + getString(R.string.celsiusUnicode));
                tvBootTime.setText(sdf.format(ufodevice.getEddystoneActiveSince()));
                tvPDUCount.setText(ufodevice.getEddystoneTLMPDUCounts() + "");
            }
//            else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID && ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_URL) {
//                tviBeacon.setText(context.getString(R.string.eddystoneuid) + " + URL");
//                lnrUID.setVisibility(View.VISIBLE);
//                tvEddystoneUIDTitle.setVisibility(View.VISIBLE);
//                tvNamespaceId.setText(ufodevice.getEddystoneNameSpace() + "");
//                tvInstanceId.setText(ufodevice.getEddystoneInstance() + "");
//                lnrURL.setVisibility(View.VISIBLE);
//                tvEddystoneURLTitle.setVisibility(View.VISIBLE);
//                tvURL.setText(ufodevice.getEddystoneURL() + "");
//                lnrTLM.setVisibility(View.GONE);
//            }
            else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID_TLM) {
                tviBeacon.setText(context.getString(R.string.eddystoneuid) + " + TLM");
                lnrUID.setVisibility(View.VISIBLE);
                tvEddystoneUIDTitle.setVisibility(View.VISIBLE);
                tvNamespaceId.setText(ufodevice.getEddystoneNameSpace() + "");
                tvInstanceId.setText(ufodevice.getEddystoneInstance() + "");
                lnrTLM.setVisibility(View.VISIBLE);
                tvEddystoneTLMTitle.setVisibility(View.VISIBLE);
                tvBatteryVoltage.setText(ufodevice.getEddystoneTLMBatteryVoltage() + " V");
                tvTemperature.setText(ufodevice.getEddystoneTLMTemperature() + getString(R.string.celsiusUnicode));
                tvBootTime.setText(sdf.format(ufodevice.getEddystoneActiveSince()));
                tvPDUCount.setText(ufodevice.getEddystoneTLMPDUCounts() + "");
                lnrURL.setVisibility(View.GONE);
            } else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_URL_TLM) {
                tviBeacon.setText(context.getString(R.string.eddystoneurl) + " + TLM");
                lnrURL.setVisibility(View.VISIBLE);
                tvEddystoneURLTitle.setVisibility(View.VISIBLE);
                tvURL.setText(ufodevice.getEddystoneURL() + "");
                lnrTLM.setVisibility(View.VISIBLE);
                tvEddystoneTLMTitle.setVisibility(View.VISIBLE);
                tvBatteryVoltage.setText(ufodevice.getEddystoneTLMBatteryVoltage() + "V");
                tvTemperature.setText(ufodevice.getEddystoneTLMTemperature() + getString(R.string.celsiusUnicode));
                tvBootTime.setText(sdf.format(ufodevice.getEddystoneActiveSince()));
                tvPDUCount.setText(ufodevice.getEddystoneTLMPDUCounts() + "");
                lnrUID.setVisibility(View.GONE);
            } else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID) {
                tviBeacon.setText(context.getString(R.string.eddystoneuid));
                tvEddystoneUIDTitle.setVisibility(View.GONE);
                lnrTLM.setVisibility(View.GONE);
                lnrURL.setVisibility(View.GONE);
                lnrUID.setVisibility(View.VISIBLE);
                tvNamespaceId.setText(ufodevice.getEddystoneNameSpace() + "");
                tvInstanceId.setText(ufodevice.getEddystoneInstance() + "");
            } else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_URL) {
                tviBeacon.setText(context.getString(R.string.eddystoneurl));
                tvEddystoneURLTitle.setVisibility(View.GONE);
                lnrTLM.setVisibility(View.GONE);
                lnrUID.setVisibility(View.GONE);
                lnrURL.setVisibility(View.VISIBLE);
                tvURL.setText(ufodevice.getEddystoneURL() + "");
            } else if (ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_TLM) {
                tviBeacon.setText(context.getString(R.string.eddystonetlm));
                tvEddystoneTLMTitle.setVisibility(View.GONE);
                lnrTLM.setVisibility(View.VISIBLE);
                lnrUID.setVisibility(View.GONE);
                lnrURL.setVisibility(View.GONE);
                tvBatteryVoltage.setText(ufodevice.getEddystoneTLMBatteryVoltage() + " V");
                tvTemperature.setText(ufodevice.getEddystoneTLMTemperature() + getString(R.string.celsiusUnicode));
                tvBootTime.setText(sdf.format(ufodevice.getEddystoneActiveSince()));
                tvPDUCount.setText(ufodevice.getEddystoneTLMPDUCounts() + "");
            }

        }

    }

    private void initScreen() {
        tvMacid = (TextView) findViewById(R.id.tvMacid_beaconDetail);
        tvBack = (TextView) findViewById(R.id.tvBack_beaconDetail);
        tvEdite = (TextView) findViewById(R.id.tvEdit_beaconDetail);
        tvUUID = (TextView) findViewById(R.id.tvUUID_beaconDetail);
        tvTxpower = (TextView) findViewById(R.id.tvTxpower_beaconDetail);
        tvRSSI = (TextView) findViewById(R.id.tvRssi_beaconDetail);
        tvMinor = (TextView) findViewById(R.id.tvMinor_beaconDetail);
        tvMajor = (TextView) findViewById(R.id.tvMajor_beaconDetail);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        lnrBeaconMain = (LinearLayout) findViewById(R.id.beacondetail_lnr_mainBeacon);
        lnrEddystoneMain = (LinearLayout) findViewById(R.id.beacondetail_lnr_mainEddyStone);
        lnrUID = (LinearLayout) findViewById(R.id.beacondetail_lnr_uid);
        lnrURL = (LinearLayout) findViewById(R.id.beacondetail_lnr_url);
        lnrTLM = (LinearLayout) findViewById(R.id.beacondetail_lnr_tlm);
        tviBeacon = (TextView) findViewById(R.id.tvEddystone_beacontype);
        tvEddystoneUIDTitle = (TextView) findViewById(R.id.tvEddystone_txtUIDTitle);
        tvEddystoneURLTitle = (TextView) findViewById(R.id.tvEddystone_txtURLTitle);
        tvEddystoneTLMTitle = (TextView) findViewById(R.id.tvEddystone_txtTLMTitle);
        tvNamespaceId = (TextView) findViewById(R.id.tvEddystone_namespaceId);
        tvInstanceId = (TextView) findViewById(R.id.tvEddystone_instanceId);
        tvURL = (TextView) findViewById(R.id.tvEddystone_url);
        tvBatteryVoltage = (TextView) findViewById(R.id.tv_batteryVoltage);
        tvTemperature = (TextView) findViewById(R.id.tv_Temperature);
        tvPDUCount = (TextView) findViewById(R.id.tv_PDUCount);
        tvBootTime = (TextView) findViewById(R.id.tv_Boottime);
        lnrBeaconTemp = (LinearLayout) findViewById(R.id.beacondetail_lnr_beaconTemp);
        txtiBeaconBatteryVoltage = (TextView) findViewById(R.id.tv_iBeaconbatteryVoltage);
        txtiBeaconTemperature = (TextView) findViewById(R.id.tv_iBeaconTemperature);

    }

    private void listneScreen() {
        context = BeaconDetailActivity.this;
        tvEdite.setOnClickListener(this);
        tvBack.setOnClickListener(this);
        setTypeface(tvEdite);
        setTypeface(tvBack);
    }

    private void setTypeface(TextView textview) {
        Typeface font = Typeface.createFromAsset(context.getAssets(),
                "fonts/fontawesome-webfont.ttf");
        textview.setTypeface(font);
    }

    @Override
    public void onClick(View v) {

        if (v == tvBack) {
            if (ufodevice != null && ufodevice.isDeviceConnected()) {
                ufodevice.disconnect(new OnSuccessListener() {
                    @Override
                    public void onSuccess(boolean isSuccess) {

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(int code, String message) {

                    }
                });
            }

            finish();
        }
        if (v == tvEdite) {

            if (ufodevice != null && ufodevice.getDeviceType() == UFODeviceType.EDDYSTONE) {

                progressdialog = ProgressDialog.show(BeaconDetailActivity.this, "", "Please Wait");
                progressdialog.setCancelable(false);

                ufodevice.connect(new OnConnectSuccessListener() {
                    @Override
                    public void onSuccess(UFODevice ufoDevice) {
                        ufodevice = ufoDevice;
                        if (progressdialog != null && progressdialog.isShowing())
                            progressdialog.dismiss();

                        Intent intent = new Intent(context, EditBeaconActivity.class);
                        startActivity(intent);
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(final int code, final String message) {

                        if (progressdialog != null && progressdialog.isShowing())
                            progressdialog.dismiss();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                             //   generateToast(code, message);

                                if (code == 1009) {
                                    Intent intent = new Intent(context, EditBeaconActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });


                    }
                });
            } else {
                popupPasswordDialog();
            }

        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (ufodevice != null && ufodevice.isDeviceConnected()) {
            ufodevice.disconnect(new OnSuccessListener() {
                @Override
                public void onSuccess(boolean isSuccess) {

                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(int code, String message) {

                }
            });
        }

    }

    private void updateTimer() {

        if (connectionHandlerThread == null) {
            connectionHandlerThread = new HandlerThread("connectionHandlerThread");
            connectionHandlerThread.start();
        }

        startUpdateTimer();

    }

    private void startUpdateTimer() {
        if (connectionHandler == null)
            connectionHandler = new Handler(connectionHandlerThread.getLooper(), connectionTimeoutCallBack);
        connectionHandler.sendEmptyMessageDelayed(0, 1000);
    }

    private void stopUpdateTimer() {
        if (connectionHandler != null)
            connectionHandler.removeCallbacksAndMessages(null);
    }

    Handler.Callback connectionTimeoutCallBack = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Log.e("Counter","Counter " + i++);
                    ufodevice = SharedUFODevice.INSTANCE.getUfodevice();
                    setValue();
                }
            });

            startUpdateTimer();

            return false;
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        stopUpdateTimer();
    }
}
