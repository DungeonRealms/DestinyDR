package net.dungeonrealms.common.game.database.data;

/**
 * Created by Nick on 8/30/2015.
 */
public enum EnumOperators {

    //Increase whats current in database.
    $INC(0, "$inc"),

    //Multiply by current in database.
    $MUL(1, "$mul"),

    //Push into an arraylist
    $PUSH(2, "$push"),

    //Set a field.
    $SET(3, "$set"),

    //Remove from an ArrayList
    $PULL(4, "$pull");

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
