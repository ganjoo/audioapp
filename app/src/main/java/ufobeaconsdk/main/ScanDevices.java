package ufobeaconsdk.main;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ufobeaconsdk.callback.OnFailureListener;
import ufobeaconsdk.callback.OnScanSuccessListener;
import ufobeaconsdk.callback.OnSuccessListener;

import static ufobeaconsdk.main.Utils.bytesToHex;


/**
 * Created by Dell on 01-02-2017.
 */

public class ScanDevices {

    private static String TAG = "ScanDevices";

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<String> scanDeviceNameList;
    private Context context;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters = new ArrayList<ScanFilter>();
    private ScanCallback mScanCallback;
    protected boolean isScanningRunning = false;
    public OnScanSuccessListener onScanSuccessListener;
    public OnFailureListener onFailureListener;
    private LinkedHashMap<String, UFODevice> hasmap = new LinkedHashMap<String, UFODevice>();

    protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private UFODeviceParser ufoDeviceParser;

    private Timer rescanTimer;
    private ReScanTimerTask rescanTimerTask;

//    private Timer rssiTimer;
//    private RSSITimerTask RssiTimerTask;

    private HandlerThread rssiTimerHandlerThread = null;
    private Handler rssiTimerHandler;

    /**
     * Constructor of the scan device class to initialization of the variables.
     *
     * @param mcontext
     * @param onScanSuccessListener
     * @param onFailureListener
     */
    protected ScanDevices(Context mcontext, OnScanSuccessListener onScanSuccessListener, OnFailureListener onFailureListener) {
        final BluetoothManager bluetoothManager = (BluetoothManager) mcontext
                .getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        scanDeviceNameList = new ArrayList<String>();
        context = mcontext;
        this.onScanSuccessListener = onScanSuccessListener;
        this.onFailureListener = onFailureListener;
        ufoDeviceParser = new UFODeviceParser();
        clearDeviceList();
        scanLEDevice();

    }

    // Start scanning of near by BLE devices.
    protected void startScanning(OnScanSuccessListener onScanSuccessListener, OnFailureListener onFailureListener) {
        this.onScanSuccessListener = onScanSuccessListener;
        this.onFailureListener = onFailureListener;
        scanLEDevice();
    }

    // Clear device list of already founded.
    protected void clearDeviceList() {

        if (scanDeviceNameList != null && scanDeviceNameList.size() > 0)
            scanDeviceNameList.clear();
    }


    @SuppressLint("NewApi")
    protected void scanLEDevice() {
        try {
            if (Build.VERSION.SDK_INT < 21) { // if android os is < 5.0
                if (mBluetoothAdapter != null) {
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                    isScanningRunning = true;

                    // StartScanDevice();
                    rssiTimer();
                }
            } else { // if android os is >= 5.0
                ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
                scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                settings = scanSettingsBuilder.build();
                mLEScanner = BluetoothAdapter.getDefaultAdapter()
                        .getBluetoothLeScanner();
                registerCallBack();
                if (mLEScanner != null) {
                    mLEScanner.startScan(filters, settings, mScanCallback);
                    isScanningRunning = true;
                    //StartScanDevice();
                    rssiTimer();
                }
            }


        } catch (Exception e) {
            onFailureListener.onFailure(Utils.ERROR_CODE_TRY_CATCH, e.getMessage());
        }

    }

    class ReScanTimerTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (Build.VERSION.SDK_INT < 21) {
                    if (mLeScanCallback != null && mBluetoothAdapter != null) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mBluetoothAdapter.startLeScan(mLeScanCallback);
                    }

                } else {
                    if (mScanCallback != null && mLEScanner != null) {
                        mLEScanner.stopScan(mScanCallback);
                        mLEScanner.startScan(filters, settings, mScanCallback);
                    }
                }

            } catch (Exception e) {
            }
        }
    }

    // Stop scanning of near by BLE device.
    protected void stopScanning(OnSuccessListener onSuccessListener, OnFailureListener onFailureListener) {
        try {
            isScanningRunning = false;
            stopScanDevice();
            if (Build.VERSION.SDK_INT < 21) {
                if (mBluetoothAdapter != null)
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

            } else {
                if (mLEScanner != null)
                    mLEScanner.stopScan(mScanCallback);
            }

            onSuccessListener.onSuccess(true);
            clearDeviceList();

        } catch (Exception e) {
            onFailureListener.onFailure(Utils.ERROR_CODE_TRY_CATCH, e.getMessage());
        }
    }

    protected void stopScanDevice() {
        try {

           stopRssiTimer();
            if (hasmap != null && hasmap.size() > 0)
                hasmap.clear();

        } catch (Exception e) {
        }
    }


    // ScanCallback of near by BLE devices. if android os is < 5.0
    protected BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             final byte[] scanRecord) {

            if (device != null) {
                parseScanDevice(device, rssi, scanRecord);
            }
        }

    };

    //registerCallBack of near by BLE devices. if android os is >= 5.0
    protected void registerCallBack() {
        if (Build.VERSION.SDK_INT >= 21) {
            mScanCallback = new ScanCallback() {
                @Override
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                public void onScanResult(int callbackType, ScanResult result) {
                    BluetoothDevice device = result.getDevice();
                    int rssi = result.getRssi();
                    byte[] scanRecord = result.getScanRecord().getBytes();
                        parseScanDevice(device, rssi, scanRecord);
                }

                @Override
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                public void onBatchScanResults(List<ScanResult> results) {
                    /* Process a batch scan results */
                    for (ScanResult sr : results) {
                    }
                }
            };
        }
    }

    // Parse scan record and fillup the model based on deviceType.
    protected void parseScanDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {


        if (device != null) {
            if (hasmap != null
                    && hasmap.containsKey(device.getAddress())) {

                UFODevice ufodevice = hasmap.get(device.getAddress());
                ufodevice.setDate(new Date());

                if (ufodevice.getRssilist() != null && ufodevice.getRssilist().size() > 20) {
                    //  Log.e("remove","remove = " + ufodevice.getRssilist().get(0));
                    ufodevice.getRssilist().remove(0);
                }
                ufodevice.getRssilist().add(rssi);
                ufodevice.setRssi(rssi);

                if (ufodevice.getRangecounter() == 4) {
                    if (ufodevice.onRangingListener != null)
                        ufodevice.onRangingListener.isDeviceInRange(RangeType.IN_RANGE);
                }
                ufodevice.setInRange(false);
                ufodevice.setRangecounter(0);

                String deviceType = ufoDeviceParser.getBeaconType(bytesToHex(scanRecord), device);
                if (deviceType != null && deviceType.equalsIgnoreCase("Eddystone")) {
                    ufodevice = ufoDeviceParser.parseScanRecord(scanRecord, ufodevice);
                    ufodevice.setScanRecord(bytesToHex(scanRecord));
                }
                onScanSuccessListener.onSuccess(ufodevice);
            } else {
                String deviceType = ufoDeviceParser.getBeaconType(bytesToHex(scanRecord), device);

                if (deviceType != null && deviceType.equalsIgnoreCase("iBeacon") && (device.getAddress().startsWith("55:46:4F") || device.getAddress().startsWith("55:46:F2"))) {
                    UFODevice ufoDevice = new UFODevice(context);
                    ufoDevice = ufoDeviceParser.decodeIbeacon(scanRecord, device, ufoDevice);
                    ufoDevice.setBtdevice(device);
                    ufoDevice.setDeviceType(UFODeviceType.IBEACON);
                    ufoDevice.setModelId(new Random().nextInt());
                    ufoDevice.setScanRecord(bytesToHex(scanRecord));
                    ArrayList<Integer> list = new ArrayList<Integer>();
                    list.add(rssi);
                    ufoDevice.setRssilist(list);
                    ufoDevice.setRssi(rssi);
                    ufoDevice.setDate(new Date());
                    ufoDevice.setInRange(false);
                    ufoDevice.setRangecounter(0);
                    ufoDeviceParser.getBeaconDetails(ufoDevice.getScanRecord(), ufoDevice.getBtdevice(),ufoDevice);
                    if (ufoDevice.onRangingListener != null)
                        ufoDevice.onRangingListener.isDeviceInRange(RangeType.IN_RANGE);
                    hasmap.put(device.getAddress(), ufoDevice);
                    onScanSuccessListener.onSuccess(ufoDevice);
                } else if (deviceType != null && deviceType.equalsIgnoreCase("Eddystone") && device.getAddress().startsWith("55:46:4F")) {
                    UFODevice ufoDevice = new UFODevice(context);
                    ufoDevice = ufoDeviceParser.parseScanRecord(scanRecord, ufoDevice);
                    ufoDevice.setBtdevice(device);
                    ufoDevice.setDeviceType(UFODeviceType.EDDYSTONE);
                    ufoDevice.setScanRecord(bytesToHex(scanRecord));
                    ufoDevice.setInRange(false);
                    ufoDevice.setRangecounter(0);
                    ufoDevice.setRssi(rssi);
                    ufoDevice.setModelId(new Random().nextInt());
                    ArrayList<Integer> list = new ArrayList<Integer>();
                    list.add(rssi);
                    ufoDevice.setRssilist(list);
                    ufoDevice.setDate(new Date());
                    if (ufoDevice.onRangingListener != null)
                        ufoDevice.onRangingListener.isDeviceInRange(RangeType.IN_RANGE);
                    hasmap.put(device.getAddress(), ufoDevice);
                    onScanSuccessListener.onSuccess(ufoDevice);
                }
            }
        }

    }

//    class RSSITimerTask extends TimerTask {
//        @Override
//        public void run() {
//
//            if (hasmap != null && hasmap.size() > 0) {
//                try {
//                    for (Map.Entry<String, UFODevice> e : hasmap.entrySet()) {
//                        UFODevice ufodevice = e.getValue();
//                        ArrayList<Integer> rssilist = new ArrayList<>();
//                        if (ufodevice != null) {
//                           // rssilist.addAll(ufodevice.getRssilist());
//                            rssilist = ufodevice.getRssilist();
//
//                            if (rssilist != null && rssilist.size() > 0) {
//
//                                Collections.sort(rssilist);
//
//                                int size = rssilist.size();
//                                //Log.e("RssiSize","ListActualSize = " + size);
//
////                                for (int i = 0; i < size; i++) {
////                                    Log.e("Rssi","Rssi = " + rssilist.get(i));
////                                }
//
//                                if (size > 0) {
//
//                                    int upper = size * 20 / 100;
//                                    int lower = size * 10 / 100;
//
//                                    for (int i = 0; i < lower; i++) {
//                                        rssilist.remove(i);
//                                    }
//                                    for (int i = 0; i < upper; i++) {
//                                        rssilist.remove(rssilist.size() - 1);
//                                    }
//
//                                  //  Log.e("RssiSize","AfterRemoveSize = " + size + " Upper = " + upper + " lower = " + lower);
//
//                                }
//
//
//                                if(rssilist != null && rssilist.size() > 0){
//
//                                    int sumOfRssi = 0;
//
//                                    for (int i=0; i<rssilist.size(); i++){
//
//                                        sumOfRssi += rssilist.get(i);
//                                    }
//
//                                    int avgrssi = sumOfRssi
//                                            / rssilist.size();
//                                  //  Log.e("avgrssi","avgrssi = " + avgrssi);
//
//                                    ufodevice.setRssi(avgrssi);
//                                   // ufodevice.getRssilist().clear();
//                                    double distance = Utils.calculateAccuracyFromRSSI(ufodevice.getRssiAt1meter(),avgrssi);
//                                    ufodevice.setDistance(distance);
//                                    ufodevice.setDistanceInString(Utils.getDistanceInString(distance));
//
//                                   // Log.e("Distance","distance = " + distance + " distanceinString = " + ufodevice.getDistanceInString());
//
//                                    onScanSuccessListener.onSuccess(ufodevice);
//
//                                }
//
//                            }
//                        }
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
//    }


    private void rssiTimer() {

        if (rssiTimerHandlerThread == null) {
            rssiTimerHandlerThread = new HandlerThread("rssiTimerHandlerThread");
            rssiTimerHandlerThread.start();
        }

        startRssiTimer();

    }


    private void startRssiTimer() {
        if (rssiTimerHandler == null)
            rssiTimerHandler = new Handler(rssiTimerHandlerThread.getLooper(), rssiTimeoutCallBack);
        rssiTimerHandler.sendEmptyMessageDelayed(0, 3000);
    }

    private void stopRssiTimer() {
        if (rssiTimerHandler != null) {
            rssiTimerHandler.removeCallbacksAndMessages(null);
            rssiTimerHandler = null;
        }

        if (rssiTimerHandlerThread != null) {
            rssiTimerHandlerThread.quit();
            rssiTimerHandlerThread = null;
        }
    }

    private Handler.Callback rssiTimeoutCallBack = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            // Log.e("Handler","Handler called");

            if (hasmap != null && hasmap.size() > 0) {
                try {
                    for (Map.Entry<String, UFODevice> e : hasmap.entrySet()) {
                        UFODevice ufodevice = e.getValue();
                        ArrayList<Integer> rssilist = new ArrayList<>();
                        if (ufodevice.isInRange() && ufodevice.getRangecounter() == 3) {
                            if (ufodevice.onRangingListener != null && !ufodevice.isDeviceConnected())
                                ufodevice.onRangingListener.isDeviceInRange(RangeType.OUT_RANGE);
                            ufodevice.setRangecounter(4);
                        } else if (ufodevice.getRangecounter() == 4) {
                            // do nothing
                        } else {
                            ufodevice.setInRange(true);
                            ufodevice.setRangecounter(ufodevice.getRangecounter() + 1);
                            if (ufodevice != null) {
                                // rssilist.addAll(ufodevice.getRssilist());
                                rssilist = ufodevice.getRssilist();

                                if (rssilist != null && rssilist.size() > 0) {

                                    Collections.sort(rssilist);

                                    int size = rssilist.size();
                                    //Log.e("RssiSize","ListActualSize = " + size);

//                                for (int i = 0; i < size; i++) {
//                                    Log.e("Rssi","Rssi = " + rssilist.get(i));
//                                }

                                    if (size > 0) {

                                        int upper = size * 20 / 100;
                                        int lower = size * 10 / 100;

                                        for (int i = 0; i < lower; i++) {
                                            rssilist.remove(i);
                                        }
                                        for (int i = 0; i < upper; i++) {
                                            rssilist.remove(rssilist.size() - 1);
                                        }

                                        //  Log.e("RssiSize","AfterRemoveSize = " + size + " Upper = " + upper + " lower = " + lower);

                                    }

                                    if (rssilist != null && rssilist.size() > 0) {

                                        int sumOfRssi = 0;

                                        for (int i = 0; i < rssilist.size(); i++) {

                                            sumOfRssi += rssilist.get(i);
                                        }

                                        int avgrssi = sumOfRssi
                                                / rssilist.size();
                                        double distance = Utils.calculateAccuracyFromRSSI(ufodevice.getRssiAt1meter(), avgrssi);
                                        ufodevice.setDistance(distance);
                                        ufodevice.setDistanceInString(Utils.getDistanceInString(distance));


                                    }

                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            startRssiTimer();
            return false;
        }
    };

}
