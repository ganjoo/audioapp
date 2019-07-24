package ufobeaconsdk.main;


import java.util.UUID;

import ufobeaconsdk.callback.OnBeaconSuccessListener;
import ufobeaconsdk.callback.OnFailureListener;
import ufobeaconsdk.callback.OnReadSuccessListener;

/**
 * Created by Himen on 06-02-2017.
 */

public class RequestModel {

    private UUID serviceUUID;
    private UUID characteristicUUID;
    private OnReadSuccessListener onReadSuccessListener;
    private OnBeaconSuccessListener onBeaconSuccessListener;
    private OnFailureListener onFailureListener;
    private byte[] data;
    private int requestType;

    protected int getRequestType() {
        return requestType;
    }

    protected void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    protected byte[] getData() {
        return data;
    }

    protected void setData(byte[] data) {
        this.data = data;
    }

    protected OnBeaconSuccessListener getOnBeaconSuccessListener() {
        return onBeaconSuccessListener;
    }

    protected void setOnBeaconSuccessListener(OnBeaconSuccessListener onBeaconSuccessListener) {
        this.onBeaconSuccessListener = onBeaconSuccessListener;
    }

    protected UUID getServiceUUID() {
        return serviceUUID;
    }

    protected void setServiceUUID(UUID serviceUUID) {
        this.serviceUUID = serviceUUID;
    }

    protected UUID getCharacteristicUUID() {
        return characteristicUUID;
    }

    protected void setCharacteristicUUID(UUID characteristicUUID) {
        this.characteristicUUID = characteristicUUID;
    }

    protected OnReadSuccessListener getOnReadSuccessListener() {
        return onReadSuccessListener;
    }

    protected void setOnReadSuccessListener(OnReadSuccessListener onReadSuccessListener) {
        this.onReadSuccessListener = onReadSuccessListener;
    }

    protected OnFailureListener getOnFailureListener() {
        return onFailureListener;
    }

    protected void setOnFailureListener(OnFailureListener onFailureListener) {
        this.onFailureListener = onFailureListener;
    }
}
