package net.dungeonrealms.mongo;

/**
 * Created by Nick on 8/30/2015.
 */
public enum EnumOperators {

    $INC(0, "$inc"),
    $MUL(1, "$mul"),
    $PUSH(2, "$push"),
    $SET(3, "$set"),
    ;

    private int id;
    private String UO;

    EnumOperators(int id, String UO) {
        this.id = id;
        this.UO = UO;
    }

    public int getId() {
        return id;
    }

    public String getUO() {
        return UO;
    }
}
