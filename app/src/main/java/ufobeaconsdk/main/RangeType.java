package ufobeaconsdk.main;

/**
 * Created by Dell on 03-04-2017.
 */

public enum RangeType {
    IN_RANGE(0),
    OUT_RANGE(1);

    int range;
    RangeType(int range){
        this.range = range;
    }

    public int getRange() {
        return this.range;
    }
}
