package net.dungeonrealms.game.world.item;

import lombok.Getter;

@Getter
public class HitTracker {

    private long lastHit;
    private int hitCounter = 0;

    public int trackHit() {
        //Withing 5 ticks?
        if (System.currentTimeMillis() - this.lastHit <= 50 * 5)
            this.hitCounter++;
        else
            this.hitCounter = 1;

        this.lastHit = System.currentTimeMillis();
        return this.hitCounter;
    }
}
