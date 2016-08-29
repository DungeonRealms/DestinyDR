package net.dungeonrealms.game.tab;

import lombok.Data;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/4/2016
 */

@Data
public class Transition {

    private int cursor, size;
    private long lastTransitionTime;

    public int next() {
        cursor++;

        if (cursor >= size || size <= 0)
            cursor = 0;

        updateTransitionTime();
        return cursor;
    }


    public void updateTransitionTime() {
        this.lastTransitionTime = System.currentTimeMillis();
    }

}
