package ufobeaconsdk.main;

/**
 * Created by Dell on 10-04-2017.
 */

public enum EddystoneType {

    EDDYSTONE_UID((byte)0x00),
    EDDYSTONE_URL((byte)0x10),
    EDDYSTONE_TLM((byte)0x20),
    EDDYSTONE_UID_TLM((byte)0xA0),
    EDDYSTONE_URL_TLM((byte)0xB0),
    EDDYSTONE_UID_URL_TLM((byte)0xC0);
    byte eddyStoneType;

    EddystoneType(byte eddyStoneType){
        this.eddyStoneType = eddyStoneType;
    }

    public byte getEddyStoneType() {
        return this.eddyStoneType;
    }
}
