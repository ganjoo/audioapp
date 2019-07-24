package ufobeaconsdk.main;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;



import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import ufobeaconsdk.callback.OnBeaconSuccessListener;
import ufobeaconsdk.callback.OnConnectSuccessListener;
import ufobeaconsdk.callback.OnFailureListener;
import ufobeaconsdk.callback.OnRSSISuccessListener;
import ufobeaconsdk.callback.OnRangingListener;
import ufobeaconsdk.callback.OnReadSuccessListener;
import ufobeaconsdk.callback.OnSuccessListener;

import static android.content.ContentValues.TAG;
import static ufobeaconsdk.main.Utils.EDDYSTONE_TRANSMISSION_POWER;
import static ufobeaconsdk.main.Utils.bytesToHex;
import static ufobeaconsdk.main.Utils.checkHex;
import static ufobeaconsdk.main.Utils.hexStringToByteArray;


;

/**
 * Created by Dell on 01-02-2017.
 */

public class UFODevice implements Comparable<UFODevice> {

    // Common variables
    private String scanRecord;
    private String deviceName;
    private BluetoothDevice btdevice;
    private int rssi;
    private int rssiAt1meter;
    public int txPower;
    public double distance;
    public int advertisingInterval;
    private Context mContext;
    private Date date;
    private CopyOnWriteArrayList<RequestModel> requestQueue = new CopyOnWriteArrayList<>();
    private ArrayList<Integer> rssilist;
    private String distanceInString = "";
    private boolean inRange = false;
    private int rangecounter = 0;
    private String temperature = "0";
    private String batteryVoltage = "0";

    // After Connecting Bluetooth Device specific variables
    private boolean isDeviceConnected = false;
    private BluetoothGatt mBluetoothGatt;
    private ArrayList<BluetoothGattService> serviceList = null;
    private boolean isdisconnectPressed = false;
    private HandlerThread connectionHandlerThread = null;
    private Handler connectionHandler;
    private UUID characteristicUUID = null;

    // Callback listener specific variables
    private OnConnectSuccessListener onConnectSuccessListener;
    private OnRSSISuccessListener onRSSISuccessListener;
    private OnSuccessListener onSuccessListener;
    private OnFailureListener onFailureListener;
    private OnReadSuccessListener onReadSuccessListener;
    private OnBeaconSuccessListener onBeaconSuccessListener;

    // iBeacon specific variables
    private String proximityUUID = "";
    private int major;
    private int minor;

    // Eddystone UID specific variables
    private String eddystoneNameSpace;
    private String eddystoneInstance;

    // Eddystone URL specific variables
    private String eddystoneURL;

    // Eddystone TLM Specific Variables
    private int eddystoneTLMVersion;
    private float eddystoneTLMBatteryVoltage;
    private int eddystoneTLMTemperature;
    private long eddystoneTLMPDUCounts;
    private Date eddystoneActiveSince;
    private EddystoneType eddystoneType;
    private UFODeviceType deviceType;
    protected OnRangingListener onRangingListener;
    private int modelId;

    public boolean isDeviceConnected() {
        return isDeviceConnected;
    }

    private void setDeviceConnected(boolean deviceConnected) {
        isDeviceConnected = deviceConnected;
    }

    public String getDistanceInString() {
        return distanceInString;
    }

    protected void setDistanceInString(String distanceInString) {
        this.distanceInString = distanceInString;
    }

    protected ArrayList<Integer> getRssilist() {
        return rssilist;
    }

    protected void setRssilist(ArrayList<Integer> rssilist) {
        this.rssilist = rssilist;
    }

    protected void setDistance(double distance) {
        this.distance = distance;
    }

    public int getRssiAt1meter() {
        return rssiAt1meter;
    }

    protected void setRssiAt1meter(int rssiAt1meter) {
        this.rssiAt1meter = rssiAt1meter;
    }

    public int getTxPower() {
        return txPower;
    }

    public double getDistance() {
        return distance;
    }

    public String getProximityUUID() {
        return proximityUUID;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public String getEddystoneNameSpace() {
        return eddystoneNameSpace;
    }

    public String getEddystoneInstance() {
        return eddystoneInstance;
    }

    public String getEddystoneURL() {
        return eddystoneURL;
    }

    public int getEddystoneTLMVersion() {
        return eddystoneTLMVersion;
    }

    public float getEddystoneTLMBatteryVoltage() {
        return eddystoneTLMBatteryVoltage;
    }

    public int getEddystoneTLMTemperature() {
        return eddystoneTLMTemperature;
    }

    public long getEddystoneTLMPDUCounts() {
        return eddystoneTLMPDUCounts;
    }

    public Date getEddystoneActiveSince() {
        return eddystoneActiveSince;
    }

    protected void setProximityUUID(String proximityUUID) {
        this.proximityUUID = proximityUUID;
    }

    protected void setMajor(int major) {
        this.major = major;
    }

    protected void setMinor(int minor) {
        this.minor = minor;
    }

    protected void setEddystoneURL(String eddystoneURL) {
        this.eddystoneURL = eddystoneURL;
    }

    protected void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    protected void setEddystoneNameSpace(String eddystoneNameSpace) {
        this.eddystoneNameSpace = eddystoneNameSpace;
    }

    protected void setEddystoneInstance(String eddystoneInstance) {
        this.eddystoneInstance = eddystoneInstance;
    }

    protected void setEddystoneTLMVersion(int eddystoneTLMVersion) {
        this.eddystoneTLMVersion = eddystoneTLMVersion;
    }

    protected void setEddystoneTLMBatteryVoltage(float eddystoneTLMBatteryVoltage) {
        this.eddystoneTLMBatteryVoltage = eddystoneTLMBatteryVoltage;
    }

    protected void setEddystoneTLMTemperature(int eddystoneTLMTemperature) {
        this.eddystoneTLMTemperature = eddystoneTLMTemperature;
    }

    protected void setEddystoneTLMPDUCounts(long eddystoneTLMPDUCounts) {
        this.eddystoneTLMPDUCounts = eddystoneTLMPDUCounts;
    }

    protected void setEddystoneActiveSince(Date eddystoneActiveSince) {
        this.eddystoneActiveSince = eddystoneActiveSince;
    }

    protected boolean isInRange() {
        return inRange;
    }

    protected void setInRange(boolean inRange) {
        this.inRange = inRange;
    }

    protected int getRangecounter() {
        return rangecounter;
    }

    protected void setRangecounter(int rangecounter) {
        this.rangecounter = rangecounter;
    }

    public Date getDate() {
        return date;
    }

    protected void setDate(Date date) {
        this.date = date;
    }

    public UFODeviceType getDeviceType() {
        return deviceType;
    }

    protected void setDeviceType(UFODeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public EddystoneType getEddystoneType() {
        return eddystoneType;
    }

    protected void setEddystoneType(EddystoneType eddystoneType) {
        this.eddystoneType = eddystoneType;
    }

    public UFODevice(Context context) {
        mContext = context;
    }

    public String getScanRecord() {
        return scanRecord;
    }

    protected void setScanRecord(String scanRecord) {
        this.scanRecord = scanRecord;
    }

    public BluetoothDevice getBtdevice() {
        return btdevice;
    }

    protected void setBtdevice(BluetoothDevice btdevice) {
        this.btdevice = btdevice;
    }

    public int getRssi() {
        return rssi;
    }

    protected void setRssi(int scanRssi) {
        this.rssi = scanRssi;
    }

    public String getTemperature() {
        return temperature;
    }

    protected void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getBatteryVoltage() {
        return batteryVoltage;
    }

    public int getModelId() {
        return modelId;
    }

    protected void setModelId(int modelId) {
        this.modelId = modelId;
    }

    protected void setBatteryVoltage(String batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    // start procedure of connected BLE device
    public void connect(OnConnectSuccessListener onConnectSuccessListener, OnFailureListener onFailureListener) {

        if (onConnectSuccessListener == null || onFailureListener == null) {
            return;
        }

        this.onConnectSuccessListener = onConnectSuccessListener;
        this.onFailureListener = onFailureListener;

        connectDevice();
    }

    // Connect with selected BLE device
    private boolean connectDevice() {

        if (getBtdevice() == null) {
            onFailureListener.onFailure(Utils.ERROR_CODE_DEVICE_FAIL_TO_CONNECT, Utils.message_connectionFail);
        }
        try {
            if (!isDeviceConnected) {
                if (mContext != null) {
                    mBluetoothGatt = getBtdevice().connectGatt(mContext, false,
                            mGattCallback);
                }
            } else {
                onFailureListener.onFailure(Utils.ERROR_CODE_DEVICE_ALREADY_CONNECTED, Utils.message_alreadyConnected);
            }
        } catch (Exception e) {
            onFailureListener.onFailure(Utils.ERROR_CODE_DEVICE_FAIL_TO_CONNECT, Utils.message_connectionFail);
        }
        return true;
    }

    // Bluetooth GATT callback for getting all the event of BLE device
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                try {
                    if (mBluetoothGatt != null) {
                        //stopThread();

                        stopConnectionTimer();

                        boolean isDiscoverded = mBluetoothGatt
                                .discoverServices();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                isDeviceConnected = false;
                stopConnectionTimer();
                Log.e("UFODevice", "Disconnected");
                if (mBluetoothGatt != null)
                    refreshDeviceCache(mBluetoothGatt);
                mBluetoothGatt.close();
                mBluetoothGatt = null;
                if (isdisconnectPressed) {
                    isdisconnectPressed = false;
                    if (onSuccessListener != null)
                        onSuccessListener.onSuccess(true);
                } else {
                    if (characteristicUUID != null && characteristicUUID.equals(Utils.PASSWORD)) {
                        onFailureListener.onFailure(Utils.ERROR_CODE_INVALID_PASSWORD, Utils.message_invalidpassword);
                    } else {
                        onFailureListener.onFailure(Utils.ERROR_CODE_DEVICE_DISCONNECTED, Utils.message_deviceDisconnected);
                    }
                }

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            discoverServices(gatt.getServices());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            if (requestQueue != null && requestQueue.size() > 0) {

                RequestModel model = null;
                model = requestQueue.get(0);

                requestQueue.remove(0);

                Log.e("UFODevice", "onCharacteristicRead called " + requestQueue.size());

                if (requestQueue.size() > 0) {
                    parseReadData(characteristic, model);

                    RequestModel modelnew = null;
                    modelnew = requestQueue.get(0);

                    if (modelnew != null && modelnew.getRequestType() == Utils.REQUEST_TYPE_WRITE)
                        writeData(modelnew);
                    else if (modelnew != null && modelnew.getRequestType() == Utils.REQUEST_TYPE_READ) {
                        readData(modelnew);
                    }
                } else {
                    parseReadData(characteristic, model);
                }
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (onSuccessListener != null)
                onSuccessListener.onSuccess(true);
            Log.e(TAG, "onCharacteristicWrite status " + status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            onRSSISuccessListener.onSuccess(rssi);
        }

    };

    private void parseReadData(BluetoothGattCharacteristic characteristic, RequestModel model) {

        byte[] data = characteristic.getValue();
        String value = parseCharacteristicData(data, characteristic);

        Log.e("Value", "Value = " + value);

        if (model != null && model.getOnReadSuccessListener() != null) {
            model.getOnReadSuccessListener().onSuccess(value);
        }
    }

    private String parseCharacteristicData(byte[] data, BluetoothGattCharacteristic characteristic) {

        String val = null;

        if (characteristic.getUuid().toString().equalsIgnoreCase(Utils.PROXIMITY_UUID.toString())) {

            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            Long high = byteBuffer.getLong();
            Long low = byteBuffer.getLong();

            val = new UUID(high, low).toString();
        } else if (characteristic.getUuid().toString().equalsIgnoreCase(Utils.MAJOR.toString())) {
            byte[] majorval = new byte[4];
            majorval[0] = 0;
            majorval[1] = 0;
            majorval[2] = data[0];
            majorval[3] = data[1];
            val = String.valueOf(byteArrayToInt(majorval));
        } else if (characteristic.getUuid().toString().equalsIgnoreCase(Utils.MINOR.toString())) {
            byte[] minorval = new byte[4];
            minorval[0] = 0;
            minorval[1] = 0;
            minorval[2] = data[0];
            minorval[3] = data[1];
            val = String.valueOf(byteArrayToInt(minorval));
        } else if (characteristic.getUuid().toString().equalsIgnoreCase(Utils.ADVERTISEMENT_INTERVAL.toString())) {
            val = String.valueOf(readShort(data, 0));
        } else if (characteristic.getUuid().toString().equalsIgnoreCase(Utils.EDDYSTONE_BEACON_PERIOD.toString())) {
            byte[] adv = new byte[2];
            adv[0] = data[1];
            adv[1] = data[0];
            val = String.valueOf(readShort(adv, 0));
        } else if (characteristic.getUuid().toString().equalsIgnoreCase(Utils.TRANSMISSION_POWER.toString()) || characteristic.getUuid().toString().equalsIgnoreCase(EDDYSTONE_TRANSMISSION_POWER.toString())) {
            val = String.valueOf((int) data[0]);
        }

        return val;
    }

    private short readShort(byte[] data, int offset) {
        return (short) (((data[offset] << 8)) | ((data[offset + 1] & 0xff)));
    }

    private int byteArrayToInt(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    // Discover the services of Connected BLE device.
    private void discoverServices(List<BluetoothGattService> services) {

        isDeviceConnected = true;
        serviceList = (ArrayList<BluetoothGattService>) services;

        if (getDeviceType() == UFODeviceType.IBEACON) {
            Utils.CUSTOMER_UUID = Utils.IBEACON_SERVICE_UUID;
        } else {
            Utils.CUSTOMER_UUID = Utils.EDDYSTONE_SERVICE_UUID;
        }

        if (onConnectSuccessListener != null)
            onConnectSuccessListener.onSuccess(this);


    }

    // Refresh the cache of bluetooth
    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod(
                    "refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(
                        localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {

        }
        return false;
    }

    // Read RSSI of connected BLE device
    public void readRssi(OnRSSISuccessListener onRSSISuccessListener, OnFailureListener onFailureListener) {

        if (onRSSISuccessListener == null || onFailureListener == null)
            return;

        this.onRSSISuccessListener = onRSSISuccessListener;
        this.onFailureListener = onFailureListener;
        if (isDeviceConnected) {
            if (mBluetoothGatt != null) {
                mBluetoothGatt.readRemoteRssi();
            }
        } else {
            onFailureListener.onFailure(Utils.ERROR_CODE_DEVICE_NOT_CONNECTED, Utils.message_deviceNotConnected);
        }
    }

    // Disconnect with BLE device
    public void disconnect(OnSuccessListener onSuccessListener, OnFailureListener onFailureListener) {

        if (onSuccessListener == null || onFailureListener == null)
            return;

        this.onSuccessListener = onSuccessListener;
        this.onFailureListener = onFailureListener;
        if (isDeviceConnected) {
            if (mBluetoothGatt != null) {
                isdisconnectPressed = true;
                mBluetoothGatt.disconnect();
            }
        } else {
            onFailureListener.onFailure(Utils.ERROR_CODE_DEVICE_ALREADY_DISCONNECTED, Utils.message_alreadyDisconnected);
        }
    }


    // write uuid to ufo beacon device
    public void setiBeaconProximityUUID(String uuid, OnBeaconSuccessListener onSuccessListener, OnFailureListener onFailureListener) {

        if (onSuccessListener == null || onFailureListener == null)
            return;

        if (!Utils.isValidUUID(uuid)) {
            onFailureListener.onFailure(Utils.ERROR_CODE_SCANING_UUID_IS_INVALID, Utils.message_invalidUUID);
            return;
        }
        uuid = uuid.replaceAll("-", "");
        byte[] data = hexStringToByteArray(uuid);
        createWriteRequestModel(Utils.PROXIMITY_UUID, onSuccessListener, onFailureListener, data);

    }


    // read uuid of ufo beacon device
    public void getiBeaconProximityUUID(OnReadSuccessListener onReadSuccessListener, OnFailureListener onFailureListener) {
        if (onReadSuccessListener == null || onFailureListener == null)
            return;
        createRequestModel(Utils.PROXIMITY_UUID, onReadSuccessListener, onFailureListener);

    }

    // write major of ufo beacon device
    public void setiBeaconMajor(int major, OnBeaconSuccessListener onSuccessListener, OnFailureListener onFailureListener) {
        if (onSuccessListener == null || onFailureListener == null)
            return;
        if (major < 0 || major > 65535) {
            onFailureListener.onFailure(Utils.ERROR_CODE_MAJOR_IS_INVALID, Utils.message_invalidMajor);
            return;
        }
        String majorhex = Integer.toHexString(major);
        if (majorhex.length() % 2 != 0)
            majorhex = "0" + majorhex;
        byte[] majordata = hexStringToByteArray(majorhex);
        byte[] data = getData(majordata);
        createWriteRequestModel(Utils.MAJOR, onSuccessListener, onFailureListener, data);
    }

    private byte[] getData(byte[] beacondata) {
        byte[] data = new byte[2];
        if (beacondata.length == 1) {
            data[0] = 0;
            data[1] = beacondata[0];
        } else {
            System.arraycopy(beacondata, 0, data, 0, beacondata.length);
        }
        return data;
    }


    // read major of ufo beacon device
    public void getiBeaconMajor(OnReadSuccessListener onReadSuccessListener, OnFailureListener onFailureListener) {
        if (onReadSuccessListener == null || onFailureListener == null)
            return;

        createRequestModel(Utils.MAJOR, onReadSuccessListener, onFailureListener);

    }

    // write minor of ufo beacon device
    public void setiBeaconMinor(int minor, OnBeaconSuccessListener onSuccessListener, OnFailureListener onFailureListener) {

        if (onSuccessListener == null || onFailureListener == null)
            return;

        if (minor < 0 || minor > 65535) {
            onFailureListener.onFailure(Utils.ERROR_CODE_MINOR_IS_INVALID, Utils.message_invalidMinor);
            return;
        }
        String minorhex = Integer.toHexString(minor);
        if (minorhex.length() % 2 != 0)
            minorhex = "0" + minorhex;
        byte[] minordata = hexStringToByteArray(minorhex);
        byte[] data = getData(minordata);
        createWriteRequestModel(Utils.MINOR, onSuccessListener, onFailureListener, data);
    }

    // read minor of ufo beacon device
    public void getiBeaconMinor(OnReadSuccessListener onReadSuccessListener, OnFailureListener onFailureListener) {
        if (onReadSuccessListener == null || onFailureListener == null)
            return;

        createRequestModel(Utils.MINOR, onReadSuccessListener, onFailureListener);

    }

    //write transmission power to ufo beacon
    public void setiBeaconTxPower(int txPower, OnBeaconSuccessListener onSuccessListener, OnFailureListener onFailureListener) {
        if (onSuccessListener == null || onFailureListener == null)
            return;
        byte[] data = new byte[1];
        data[0] = (byte) txPower;
        createWriteRequestModel(Utils.TRANSMISSION_POWER, onSuccessListener, onFailureListener, data);
    }

    //read transmission power to ufo beacon
    public void getiBeaconTxPower(OnReadSuccessListener onReadSuccessListener, OnFailureListener onFailureListener) {
        if (onReadSuccessListener == null || onFailureListener == null)
            return;

        createRequestModel(Utils.TRANSMISSION_POWER, onReadSuccessListener, onFailureListener);
    }

    // write beacon password for authentication
    public void setPassword(String password, OnBeaconSuccessListener onBeaconSuccessListener, OnFailureListener onFailureListener) {
        if (onBeaconSuccessListener == null || onFailureListener == null)
            return;
        this.onBeaconSuccessListener = onBeaconSuccessListener;
        this.onFailureListener = onFailureListener;
        if (!isDeviceConnected) {
            onFailureListener.onFailure(Utils.ERROR_CODE_DEVICE_NOT_CONNECTED, Utils.message_deviceNotConnected);
            return;

        }
        characteristicUUID = Utils.PASSWORD;
        if (TextUtils.isEmpty(password) || !((password.length() % 2) == 0) || !(password.matches(checkHex))) {
            //onFailureListener.onFailure(Utils.ERROR_CODE_INVALID_PASSWORD, Utils.message_invalidpassword);
            if (mBluetoothGatt != null) {
                mBluetoothGatt.disconnect();
            }
            return;
        }


        if (Utils.CUSTOMER_UUID != null && characteristicUUID != null) {
            if (mBluetoothGatt != null) {

                BluetoothGattService service = mBluetoothGatt.getService(Utils.CUSTOMER_UUID);

                BluetoothGattCharacteristic characteristic = null;
                if (service != null) {
                    characteristic = service.getCharacteristic(Utils.PASSWORD);

                    if (characteristic != null) {

                        byte[] byt = hexStringToByteArray(password);

                        characteristic.setValue(byt);

                        boolean write = mBluetoothGatt.writeCharacteristic(
                                characteristic);
                        Log.e("Write", "Write = " + write);
                        if (!write) {
                            onFailureListener.onFailure(Utils.ERROR_CODE_FAIL_TO_WRITE, Utils.message_failToWriteData);
                        } else {
                            connectionTimer();

                        }

                    } else {
                        onFailureListener.onFailure(Utils.ERROR_CODE_FAIL_TO_WRITE, Utils.message_failToWriteData);
                    }
                } else {
                    onFailureListener.onFailure(Utils.ERROR_CODE_FAIL_TO_WRITE, Utils.message_failToWriteData);
                }
            } else {
                onFailureListener.onFailure(Utils.ERROR_CODE_GATT_NOT_CONNECTED, Utils.message_gattNotConnected);
            }
        }
    }


    // write advertise interval to ufo beacon
    public void setiBeaconAdvertisementInterval(int advertiseInterval, OnBeaconSuccessListener onBeaconSuccessListener, OnFailureListener onFailureListener) {
        if (onBeaconSuccessListener == null || onFailureListener == null)
            return;

        if (advertiseInterval < 100 || advertiseInterval > 1000) {
            onFailureListener.onFailure(Utils.ERROR_CODE_INVALID_ADVERTISE_INTERVAL, Utils.message_invalid_advertise);
            return;
        }

        String hex = Integer.toHexString(advertiseInterval);
        if (hex.length() % 2 != 0)
            hex = "0" + hex;
        byte[] data = new byte[2];
        byte[] receBytes = hexStringToByteArray(hex);
        if (receBytes != null && receBytes.length == 1) {
            data[0] = 0;
            data[1] = receBytes[0];
        } else {
            System.arraycopy(receBytes, 0, data, 0, receBytes.length);
        }
        createWriteRequestModel(Utils.ADVERTISEMENT_INTERVAL, onBeaconSuccessListener, onFailureListener, data);

    }

    // read advertise interval from ufo beacon
    public void getiBeaconAdvertisementInterval(OnReadSuccessListener onReadSuccessListener, OnFailureListener onFailureListener) {
        if (onReadSuccessListener == null || onFailureListener == null)
            return;

        createRequestModel(Utils.ADVERTISEMENT_INTERVAL, onReadSuccessListener, onFailureListener);

    }


    //write transmission power to ufo Eddystone beacon
    public void setEddystoneTxPower(int txPower, OnBeaconSuccessListener onSuccessListener, OnFailureListener onFailureListener) {
        if (onSuccessListener == null || onFailureListener == null)
            return;
        byte[] data = new byte[1];
        data[0] = (byte) txPower;
        createWriteRequestModel(EDDYSTONE_TRANSMISSION_POWER, onSuccessListener, onFailureListener, data);
    }

    //read transmission power from ufo Eddystone beacon
    public void getEddystoneTxPower(OnReadSuccessListener onReadSuccessListener, OnFailureListener onFailureListener) {
        if (onReadSuccessListener == null || onFailureListener == null)
            return;

        createRequestModel(EDDYSTONE_TRANSMISSION_POWER, onReadSuccessListener, onFailureListener);
    }

    // write advertise interval to ufo Eddystone beacon
    public void setEddystoneAdvertisementInterval(int advertiseInterval, OnBeaconSuccessListener onBeaconSuccessListener, OnFailureListener onFailureListener) {
        if (onBeaconSuccessListener == null || onFailureListener == null)
            return;
        String hex = Integer.toHexString(advertiseInterval);
        if (hex.length() % 2 != 0)
            hex = "0" + hex;
        byte[] data = new byte[2];
        byte[] receBytes = hexStringToByteArray(hex);
        if (receBytes != null && receBytes.length == 1) {
            data[0] = receBytes[0];
            data[1] = 0;
        } else {
            data[0] = receBytes[1];
            data[1] = receBytes[0];
        }
        createWriteRequestModel(Utils.EDDYSTONE_BEACON_PERIOD, onBeaconSuccessListener, onFailureListener, data);

    }

    // read advertise interval from ufo Eddystone beacon
    public void getEddystoneAdvertisementInterval(OnReadSuccessListener onReadSuccessListener, OnFailureListener onFailureListener) {
        if (onReadSuccessListener == null || onFailureListener == null)
            return;

        createRequestModel(Utils.EDDYSTONE_BEACON_PERIOD, onReadSuccessListener, onFailureListener);

    }

    public void setEddystoneFrames(EddystoneType eddystoneType, OnBeaconSuccessListener onBeaconSuccessListener, OnFailureListener onFailureListener) {
        if (onBeaconSuccessListener == null || onFailureListener == null)
            return;
        byte eddType = eddystoneType.getEddyStoneType();
        byte[] data = new byte[]{eddType};
        createWriteRequestModel(Utils.EDDYSTONE_FLAGS, onBeaconSuccessListener, onFailureListener, data);
    }

    public void setEddystoneURI(String url, OnBeaconSuccessListener onBeaconSuccessListener, OnFailureListener onFailureListener) {
        if (onBeaconSuccessListener == null || onFailureListener == null)
            return;
        if (TextUtils.isEmpty(url)) {
            onFailureListener.onFailure(Utils.ERROR_CODE_INVALID_URL, Utils.message_invalidURL);
            return;
        }

        boolean isUrlValid = Utils.validateByRegEx(url);

        if (isUrlValid) {

            byte[] data = Utils.generateByteArraty(url);

            byte[] urldata = new byte[data.length + 1];

            urldata[0] = getHeader(url);

            System.arraycopy(data, 0, urldata, 1, data.length);

            createWriteRequestModel(Utils.EDDYSTONE_URI_DATA, onBeaconSuccessListener, onFailureListener, urldata);

        } else {
            onFailureListener.onFailure(Utils.ERROR_CODE_INVALID_URL, Utils.message_invalidURL);
            return;
        }

    }

    // read eddystone URL
    public void readEddystoneURL(OnReadSuccessListener onReadSuccessListener, OnFailureListener onFailureListener) {
        if (onReadSuccessListener == null || onFailureListener == null)
            return;

        createRequestModel(Utils.EDDYSTONE_URI_DATA, onReadSuccessListener, onFailureListener);

    }

    // read eddystone URL
    public void readEddystoneUID(OnReadSuccessListener onReadSuccessListener, OnFailureListener onFailureListener) {
        if (onReadSuccessListener == null || onFailureListener == null)
            return;

        createRequestModel(Utils.EDDYSTONE_URI_DATA, onReadSuccessListener, onFailureListener);

    }

    private byte getHeader(String url) {

        byte header = 0;

        if (url.startsWith("http://www")) {
            header = 0;
        } else if (url.startsWith("https://www")) {
            header = 1;
        } else if (url.startsWith("http://")) {
            header = 2;
        } else if (url.startsWith("https://")) {
            header = 3;
        } else if (url.startsWith("www")) {
            header = 0;
        }
        return header;
    }

    public void setEddystoneUID(String nameSpaceId, String instanceId, OnBeaconSuccessListener onBeaconSuccessListener, OnFailureListener onFailureListener) {

        if (onBeaconSuccessListener == null || onFailureListener == null)
            return;

        if (TextUtils.isEmpty(nameSpaceId) || !nameSpaceId.matches(checkHex) || (nameSpaceId.length() % 2 != 0)) {
            onFailureListener.onFailure(Utils.ERROR_CODE_INVALID_NAMESPACEID, Utils.message_invalidNamespaceId);
            return;
        }
        if (TextUtils.isEmpty(instanceId) || !instanceId.matches(checkHex) || (instanceId.length() % 2 != 0)) {
            onFailureListener.onFailure(Utils.ERROR_CODE_INVALID_INSTANCEID, Utils.message_invalidInstanceId);
            return;
        }


        byte[] namespaceid = hexStringToByteArray(nameSpaceId);
        byte[] instanceid = hexStringToByteArray(instanceId);

        if (namespaceid.length != 10 || instanceid.length != 6) {
            onFailureListener.onFailure(Utils.ERROR_CODE_INVALID_LENGTH, Utils.message_invalidLength);
        }

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            os.write(namespaceid);
            os.write(instanceid);
            createWriteRequestModel(Utils.EDDYSTONE_URI_DATA, onBeaconSuccessListener, onFailureListener, os.toByteArray());

        } catch (Exception e) {

        }


    }

//    // get the complete packet of advertisement of ufo beacon
//    public void getAdvertisementPacket(OnBeaconSuccessListener onBeaconSuccessListener, OnFailureListener onFailureListener){
//
//    }
//
//    // get the name of ufo beacon
//    public void getIBeaconName(OnBeaconSuccessListener onBeaconSuccessListener, OnFailureListener onFailureListener){
//
//    }


    private void connectionTimer() {

        if (connectionHandlerThread == null) {
            connectionHandlerThread = new HandlerThread("connectionHandlerThread");
            connectionHandlerThread.start();
        }

        startConnectionTimer();

    }


    private void startConnectionTimer() {
        if (connectionHandler == null)
            connectionHandler = new Handler(connectionHandlerThread.getLooper(), connectionTimeoutCallBack);
        connectionHandler.sendEmptyMessageDelayed(0, 2000);
    }

    private void stopConnectionTimer() {
        if (connectionHandler != null) {
            connectionHandler.removeCallbacksAndMessages(null);
            connectionHandler = null;
        }

        if (connectionHandlerThread != null) {
            connectionHandlerThread.quit();
            connectionHandlerThread = null;
        }
    }

    private Handler.Callback connectionTimeoutCallBack = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (isDeviceConnected) {
                onBeaconSuccessListener.onSuccess(true);
            } else {
                onFailureListener.onFailure(Utils.ERROR_CODE_INVALID_PASSWORD, Utils.message_invalidpassword);
            }

            return false;
        }
    };

    private void createRequestModel(UUID charuuid, OnReadSuccessListener onReadSuccessListener, OnFailureListener onFailureListener) {

        RequestModel request = new RequestModel();
        request.setCharacteristicUUID(charuuid);
        request.setServiceUUID(Utils.CUSTOMER_UUID);
        request.setOnReadSuccessListener(onReadSuccessListener);
        request.setOnFailureListener(onFailureListener);
        request.setRequestType(Utils.REQUEST_TYPE_READ);
        requestQueue.add(request);

        if (requestQueue != null && requestQueue.size() == 1) {

            readData(requestQueue.get(0));

        }

    }

    private void createWriteRequestModel(UUID charuuid, OnBeaconSuccessListener onBeaconSuccessListener, OnFailureListener onFailureListener, byte[] data) {

        RequestModel request = new RequestModel();
        request.setCharacteristicUUID(charuuid);
        request.setServiceUUID(Utils.CUSTOMER_UUID);
        request.setOnBeaconSuccessListener(onBeaconSuccessListener);
        request.setOnFailureListener(onFailureListener);
        request.setRequestType(Utils.REQUEST_TYPE_WRITE);
        request.setData(data);
        requestQueue.add(request);

        if (requestQueue != null && requestQueue.size() == 1) {
            writeData(requestQueue.get(0));
//            Handler h = new Handler();
//            h.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (requestQueue.size() > 0) {
//                        writeData(requestQueue.get(0));
//                    }
//                }
//            },1000);
        }

    }

    private void writeData(RequestModel requestModel) {

        if (requestModel != null) {
            this.onBeaconSuccessListener = requestModel.getOnBeaconSuccessListener();
            this.onFailureListener = requestModel.getOnFailureListener();
            if (!isDeviceConnected) {
                onFailureListener.onFailure(Utils.ERROR_CODE_DEVICE_NOT_CONNECTED, Utils.message_deviceNotConnected);
                return;

            }
            characteristicUUID = requestModel.getCharacteristicUUID();

            if (Utils.CUSTOMER_UUID != null && characteristicUUID != null) {
                if (mBluetoothGatt != null) {

                    BluetoothGattService service = mBluetoothGatt.getService(Utils.CUSTOMER_UUID);

                    BluetoothGattCharacteristic characteristic = null;
                    if (service != null) {
                        characteristic = service.getCharacteristic(characteristicUUID);

                        if (characteristic != null) {

                            characteristic.setValue(requestModel.getData());
                            boolean write = mBluetoothGatt.writeCharacteristic(
                                    characteristic);
                            Log.e("write", "write = " + write + " UUID = " + characteristicUUID + " Data = " + bytesToHex(requestModel.getData()));
                            if (!write) {
                                onFailureListener.onFailure(Utils.ERROR_CODE_FAIL_TO_WRITE, Utils.message_failToWriteData);
                                checkIfWriteRequestExist();
                            } else {
                                checkIfWriteRequestExist();
                            }

                        } else {
                            onFailureListener.onFailure(Utils.ERROR_CODE_FAIL_TO_WRITE, Utils.message_failToWriteData);
                            checkIfWriteRequestExist();
                        }
                    } else {
                        onFailureListener.onFailure(Utils.ERROR_CODE_FAIL_TO_WRITE, Utils.message_failToWriteData);
                        checkIfWriteRequestExist();
                    }
                } else {
                    onFailureListener.onFailure(Utils.ERROR_CODE_GATT_NOT_CONNECTED, Utils.message_gattNotConnected);
                }
            }

        }
    }

    private void checkIfWriteRequestExist() {

        if (requestQueue != null && requestQueue.size() > 0) {

            RequestModel model = null;
            model = requestQueue.get(0);

            model.getOnBeaconSuccessListener().onSuccess(true);

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {

                    requestQueue.remove(0);
                    if (requestQueue.size() > 0) {
                        RequestModel modelnew = null;
                        modelnew = requestQueue.get(0);
                        if (modelnew != null && modelnew.getRequestType() == Utils.REQUEST_TYPE_WRITE)
                            writeData(modelnew);
                        else if (modelnew != null && modelnew.getRequestType() == Utils.REQUEST_TYPE_READ) {
                            readData(modelnew);
                        }
                    }
                }
            }, 180);
        }
    }

    private void readData(RequestModel requestModel) {

        if (requestModel != null) {

            this.onReadSuccessListener = requestModel.getOnReadSuccessListener();
            this.onFailureListener = requestModel.getOnFailureListener();

            if (!isDeviceConnected) {
                onFailureListener.onFailure(Utils.ERROR_CODE_DEVICE_NOT_CONNECTED, Utils.message_deviceNotConnected);
                return;

            }
            characteristicUUID = requestModel.getCharacteristicUUID();

            if (Utils.CUSTOMER_UUID != null && characteristicUUID != null) {
                if (mBluetoothGatt != null) {

                    BluetoothGattService service = mBluetoothGatt.getService(Utils.CUSTOMER_UUID);

                    BluetoothGattCharacteristic characteristic = null;
                    if (service != null) {
                        characteristic = service.getCharacteristic(characteristicUUID);

                        if (characteristic != null) {

                            boolean read = mBluetoothGatt.readCharacteristic(
                                    characteristic);
                            Log.e("Read", "Read = " + read);
                            if (!read) {
                                onFailureListener.onFailure(Utils.ERROR_CODE_FAIL_TO_READ, Utils.message_failToReadData);
                                readnext();
                            }

                        } else {
                            onFailureListener.onFailure(Utils.ERROR_CODE_FAIL_TO_READ, Utils.message_failToReadData);
                            readnext();
                        }
                    } else {
                        onFailureListener.onFailure(Utils.ERROR_CODE_FAIL_TO_READ, Utils.message_failToReadData);
                        readnext();
                    }
                } else {
                    onFailureListener.onFailure(Utils.ERROR_CODE_GATT_NOT_CONNECTED, Utils.message_gattNotConnected);
                }
            }


        }

    }

    public void readnext() {
        if (requestQueue != null && requestQueue.size() > 0) {

            RequestModel model = null;
            model = requestQueue.get(0);

            requestQueue.remove(0);

            if (requestQueue.size() > 0) {

                RequestModel modelnew = null;
                modelnew = requestQueue.get(0);

                if (modelnew != null && modelnew.getRequestType() == Utils.REQUEST_TYPE_READ) {
                    readData(modelnew);
                }

            }

        }
    }

    public void startRangeMonitoring(OnRangingListener onRangingListener) {
        onRangingListener.isDeviceInRange(RangeType.IN_RANGE);
        this.onRangingListener = onRangingListener;
    }

    @Override
    public int compareTo(UFODevice ufoDevice) {
        if (getRssi() > ufoDevice.getRssi()) {
            return -1;
        } else if (getRssi() < ufoDevice.getRssi()) {
            return 1;
        } else {
            return 0;
        }
    }
}
