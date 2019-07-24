package ufobeaconsdk.main;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import ufobeaconsdk.callback.OnFailureListener;
import ufobeaconsdk.callback.OnScanSuccessListener;
import ufobeaconsdk.callback.OnSuccessListener;


/**
 * Created by Dell on 01-02-2017.
 */

public class UFOBeaconManager {

    private Context mcontext;
    private BluetoothAdapter btAdapter;
    protected ScanDevices scandevice;

    public UFOBeaconManager(Context context) {
        mcontext = context;
    }

    // Start scanning for near by BluetoothDevice.
    public void startScan(OnScanSuccessListener onScanSuccessListener, OnFailureListener onFailureListener) {

        if (onScanSuccessListener == null || onFailureListener == null)
            return;

        if (isBluetoothEnabled()) {

            if (mcontext != null) {

                if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 23) {
                    if (Utils.initLocation(mcontext)) {
                        if (scandevice == null) {
                            scandevice = new ScanDevices(mcontext, onScanSuccessListener, onFailureListener);
                        } else {

                            if (!scandevice.isScanningRunning) {
                                scandevice.startScanning(onScanSuccessListener, onFailureListener);
                            } else {
                                onFailureListener.onFailure(Utils.ERROR_CODE_SCANING_ALREADY_RUNNING, Utils.message_scanningRunning);
                            }
                        }
                    } else {
                        onFailureListener.onFailure(Utils.ERROR_CODE_LOCATION_IS_OFF, Utils.message_locationServiceOff);

                    }
                } else {

                    if (scandevice == null) {

                        scandevice = new ScanDevices(mcontext, onScanSuccessListener, onFailureListener);
                    } else {

                        if (!scandevice.isScanningRunning) {
                            scandevice.startScanning(onScanSuccessListener, onFailureListener);
                        } else {
                            onFailureListener.onFailure(Utils.ERROR_CODE_SCANING_ALREADY_RUNNING, Utils.message_scanningRunning);
                        }
                    }
                }
            } else {
                onFailureListener.onFailure(Utils.ERROR_CODE_CONTEXT_NULL, Utils.message_contextNull);
            }

        } else {
            onFailureListener.onFailure(Utils.ERROR_CODE_BLUETOOTH_IS_OFF, Utils.message_bluetoothOff);
        }
    }

    // Stop the scanning of near by BLE device.
    public void stopScan(OnSuccessListener onSuccessListener, OnFailureListener onFailureListener) {

        if (onSuccessListener == null || onFailureListener == null)
            return;

        if (scandevice != null)
            scandevice.stopScanning(onSuccessListener, onFailureListener);

    }

    // Check for Bluetooth is enabled or not
    public void isBluetoothEnabled(OnSuccessListener onSuccessListener, OnFailureListener onFailureListener) {

        if (onSuccessListener == null || onFailureListener == null)
            return;

        if (isBluetoothEnabled()) {
            onSuccessListener.onSuccess(true);
        } else {
            onFailureListener.onFailure(Utils.ERROR_CODE_BLUETOOTH_IS_OFF, Utils.message_bluetoothOff);
        }

    }


    // Forcefully enabled the Bluetooth
    public void enable(OnSuccessListener onSuccessListener, OnFailureListener onFailureListener) {

        if (onSuccessListener == null || onFailureListener == null)
            return;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.enable();

        if (isEnabled) {
            onSuccessListener.onSuccess(true);
        } else {
            onFailureListener.onFailure(Utils.ERROR_CODE_BLUETOOTH_IS_OFF, Utils.message_bluetoothOff);
        }

    }

    // check for the location service is enabled or not. It is required if android os is >= 6.0
    public void isLocationServiceEnabled(OnSuccessListener onSuccessListener, OnFailureListener onFailureListener) {

        if (onSuccessListener == null || onFailureListener == null)
            return;

        if (mcontext != null && Utils.initLocation(mcontext)) {
            onSuccessListener.onSuccess(true);
        } else {
            onFailureListener.onFailure(Utils.ERROR_CODE_LOCATION_IS_OFF, Utils.message_locationServiceOff);
        }

    }

    // Check for bluetooth is enabled or not
    private boolean isBluetoothEnabled() {

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null && btAdapter.isEnabled()) {
            return true;
        }

        return false;
    }

    public boolean isScanRunning() {
        if (scandevice != null) {
            return scandevice.isScanningRunning;
        }
        return false;
    }


}
