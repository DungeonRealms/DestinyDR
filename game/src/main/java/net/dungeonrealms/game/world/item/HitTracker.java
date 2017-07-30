package net.dungeonrealms.game.world.item;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

@Getter
public class HitTracker extends HashMap<UUID, HitTracker.TargetHitTracker> {

    public int trackHit(Player damaged) {
        TargetHitTracker tracker = computeIfAbsent(damaged.getUniqueId(), m -> new TargetHitTracker(120));
        return tracker.trackHit();
    }

    @Getter
    public static class TargetHitTracker {

        int delay;

        public TargetHitTracker(int delay) {
            this.delay = delay;
        }

        private long lastHit;
        private int hitCounter = 0;

        public int trackHit() {
            //Withing 5 ticks?
            if (System.currentTimeMillis() - this.lastHit <= delay)
                this.hitCounter++;
            else
                this.hitCounter = 1;

            this.lastHit = System.currentTimeMillis();
            return this.hitCounter;
        }
    }
}
