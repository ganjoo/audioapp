package ufobeaconsdk.main;

import android.content.Context;
import android.provider.Settings;
import android.util.Patterns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dell on 01-02-2017.
 */

public class Utils {

    public static final UUID BEACON_UUID = UUID.fromString("4B54504C-5546-4F00-0000-000000000001");
    public static final UUID IBEACON_SERVICE_UUID = UUID.fromString("00031234-0000-1000-8000-00805F9B0131");
    public static UUID CUSTOMER_UUID = null;
    public static final UUID PROXIMITY_UUID = UUID.fromString("00031235-0100-0800-0008-05F9B34F0131");
    public static final UUID MAJOR = UUID.fromString("00031235-0100-0800-0008-05F9B34F0132");
    public static final UUID MINOR = UUID.fromString("00031235-0100-0800-0008-05F9B34F0133");
    public static final UUID ADVERTISEMENT_INTERVAL = UUID.fromString("00031235-0100-0800-0008-05F9B34F0134");
    public static final UUID TRANSMISSION_POWER = UUID.fromString("00031235-0100-0800-0008-05F9B34F0135");
    public static final UUID PASSWORD = UUID.fromString("00031235-0100-0800-0008-05F9B34F0136");
    public static final UUID EDDYSTONE_URI_DATA = UUID.fromString("ee0c2084-8786-40ba-ab96-99b91ac981d8");
    public static final UUID EDDYSTONE_FLAGS = UUID.fromString("ee0c2085-8786-40ba-ab96-99b91ac981d8");
    public static final UUID EDDYSTONE_ADVERTISEMENT_INTERVAL = UUID.fromString("ee0c2086-8786-40ba-ab96-99b91ac981d8");
    public static final UUID EDDYSTONE_TRANSMISSION_POWER = UUID.fromString("ee0c2087-8786-40ba-ab96-99b91ac981d8");
    public static final UUID EDDYSTONE_BEACON_PERIOD = UUID.fromString("ee0c2088-8786-40ba-ab96-99b91ac981d8");
    public static final UUID EDDYSTONE_RESET = UUID.fromString("ee0c2089-8786-40ba-ab96-99b91ac981d8");
    public static final UUID EDDYSTONE_SERVICE_UUID = UUID.fromString("EE0C2080-8786-40BA-AB96-99B91AC981D8");

    public static final int ERROR_CODE_BLUETOOTH_IS_OFF = 1001;
    public static final int ERROR_CODE_LOCATION_IS_OFF = 1002;
    public static final int ERROR_CODE_TRY_CATCH = 1003;
    public static final int ERROR_CODE_CONTEXT_NULL = 1004;
    public static final int ERROR_CODE_SCANING_UUID_IS_INVALID = 1005;
    public static final int ERROR_CODE_SCANING_ALREADY_RUNNING = 1006;
    public static final int ERROR_CODE_DEVICE_FAIL_TO_CONNECT = 1007;
    public static final int ERROR_CODE_DEVICE_NOT_CONNECTED = 1008;
    public static final int ERROR_CODE_DEVICE_ALREADY_CONNECTED = 1009;
    public static final int ERROR_CODE_DEVICE_ALREADY_DISCONNECTED = 1010;
    public static final int ERROR_CODE_DEVICE_DISCONNECTED = 1011;
    public static final int ERROR_CODE_FAIL_TO_READ = 1012;
    public static final int ERROR_CODE_GATT_NOT_CONNECTED = 1013;
    public static final int ERROR_CODE_CHARACTERISTIC_NOT_READABLE = 1014;
    public static final int ERROR_CODE_CHARACTERISTIC_NOT_WRITABLE = 1015;
    public static final int ERROR_CODE_FAIL_TO_WRITE = 1016;
    public static final int ERROR_CODE_INVALID_PASSWORD = 1017;
    public static final int ERROR_CODE_MAJOR_IS_INVALID = 1018;
    public static final int ERROR_CODE_MINOR_IS_INVALID = 1019;
    public static final int ERROR_CODE_INVALID_URL = 1020;
    public static final int ERROR_CODE_INVALID_NAMESPACEID = 1021;
    public static final int ERROR_CODE_INVALID_INSTANCEID = 1022;
    public static final int ERROR_CODE_INVALID_LENGTH = 1023;
    public static final int ERROR_CODE_INVALID_ADVERTISE_INTERVAL = 1024;

    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String checkHex = "^[0-9A-Fa-f]+$";

    public static final int REQUEST_TYPE_READ = 1;
    public static final int REQUEST_TYPE_WRITE = 2;
    // Error message strings

    public static final String message_bluetoothOff = "Bluetooth is off";
    public static final String message_locationServiceOff = "Location service is off";
    public static final String message_invalidUUID = "uuid is wrong or invalid";
    public static final String message_scanningRunning = "Scanning already running";
    public static final String message_connectionFail = "Failed to connect with device";
    public static final String message_alreadyConnected = "Already connected with device";
    public static final String message_alreadyDisconnected = "Already disconnected with device";
    public static final String message_deviceNotConnected = "Device not connected";
    public static final String message_deviceDisconnected = "Device disconnected";
    public static final String message_failToReadData = "Failed to read data";
    public static final String message_gattNotConnected = "BluetoothGatt Not Connected";
    public static final String message_characteristicNotReadable = "Characteristic is not readable";
    public static final String message_characteristicNotWritable = "Characteristic is not writable";
    public static final String message_failToWriteData = "Failed to write data";
    public static final String message_invalidpassword = "Invalid Password";
    public static final String message_invalidMajor = "Invalid Major";
    public static final String message_invalidMinor = "Invalid Minor";
    public static final String message_invalidURL = "Invalid URL";
    public static final String message_invalidNamespaceId = "Invalid Namespace Id";
    public static final String message_invalidInstanceId = "Invalid Instance Id";
    public static final String message_invalidLength = "Invalid Length";
    public static final String message_contextNull = "Context should not be null";
    public static final String message_invalid_advertise = "Advertise interval must be between 100 to 1000";


    public static byte[] previousTlmService = null;
    //    public static long startedtime;
    public static boolean tlmDataChanging;
    private static ArrayList<String> arrListOfSuffix;
    private static ArrayList<Byte> arrListOfSuffixByte;

    public final byte HTTP_WWW_SCHEMA = 0x00;
    public final byte HTTPS_WWW_SCHEMA = 0x01;
    public final byte HTTP_SCHEMA = 0x02;
    public final byte HTTPS_SCHEMA = 0x03;

    // Check for location service is enable or not. It is required if android os is above 6.0
    protected static boolean initLocation(Context context) {
        String locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (locationProviders == null || locationProviders.equals("")) {
            return false;
        }
        return true;
    }


    // Check is UUID is valid or not
    protected static boolean isValidUUID(String uuid) {

        String uuidwithoutdes = uuid.replaceAll("-", "");

        if (uuidwithoutdes.matches(checkHex)) {
            return true;
        }

        return false;
    }

    protected static boolean isTlmDataChanging() {
        return tlmDataChanging;
    }

    protected static void setTlmDataChanging(boolean tlmDataChanging) {
        Utils.tlmDataChanging = tlmDataChanging;
    }

    protected Utils() {
    }

    // Function is used to convert the hex string to byte array.
    protected static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // Function is used to convert the byte array data into hex string.
    protected static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    // It convert hex string into decimal number.
    protected static int hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16 * val + d;
        }
        return val;
    }

    // It is used to check the uri is having a valid pattern or not.
    protected static boolean validateByRegEx(String uri) {
        // Pattern p = Pattern.compile("^(http://|https://)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}.?([a-z]+)?$");         Matcher m;
        Pattern p = Patterns.WEB_URL;
        Matcher m = p.matcher(uri.toLowerCase());
        boolean matches = m.matches();
        if (matches) {
            return true;
        } else {
            return false;
        }

    }

    // Fill the suffix value of the uri into hashtable.
    protected static void fillSuffixHasTable() {
        arrListOfSuffix = new ArrayList<>();
        arrListOfSuffixByte = new ArrayList<>();
        arrListOfSuffix.add(".com/");
        arrListOfSuffix.add(".org/");
        arrListOfSuffix.add(".edu/");
        arrListOfSuffix.add(".net/");
        arrListOfSuffix.add(".info/");
        arrListOfSuffix.add(".biz/");
        arrListOfSuffix.add(".gov/");
        arrListOfSuffix.add(".com");
        arrListOfSuffix.add(".org");
        arrListOfSuffix.add(".edu");
        arrListOfSuffix.add(".net");
        arrListOfSuffix.add(".info");
        arrListOfSuffix.add(".biz");
        arrListOfSuffix.add(".gov");
        arrListOfSuffixByte.add((byte) 0x00);
        arrListOfSuffixByte.add((byte) 0x01);
        arrListOfSuffixByte.add((byte) 0x02);
        arrListOfSuffixByte.add((byte) 0x03);
        arrListOfSuffixByte.add((byte) 0x04);
        arrListOfSuffixByte.add((byte) 0x05);
        arrListOfSuffixByte.add((byte) 0x06);
        arrListOfSuffixByte.add((byte) 0x07);
        arrListOfSuffixByte.add((byte) 0x08);
        arrListOfSuffixByte.add((byte) 0x09);
        arrListOfSuffixByte.add((byte) 0x0a);
        arrListOfSuffixByte.add((byte) 0x0b);
        arrListOfSuffixByte.add((byte) 0x0c);
        arrListOfSuffixByte.add((byte) 0x0d);
    }


    // It generates the byte array from the uri.
    protected static byte[] generateByteArraty(String uri) {

        fillSuffixHasTable();
        String urlString = null;


        if (uri.startsWith("http://www")) {
            urlString = uri.substring(uri.indexOf("http://www") + 11, uri.length());
        } else if (uri.startsWith("https://www")) {
            urlString = uri.substring(uri.indexOf("https://www") + 12, uri.length());
        } else if (uri.startsWith("http://")) {
            urlString = uri.substring(uri.indexOf("http://") + 7, uri.length());
        } else if (uri.startsWith("https://")) {
            urlString = uri.substring(uri.indexOf("https://") + 8, uri.length());
        } else if (uri.startsWith("www")) {
            urlString = uri.substring(uri.indexOf("www") + 4, uri.length());
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean isSuffixExists = false;
        byte value = 0;
        String suffixKey = null;
        for (int i = 0; i < arrListOfSuffix.size(); i++) {
            if (urlString.contains(arrListOfSuffix.get(i))) {
                suffixKey = arrListOfSuffix.get(i);
                value = arrListOfSuffixByte.get(i);
                isSuffixExists = true;
                break;
            }
        }

        String appendURL = "";
        String endURL = "";
        if (isSuffixExists) {
            appendURL = urlString.substring(0, urlString.indexOf("."));
            try {
                os.write(appendURL.getBytes());
                os.write(value);
                if (!endURL.equalsIgnoreCase("")) {
                    os.write(endURL.getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            appendURL = urlString;
            try {
                os.write(appendURL.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        os
//
//
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        if (uri != null && uri.length() > 0) {
//            String[] splitString = uri.split("\\.");
//            for (int i = 0; i < splitString.length; i++) {
//                String mainString;
//                int position = uri.indexOf(splitString[i]);
//                if (position > 0) {
//                    mainString = uri.substring(position - 1, position + splitString[i].length());
//                } else {
//                    mainString = uri.substring(position, position + splitString[i].length());
//                }
//                System.out.println("From HashTable:" + mainString);
//                if (mainString.equalsIgnoreCase("www"))
//                    continue;
//                if (!suffixHeshTable.containsKey(mainString)) {
//                    try {
//                        if (mainString.contains("/")) {
//
//                        } else {
//
//                            if (!TextUtils.isEmpty(mainString) && mainString.startsWith("."))
//                                mainString = mainString.substring(1);
//                            os.write(mainString.getBytes());
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    os.write(suffixHeshTable.get(mainString));
//                }
//            }
//        }
        byte[] mByte = os.toByteArray();
        return mByte;
    }

    protected static int transmitPowerInDBM(int txDecimal) {

        int txPower = -99;

        switch (txDecimal) {
            case 164:  // A4
                txPower = -18;
                break;
            case 172:  // AC
                txPower = -12;
                break;
            case 184:  // B8
                txPower = -6;
                break;
            case 188:  // BC
                txPower = -3;
                break;
            case 189:  // BD
                txPower = -2;
                break;
            case 190:  // BE
                txPower = -1;
                break;
            case 191:  // BF
                txPower = 0;
                break;
            case 196:  // C4
                txPower = 3;
                break;

        }
        return txPower;
    }

    protected static int rssiAt1MeterforiBeacon(int txDecimal) {

        int rssi = -1;

        switch (txDecimal) {
            case 164:  // A4
                rssi = -92;
                break;
            case 172:  // AC
                rssi = -84;
                break;
            case 184:  // B8
                rssi = -72;
                break;
            case 188:  // BC
                rssi = -68;
                break;
            case 189:  // BD
                rssi = -67;
                break;
            case 190:  // BE
                rssi = -66;
                break;
            case 191:  // BF
                rssi = -65;
                break;
            case 196:  // C4
                rssi = -60;
                break;

        }
        return rssi;
    }

    protected static int eddystoneTransmitPowerInDBM(int txDecimal) {

        int txPower = -99;

        switch (txDecimal) {
            case 205:  // CD
                txPower = -51;
                break;
            case 223:  // DF
                txPower = -33;
                break;
            case 232:  // E8
                txPower = -24;
                break;
            case 235:  // EB
                txPower = -21;
                break;
        }
        return txPower;
    }

    protected static int rssiAt1MeterforEddystone(int txDecimal) {

        int txPower = -1;
        switch (txDecimal) {
            case 205:  // CD
                txPower = -91;
                break;
            case 223:  // DF
                txPower = -73;
                break;
            case 232:  // E8
                txPower = -64;
                break;
            case 235:  // EB
                txPower = -61;
                break;
        }
        return txPower;
    }

    protected static double calculateAccuracyFromRSSI(int txPower, double rssi) {

        double value = 0;

        if (rssi == 0) {
            value = -1.0; // if we cannot determine accuracy, return -1.
        }
        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            value = Math.pow(ratio, 10);
        } else {
            //double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            double accuracy = (0.42093) * Math.pow(ratio, 6.9476) + 0.54992; //nexus 5
            //double accuracy =  (0.1862616782)*Math.pow(ratio,8.235367435) + -0.45324519; //Moto pro
            value = accuracy;
        }

        if (Double.isInfinite(value))
            return value;


        DecimalFormat df = new DecimalFormat("#.##");
        value = Double.valueOf(df.format(value));
        return value;
    }

    protected static String getDistanceInString(double accuracy) {
        if (accuracy < 0) {
            return "Unknown";
        } else if (accuracy <= 1.0) {
            return "Immediate";
        } else if (accuracy <= 4.0) {
            return "Near";
        } else {
            return "Far";
        }
    }
}
