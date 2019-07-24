package ufobeaconsdk.main;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.URLUtil;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static ufobeaconsdk.main.Utils.bytesToHex;
import static ufobeaconsdk.main.Utils.hex2decimal;


/**
 * Created by Dell on 02-02-2017.
 */

public class UFODeviceParser {

    private static final String TAG = "UFODeviceParser";
    private static final int DATA_TYPE_SERVICE_DATA = 0x16;

    private static final byte[] TEST_SERVICE_16_BIT_UUID_BYTES = {(byte) 0xaa,
            (byte) 0xfe};
    public static final String NO_URI = "";
    private static final byte TEST_UID_FRAME_TYPE = 0x00;
    private static final byte TEST_URI_FRAME_TYPE = 0x10;
    private static final byte TEST_TLM_FRAME_TYPE = 0x20;
    private static final byte EXPECTED_VERSION = 0x00;

    private static final SparseArray<String> URL_CODES = new SparseArray<String>() {
        {
            put((byte) 0, ".com/");
            put((byte) 1, ".org/");
            put((byte) 2, ".edu/");
            put((byte) 3, ".net/");
            put((byte) 4, ".info/");
            put((byte) 5, ".biz/");
            put((byte) 6, ".gov/");
            put((byte) 7, ".com");
            put((byte) 8, ".org");
            put((byte) 9, ".edu");
            put((byte) 10, ".net");
            put((byte) 11, ".info");
            put((byte) 12, ".biz");
            put((byte) 13, ".gov");
        }
    };
    private static final SparseArray<String> URI_SCHEMES = new SparseArray<String>() {
        {
            put((byte) 0, "http://www.");
            put((byte) 1, "https://www.");
            put((byte) 2, "http://");
            put((byte) 3, "https://");
            put((byte) 4, "urn:uuid:");
        }
    };
    // private Beacon beacon;

    // __________________________IDENTIFYING TYPE OF EDDYSTONE____________//
    // geting scanRecords and identifying type of eddystone and parsing
    // perticular type of eddystone and return Beacon object
    @SuppressLint("NewApi")
    protected UFODevice parseScanRecord(byte[] scanRecord, UFODevice ufodevice) {

        byte[] data = getServiceDataFromUUID(scanRecord);

        if (data != null)
            ufodevice = typeOfFrame(data, scanRecord, ufodevice);
        return ufodevice;
    }

    // getting the Type of frame from the scan record of the device.
    private UFODevice typeOfFrame(byte[] serviceData, byte[] scanRecord, UFODevice ufodevice) {

        byte frameType = serviceData[0];
        switch (frameType) {
            case TEST_TLM_FRAME_TYPE: {
                ufodevice = parseTLMBeaconData(scanRecord, ufodevice);
                break;
            }
            case TEST_UID_FRAME_TYPE: {
                ufodevice = parseUIDBeaconData(scanRecord, ufodevice);
                break;
            }
            case TEST_URI_FRAME_TYPE: {
                ufodevice = parseURIBeaconData(scanRecord, ufodevice);
                break;
            }
            default: {
                ufodevice = null;
                break;
            }

        }
        return ufodevice;
    }

    // ________________________ UID PARSING STARTS________________________//

    private UFODevice parseUIDBeaconData(byte[] scanRecord, UFODevice ufoDevice) {
        byte[] serviceData;
        serviceData = getServiceData(scanRecord, TEST_UID_FRAME_TYPE);
        if (serviceData != null && serviceData.length >= 2) {
            int currentPos = 0;
            byte[] byt = new byte[1];
            byt[0] = serviceData[currentPos++];
            byt[0] = (byte) (byt[0] + (-40));
            String hex = bytesToHex(byt);

            int txPowerLevel = hex2decimal(hex);

            serviceData[currentPos] = (byte) (serviceData[currentPos] & 0xFF);
            String nameSpaceId = decodeNameSpace(serviceData, currentPos);
            currentPos = currentPos + 10;
            String InstanceId = decodeInstanceId(serviceData, currentPos);

            ufoDevice.setEddystoneNameSpace(nameSpaceId);
            ufoDevice.setEddystoneInstance(InstanceId);

            // Here is your txPower in DBm value
            int txpower = Utils.eddystoneTransmitPowerInDBM(txPowerLevel);

            // int rssiAt1Meter = Utils.rssiAt1MeterforEddystone(txPowerLevel);
            int rssiAt1Meter = byt[0];
            ufoDevice.setTxPower(txpower);
            ufoDevice.setRssiAt1meter(rssiAt1Meter);
            setFrameType(ufoDevice, ufoDevice.getEddystoneType(), EddystoneType.EDDYSTONE_UID);
        }
        return ufoDevice;
    }

    // Decode the Namespace from the UID beacon service data.
    private static String decodeNameSpace(byte[] serviceData, int offset) {
        if (serviceData.length == offset) {
            return null;
        }
        String nameSpaceString = bytesToHex(Arrays.copyOfRange(serviceData,
                offset, offset + 10));
        return nameSpaceString;
    }

    // Decode the InstanceId from the UID beacon service data.
    private static String decodeInstanceId(byte[] serviceData, int offset) {
        if (serviceData.length == offset) {
            return null;
        }
        String InstanceId = bytesToHex(Arrays.copyOfRange(serviceData, offset,
                offset + 6));
        return InstanceId;
    }

    // ___________________ UID PARSING FINISHED______________________//


    // ___________________ URI PARSING STARTS_________________________//

    @SuppressWarnings("static-access")
    public UFODevice parseURIBeaconData(byte[] scanRecordBytes, UFODevice ufoDevice) {
        byte[] serviceData;
        serviceData = getServiceData(scanRecordBytes, TEST_URI_FRAME_TYPE);
        if (serviceData != null && serviceData.length >= 2) {
            int currentPos = 0;
            byte[] byt = new byte[1];
            byt[0] = serviceData[currentPos++];
            byt[0] = (byte) (byt[0] + (-40));
            String hex = bytesToHex(byt);
            int txPowerLevel = hex2decimal(hex);

            byte flags = (byte) (serviceData[currentPos] >> 4);
            serviceData[currentPos] = (byte) (serviceData[currentPos] & 0xFF);
            String uri = decodeUri(serviceData, currentPos);

            ufoDevice.setEddystoneURL(uri);

            // Here is your txPower in DBm value
            int txpower = Utils.eddystoneTransmitPowerInDBM(txPowerLevel);
            //  int rssiAt1Meter = Utils.rssiAt1MeterforEddystone(txPowerLevel);
            int rssiAt1Meter = byt[0];
            ufoDevice.setTxPower(txpower);
            ufoDevice.setRssiAt1meter(rssiAt1Meter);
            setFrameType(ufoDevice, ufoDevice.getEddystoneType(), EddystoneType.EDDYSTONE_URL);
            //ufoDevice.setFlags(flags);


        }
        return ufoDevice;
    }

    // Decode the URI from the Eddystone service data.
    private static String decodeUri(byte[] serviceData, int offset) {
        if (serviceData.length == offset) {
            return NO_URI;
        }
        StringBuilder uriBuilder = new StringBuilder();
        if (offset < serviceData.length) {
            byte b = serviceData[offset++];
            String scheme = URI_SCHEMES.get(b);
            if (scheme != null) {
                uriBuilder.append(scheme);
                if (URLUtil.isNetworkUrl(scheme)) {
                    return decodeUrl(serviceData, offset, uriBuilder);
                } else if ("urn:uuid:".equals(scheme)) {
                    return decodeUrnUuid(serviceData, offset, uriBuilder);
                }
            }
            Log.w(TAG, "decodeUri unknown Uri scheme code=" + b);
        }
        return null;
    }

    // Decode the URI from the Eddystone service data.
    private static String decodeUrl(byte[] serviceData, int offset,
                                    StringBuilder urlBuilder) {
        while (offset < serviceData.length) {
            byte b = serviceData[offset++];
            String code = URL_CODES.get(b);
            if (code != null) {
                urlBuilder.append(code);
            } else {
                urlBuilder.append((char) b);
            }
        }
        return urlBuilder.toString();
    }

    // Decode the UrnUUID from the Eddystone service data.
    private static String decodeUrnUuid(byte[] serviceData, int offset,
                                        StringBuilder urnBuilder) {
        ByteBuffer bb = ByteBuffer.wrap(serviceData);
        // UUIDs are ordered as byte array, which means most significant first
        bb.order(ByteOrder.BIG_ENDIAN);
        long mostSignificantBytes, leastSignificantBytes;
        try {
            bb.position(offset);
            mostSignificantBytes = bb.getLong();
            leastSignificantBytes = bb.getLong();
        } catch (BufferUnderflowException e) {
            Log.w(TAG, "decodeUrnUuid BufferUnderflowException!");
            return null;
        }
        UUID uuid = new UUID(mostSignificantBytes, leastSignificantBytes);
        urnBuilder.append(uuid.toString());
        return urnBuilder.toString();
    }

    // ______________________________URI PARSING FINISHED______________//

    // _____________________________TLM PARSING STARTS________________//

    @SuppressWarnings("static-access")
    private static UFODevice parseTLMBeaconData(byte[] scanRecord, UFODevice ufodevice) {
        byte[] serviceData = getServiceData(scanRecord, TEST_TLM_FRAME_TYPE);
        if (serviceData != null) {

            if (serviceData.length < 11) {
                return ufodevice;
            }

            if (Utils.previousTlmService == null) {
                Utils.previousTlmService = serviceData;
            } else {
                if (Arrays.equals(Utils.previousTlmService, serviceData)) {
                    //	Log.e("Beacon", "TLM DATA CHANGING :" + false);
                    Utils.setTlmDataChanging(false);
                    Utils.previousTlmService = serviceData;
                } else {
                    //	Log.e("Beacon", "TLM DATA CHANGING :" + true);
                    Utils.previousTlmService = serviceData;
                    Utils.setTlmDataChanging(true);
                }
            }

            ByteBuffer buf = ByteBuffer.wrap(serviceData);
            byte versionByte = serviceData[0];
            int version = (int) versionByte;
            if (versionByte != EXPECTED_VERSION) {
                version = 0;
            }

            byte[] voltageByte = Arrays.copyOfRange(serviceData, 1, 3);
            int voltage = getVoltage(voltageByte);

            byte[] tempByte = Arrays.copyOfRange(serviceData, 3, 5);

            int temprature = getTemprature(tempByte);

            long packetCounts = buf.getInt(5);

            //String packetString = String.valueOf(packetCounts);

            long uptime = buf.getInt(9);
            uptime *= 100;

            long currenttime = System.currentTimeMillis();

            //  Log.e("Time","Uptime " + uptime + " currentTime = " + currenttime);

            long diff = currenttime - uptime;

            //Log.e("Time","diff " + diff );

            Date date = new Date(diff);
            // Log.e("Uptime","Uptime = " + uptime);

//            long second = (uptime / 1000) % 60;
//            long minute = (uptime / (1000 * 60)) % 60;
//            long hour = (uptime / (1000 * 60 * 60)) % 24;
//
//            String upTimeString = String.format("%02d:%02d:%02d", hour, minute,
//                    second);

            //Log.e("Uptime","Uptime String = " + upTimeString);

            ufodevice.setEddystoneTLMTemperature(temprature);
            ufodevice.setEddystoneTLMVersion(version);
            ufodevice.setEddystoneTLMBatteryVoltage(Float.parseFloat(String.format("%.1f", (voltage/1000.0f))));
            ufodevice.setEddystoneTLMPDUCounts(packetCounts);
            ufodevice.setEddystoneActiveSince(date);
            setFrameType(ufodevice, ufodevice.getEddystoneType(), EddystoneType.EDDYSTONE_TLM);

        }
        return ufodevice;

    }


    private static void setFrameType(UFODevice ufodevice, EddystoneType existingType, EddystoneType currentFrameType) {
//        if (existingType == null) {
//            ufodevice.setEddystoneType(currentFrameType);
//        } else
        if ((existingType == EddystoneType.EDDYSTONE_UID && currentFrameType == EddystoneType.EDDYSTONE_TLM) || (existingType == EddystoneType.EDDYSTONE_TLM && currentFrameType == EddystoneType.EDDYSTONE_UID)) {
            ufodevice.setEddystoneType(EddystoneType.EDDYSTONE_UID_TLM);
        } else if ((existingType == EddystoneType.EDDYSTONE_URL && currentFrameType == EddystoneType.EDDYSTONE_TLM) || (existingType == EddystoneType.EDDYSTONE_TLM && currentFrameType == EddystoneType.EDDYSTONE_URL)) {
            ufodevice.setEddystoneType(EddystoneType.EDDYSTONE_URL_TLM);
        } else if ((existingType == EddystoneType.EDDYSTONE_URL_TLM && currentFrameType == EddystoneType.EDDYSTONE_UID) || (existingType == EddystoneType.EDDYSTONE_UID_TLM && currentFrameType == EddystoneType.EDDYSTONE_URL)) {
            ufodevice.setEddystoneType(EddystoneType.EDDYSTONE_UID_URL_TLM);
        } else if ((existingType != EddystoneType.EDDYSTONE_UID_URL_TLM && existingType != EddystoneType.EDDYSTONE_UID_TLM && existingType != EddystoneType.EDDYSTONE_URL_TLM)) {
            ufodevice.setEddystoneType(currentFrameType);
        }

    }

    // Get the voltage of the eddystone TLM beacon from the service data voltage bytes.
    private static int getVoltage(byte[] voltageBytes) {
        int voltage;
        if (voltageBytes != null && voltageBytes.length == 2) {
            int voltage1 = voltageBytes[1] & 0xff;
            int voltage0 = voltageBytes[0] & 0xff;
            voltage0 <<= 8;
            float finalVoltage = voltage0 + voltage1;
            voltage = (int) finalVoltage;
            return voltage;
        }
        return 0;
    }

    // Get the Temperature of the eddystone TLM beacon from the service data temperature bytes.
    private static int getTemprature(byte[] tempByte) {
        int tempString = 0;
        if (tempByte != null && tempByte.length == 2) {
            int temp1 = tempByte[1] & 0xff;
            int temp0 = tempByte[0] & 0xff;
            temp0 <<= 8;
            float finalTemp = temp0 + temp1;
            finalTemp /= 256;

            tempString = (int) finalTemp;
            return tempString;
        }
        return tempString;
    }

    // ________________________________TLM PARSING FINISHED___________________//


    // _____________________COMMON FOR ALL____________________________//

    // Get the service data from the scan record.
    private static byte[] getServiceData(byte[] scanRecord, byte BeaconTypeByte) {
        int currentPos = 0;
        try {
            while (currentPos < scanRecord.length) {
                int fieldLength = scanRecord[currentPos++] & 0xff;
                if (fieldLength == 0) {
                    break;
                }
                int fieldType = scanRecord[currentPos] & 0xff;
                if (fieldType == DATA_TYPE_SERVICE_DATA) {
                    if (scanRecord[currentPos + 1] == TEST_SERVICE_16_BIT_UUID_BYTES[0]
                            && scanRecord[currentPos + 2] == TEST_SERVICE_16_BIT_UUID_BYTES[1]
                            && scanRecord[currentPos + 3] == BeaconTypeByte) {
                        // Jump to beginning offrame.
                        currentPos += 4;
                        // frame type
                        byte[] bytes = new byte[fieldLength - 4];
                        System.arraycopy(scanRecord, currentPos, bytes, 0,
                                fieldLength - 4);
                        return bytes;
                    }
                } // length includes the length of the field type.
                currentPos += fieldLength;
            }
        } catch (Exception e) {
            Log.e(TAG,
                    "unable to parse scan record: "
                            + Arrays.toString(scanRecord), e);
        }
        return null;
    }

    // Get the service data from the UUID scan record.
    public static byte[] getServiceDataFromUUID(byte[] scanRecord) {
        int currentPos = 0;
        try {
            while (currentPos < scanRecord.length) {
                int fieldLength = scanRecord[currentPos++] & 0xff;
                if (fieldLength == 0) {
                    break;
                }
                int fieldType = scanRecord[currentPos] & 0xff;
                if (fieldType == DATA_TYPE_SERVICE_DATA) {
                    if (scanRecord[currentPos + 1] == TEST_SERVICE_16_BIT_UUID_BYTES[0]
                            && scanRecord[currentPos + 2] == TEST_SERVICE_16_BIT_UUID_BYTES[1]) {
                        // Jump to beginning of frame.
                        currentPos += 4;
                        // TODO: Add tests
                        // field length - field type - ID - frame type
                        byte[] bytes = new byte[fieldLength - 4];
                        System.arraycopy(scanRecord, currentPos - 1, bytes, 0,
                                fieldLength - 4);
                        return bytes;
                    }
                }
                // length includes the length of the field type.
                currentPos += fieldLength;
            }
        } catch (Exception e) {
            Log.e(TAG,
                    "unable to parse scan record: "
                            + Arrays.toString(scanRecord), e);
        }
        return null;
    }

    // Parse the iBeacon data from the scan records.
    protected UFODevice decodeIbeacon(byte[] scanRecord, BluetoothDevice device, UFODevice ufoDevice) {

        int startByte = 2;
        boolean patternFound = false;
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && // Identifies
                    // an
                    // iBeacon
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { // Identifies
                // correct
                // data
                // length
                patternFound = true;
                break;
            }
            startByte++;
        }

        if (patternFound) {
            // Convert to hex String
            byte[] uuidBytes = new byte[16];
            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
            String hexString = bytesToHex(uuidBytes);

            // Here is your UUID
            String uuid = hexString.substring(0, 8) + "-"
                    + hexString.substring(8, 12) + "-"
                    + hexString.substring(12, 16) + "-"
                    + hexString.substring(16, 20) + "-"
                    + hexString.substring(20, 32);

            // Here is your Major value
            int major = (scanRecord[startByte + 20] & 0xff) * 0x100
                    + (scanRecord[startByte + 21] & 0xff);

            // Here is your Minor value
            int minor = (scanRecord[startByte + 22] & 0xff) * 0x100
                    + (scanRecord[startByte + 23] & 0xff);

            int txpw = scanRecord[startByte + 24] & 0xff;

            // Here is your txPower in DBm value
            int txpower = Utils.transmitPowerInDBM(txpw);

            int rssiAt1Meter = Utils.rssiAt1MeterforiBeacon(txpw);

            ufoDevice.setProximityUUID(uuid);
            ufoDevice.setMajor(major);
            ufoDevice.setMinor(minor);
            ufoDevice.setTxPower(txpower);
            ufoDevice.setRssiAt1meter(rssiAt1Meter);
        }

        return ufoDevice;
    }


    // Parse the scan record string to identify the beacon is an Eddystone or iBeacon.
    protected String getBeaconType(String data, BluetoothDevice device) {
        String uuidofservice = null;
        int datalength = 0;
        int length;
        int type;
        int i = 0;
        int j = 2;
        String deviceType = "";
        while (datalength != data.length()) {
            length = Integer.parseInt(data.substring(i, i + j), 16);
            if (length == 0)
                break;
            type = Integer.parseInt(data.substring((i + j), i + j + 2), 16);

            switch (type) {

                case 2:
                    uuidofservice = data.substring((i + j + 2), i + j + (length * 2));
                    if (uuidofservice.contains("AAFE")) {
                        deviceType = "Eddystone";
                    }
                    break;
                case 3:
                    uuidofservice = data.substring((i + j + 2), i + j + (length * 2));
                    if (uuidofservice.contains("AAFE")) {
                        deviceType = "Eddystone";
                    }
                    break;

                case 255:
                    uuidofservice = data.substring((i + j + 2), i + j + (length * 2));
                    if (uuidofservice.length() > 9) {
                        String strbeacon = uuidofservice.substring(4, 8);
                        String strapple = uuidofservice.substring(0, 2);
                        if (strbeacon.equalsIgnoreCase("0215") && strapple.equalsIgnoreCase("4c")) {
                            deviceType = "iBeacon";
                        }
                    }
                    break;

            }
            if (!deviceType.equalsIgnoreCase(""))
                break;
            i = i + j + (length * 2);
            datalength = i;
        }
        return deviceType;
    }


    // Parse the scan record string to identify the beacon is an Eddystone or iBeacon.
    public void getBeaconDetails(String data, BluetoothDevice device, UFODevice ufoDevice) {
        String uuidofservice = "";
        int datalength = 0;
        int length;
        int type;
        int i = 0;
        int j = 2;
        String deviceType = "";
        while (datalength != data.length()) {
            length = Integer.parseInt(data.substring(i, i + j), 16);
            if (length == 0)
                break;
            type = Integer.parseInt(data.substring((i + j), i + j + 2), 16);

            switch (type) {
                case 22:
                    uuidofservice = data.substring((i + j + 2), i + j + (length * 2));
                    break;
            }
            if (!uuidofservice.equalsIgnoreCase(""))
                break;
            i = i + j + (length * 2);
            datalength = i;
        }
        if (!uuidofservice.equalsIgnoreCase("")) {
            String iBeaconData = uuidofservice.substring(uuidofservice.length() - 6, uuidofservice.length());
            String voltage = iBeaconData.substring(0, 4);
            String temperature = iBeaconData.substring(4, 6);
            short batteryVoltage = (short) Integer.parseInt(voltage, 16);
            short temp = (short) Integer.parseInt(temperature, 16);
            float bVoltage = batteryVoltage / 1000.0f;
            ufoDevice.setTemperature(temp + "");
            if(ufoDevice.getTemperature().equalsIgnoreCase("null") || ufoDevice.getTemperature() == null){
                ufoDevice.setTemperature(0 + "");
            }
            ufoDevice.setBatteryVoltage(String.format("%.1f", bVoltage));
            if(ufoDevice.getBatteryVoltage().equalsIgnoreCase("null") || ufoDevice.getBatteryVoltage() == null){
                ufoDevice.setTemperature(0 + "");
            }
        }
    }

    // Trim the space from the bytes.
    private byte[] trim(byte[] bytes) {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0) {
            --i;
        }
        return Arrays.copyOf(bytes, i + 1);
    }

    // If UUID contain - then replace it will blank space
    public boolean containsCaseInsensitive(String s, List<String> l) {
        for (String string : l) {

            string = string.replace("-", "");

            if (string.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }
}
