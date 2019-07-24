package ufobeacon.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.fplay.audioapp.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ufobeacon.adapter.UfoBeaconAdapter;
import ufobeacon.common.Utililty;
import ufobeaconsdk.callback.OnFailureListener;
import ufobeaconsdk.callback.OnRangingListener;
import ufobeaconsdk.callback.OnScanSuccessListener;
import ufobeaconsdk.callback.OnSuccessListener;
import ufobeaconsdk.main.RangeType;
import ufobeaconsdk.main.UFOBeaconManager;
import ufobeaconsdk.main.UFODevice;
import ufobeaconsdk.main.UFODeviceType;


public class UFOBeaconActivity extends Activity implements View.OnClickListener {

    TextView tvMenu, tvStopScan;
    RelativeLayout rel_StartScan;

    ListView lvufoBeacon;
    UfoBeaconAdapter ufoAdapter;
    Context context;
    private RelativeLayout relMenu;
    private SwipeRefreshLayout swipeContainer;
    private ArrayList<UFODevice> ufoDevicesList = new ArrayList<>();

    public static final int REQUEST_ENABLE_BT = 1000;

    UFOBeaconManager ufoBeaconManager;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private boolean isFirstTimeLoad = false;

    private HandlerThread sortingTimerHandlerThread = null;
    private Handler sortingTimerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ufobeacon_screen);

        initScreen();
        listneScreen();
        isFirstTimeLoad = true;
        ufoAdapter = new UfoBeaconAdapter(context, lvufoBeacon);
        lvufoBeacon.setAdapter(ufoAdapter);
    }

    private void initScreen() {
        context = UFOBeaconActivity.this;
        swipeContainer = (SwipeRefreshLayout)
                findViewById(R.id.swipeContainer);
        lvufoBeacon = (ListView) findViewById(R.id.lvufoBeacon);
        relMenu = (RelativeLayout) findViewById(R.id.homescreen_rel_menu);
        tvMenu = (TextView) findViewById(R.id.tvMenu);
        setTypeface();
        tvStopScan = (TextView) findViewById(R.id.tvStopscan);
        tvStopScan.setVisibility(View.GONE);
        rel_StartScan = (RelativeLayout) findViewById(R.id.rel_bottom_ufobeacon);
        ufoBeaconManager = new UFOBeaconManager(UFOBeaconActivity.this);
        if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 23) {
            ufoBeaconManager.isLocationServiceEnabled(new OnSuccessListener() {
                @Override
                public void onSuccess(boolean isSuccess) {
                    verifyBluetoothState();
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(int code, String message) {
                    Utililty.ShowDialogToStartLocation(UFOBeaconActivity.this);
                }
            });
        }

        lvufoBeacon.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (lvufoBeacon == null || lvufoBeacon.getChildCount() == 0) ? 0 : lvufoBeacon.getChildAt(0).getTop();
                swipeContainer.setEnabled((topRowVerticalPosition >= 0));
            }
        });
    }

    private void startSortingTimer() {

        if (sortingTimerHandlerThread == null) {
            sortingTimerHandlerThread = new HandlerThread("sortingTimerHandlerThread");
            sortingTimerHandlerThread.start();
        }
        if (sortingTimerHandler == null)
            sortingTimerHandler = new Handler(sortingTimerHandlerThread.getLooper(), sortingTimeoutCallBack);
        sortingTimerHandler.sendEmptyMessageDelayed(0, 500);
    }

    private void stopSortingTimer() {
        if (sortingTimerHandler != null) {
            sortingTimerHandler.removeCallbacksAndMessages(null);
            sortingTimerHandler = null;
        }

        if (sortingTimerHandlerThread != null) {
            sortingTimerHandlerThread.quit();
            sortingTimerHandlerThread = null;
        }
    }

    private void verifyBluetoothState() {
        ufoBeaconManager.isBluetoothEnabled(new OnSuccessListener() {
            @Override
            public void onSuccess(boolean isSuccess) {
                // start scanning
                if (ufoAdapter != null)
                    ufoAdapter.clearDevices();
                stopScan();
                startScan();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(int code, String message) {
                // ask to turn on bluetooth
                Intent enableIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }
        });
        swipeContainer.setRefreshing(false);
    }

    private void listneScreen() {
        relMenu.setOnClickListener(this);
        tvStopScan.setOnClickListener(this);
        rel_StartScan.setOnClickListener(this);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                verifyBluetoothState();
            }
        });
    }

    private void setTypeface() {
        // TODO Auto-generated method stub
        Typeface font = Typeface.createFromAsset(context.getAssets(),
                "fonts/fontawesome-webfont.ttf");
        tvMenu.setTypeface(font);
    }

    @Override
    public void onClick(View v) {

        if (v == tvStopScan) {
            swipeContainer.setRefreshing(false);
            stopScan();
            scanButtonVisible();
        }
        if (v == relMenu) {
            openOptionsMenuWithItem();
        }
        if (v == rel_StartScan) {
            verifyBluetoothState();
        }
    }

    // Start scanning of BLE devices
    private void startScan() {

        if (ufoBeaconManager != null) {

            if (ufoAdapter != null)
                ufoAdapter.clearDevices();

            tvStopScan.setVisibility(View.VISIBLE);
            rel_StartScan.setVisibility(View.GONE);

            ufoBeaconManager.startScan(new OnScanSuccessListener() {
                @Override
                public void onSuccess(final UFODevice ufodevice) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeContainer.setRefreshing(false);
                            Log.e("UfoBeaconActivity", "Scanning updated " + ufodevice.getRssi() + " Date " + sdf.format(ufodevice.getDate()) + " Mac = " + ufodevice.getBtdevice().getAddress());
                            ufoAdapter.addDevice(ufodevice);
                            if (ufoDevicesList != null && !ufoDevicesList.contains(ufodevice)) {
                                ufoDevicesList.add(ufodevice);
                                ufodevice.startRangeMonitoring(new OnRangingListener() {
                                    @Override
                                    public void isDeviceInRange(final RangeType range) {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (range == RangeType.IN_RANGE) {
                                                    generateNotification(range, ufodevice);
                                                } else {
                                                    generateNotification(range, ufodevice);
                                                }
                                            }
                                        });

                                    }
                                });
                            }
                        }
                    });

                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(final int code, final String message) {

                    // Log.e("startScan", "Error code:- " + code + " Message:- " + message);
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

    private void generateNotification(RangeType inRange, UFODevice ufoDevice) {
        // Prepare a notification with vibration, sound and lights
        String msg = ufoDevice.getDeviceType() == UFODeviceType.EDDYSTONE ? "Eddystone" : "iBeacon";
        String range = inRange == RangeType.IN_RANGE ? "in Range" : "out Range";
        String message = "Your " + msg + "(" + ufoDevice.getBtdevice().getAddress() + ") is " + range;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("UFO Beacons")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentText(message)
                .setAutoCancel(true)
                .setLights(Color.RED, 1000, 1000)
                .setVibrate(new long[]{0, 400, 250, 400})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        // Get an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        // Build the notification and display it
        mNotifyMgr.notify(ufoDevice.getModelId(), mBuilder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ufoBeaconManager != null && ufoBeaconManager.isScanRunning() && !isFirstTimeLoad) {
            stopScan();
            scanButtonVisible();
            startScan();
        }
        if (isFirstTimeLoad)
            isFirstTimeLoad = false;
        startSortingTimer();
    }

    private void scanButtonVisible(){
        rel_StartScan.setVisibility(View.VISIBLE);
    }
    // Stop the scanning of BLE device
    private void stopScan() {

        if (ufoBeaconManager != null) {
            // swipeContainer.setRefreshing(false);
            tvStopScan.setVisibility(View.GONE);


            ufoBeaconManager.stopScan(new OnSuccessListener() {

                @Override
                public void onSuccess(boolean isStop) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // generateSuccessToast("Scanning stop");
                        }
                    });


                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(final int code, final String message) {
                    //Log.e("stopScan", "Error code:- " + code + " Message:- " + message);

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

    // Print Toast message
    public void generateToast(int code, String message) {
        Toast.makeText(UFOBeaconActivity.this, "code:- " + code + " - " + message, Toast.LENGTH_SHORT).show();
    }

    // Print Toast message
    public void generateSuccessToast(String message) {
        Toast.makeText(UFOBeaconActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ufoBeaconManager != null) {
            stopScan();
        }
        stopSortingTimer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    startScan();
                } else {
                    generateSuccessToast("Please enable bluetooth from settings.");
                }
                break;
            case Utililty.REQUEST_ENABLE_LOCATION:
                LocationManager locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    verifyBluetoothState();
                } else {
                    if (Utililty.adialog != null && Utililty.adialog.isShowing())
                        Utililty.adialog.dismiss();
                    Utililty.ShowDialogToStartLocation(this);
                }

            default:
                break;
        }
    }

    private void openOptionsMenuWithItem() {
        PopupMenu popup = new PopupMenu(UFOBeaconActivity.this, relMenu);
        // Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.menu_action,
                popup.getMenu());

        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class
                            .forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);

                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        // registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NewApi")
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.action_clearList) {
                    if (ufoAdapter != null)
                        ufoAdapter.clearDevices();
                    stopScan();
                    scanButtonVisible();
                    startScan();
                } else if (item.getItemId() == R.id.action_AboutUS) {
                    Intent intent = new Intent(context, AboutUsActivity.class);
                    startActivity(intent);
                }
                return true;
            }

        });
        popup.show();// showing popup menu
    }

    private Handler.Callback sortingTimeoutCallBack = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ufoAdapter != null) {
                        ufoAdapter.updateSortList();
                    }
                }
            });
            startSortingTimer();
            return false;
        }
    };

    public void restartScan() {
        stopScan();
        reIntiateScan();
    }

    private void reIntiateScan() {
        if (ufoBeaconManager != null) {
          ufoBeaconManager.startScan(new OnScanSuccessListener() {
                @Override
                public void onSuccess(final UFODevice ufodevice) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.e("UfoBeaconActivity", "Scanning updated " + ufodevice.getRssi() + " Date " + sdf.format(ufodevice.getDate()) + " Mac = " + ufodevice.getBtdevice().getAddress());
                            if (ufodevice.getBtdevice().getAddress().equalsIgnoreCase(SharedUFODevice.INSTANCE.getUfodevice().getBtdevice().getAddress())) {
                               SharedUFODevice.INSTANCE.setUfodevice(ufodevice);
                            }
                        }
                    });

                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(final int code, final String message) {
                    // Log.e("startScan", "Error code:- " + code + " Message:- " + message);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });


                }
            });
        }
    }
}
