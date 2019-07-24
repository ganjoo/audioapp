package ufobeacon.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fplay.audioapp.R;

import ufobeacon.common.Utililty;
import ufobeaconsdk.callback.OnBeaconSuccessListener;
import ufobeaconsdk.callback.OnConnectSuccessListener;
import ufobeaconsdk.callback.OnFailureListener;
import ufobeaconsdk.callback.OnReadSuccessListener;
import ufobeaconsdk.callback.OnSuccessListener;
import ufobeaconsdk.main.EddystoneType;
import ufobeaconsdk.main.UFODevice;
import ufobeaconsdk.main.UFODeviceType;


public class EditBeaconActivity extends AppBaseActivity implements View.OnClickListener {

    TextView tvCancel, tvBack;
    RelativeLayout rel_saveChanges;
    Spinner spTxpower;
    CheckBox chkEddystoneUID, chkEddystoneTLM, chkEddystoneURL;
    LinearLayout lnrBeacon, lnrEddystone, lnrEddystoneType, lnrUID, lnrURL;
    Context context;
    EditText etUUID, etMajor, etMinor, etURIFlag, etNameSpaceId, etInstanceId, etURL;
    ArrayAdapter<String> txPowerAdapter;
    ArrayAdapter<String> powerLevelAdapter;
    String[] powerLevel;
    private UFODevice ufodevice;
    EditText edtAdvertiseInterval;
    private ProgressDialog progressdialog;
    private int frameType;
    private String password = "";
    private boolean isLoadFirstTime = false;
    private EddystoneType eddystoneType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editbeaon_screen);

        if (getIntent() != null && getIntent().hasExtra("password")) {

            password = getIntent().getStringExtra("password");
        }
        isLoadFirstTime = true;
        initScreen();
        listnearScreen();
        registerBaseActivityReceiver();
        ufodevice = SharedUFODevice.INSTANCE.getUfodevice();

        if (ufodevice != null && ufodevice.getDeviceType() == UFODeviceType.IBEACON) {
            txPowerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.rssiAt1meter));
            powerLevel = getResources().getStringArray(R.array.txPowerLevel);
        } else if (ufodevice != null && ufodevice.getDeviceType() == UFODeviceType.EDDYSTONE) {
            txPowerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.txPowerEddystone));
            powerLevel = getResources().getStringArray(R.array.txPowerLevelEddystone);
        }
        spTxpower.setAdapter(txPowerAdapter);
        powerLevelAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, powerLevel);
        readConfiguration();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isLoadFirstTime = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBaseActivityReceiver();
    }

    private void setTypeface() {
        Typeface font = Typeface.createFromAsset(context.getAssets(),
                "fonts/fontawesome-webfont.ttf");
        tvBack.setTypeface(font);
    }

    private void initScreen() {

        context = EditBeaconActivity.this;
        tvBack = (TextView) findViewById(R.id.tvBack_editeBeacon);
        setTypeface();
        tvCancel = (TextView) findViewById(R.id.tvcancel_editeBeacon);
        rel_saveChanges = (RelativeLayout) findViewById(R.id.rel_bottom_editeBeacon);

        etMajor = (EditText) findViewById(R.id.etMajor_editBeacon);
        etMinor = (EditText) findViewById(R.id.etMinor_editBeacon);
        etUUID = (EditText) findViewById(R.id.etUUID_editBeacon);

        chkEddystoneTLM = (CheckBox) findViewById(R.id.chkEddTLM_editBeacon);
        chkEddystoneUID = (CheckBox) findViewById(R.id.chkEddUID_editBeacon);
        chkEddystoneURL = (CheckBox) findViewById(R.id.chkEddURL_editBeacon);

        edtAdvertiseInterval = (EditText) findViewById(R.id.spAdventisinginterval_editBeacon);
        spTxpower = (Spinner) findViewById(R.id.spTxpower_editBeacon);

        lnrBeacon = (LinearLayout) findViewById(R.id.lnrBeacon_editBeacon);
        lnrEddystone = (LinearLayout) findViewById(R.id.lnrEddystone_editBeacon);
        lnrEddystoneType = (LinearLayout) findViewById(R.id.lnrEddystoneType_edtBeacon);
        lnrUID = (LinearLayout) findViewById(R.id.lnrEdyUID_editBeacon);
        lnrURL = (LinearLayout) findViewById(R.id.lnrEdyURL_editBeacon);

        etURIFlag = (EditText) findViewById(R.id.etURIFlag_editBeacon);
        etNameSpaceId = (EditText) findViewById(R.id.etNameSpaceId_editBeacon);
        etInstanceId = (EditText) findViewById(R.id.etInstanceId_editBeacon);
        etURL = (EditText) findViewById(R.id.etURL_editBeacon);

        chkEddystoneUID.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isLoadFirstTime)
                    checkboxConfiguration();
            }
        });
        chkEddystoneURL.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isLoadFirstTime)
                    checkboxConfiguration();
            }
        });
        chkEddystoneTLM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isLoadFirstTime)
                    checkboxConfiguration();
            }
        });

    }

    private void listnearScreen() {

        tvBack.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        rel_saveChanges.setOnClickListener(this);

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
        } else if (v == tvCancel) {

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

        } else if (v == rel_saveChanges) {

            writeConfiguration();
        }
    }

    private void writeConfiguration() {

        if (ufodevice != null && ufodevice.isDeviceConnected()) {

            if (ufodevice != null && ufodevice.getDeviceType() == UFODeviceType.IBEACON) {

                if (TextUtils.isEmpty(etUUID.getText().toString())) {
                    generateSimpleToast("invalid UUID");
                    return;
                }

                if (TextUtils.isEmpty(etMajor.getText().toString())) {
                    generateSimpleToast("invalid Major");
                    return;
                }

                if (TextUtils.isEmpty(etMinor.getText().toString())) {
                    generateSimpleToast("invalid Minor");
                    return;
                }

                ufodevice.setiBeaconProximityUUID(etUUID.getText().toString(), new OnBeaconSuccessListener() {
                    @Override
                    public void onSuccess(boolean isSuccess) {

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(final int code, final String message) {

                    }
                });


                ufodevice.setiBeaconMajor(Integer.parseInt(etMajor.getText().toString()), new OnBeaconSuccessListener() {
                    @Override
                    public void onSuccess(boolean isSuccess) {

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(final int code, final String message) {

                    }
                });


                ufodevice.setiBeaconMinor(Integer.parseInt(etMinor.getText().toString()), new OnBeaconSuccessListener() {
                    @Override
                    public void onSuccess(boolean isSuccess) {

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(final int code, final String message) {

                    }
                });

                ufodevice.setiBeaconTxPower((spTxpower.getSelectedItemPosition() + 1), new OnBeaconSuccessListener() {
                    @Override
                    public void onSuccess(boolean isSuccess) {

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(final int code, final String message) {

                    }
                });

                if (TextUtils.isEmpty(edtAdvertiseInterval.getText().toString())) {
                    generateSimpleToast("Please provide valid advertise interval");
                    return;
                }

                ufodevice.setiBeaconAdvertisementInterval(Integer.parseInt(edtAdvertiseInterval.getText().toString()), new OnBeaconSuccessListener() {
                    @Override
                    public void onSuccess(boolean isSuccess) {
                        showSuccessMessage(EditBeaconActivity.this);
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(final int code, final String message) {

                    }
                });

            } else if (ufodevice != null && ufodevice.getDeviceType() == UFODeviceType.EDDYSTONE) {

                ufodevice.setEddystoneTxPower((spTxpower.getSelectedItemPosition()), new OnBeaconSuccessListener() {
                    @Override
                    public void onSuccess(boolean isSuccess) {

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(int code, String message) {
                        generateToast(code, message);
                    }
                });

                if (TextUtils.isEmpty(edtAdvertiseInterval.getText().toString())) {
                    generateSimpleToast("Please provide valid advertise interval");
                    return;
                }

                ufodevice.setEddystoneAdvertisementInterval(Integer.parseInt(edtAdvertiseInterval.getText().toString()), new OnBeaconSuccessListener() {
                    @Override
                    public void onSuccess(boolean isSuccess) {

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(int code, String message) {
                        generateToast(code, message);
                    }
                });

                if (chkEddystoneUID.isChecked()) {

                    if (TextUtils.isEmpty(etNameSpaceId.getText().toString())) {
                        generateSimpleToast("Please provide valid namespace id");
                        return;
                    }

                    if (TextUtils.isEmpty(etInstanceId.getText().toString())) {
                        generateSimpleToast("Please provide valid instance id");
                        return;
                    }

                    ufodevice.setEddystoneFrames(EddystoneType.EDDYSTONE_UID, new OnBeaconSuccessListener() {
                        @Override
                        public void onSuccess(boolean isSuccess) {

                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(final int code, final String message) {

                        }
                    });


                    ufodevice.setEddystoneUID(etNameSpaceId.getText().toString(), etInstanceId.getText().toString(), new OnBeaconSuccessListener() {
                        @Override
                        public void onSuccess(boolean isSuccess) {

                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(final int code, final String message) {

                        }
                    });

                    ufodevice.setEddystoneFrames(eddystoneType, new OnBeaconSuccessListener() {
                        @Override
                        public void onSuccess(boolean isSuccess) {
                            showSuccessMessage(EditBeaconActivity.this);
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(final int code, final String message) {

                        }
                    });

                }
                if (chkEddystoneURL.isChecked()) {

                    if (TextUtils.isEmpty(etURL.getText().toString()) || !Patterns.WEB_URL.matcher(etURL.getText().toString().trim().toLowerCase()).matches()) {
                        generateSimpleToast("Please provide valid URL");
                        return;
                    }

                    ufodevice.setEddystoneFrames(EddystoneType.EDDYSTONE_URL, new OnBeaconSuccessListener() {
                        @Override
                        public void onSuccess(boolean isSuccess) {

                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(final int code, final String message) {

                        }
                    });

                    ufodevice.setEddystoneURI(etURL.getText().toString(), new OnBeaconSuccessListener() {
                        @Override
                        public void onSuccess(boolean isSuccess) {

                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(final int code, final String message) {

                        }
                    });

                    ufodevice.setEddystoneFrames(eddystoneType, new OnBeaconSuccessListener() {
                        @Override
                        public void onSuccess(boolean isSuccess) {
                            showSuccessMessage(EditBeaconActivity.this);
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(final int code, final String message) {

                        }
                    });

                }
                if (chkEddystoneTLM.isChecked()) {

                    ufodevice.setEddystoneFrames(eddystoneType, new OnBeaconSuccessListener() {
                        @Override
                        public void onSuccess(boolean isSuccess) {
                            showSuccessMessage(EditBeaconActivity.this);
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(final int code, final String message) {

                        }
                    });
                }
            }
        } else {
            progressdialog = ProgressDialog.show(EditBeaconActivity.this, "", "Connecting with UFO Device");
            progressdialog.setCancelable(false);

            ufodevice.connect(new OnConnectSuccessListener() {
                @Override
                public void onSuccess(final UFODevice ufoDevice) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ufodevice = ufoDevice;
                            if (progressdialog != null && progressdialog.isShowing())
                                progressdialog.dismiss();
                            writeAgain();
                        }
                    });


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
    }

    private void writeAgain() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ufodevice != null && ufodevice.getDeviceType() == UFODeviceType.IBEACON) {
                    writePassword(password);
                } else {
                    writeConfiguration();
                }
            }
        }, 300);
    }

    private void writePassword(String password) {
        ufodevice.setPassword(password, new OnBeaconSuccessListener() {
            @Override
            public void onSuccess(boolean isSuccess) {

                if (progressdialog != null && progressdialog.isShowing())
                    progressdialog.dismiss();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                writeConfiguration();
                            }
                        }, 200);

                    }
                });


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

    private void readConfiguration() {

        if (ufodevice != null && ufodevice.isDeviceConnected()) {
            if (ufodevice != null && ufodevice.getDeviceType() == UFODeviceType.IBEACON) {

                lnrBeacon.setVisibility(View.VISIBLE);
                lnrEddystone.setVisibility(View.GONE);

                ufodevice.getiBeaconProximityUUID(new OnReadSuccessListener() {
                    @Override
                    public void onSuccess(final String data) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                etUUID.setText(data);
                            }
                        });

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(final int code, final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                generateToast(code, message);

                            }
                        });
                    }
                });

                ufodevice.getiBeaconMajor(new OnReadSuccessListener() {
                    @Override
                    public void onSuccess(final String data) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                etMajor.setText(data);
                            }
                        });

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(int code, String message) {
                        generateToast(code, message);
                    }
                });

                ufodevice.getiBeaconMinor(new OnReadSuccessListener() {
                    @Override
                    public void onSuccess(final String data) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                etMinor.setText(data);
                            }
                        });

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(final int code, final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                generateToast(code, message);

                            }
                        });
                    }
                });

                ufodevice.getiBeaconAdvertisementInterval(new OnReadSuccessListener() {
                    @Override
                    public void onSuccess(final String data) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                edtAdvertiseInterval.setText(data);

                            }
                        });

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(final int code, final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                generateToast(code, message);

                            }
                        });
                    }
                });

                ufodevice.getiBeaconTxPower(new OnReadSuccessListener() {
                    @Override
                    public void onSuccess(final String data) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                int position = powerLevelAdapter.getPosition(data);
                                if (position >= 1)
                                    spTxpower.setSelection(position);

                            }
                        });

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(final int code, final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                generateToast(code, message);

                            }
                        });
                    }
                });

            } else if (ufodevice != null && ufodevice.getDeviceType() == UFODeviceType.EDDYSTONE) {
                lnrBeacon.setVisibility(View.GONE);
                lnrEddystone.setVisibility(View.VISIBLE);
                lnrEddystoneType.setVisibility(View.VISIBLE);

                if (ufodevice != null && ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID_URL_TLM) {
                    chkEddystoneUID.setChecked(true);
                    chkEddystoneURL.setChecked(true);
                    chkEddystoneTLM.setChecked(true);
                } else if (ufodevice != null && ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID_TLM) {
                    chkEddystoneUID.setChecked(true);
                    chkEddystoneTLM.setChecked(true);
                } else if (ufodevice != null && ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_URL_TLM) {
                    chkEddystoneURL.setChecked(true);
                    chkEddystoneTLM.setChecked(true);
                } else if (ufodevice != null && ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_UID) {
                    chkEddystoneUID.setChecked(true);
                } else if (ufodevice != null && ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_URL) {
                    chkEddystoneURL.setChecked(true);
                } else if (ufodevice != null && ufodevice.getEddystoneType() == EddystoneType.EDDYSTONE_TLM) {
                    chkEddystoneTLM.setChecked(true);
                }


                setFrameType();
                // fillValue();
                checkboxConfiguration();

                ufodevice.getEddystoneAdvertisementInterval(new OnReadSuccessListener() {
                    @Override
                    public void onSuccess(final String data) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (data != null)
                                    edtAdvertiseInterval.setText(data + "");

                            }
                        });

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(final int code, final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                generateToast(code, message);

                            }
                        });
                    }
                });

                ufodevice.getEddystoneTxPower(new OnReadSuccessListener() {
                    @Override
                    public void onSuccess(final String data) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                int position = powerLevelAdapter.getPosition(data);
                                if (position >= 1)
                                    spTxpower.setSelection(position);

                            }
                        });

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(final int code, final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                generateToast(code, message);

                            }
                        });

                    }
                });

            }
        }

    }

    private void setFrameType() {

        if (chkEddystoneUID.isChecked() && chkEddystoneURL.isChecked() && chkEddystoneTLM.isChecked()) {
            frameType = 1;
        } else if ((chkEddystoneUID.isChecked() && chkEddystoneTLM.isChecked())) {
            frameType = 2;
        } else if (chkEddystoneURL.isChecked() && chkEddystoneTLM.isChecked()) {
            frameType = 3;
        } else if (chkEddystoneUID.isChecked()) {
            frameType = 4;
        } else if (chkEddystoneURL.isChecked()) {
            frameType = 5;
        } else if (chkEddystoneTLM.isChecked()) {
            frameType = 6;
        }
    }

    private void checkboxConfiguration() {

        if (chkEddystoneUID.isChecked() && chkEddystoneURL.isChecked() && chkEddystoneTLM.isChecked()) {
            lnrURL.setVisibility(View.VISIBLE);
            lnrUID.setVisibility(View.VISIBLE);
            etNameSpaceId.setText(ufodevice.getEddystoneNameSpace());
            etInstanceId.setText(ufodevice.getEddystoneInstance());
            etURL.setText(ufodevice.getEddystoneURL());
            etURIFlag.setText(Utililty.UID_URL_TLM_Mode);
            eddystoneType = EddystoneType.EDDYSTONE_UID_URL_TLM;
        } else if ((chkEddystoneUID.isChecked() && chkEddystoneTLM.isChecked())) {
            lnrURL.setVisibility(View.GONE);
            etNameSpaceId.setText(ufodevice.getEddystoneNameSpace());
            etInstanceId.setText(ufodevice.getEddystoneInstance());
            lnrUID.setVisibility(View.VISIBLE);
            etURIFlag.setText(Utililty.UID_TLM_Mode);
            eddystoneType = EddystoneType.EDDYSTONE_UID_TLM;
        } else if (chkEddystoneURL.isChecked() && chkEddystoneTLM.isChecked()) {
            lnrURL.setVisibility(View.VISIBLE);
            lnrUID.setVisibility(View.GONE);
            etURL.setText(ufodevice.getEddystoneURL());
            etURIFlag.setText(Utililty.URL_TLM_Mode);
            eddystoneType = EddystoneType.EDDYSTONE_URL_TLM;
        } else if (chkEddystoneUID.isChecked() && chkEddystoneURL.isChecked()) {
            lnrUID.setVisibility(View.VISIBLE);
            lnrURL.setVisibility(View.VISIBLE);
            generateSimpleToast("Invalid frame type");
        } else if (chkEddystoneUID.isChecked()) {
            if (frameType == 1 || frameType == 2 || frameType == 3) {
                etURIFlag.setText(Utililty.UIDMode);
            } else {
                lnrUID.setVisibility(View.VISIBLE);
                etURIFlag.setText(Utililty.UIDMode);
                lnrURL.setVisibility(View.GONE);
                etNameSpaceId.setText(ufodevice.getEddystoneNameSpace());
                etInstanceId.setText(ufodevice.getEddystoneInstance());
            }
            eddystoneType = EddystoneType.EDDYSTONE_UID;
        } else if (chkEddystoneURL.isChecked()) {
            if (frameType == 1 || frameType == 2 || frameType == 3) {
                etURIFlag.setText(Utililty.URLMode);
            } else {
                lnrURL.setVisibility(View.VISIBLE);
                etURIFlag.setText(Utililty.URLMode);
                lnrUID.setVisibility(View.GONE);
                etURL.setText(ufodevice.getEddystoneURL());
            }
            eddystoneType = EddystoneType.EDDYSTONE_URL;
        } else if (chkEddystoneTLM.isChecked()) {
            if (frameType == 1 || frameType == 2 || frameType == 3) {
                etURIFlag.setText(Utililty.TLMMode);
                lnrURL.setVisibility(View.GONE);
                lnrUID.setVisibility(View.GONE);
            } else {
                lnrURL.setVisibility(View.GONE);
                lnrUID.setVisibility(View.GONE);
                etURIFlag.setText(Utililty.TLMMode);
            }
            eddystoneType = EddystoneType.EDDYSTONE_TLM;
        }else{
            lnrURL.setVisibility(View.GONE);
            lnrUID.setVisibility(View.GONE);
        }
    }

    // Print Toast message
    public void generateToast(int code, String message) {
        if (code != 1011)
            Toast.makeText(EditBeaconActivity.this, "code:- " + code + " - " + message, Toast.LENGTH_SHORT).show();
    }

    // Print Toast message
    public void generateSimpleToast(String message) {
        Toast.makeText(EditBeaconActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public void showSuccessMessage(final Context context) {
        AlertDialog adialog = new AlertDialog.Builder(context)
                .setMessage(
                        "Parameter updated successfully.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
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

                        closeAllActivities();
                    }
                }).show();
    }
}
