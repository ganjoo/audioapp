package ufobeacon.main;


import ufobeaconsdk.main.UFODevice;

/**
 * Created by Himen on 06-02-2017.
 */

public enum SharedUFODevice {

    INSTANCE;

    UFODevice ufodevice;

    public UFODevice getUfodevice() {
        return ufodevice;
    }

    public void setUfodevice(UFODevice ufodevice) {
        this.ufodevice = ufodevice;
    }
}
