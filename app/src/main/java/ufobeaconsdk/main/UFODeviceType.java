package ufobeaconsdk.main;

/**
 * Created by Dell on 10-04-2017.
 */

public enum UFODeviceType {
    IBEACON(0),
    EDDYSTONE(1);

    int deviceType;
    UFODeviceType(int deviceType){
        this.deviceType = deviceType;
    }

    public int getDeviceType() {
        return this.deviceType;
    }
}