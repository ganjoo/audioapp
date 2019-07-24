package com.fplay.audioapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fplay.audioapp.BuildConfig;
import com.fplay.audioapp.R;
import com.fplay.audioapp.utils.AudioPointDetails;
import com.fplay.audioapp.utils.MarkerView;
import com.fplay.audioapp.utils.Utils;
import com.qozix.tileview.TileView;
import com.qozix.tileview.markers.MarkerLayout;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import demo.TileViewActivity;
import pl.droidsonroids.gif.GifImageView;
import ufobeacon.adapter.UfoBeaconAdapter;
import ufobeacon.common.Utililty;
import ufobeacon.main.AboutUsActivity;
import ufobeacon.main.SharedUFODevice;
import ufobeaconsdk.callback.OnFailureListener;
import ufobeaconsdk.callback.OnRangingListener;
import ufobeaconsdk.callback.OnScanSuccessListener;
import ufobeaconsdk.callback.OnSuccessListener;
import ufobeaconsdk.main.RangeType;
import ufobeaconsdk.main.UFOBeaconManager;
import ufobeaconsdk.main.UFODevice;
import ufobeaconsdk.main.UFODeviceType;

import static java.lang.Math.abs;


public class AudioMainPage extends TileViewActivity implements View.OnClickListener {

    TextView tvMenu, tvStopScan;
    RelativeLayout rel_StartScan;
    TileView tileView;
    ListView lvufoBeacon;
    UfoBeaconAdapter ufoAdapter;
    Context context;
    private RelativeLayout relMenu;
    private SwipeRefreshLayout swipeContainer;
    private ArrayList<UFODevice> ufoDevicesList = new ArrayList<>();
    private ArrayList<AnimalDetail> animalDetails = new ArrayList<>();
    private ArrayList<AudioPointDetails> audioPointDetails = new ArrayList<>();

    public static final int REQUEST_ENABLE_BT = 1000;

    UFOBeaconManager ufoBeaconManager;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private boolean isFirstTimeLoad = false;

    private HandlerThread sortingTimerHandlerThread = null;
    private Handler sortingTimerHandler;

    long current_milli_sec = 0;
    GifImageView gif;
    View found;
    private List<String> mListAudio = new ArrayList<String>(); // List of object names in the AWS bucket
    MediaPlayer player = new MediaPlayer();
    private List<String> mFileList = new ArrayList<String>();

    AmazonS3 s3Client;
    String bucket = "lucknowzooaudiolowbit";
    File uploadToS3 = new File("/storage/sdcard0/Pictures/Screenshots/Screenshot.png");
    File downloadFromS3 = new File("/storage/sdcard0/Pictures/Screenshot.png");
    TransferUtility transferUtility;

    boolean doubleBackToExitPressedOnce = false;
    private Snackbar snackbar;

    private AnimalDetail otherAnimal = new AnimalDetail();

    private AudioPointDetails otherPoint = new AudioPointDetails();

    private int current_Simulator_beacon = 1;


    private int count = 0;
    private long startMillis = 0;
    Dialog dialog;

    private void showImageForPoint(Drawable image) {
        // custom dialog
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);

        // set the custom dialog components - text, image and button
        ImageButton close = (ImageButton) dialog.findViewById(R.id.btnClose);
        ImageView imagev = (ImageView)dialog.findViewById(R.id.point_image);
        imagev.setImageDrawable(image);
        Button buy = (Button) dialog.findViewById(R.id.btnBuy);

        // Close Button
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //TODO Close button action
            }
        });

        // Buy Button
        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //TODO Buy button action
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.audio_animal_popup.activity_audio_main_page);
        setTileView();
        setAnimalDetails();
        setAudioPointDetails();
        requestpermissions();
        initScreen();
        listneScreen();
        isFirstTimeLoad = true;
        ufoAdapter = new UfoBeaconAdapter(context, lvufoBeacon);
//        lvufoBeacon.setAdapter(ufoAdapter);
        gif = new GifImageView(this);
        gif.setBackgroundResource(R.drawable.found);
        found = getTileView().addMarker(gif, 12, 10, null, null);
        found.setVisibility(View.GONE);


        mFileList.clear();
        listAssetFilesNew("");

        // The following block commented because asset folder is now used
//        mFileList = getFilesList();
//
//        if(mFileList.size() < 124){
//            createAudioDialog(mFileList.size());
//        }


        // callback method to call credentialsProvider method.
        s3credentialsProvider();

        // callback method to call the setTransferUtility method
        setTransferUtility();

        fetchFileFromS3(new View(this));

        if (BuildConfig.DEBUG) {
            //setupDebugTools();
        }

    }


    private void createAudioDialog(int progress) {

        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

// 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Audio Tour setup is in progress. Please try after some time.. " + (128 - progress) + " clips left for processing...")
                .setTitle("Audio Tour Setup");

        builder.setPositiveButton("Notify me", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });


// 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private List<String> getFilesList()//Returns the files written earlier
    {
        mFileList.clear();
        String list_files[] = this.getFilesDir().list();
        for (String ele : list_files) {
            mFileList.add(this.getFilesDir() + File.separator + ele);
        }
        return mFileList;
    }

    private void initScreen() {
        context = this;
//        swipeContainer = (SwipeRefreshLayout)
//                findViewById(R.id.swipeContainer);
//        lvufoBeacon = (ListView) findViewById(R.id.lvufoBeacon);
//        relMenu = (RelativeLayout) findViewById(R.id.homescreen_rel_menu);
//        tvMenu = (TextView) findViewById(R.id.tvMenu);
//        setTypeface();
//        tvStopScan = (TextView) findViewById(R.id.tvStopscan);
//        tvStopScan.setVisibility(View.GONE);
//        rel_StartScan = (RelativeLayout) findViewById(R.id.rel_bottom_ufobeacon);
        ufoBeaconManager = new UFOBeaconManager(this);
        if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 23) {
            ufoBeaconManager.isLocationServiceEnabled(new OnSuccessListener() {
                @Override
                public void onSuccess(boolean isSuccess) {
                    verifyBluetoothState();
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(int code, String message) {
                    Utililty.ShowDialogToStartLocation(AudioMainPage.this);
                }
            });
        }

//        lvufoBeacon.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                int topRowVerticalPosition = (lvufoBeacon == null || lvufoBeacon.getChildCount() == 0) ? 0 : lvufoBeacon.getChildAt(0).getTop();
//                swipeContainer.setEnabled((topRowVerticalPosition >= 0));
//            }
//        });
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
//        swipeContainer.setRefreshing(false);
    }

    private void requestpermissions() {


        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "The permission to get BLE location data is required", Toast.LENGTH_SHORT).show();
            } else {
                //requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void listneScreen() {
        //  relMenu.setOnClickListener(this);
        // tvStopScan.setOnClickListener(this);
        //  rel_StartScan.setOnClickListener(this);
//        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                verifyBluetoothState();
//            }
//        });
    }

    private void setTypeface() {
        // TODO Auto-generated method stub
        Typeface font = Typeface.createFromAsset(context.getAssets(),
                "fonts/fontawesome-webfont.ttf");
//        tvMenu.setTypeface(font);
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

//            tvStopScan.setVisibility(View.VISIBLE);
            //  rel_StartScan.setVisibility(View.GONE);

            ufoBeaconManager.startScan(new OnScanSuccessListener() {

                @Override
                public void onSuccess(final UFODevice ufodevice) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            swipeContainer.setRefreshing(false);
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
                                                    //generateAnimalNotification(range, ufodevice);
                                                    if (current_milli_sec == 0) {
                                                        //generateAnimalNotification(range, ufodevice);
                                                        generateAudioNotification(range, ufodevice);
                                                        current_milli_sec = System.currentTimeMillis();
                                                    } else if (abs(current_milli_sec - System.currentTimeMillis()) > 12) {
                                                        //generateAnimalNotification(range, ufodevice);
                                                        generateAudioNotification(range, ufodevice);
                                                        current_milli_sec = System.currentTimeMillis();
                                                    } else {
                                                        Log.i("LucknowZoo", "Beacon Found in Range but not notifying user again so soon");
                                                    }

                                                } else {
                                                    //generateNotification(range, ufodevice);
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
    boolean AskUserToSwitchPoint(final AudioPointDetails point) {
        snackbar = Snackbar
                .make(getWindow().getDecorView().getRootView(), point.name + " is also near you. Do you want to switch?", Snackbar.LENGTH_LONG)
                .setDuration(15000)
                .setAction("YES", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        otherPoint = point;
                        playAudioAndImageForPoint(otherPoint, true);
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
        return true;
    }
    boolean AskUserToSwitch(final AnimalDetail animal) {
        snackbar = Snackbar
                .make(getWindow().getDecorView().getRootView(), animal.name + " is also near you. Do you want to switch?", Snackbar.LENGTH_LONG)
                .setDuration(15000)
                .setAction("YES", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        otherAnimal = animal;
                        playAudioForAnimal(otherAnimal, true);
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
        return true;
    }
    private void playAudioAndImageForPoint(final AudioPointDetails otherPoint, boolean live) {
        if ((otherPoint == null) || (otherPoint.sticker_number == null)) {
            return;
        }
        Runnable audiorunnable = new Runnable() {

            public void run() {


                player.stop();

                playCageNumber(otherPoint.sticker_number);
            }
        };
        showImageForPoint(otherPoint.image_drawable);
        Thread thread = new Thread(audiorunnable);
        thread.start();

        if (live) {
            tileView.setScale(1.0f);

            tileView.slideToAndCenter(otherPoint.map_cordinates.x, otherPoint.map_cordinates.y);


            tileView.moveMarker(found, otherPoint.map_cordinates.x, otherPoint.map_cordinates.y);

        }
    }


    private void playAudioForAnimal(final AnimalDetail otherAnimal, boolean live) {
        if ((otherAnimal == null) || (otherAnimal.sticker_number == null)) {
            return;
        }
        Runnable audiorunnable = new Runnable() {

            public void run() {


                player.stop();

                playCageNumber(otherAnimal.sticker_number);
            }
        };

        Thread thread = new Thread(audiorunnable);
        thread.start();

        if (live) {
            tileView.setScale(1.0f);

            tileView.slideToAndCenter(otherAnimal.map_cordinates.x, otherAnimal.map_cordinates.y);


            tileView.moveMarker(found, otherAnimal.map_cordinates.x, otherAnimal.map_cordinates.y);

        }
    }

    private void generateAnimalNotification_old(RangeType inRange, UFODevice ufoDevice) {
        if (false) {
            Intent resultIntent = new Intent(this, AudioMainPage.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            AnimalDetail animal = getAnimalDetailsfromId(ufoDevice);
            String range = inRange == RangeType.IN_RANGE ? "in Range" : "out Range";
            String message = "You are near a " + animal.name;
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Lucknow Zoo Audio Tour")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent)
                    .setLights(Color.RED, 1000, 1000)
                    .setVibrate(new long[]{0, 400, 250, 400})
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            mNotifyMgr.notify(ufoDevice.getModelId(), mBuilder.build());
        } else {
            String test[] = {"55:46:4F:FB:78:70",
                    "55:46:4F:FB:7A:98",
                    "55:46:4F:FB:7A:49",
                    "55:46:4F:FB:7A:6A",
                    "55:46:4F:FB:7A:59",
                    "55:46:4F:FB:7A:9B",
                    "55:46:4F:FB:78:66",
                    "55:46:4F:FB:7A:AD",
                    "55:46:4F:FB:7A:5F",
                    "55:46:4F:FB:7A:4A",
                    "55:46:4F:FB:7A:6E",
                    "55:46:4F:FB:7A:96",
                    "55:46:4F:FB:78:B2",
                    "55:46:4F:FB:78:60"};
            int r = (int) (Math.random() * 15);
            final AnimalDetail animal = null;
            if (animal.map_cordinates == null) {
                // Toast.makeText(this,"No Animal associated with " + ufoDevice.getBtdevice().getAddress(),Toast.LENGTH_LONG).show();
                return;
            }

            if (player.isPlaying()) {
                if (AskUserToSwitch(animal)) {

                }
                return;
            }
            Runnable audiorunnable = new Runnable() {

                public void run() {


                    player.stop();

                    playCageNumber(animal.sticker_number);
                }
            };

            Thread thread = new Thread(audiorunnable);
            thread.start();

            boolean inrange = inRange == RangeType.IN_RANGE ? true : false;
            if (inrange) {
                tileView.setScale(1.0f);

                tileView.slideToAndCenter(animal.map_cordinates.x, animal.map_cordinates.y);


                LayoutInflater li = LayoutInflater.from(getApplicationContext());
////                final View cv = li.inflate(R.layout.audio_animal_popup, null);
////                TextView txt = (TextView)cv.findViewById(R.id.animal_name);
////                txt.setText(animal.name);
////                addContentView(cv,new LinearLayout.LayoutParams(128, 128));
////                ImageButton dismiss = (ImageButton)cv.findViewById(R.id.audio_popup_dismiss);
//                dismiss.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        cv.setVisibility(View.GONE);
//                    }
//                });

                Toast.makeText(this, "You are close to " + animal.name, Toast.LENGTH_LONG).show();
                found.setVisibility(View.VISIBLE);
                tileView.moveMarker(found, animal.map_cordinates.x, animal.map_cordinates.y);
            }


        }
    }

    private void generateAudioNotification(RangeType inRange, UFODevice ufoDevice) {

        final AudioPointDetails point = getAudioPointDetailsfromId(ufoDevice);
        if ((point == null) || (point.map_cordinates == null)) {
            //Toast.makeText(this,"No Animal associated with " + ufoDevice.getBtdevice().getAddress(),Toast.LENGTH_LONG).show();
            return;
        }

        if (player.isPlaying()) {
            if (AskUserToSwitchPoint(point)) {

            }
            return;
        }
        Runnable audiorunnable = new Runnable() {

            public void run() {


                player.stop();
                //playAudioAndImageForPoint(point,true);
               playCageNumber(point.sticker_number);
            }
        };
        showImageForPoint(point.image_drawable);
        Thread thread = new Thread(audiorunnable);
        thread.start();

        boolean inrange = inRange == RangeType.IN_RANGE ? true : false;
        if (inrange) {
            tileView.setScale(1.0f);

            tileView.slideToAndCenter(point.map_cordinates.x, point.map_cordinates.y);


            LayoutInflater li = LayoutInflater.from(getApplicationContext());
////                final View cv = li.inflate(R.layout.audio_animal_popup, null);
////                TextView txt = (TextView)cv.findViewById(R.id.animal_name);
////                txt.setText(animal.name);
////                addContentView(cv,new LinearLayout.LayoutParams(128, 128));
////                ImageButton dismiss = (ImageButton)cv.findViewById(R.id.audio_popup_dismiss);
//                dismiss.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        cv.setVisibility(View.GONE);
//                    }
//                });

            Toast.makeText(this, "You are close to " + point.name, Toast.LENGTH_LONG).show();
            found.setVisibility(View.VISIBLE);
            tileView.moveMarker(found, point.map_cordinates.x, point.map_cordinates.y);


        }
    }

    private void generateAnimalNotification(RangeType inRange, UFODevice ufoDevice) {
        if (false) {
            Intent resultIntent = new Intent(this, AudioMainPage.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            AnimalDetail animal = getAnimalDetailsfromId(ufoDevice);
            String range = inRange == RangeType.IN_RANGE ? "in Range" : "out Range";
            String message = "You are near a " + animal.name;
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Lucknow Zoo Audio Tour")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent)
                    .setLights(Color.RED, 1000, 1000)
                    .setVibrate(new long[]{0, 400, 250, 400})
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            mNotifyMgr.notify(ufoDevice.getModelId(), mBuilder.build());
        } else {
            final AnimalDetail animal = getAnimalDetailsfromId(ufoDevice);
            if ((animal == null) || (animal.map_cordinates == null)) {
                //Toast.makeText(this,"No Animal associated with " + ufoDevice.getBtdevice().getAddress(),Toast.LENGTH_LONG).show();
                return;
            }

            if (player.isPlaying()) {
                if (AskUserToSwitch(animal)) {

                }
                return;
            }
            Runnable audiorunnable = new Runnable() {

                public void run() {


                    player.stop();

                    playCageNumber(animal.sticker_number);
                }
            };

            Thread thread = new Thread(audiorunnable);
            thread.start();

            boolean inrange = inRange == RangeType.IN_RANGE ? true : false;
            if (inrange) {
                tileView.setScale(1.0f);

                tileView.slideToAndCenter(animal.map_cordinates.x, animal.map_cordinates.y);


                LayoutInflater li = LayoutInflater.from(getApplicationContext());
////                final View cv = li.inflate(R.layout.audio_animal_popup, null);
////                TextView txt = (TextView)cv.findViewById(R.id.animal_name);
////                txt.setText(animal.name);
////                addContentView(cv,new LinearLayout.LayoutParams(128, 128));
////                ImageButton dismiss = (ImageButton)cv.findViewById(R.id.audio_popup_dismiss);
//                dismiss.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        cv.setVisibility(View.GONE);
//                    }
//                });

                Toast.makeText(this, "You are close to " + animal.name, Toast.LENGTH_LONG).show();
                found.setVisibility(View.VISIBLE);
                tileView.moveMarker(found, animal.map_cordinates.x, animal.map_cordinates.y);
            }


        }
    }

    private AudioPointDetails getAudioPointDetailsfromId(UFODevice device) {
        String address = device.getBtdevice().getAddress();
        if (device.getEddystoneNameSpace() != null) {
            int namespace = Integer.parseInt(device.getEddystoneInstance());

            if (BuildConfig.DEBUG) {
                Toast.makeText(this, "Detected Beacon with Instance Id: " + namespace + " and MAC ID: " + address, Toast.LENGTH_LONG).show();
            }

            if (namespace == 5) {
                return new AudioPointDetails("Bodh Gaya 1", "", new Point(1757, 645), Integer.toString(786), context.getResources().getDrawable(R.drawable.bd1));
            }

            if (namespace == 30) {
                return new AudioPointDetails("Bodh Gaya 2", "", new Point(1757, 645), Integer.toString(787), context.getResources().getDrawable(R.drawable.bd2));
            }

            if (namespace == 81) {
                return new AudioPointDetails("Bodh Gaya 3", "", new Point(1757, 645), Integer.toString(788), context.getResources().getDrawable(R.drawable.bd3));
            }
        }
        return  null;
    }

    private AnimalDetail getAnimalDetailsfromId(UFODevice device) {


        String address = device.getBtdevice().getAddress();
        if (device.getEddystoneNameSpace() != null) {
            int namespace = Integer.parseInt(device.getEddystoneInstance());

            if (BuildConfig.DEBUG) {
                Toast.makeText(this, "Detected Beacon with Instance Id: " + namespace + " and MAC ID: " + address, Toast.LENGTH_LONG).show();
            }

            if (namespace == 5) {

            }
            if (namespace == 5) {

            }
            if (namespace == 5) {

            }


            if (namespace == 84) {
                return getAnimalObjectFromName("kakatel");
            }
            if (namespace == 70) {
                return getAnimalObjectFromName("magarmach");
            }
            if (namespace == 31) {
                return getAnimalObjectFromName("cheetal");
            }
            if (namespace == 19) {
                return getAnimalObjectFromName("safedbhag");
            }
            if ((namespace == 23) || (namespace == 24)) {
                return getAnimalObjectFromName("blackbuckshvet");
            }

        } else {
            if (BuildConfig.DEBUG) {
                Toast.makeText(this, "Detected Beacon with NULL INSTANCE ID " + " and MAC ID: " + address, Toast.LENGTH_LONG).show();
            }
        }
        switch (address) {
            case "55:46:4F:E6:95:52":
                return getAnimalObjectFromName("General1");
            case "55:46:4F:FB:7A:98":
                return getAnimalObjectFromName("General2");
            case "55:46:4F:FB:7A:49":
                return getAnimalObjectFromName("balrail");
            case "55:46:4F:FB:7A:6A":
                return getAnimalObjectFromName("balrail");
            case "55:46:4F:FB:7A:59":
                return getAnimalObjectFromName("Narchimpanzee");
            case "55:46:4F:FB:7A:9F":
                return getAnimalObjectFromName("Narchimpanzee");
            case "55:46:4F:FB:7A:9A":
                return getAnimalObjectFromName("himalayan_baloo");
            case "55:46:4F:FB:78:5A":
                return getAnimalObjectFromName("tigerhouse");
            case "55:46:4F:FB:7A:5B":
                return getAnimalObjectFromName("tigerhouse");
            case "55:46:4F:FB:7A:AB":
                return getAnimalObjectFromName("tigerhouse");
            case "55:46:4F:FB:7A:9C":
                return getAnimalObjectFromName("duckpond");
            case "55:46:4F:FB:78:71":
                return getAnimalObjectFromName("duckpond");
            case "55:46:4F:FB:7A:8F":
                return getAnimalObjectFromName("duckpond");
            case "55:46:4F:FB:7A:A7":
                return getAnimalObjectFromName("duckpond");
            case "55:46:4F:FB:7A:62":
                return getAnimalObjectFromName("narbabbarsher_prithvi");
            case "55:46:4F:FB:7A:61":
                return getAnimalObjectFromName("narbabbarsher_prithvi");

            case "55:46:4F:FB:7A:AF":
                return getAnimalObjectFromName("leopardhouse");
            case "55:46:4F:FB:7A:46":
                return getAnimalObjectFromName("safedbhag");
            case "55:46:4F:FB:7A:A6":
                return getAnimalObjectFromName("safedbhag");
            case "55:46:4F:FB:7A:AE":
                return getAnimalObjectFromName("safedbhag");
            case "55:46:4F:FB:7A:54":
                return getAnimalObjectFromName("blackbuck");
            case "55:46:4F:FB:7A:66":
                return getAnimalObjectFromName("blackbuck");
            case "55:46:4F:FB:7A:92":
                return getAnimalObjectFromName("blackbuckshvet");
            case "55:46:4F:FB:7A:5E":
                return getAnimalObjectFromName("blackbuckshvet");
            case "55:46:4F:FB:7A:8B":
                return getAnimalObjectFromName("padha");
            case "55:46:4F:FB:7A:56":
                return getAnimalObjectFromName("padha");
            case "55:46:4F:FB:7A:5C":
                return getAnimalObjectFromName("barasingha");
            case "55:46:4F:FB:7A:52":
                return getAnimalObjectFromName("barasingha");
            case "55:46:4F:FB:78:AF":
                return getAnimalObjectFromName("barkingdear");
            case "55:46:4F:FB:7A:95":
                return getAnimalObjectFromName("barkingdear");
            case "55:46:4F:FB:78:AL":
                return getAnimalObjectFromName("cheetal");
            case "55:46:4F:E6:96:05":
                return getAnimalObjectFromName("cheetal");
            case "55:46:4F:FB:7A:A2":
                return getAnimalObjectFromName("sambar_hiran");
            case "55:46:4F:FB:7A:50":
                return getAnimalObjectFromName("sambar_hiran");
            case "55:46:4F:FB:7A:9B":
                return getAnimalObjectFromName("chinkara");
            case "55:46:4F:FB:78:66":
                return getAnimalObjectFromName("chinkara");
            case "55:46:4F:FB:7A:AD":
                return getAnimalObjectFromName("sanpghar");
            case "55:46:4F:FB:7A:5F":
                return getAnimalObjectFromName("sanpghar");
            case "55:46:4F:FB:7A:4A":
                return getAnimalObjectFromName("liontailedmonkey");
            case "55:46:4F:FB:7A:6E":
                return getAnimalObjectFromName("bonet_bandar");
            case "55:46:4F:FB:7A:96":
                return getAnimalObjectFromName("langur_bandar");
            case "55:46:4F:FB:78:B2":
                return getAnimalObjectFromName("desi_bandar");
            case "55:46:4F:FB:78:60":
                return getAnimalObjectFromName("saras_crane");
            case "55:46:4F:FB:7A:55":
                return getAnimalObjectFromName("shaturmurg");
            case "55:46:4F:FB:7A:6D":
                return getAnimalObjectFromName("shaturmurg");
            case "55:46:4F:FB:7A:53":
                return getAnimalObjectFromName("desibalu");
            case "55:46:4F:FB:7A:A9":
                return getAnimalObjectFromName("desibalu");
            case "55:46:4F:FB:7A:9D":
                return getAnimalObjectFromName("hukku_bandar");
            case "55:46:4F:FB:7A:4B":
                return getAnimalObjectFromName("rosy_pelican");
            case "55:46:4F:FB:7A:4D":
                return getAnimalObjectFromName("kachuwua");
            case "55:46:4F:FB:78:14":
                return getAnimalObjectFromName("fishing_cat");
            case "55:46:4F:FB:7A:57":
                return getAnimalObjectFromName("jungle_cat");
            case "55:46:4F:FB:7A:6C":
                return getAnimalObjectFromName("dhanesh_pakshi");
            case "55:46:4F:FB:7A:A1":
                return getAnimalObjectFromName("old_bird_section");
            case "55:46:4F:FB:7A:8C":
                return getAnimalObjectFromName("old_bird_section");
            case "55:46:4F:FB:7A:A5":
                return getAnimalObjectFromName("old_bird_section");
            case "55:46:4F:FB:7A:A4":
                return getAnimalObjectFromName("emu");
            case "55:46:4F:FB:7A:8D":
                return getAnimalObjectFromName("emu");
            case "55:46:4F:FB:78:B3":
                return getAnimalObjectFromName("giraffe_mada");
            case "55:46:4F:FB:78:72":
                return getAnimalObjectFromName("giraffe_mada");
            case "55:46:4F:FB:7A:5D":
                return getAnimalObjectFromName("sehi");
            case "55:46:4F:FB:78:63":
                return getAnimalObjectFromName("barn_ullu");
            case "55:46:4F:FB:7A:91":
                return getAnimalObjectFromName("cheel_ullu");


            //[TODO.. Record Audio for Kasht Ullu] case "55:46:4F:FB:7A:AA": return getAnimalObjectFromName("kashth_ullu");
            case "55:46:4F:FB:7A:AA":
                return getAnimalObjectFromName("");


            case "55:46:4F:FB:7A:4E":
                return getAnimalObjectFromName("owlet");
            case "55:46:4F:FB:7A:6F":
                return getAnimalObjectFromName("balrampurhouse_babar_sher_bada");
            case "55:46:4F:FB:78:65":
                return getAnimalObjectFromName("balrampurhouse_babar_sher_bada");
            case "55:46:4F:FB:7A:51":
                return getAnimalObjectFromName("vasundhara_mada_babar_sherni");
            case "55:46:4F:FB:78:6F":
                return getAnimalObjectFromName("vasundhara_mada_babar_sherni");
            //FIXMEcase "55:46:4F:FB:7A:AD": return getAnimalObjectFromName("magarmach");
            case "55:46:4F:E6:93:AB":
                return getAnimalObjectFromName("ghariyal");
            case "55:46:4F:FB:7A:67":
                return getAnimalObjectFromName("karkel_cat");
            case "55:46:4F:FB:78:0C":
                return getAnimalObjectFromName("lomdi");
            case "55:46:4F:FB:78:13":
                return getAnimalObjectFromName("siyar");
            case "55:46:4F:FB:7A:93":
                return getAnimalObjectFromName("bhediya");
            case "55:46:4F:FB:7A:90":
                return getAnimalObjectFromName("lakadbhagga");
            case "55:46:4F:FB:7A:8E":
                return getAnimalObjectFromName("hippo");
            case "55:46:4F:FB:7A:99":
                return getAnimalObjectFromName("hippo");
            case "55:46:4F:FB:7A:69":
                return getAnimalObjectFromName("new_bird_section");
            case "55:46:4F:FB:78:B1":
                return getAnimalObjectFromName("new_bird_section");
            case "55:46:4F:FB:78:B4":
                return getAnimalObjectFromName("new_bird_section");
            case "55:46:4F:FB:7A:BO":
                return getAnimalObjectFromName("udan_gilhari");
            case "55:46:4F:FB:7A:A3":
                return getAnimalObjectFromName("safed_mor");
            //FIXMEcase "55:46:4F:FB:7A:A6": return getAnimalObjectFromName("kakatel");
            case "55:46:4F:FB:78:73":
                return getAnimalObjectFromName("fijantas");
            case "55:46:4F:FB:7A:97":
                return getAnimalObjectFromName("neele_peele_makau");
            case "55:46:4F:FB:78:AE":
                return getAnimalObjectFromName("bari_gilhari");
            case "55:46:4F:FB:7A:94":
                return getAnimalObjectFromName("loha_saras");
            case "55:46:4F:FB:78:6B":
                return getAnimalObjectFromName("butterfly_park");
            case "55:46:4F:FB:7A:B5":
                return getAnimalObjectFromName("butterfly_park");
            case "55:46:4F:E6:95:62":
                return getAnimalObjectFromName("nocturnal_house");
            case "55:46:4F:E6:95:4C":
                return getAnimalObjectFromName("acuarium_brief_note");
            case "55:46:4F:E6:96:04":
                return getAnimalObjectFromName("acuarium_brief_note");
        }


        return new AnimalDetail();
    }

    private AnimalDetail getAnimalObjectFromName(String point_name) {

        if (0 == point_name.compareToIgnoreCase("lomdi")) {
            int i = 0;
            i++;
        }
        for (AnimalDetail animal : animalDetails) {
            if (animal.name.compareToIgnoreCase(point_name) == 0) {
                return animal;
            }
        }
        return null;
    }

    private void setAudioPointDetails() {
        if (audioPointDetails.size() > 0) {
            audioPointDetails.clear();

        }

        int count = 786;

//        audioPointDetails.add(new AudioPointDetails("Bodh Gaya 1", "", new Point(1757, 645), Integer.toString(count++), ""));
//        audioPointDetails.add(new AudioPointDetails("Bodh Gaya 2", "", new Point(1000, 645), Integer.toString(count++), ""));
//        audioPointDetails.add(new AudioPointDetails("Bodh Gaya 3", "", new Point(970, 645), Integer.toString(count++), ""));

    }

    private void setAnimalDetails() {
        if (animalDetails.size() > 0) {
            animalDetails.clear();

        }
        int count = 100;

        animalDetails.add(new AnimalDetail("General1", "", new Point(1757, 645), Integer.toString(count))); //Gate number 2
        animalDetails.add(new AnimalDetail("General2", "", new Point(3414, 1371), Integer.toString(count++))); // Gate number 1

        animalDetails.add(new AnimalDetail("Balrail", "", new Point(1730, 828), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("Narchimpanzee", "", new Point(2309, 891), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("himalayan_baloo", "", new Point(3525, 904), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("tigerhouse", "", new Point(3261, 770), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("duckpond", "", new Point(3141, 1059), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("narbabbarsher_prithvi", "", new Point(2330, 2041), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("leopardhouse", "", new Point(1740, 1848), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("safedbhag", "", new Point(1925, 1543), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("blackbuck", "", new Point(1948, 705), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("blackbuckshvet", "", new Point(1976, 705), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("padha", "", new Point(2031, 694), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("barasingha", "", new Point(2148, 683), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("barkingdear", "", new Point(2381, 713), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("cheetal", "", new Point(2566, 751), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("sambar_hiran", "", new Point(3630, 1193), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("chinkara", "", new Point(2905, 1477), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("sanpghar", "", new Point(2167, 889), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("liontailedmonkey", "", new Point(2544, 932), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("bonet_bandar", "", new Point(2775, 904), Integer.toString(count++))); //CHECK CORDINATES
        animalDetails.add(new AnimalDetail("langur_bandar", "", new Point(2831, 900), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("desi_bandar", "", new Point(2860, 966), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("saras_crane", "", new Point(3020, 925), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("shaturmurg", "", new Point(2905, 1558), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("desibalu", "", new Point(3073, 692), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("hukku_bandar", "", new Point(3291, 1050), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("rosy_pelican", "", new Point(3457, 1180), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("kachuwua", "", new Point(3323, 1199), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("fishing_cat", "", new Point(3174, 1343), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("jungle_cat", "", new Point(1577, 1409), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("dhanesh_pakshi", "", new Point(1554, 883), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("old_bird_section", "", new Point(3070, 1343), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("emu", "", new Point(3433, 1330), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("giraffe_mada", "", new Point(3151, 1613), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("sehi", "", new Point(2771, 1670), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("barn_ullu", "", new Point(2693, 1666), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("cheel_ullu", "", new Point(2674, 1746), Integer.toString(count++)));
        count++;
        //animalDetails.add(new AnimalDetail("kashth_ullu","",new Point(2716,1806),Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("owlet", "", new Point(2805, 1767), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("balrampurhouse_babar_sher_bada", "", new Point(1921, 1967), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("vasundhara_mada_babar_sherni", "", new Point(2173, 1977), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("magarmach", "", new Point(1810, 1418), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("ghariyal", "", new Point(2042, 1352), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("karkel_cat", "", new Point(1575, 1269), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("lomdi", "", new Point(1403, 1280), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("siyar", "", new Point(2071, 1483), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("bhediya", "", new Point(1515, 1513), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("lakadbhagga", "", new Point(2103, 1560), Integer.toString(count++)));
        //animalDetails.add(new AnimalDetail("General","",new Point(1643,736),Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("hippo", "", new Point(1763, 1131), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("new_bird_section", "", new Point(1556, 824), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("udan_gilhari", "", new Point(1554, 934), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("safed_mor", "", new Point(2973, 1051), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("kakatel", "", new Point(1541, 857), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("fijantas", "", new Point(1564, 1112), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("neele_peele_makau", "", new Point(1638, 817), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("bari_gilhari", "", new Point(1556, 947), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("loha_saras", "", new Point(1566, 1038), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("butterfly_park", "", new Point(1554, 1216), Integer.toString(count++)));
        animalDetails.add(new AnimalDetail("nocturnal_house", "", new Point(2740, 1712), Integer.toString(count++)));

        animalDetails.add(new AnimalDetail("acuarium_brief_note", "", new Point(3017, 1646), Integer.toString(66)));


        DrawAnimalMarkers();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ufoBeaconManager != null && ufoBeaconManager.isScanRunning() && !isFirstTimeLoad) {
            stopScan();
            // scanButtonVisible();
            startScan();
        }
        if (isFirstTimeLoad)
            isFirstTimeLoad = false;
        startSortingTimer();
    }

    private void scanButtonVisible() {
        rel_StartScan.setVisibility(View.VISIBLE);
    }

    // Stop the scanning of BLE device
    private void stopScan() {

        if (ufoBeaconManager != null) {
            // swipeContainer.setRefreshing(false);
            //   tvStopScan.setVisibility(View.GONE);


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
                    Log.e("stopScan", "Error code:- " + code + " Message:- " + message);

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
        Toast.makeText(this, "code:- " + code + " - " + message, Toast.LENGTH_SHORT).show();
    }

    // Print Toast message
    public void generateSuccessToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ufoBeaconManager != null) {
            stopScan();
        }
        killMediaPlayer();
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
        PopupMenu popup = new PopupMenu(this, relMenu);
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

    private void setTileView() {
        // multiple references
        tileView = getTileView();

        // let the image explode
        tileView.setScaleLimits(0, 4);

        // size of original image at 100% mScale
        tileView.setSize(4608, 2592);

        //tileView.setSize( 1000, 916);

        // we're running from assets, should be fairly fast decodes, go ahead and render asap
        tileView.setShouldRenderWhilePanning(true);

        // detail levels
        // tileView.addDetailLevel( 2.000f, "zoo_tiles_2/tiles_old/zoo_map/2000/%d_%d.jpg");

        tileView.addDetailLevel(1.000f, "zoo_tiles_2/tiles/zoo_map/1000/%d_%d.jpg");
        tileView.addDetailLevel(0.500f, "zoo_tiles_2/tiles/zoo_map/500/%d_%d.jpg");
        tileView.addDetailLevel(0.250f, "zoo_tiles_2/tiles/zoo_map/250/%d_%d.jpg");
        tileView.addDetailLevel(0.125f, "zoo_tiles_2/tiles/zoo_map/125/%d_%d.jpg");


        // add a marker listener
        tileView.setMarkerTapListener(mMarkerTapListener);
        tileView.defineBounds(0, 0, 4608, 2592);
        // tileView.defineBounds( 0, 0, 4608, 2592 );
        // add some pins...
        //      addPin( 0.5, 0.5);
//        addPin( 123, 100 );
        //addPin( 2250,1206);
//        addPin( 0.75, 0.75 );
//        addPin( 0.50, 0.50 );

        // set mScale to 0, but keep scaleToFit true, so it'll be as small as possible but still match the container
        tileView.setScale(1);

        // let's use 0-1 positioning...


        // frame to center
        frameTo(0.5, 0.5);

        // render while panning
        tileView.setShouldRenderWhilePanning(true);

        // center markers along both axes
        tileView.setMarkerAnchorPoints(-0.5f, -0.5f);


        //tileView.slideToAndCenter( 1400,1200 );

        // disallow going back to minimum scale while double-taping at maximum scale (for demo purpose)
        //tileView.setShouldLoopScale( false );
        tileView.slideToAndCenter(1200, 1200);


    }

    private void addPin(String animal, double x, double y) {
        MarkerView imageView = new MarkerView(this);
        imageView.setImageResource(R.drawable.push_pin);
        imageView.animal = animal;
        getTileView().addMarker(imageView, x, y, null, null);
    }

    private void addPinSmall(String animal, double x, double y) {
        MarkerView imageView = new MarkerView(this);
        imageView.setImageResource(R.drawable.push_pin_small);
        imageView.animal = animal;
        getTileView().addMarker(imageView, x, y, null, null);
    }

    private void addMapMarker(String animal, double x, double y) {
        MarkerView imageView = new MarkerView(this);
        imageView.setImageResource(R.drawable.map_marker_normal_small);
        imageView.animal = animal;
        getTileView().addMarker(imageView, x, y, null, null);
    }

    private void addMapMarkerFeatured(String animal, double x, double y) {
        MarkerView imageView = new MarkerView(this);
        imageView.setImageResource(R.drawable.map_marker_featured);
        imageView.animal = animal;
        getTileView().addMarker(imageView, x, y, null, null);
    }

    private MarkerLayout.MarkerTapListener mMarkerTapListener = new MarkerLayout.MarkerTapListener() {
        @Override
        public void onMarkerTap(View v, int x, int y) {
            if (((MarkerView) v).animal == "Debug Tool Setup Complete") {
                setupDebugTools();
                return;
            }
            if (v instanceof GifImageView) {
                return;
            }
            //AnimalDetail animal = findAnimalbyMarker(new Point(x,y));
            Toast.makeText(getApplicationContext(), ((MarkerView) v).animal, Toast.LENGTH_SHORT).show();
//            tileView.setScale(1.0f);
//            tileView.slideToAndCenter( x,y);
            playAudioForAnimal(getAnimalObjectFromName(((MarkerView) v).animal), false);


        }
    };

    //Find animal by marker cordinates
    private AnimalDetail findAnimalbyMarker(Point marker) {
        double distance = 1000f;
        AnimalDetail detail = new AnimalDetail("Unknown animal", "", new Point(0, 0), "-1");
        for (AnimalDetail animal : animalDetails) {
            double curr_dist = Math.sqrt(Math.pow((marker.x - animal.map_cordinates.x), 2) + Math.pow((marker.y - animal.map_cordinates.y), 2));
            if (curr_dist < distance) {
                detail = animal;
                distance = curr_dist;
            }
        }
        return detail;
    }

    //Add pins for each animal
    private void DrawAnimalMarkers() {
        for (AnimalDetail animal : animalDetails) {
            addMapMarkerFeatured(animal.name, animal.map_cordinates.x, animal.map_cordinates.y);
        }
        addMapMarkerFeatured("Debug Tool Setup Complete", 4500, 2500);

    }


    private boolean listAssetFiles(String path) {
        String[] list;
        try {
            list = this.getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    if (!listAssetFiles(path + "/" + file))
                        return false;
                    else {
                        // This is a file
                        // TODO: add file name to an array list
                        mListAudio.add(file);
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private boolean listAssetFilesNew(String path) {
        String[] list;
        try {
            list = this.getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    if (!listAssetFilesNew(path + "/" + file))
                        return false;
                    else {
                        // This is a file
                        // TODO: add file name to an array list
                        mFileList.add(file);
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private void playCageNumber(String tracker_number) {
        String to_play = "";

        //First search for local assets:
        for (String file : mFileList) {
            if (file.split("/")[file.split("/").length - 1].startsWith(tracker_number + "_")) {
                to_play = file;
                break;
            }
        }
        if (to_play.length() == 0) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Downloading file from server. Please make sure you have good internet.", Toast.LENGTH_LONG);
                }
            });

            for (String file : mListAudio) {
                if (file.startsWith(tracker_number + "_")) {
                    to_play = "https://s3.amazonaws.com/" + bucket + "/" + file;
                    break;
                }
            }

        }

        if (to_play.compareToIgnoreCase("") == 0) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Please enter a valid cage number from 1 to 66 or 100-150", Toast.LENGTH_LONG);
                }
            });

            return;
        }


        try {
            //playAudio(to_play);
            playAssetAudio(to_play);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void playCageNumberOld(String cage_number) {
        String to_play = "";
        for (String file : mListAudio) {
            if (file.startsWith(cage_number + "_")) {
                to_play = file;
                break;
            }
        }
        if (to_play.compareToIgnoreCase("") == 0) {
            Toast.makeText(this, "Please enter a valid cage number from 1 to 66 or 100-150", Toast.LENGTH_LONG);
            return;
        }

        try {
            player.reset();
            AssetFileDescriptor afd = this.getAssets().openFd(to_play);
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.start();
            //status_bar.setText(to_play.split("_")[2].split(".mp3")[0].replace("rev",""));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void playAssetAudio(String assetFile) throws Exception {
        killMediaPlayer();

        player = new MediaPlayer();
        AssetFileDescriptor afd = this.getAssets().openFd(assetFile);
        player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(),"Loading Audio from the server...",Toast.LENGTH_LONG).show();

                // pg.setVisibility(View.VISIBLE);
            }
        });
        player.prepare();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(),"Playing...",Toast.LENGTH_LONG).show();

                // pg.setVisibility(View.GONE);
            }
        });
        player.start();
    }

    private void playAudio(String url) throws Exception {
        killMediaPlayer();

        player = new MediaPlayer();
        player.setDataSource(url);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(),"Loading Audio from the server...",Toast.LENGTH_LONG).show();

                // pg.setVisibility(View.VISIBLE);
            }
        });
        player.prepare();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getApplicationContext(),"Playing...",Toast.LENGTH_LONG).show();

                // pg.setVisibility(View.GONE);
            }
        });
        player.start();
    }

    private void killMediaPlayer() {
        if (player != null) {
            try {
                player.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void s3credentialsProvider() {

        // Initialize the AWS Credential
        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:dc45a7f4-f4d2-4577-9c5f-5cd1e23ec795", // Identity pool ID
                Regions.US_EAST_1 // Region
        );

        createAmazonS3Client(credentialsProvider);
    }

    /**
     * Create a AmazonS3Client constructor and pass the credentialsProvider.
     *
     * @param credentialsProvider
     */
    public void createAmazonS3Client(CognitoCachingCredentialsProvider
                                             credentialsProvider) {

        // Create an S3 client
        s3Client = new AmazonS3Client(credentialsProvider);

        // Set the region of your S3 bucket
        s3Client.setRegion(Region.getRegion(Regions.US_EAST_1));
    }

    public void setTransferUtility() {

        transferUtility = new TransferUtility(s3Client, getApplicationContext());
    }

    /**
     * This method is used to upload the file to S3 by using TransferUtility class
     *
     * @param view
     */
    public void uploadFileToS3(View view) {

        TransferObserver transferObserver = transferUtility.upload(
                bucket,     /* The bucket to upload to */
                "Screenshot.png",    /* The key for the uploaded object */
                uploadToS3       /* The file where the data to upload exists */
        );

        transferObserverListener(transferObserver);
    }

    /**
     * This method is used to Download the file to S3 by using transferUtility class
     *
     * @param view
     **/
    public void downloadFileFromS3(View view) {

        TransferObserver transferObserver = transferUtility.download(
                bucket,     /* The bucket to download from */
                "Screenshot.png",    /* The key for the object to download */
                downloadFromS3        /* The file to download the object to */
        );
        transferObserverListener(transferObserver);
    }

    public void fetchFileFromS3(View view) {

        // Get List of files from S3 Bucket
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Looper.prepare();
                    mListAudio = getObjectNamesForBucket(bucket, s3Client);
//
//                    for (int i=0; i< mListAudio.size(); i++){
//                        Toast.makeText(ARScanPage.this, mListAudio.get(i),Toast.LENGTH_SHORT).show();
//                    }
                    Looper.loop();
                    // Log.e("tag", "listing "+ listing);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("tag", "Exception found while listing " + e);
                }

            }
        });
        thread.start();
    }

    private List<String> getObjectNamesForBucket(String bucket, AmazonS3 s3Client) {
        ObjectListing objects = s3Client.listObjects(bucket);
        List<String> objectNames = new ArrayList<String>(objects.getObjectSummaries().size());
        Iterator<S3ObjectSummary> iterator = objects.getObjectSummaries().iterator();
        while (iterator.hasNext()) {
            objectNames.add(iterator.next().getKey());
        }
        while (objects.isTruncated()) {
            objects = s3Client.listNextBatchOfObjects(objects);
            iterator = objects.getObjectSummaries().iterator();
            while (iterator.hasNext()) {
                objectNames.add(iterator.next().getKey());
            }
        }
        return objectNames;
    }

    public void transferObserverListener(TransferObserver transferObserver) {

        transferObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                Toast.makeText(getApplicationContext(), "State Change"
                        + state, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent / bytesTotal * 100);
                Toast.makeText(getApplicationContext(), "Progress in %"
                        + percentage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("error", "error");
            }

        });
    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Are you sure you want to quit the Audio tour? Press back again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    private void setupDebugTools() {
        LinearLayout layout = new LinearLayout(this);
        LinearLayout namespacelayout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        namespacelayout.setOrientation(LinearLayout.HORIZONTAL);

        final EditText edt_namespace = new EditText(this);

        edt_namespace.setLayoutParams(new LinearLayout.LayoutParams(400, 100));
        Button btn_namespace = new Button(this);
        btn_namespace.setText("Play Audio For Instance Id");
        namespacelayout.addView(edt_namespace);
        namespacelayout.addView(btn_namespace);
        btn_namespace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                found.setVisibility(View.VISIBLE);

                AnimalDetail animal = getAnimalDetailsfromId_Debug(Integer.decode(edt_namespace.getEditableText().toString()), "");
                if ((animal == null) || (animal.map_cordinates == null)) {
                    Toast.makeText(context, "No animal associated", Toast.LENGTH_LONG).show();
                    return;
                }
                tileView.moveMarker(found, animal.map_cordinates.x, animal.map_cordinates.y);
                playAudioForAnimal(animal, true);
            }
        });
        layout.addView(namespacelayout);

        LinearLayout maclayout = new LinearLayout(this);
        maclayout.setOrientation(LinearLayout.HORIZONTAL);
        final EditText edt_macid = new EditText(this);
        edt_macid.setLayoutParams(new LinearLayout.LayoutParams(600, 100));

        Button btn_macid = new Button(this);
        btn_macid.setText("Play Audio For MAC Id");
        maclayout.addView(edt_macid);
        maclayout.addView(btn_macid);
        btn_macid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                found.setVisibility(View.VISIBLE);

                AnimalDetail animal = getAnimalDetailsfromId_Debug(0, edt_macid.getEditableText().toString());
                if ((animal == null) || (animal.map_cordinates == null)) {
                    Toast.makeText(context, "No animal associated", Toast.LENGTH_LONG).show();
                    return;
                }
                tileView.moveMarker(found, animal.map_cordinates.x, animal.map_cordinates.y);
                playAudioForAnimal(animal, true);
            }
        });


        layout.addView(maclayout);

        layout.setOrientation(LinearLayout.VERTICAL);
        Button startTest = new Button(this);
        Button nextBeacon = new Button(this);
        nextBeacon.setText("Move to Next Beacon");
        Button prevBeacon = new Button(this);
        prevBeacon.setText("Move to Prev Beacon");
        final TextView mac_id = new TextView(this);
        final TextView namespace = new TextView(this);

        layout.setBackgroundColor(Color.RED);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); //Layout params for Button

        layout.addView(startTest);
        layout.addView(nextBeacon);
        layout.addView(prevBeacon);
        layout.addView(mac_id);
        layout.addView(namespace);
        addContentView(layout, params);
        startTest.setText("Start/Replay Beacon Simulation");
        final Activity context = this;
        startTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                found.setVisibility(View.VISIBLE);
                AnimalDetail animal = getAnimalDetailsfromId_Debug(0, Utils.reference_mac_ids[current_Simulator_beacon]);
                if ((animal == null) || (animal.map_cordinates == null)) {
                    Toast.makeText(context, "No animal associated", Toast.LENGTH_LONG).show();
                    return;
                }
                tileView.moveMarker(found, animal.map_cordinates.x, animal.map_cordinates.y);
                playAudioForAnimal(animal, true);
                mac_id.setText(Utils.reference_mac_ids[current_Simulator_beacon]);
                namespace.setText(String.valueOf(current_Simulator_beacon));

            }
        });
        nextBeacon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_Simulator_beacon++;
                if (current_Simulator_beacon == 92)
                    current_Simulator_beacon = 1;
                AnimalDetail animal = getAnimalDetailsfromId_Debug(0, Utils.reference_mac_ids[current_Simulator_beacon]);
                if ((animal == null) || (animal.map_cordinates == null)) {
                    Toast.makeText(context, "No animal associated", Toast.LENGTH_LONG).show();
                    return;
                }
                tileView.moveMarker(found, animal.map_cordinates.x, animal.map_cordinates.y);
                playAudioForAnimal(animal, true);
                mac_id.setText(Utils.reference_mac_ids[current_Simulator_beacon]);
                namespace.setText(String.valueOf(current_Simulator_beacon));

            }
        });

        prevBeacon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current_Simulator_beacon--;
                if (current_Simulator_beacon == 1)
                    current_Simulator_beacon = 92;
                AnimalDetail animal = getAnimalDetailsfromId_Debug(0, Utils.reference_mac_ids[current_Simulator_beacon]);
                if ((animal == null) || (animal.map_cordinates == null)) {
                    Toast.makeText(context, "No animal associated", Toast.LENGTH_LONG).show();
                    return;
                }
                tileView.moveMarker(found, animal.map_cordinates.x, animal.map_cordinates.y);
                playAudioForAnimal(animal, true);
                mac_id.setText(Utils.reference_mac_ids[current_Simulator_beacon]);
                namespace.setText(String.valueOf(current_Simulator_beacon));

            }
        });
    }


    private AnimalDetail getAnimalDetailsfromId_Debug(int namespace, String address) {


        if (address.equalsIgnoreCase("")) {


            if (namespace == 84) {
                return getAnimalObjectFromName("kakatel");
            }
            if (namespace == 70) {
                return getAnimalObjectFromName("magarmach");
            }
            if (namespace == 31) {
                return getAnimalObjectFromName("cheetal");
            }
            if (namespace == 19) {
                return getAnimalObjectFromName("safedbhag");
            }
            if ((namespace == 23) || (namespace == 24)) {
                return getAnimalObjectFromName("blackbuckshvet");
            }

        }
        switch (address) {
            case "55:46:4F:E6:95:52":
                return getAnimalObjectFromName("General1");
            case "55:46:4F:FB:7A:98":
                return getAnimalObjectFromName("General2");
            case "55:46:4F:FB:7A:49":
                return getAnimalObjectFromName("balrail");
            case "55:46:4F:FB:7A:6A":
                return getAnimalObjectFromName("balrail");
            case "55:46:4F:FB:7A:59":
                return getAnimalObjectFromName("Narchimpanzee");
            case "55:46:4F:FB:7A:9F":
                return getAnimalObjectFromName("Narchimpanzee");
            case "55:46:4F:FB:7A:9A":
                return getAnimalObjectFromName("himalayan_baloo");
            case "55:46:4F:FB:78:5A":
                return getAnimalObjectFromName("tigerhouse");
            case "55:46:4F:FB:7A:5B":
                return getAnimalObjectFromName("tigerhouse");
            case "55:46:4F:FB:7A:AB":
                return getAnimalObjectFromName("tigerhouse");
            case "55:46:4F:FB:7A:9C":
                return getAnimalObjectFromName("duckpond");
            case "55:46:4F:FB:78:71":
                return getAnimalObjectFromName("duckpond");
            case "55:46:4F:FB:7A:8F":
                return getAnimalObjectFromName("duckpond");
            case "55:46:4F:FB:7A:A7":
                return getAnimalObjectFromName("duckpond");
            case "55:46:4F:FB:7A:62":
                return getAnimalObjectFromName("narbabbarsher_prithvi");
            case "55:46:4F:FB:7A:61":
                return getAnimalObjectFromName("narbabbarsher_prithvi");

            case "55:46:4F:FB:7A:AF":
                return getAnimalObjectFromName("leopardhouse");
            case "55:46:4F:FB:7A:46":
                return getAnimalObjectFromName("safedbhag");
            case "55:46:4F:FB:7A:A6":
                return getAnimalObjectFromName("safedbhag");
            case "55:46:4F:FB:7A:AE":
                return getAnimalObjectFromName("safedbhag");
            case "55:46:4F:FB:7A:54":
                return getAnimalObjectFromName("blackbuck");
            case "55:46:4F:FB:7A:66":
                return getAnimalObjectFromName("blackbuck");
            case "55:46:4F:FB:7A:92":
                return getAnimalObjectFromName("blackbuckshvet");
            case "55:46:4F:FB:7A:5E":
                return getAnimalObjectFromName("blackbuckshvet");
            case "55:46:4F:FB:7A:8B":
                return getAnimalObjectFromName("padha");
            case "55:46:4F:FB:7A:56":
                return getAnimalObjectFromName("padha");
            case "55:46:4F:FB:7A:5C":
                return getAnimalObjectFromName("barasingha");
            case "55:46:4F:FB:7A:52":
                return getAnimalObjectFromName("barasingha");
            case "55:46:4F:FB:78:AF":
                return getAnimalObjectFromName("barkingdear");
            case "55:46:4F:FB:7A:95":
                return getAnimalObjectFromName("barkingdear");
            case "55:46:4F:FB:78:AL":
                return getAnimalObjectFromName("cheetal");
            case "55:46:4F:E6:96:05":
                return getAnimalObjectFromName("cheetal");
            case "55:46:4F:FB:7A:A2":
                return getAnimalObjectFromName("sambar_hiran");
            case "55:46:4F:FB:7A:50":
                return getAnimalObjectFromName("sambar_hiran");
            case "55:46:4F:FB:7A:9B":
                return getAnimalObjectFromName("chinkara");
            case "55:46:4F:FB:78:66":
                return getAnimalObjectFromName("chinkara");
            case "55:46:4F:FB:7A:AD":
                return getAnimalObjectFromName("sanpghar");
            case "55:46:4F:FB:7A:5F":
                return getAnimalObjectFromName("sanpghar");
            case "55:46:4F:FB:7A:4A":
                return getAnimalObjectFromName("liontailedmonkey");
            case "55:46:4F:FB:7A:6E":
                return getAnimalObjectFromName("bonet_bandar");
            case "55:46:4F:FB:7A:96":
                return getAnimalObjectFromName("langur_bandar");
            case "55:46:4F:FB:78:B2":
                return getAnimalObjectFromName("desi_bandar");
            case "55:46:4F:FB:78:60":
                return getAnimalObjectFromName("saras_crane");
            case "55:46:4F:FB:7A:55":
                return getAnimalObjectFromName("shaturmurg");
            case "55:46:4F:FB:7A:6D":
                return getAnimalObjectFromName("shaturmurg");
            case "55:46:4F:FB:7A:53":
                return getAnimalObjectFromName("desibalu");
            case "55:46:4F:FB:7A:A9":
                return getAnimalObjectFromName("desibalu");
            case "55:46:4F:FB:7A:9D":
                return getAnimalObjectFromName("hukku_bandar");
            case "55:46:4F:FB:7A:4B":
                return getAnimalObjectFromName("rosy_pelican");
            case "55:46:4F:FB:7A:4D":
                return getAnimalObjectFromName("kachuwua");
            case "55:46:4F:FB:78:14":
                return getAnimalObjectFromName("fishing_cat");
            case "55:46:4F:FB:7A:57":
                return getAnimalObjectFromName("jungle_cat");
            case "55:46:4F:FB:7A:6C":
                return getAnimalObjectFromName("dhanesh_pakshi");
            case "55:46:4F:FB:7A:A1":
                return getAnimalObjectFromName("old_bird_section");
            case "55:46:4F:FB:7A:8C":
                return getAnimalObjectFromName("old_bird_section");
            case "55:46:4F:FB:7A:A5":
                return getAnimalObjectFromName("old_bird_section");
            case "55:46:4F:FB:7A:A4":
                return getAnimalObjectFromName("emu");
            case "55:46:4F:FB:7A:8D":
                return getAnimalObjectFromName("emu");
            case "55:46:4F:FB:78:B3":
                return getAnimalObjectFromName("giraffe_mada");
            case "55:46:4F:FB:78:72":
                return getAnimalObjectFromName("giraffe_mada");
            case "55:46:4F:FB:7A:5D":
                return getAnimalObjectFromName("sehi");
            case "55:46:4F:FB:78:63":
                return getAnimalObjectFromName("barn_ullu");
            case "55:46:4F:FB:7A:91":
                return getAnimalObjectFromName("cheel_ullu");


            //[TODO.. Record Audio for Kasht Ullu] case "55:46:4F:FB:7A:AA": return getAnimalObjectFromName("kashth_ullu");
            case "55:46:4F:FB:7A:AA":
                return getAnimalObjectFromName("");


            case "55:46:4F:FB:7A:4E":
                return getAnimalObjectFromName("owlet");
            case "55:46:4F:FB:7A:6F":
                return getAnimalObjectFromName("balrampurhouse_babar_sher_bada");
            case "55:46:4F:FB:78:65":
                return getAnimalObjectFromName("balrampurhouse_babar_sher_bada");
            case "55:46:4F:FB:7A:51":
                return getAnimalObjectFromName("vasundhara_mada_babar_sherni");
            case "55:46:4F:FB:78:6F":
                return getAnimalObjectFromName("vasundhara_mada_babar_sherni");
            //FIXMEcase "55:46:4F:FB:7A:AD": return getAnimalObjectFromName("magarmach");
            case "55:46:4F:E6:93:AB":
                return getAnimalObjectFromName("ghariyal");
            case "55:46:4F:FB:7A:67":
                return getAnimalObjectFromName("karkel_cat");
            case "55:46:4F:FB:78:0C":
                return getAnimalObjectFromName("lomdi");
            case "55:46:4F:FB:78:13":
                return getAnimalObjectFromName("siyar");
            case "55:46:4F:FB:7A:93":
                return getAnimalObjectFromName("bhediya");
            case "55:46:4F:FB:7A:90":
                return getAnimalObjectFromName("lakadbhagga");
            case "55:46:4F:FB:7A:8E":
                return getAnimalObjectFromName("hippo");
            case "55:46:4F:FB:7A:99":
                return getAnimalObjectFromName("hippo");
            case "55:46:4F:FB:7A:69":
                return getAnimalObjectFromName("new_bird_section");
            case "55:46:4F:FB:78:B1":
                return getAnimalObjectFromName("new_bird_section");
            case "55:46:4F:FB:78:B4":
                return getAnimalObjectFromName("new_bird_section");
            case "55:46:4F:FB:7A:BO":
                return getAnimalObjectFromName("udan_gilhari");
            case "55:46:4F:FB:7A:A3":
                return getAnimalObjectFromName("safed_mor");
            //FIXMEcase "55:46:4F:FB:7A:A6": return getAnimalObjectFromName("kakatel");
            case "55:46:4F:FB:78:73":
                return getAnimalObjectFromName("fijantas");
            case "55:46:4F:FB:7A:97":
                return getAnimalObjectFromName("neele_peele_makau");
            case "55:46:4F:FB:78:AE":
                return getAnimalObjectFromName("bari_gilhari");
            case "55:46:4F:FB:7A:94":
                return getAnimalObjectFromName("loha_saras");
            case "55:46:4F:FB:78:6B":
                return getAnimalObjectFromName("butterfly_park");
            case "55:46:4F:FB:7A:B5":
                return getAnimalObjectFromName("butterfly_park");
            case "55:46:4F:E6:95:62":
                return getAnimalObjectFromName("nocturnal_house");
            case "55:46:4F:E6:95:4C":
                return getAnimalObjectFromName("acuarium_brief_note");
            case "55:46:4F:E6:96:04":
                return getAnimalObjectFromName("acuarium_brief_note");
        }


        return new AnimalDetail();
    }
}