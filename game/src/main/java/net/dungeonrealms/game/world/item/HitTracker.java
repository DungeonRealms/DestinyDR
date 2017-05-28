package net.dungeonrealms.game.world.item;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

@Getter
public class HitTracker extends HashMap<UUID, HitTracker.TargetHitTracker> {

    public int trackHit(Player damaged) {
        TargetHitTracker tracker = computeIfAbsent(damaged.getUniqueId(), m -> new TargetHitTracker());
        return tracker.trackHit();
    }

    @Getter
    class TargetHitTracker {

        private long lastHit;
        private int hitCounter = 0;

        public int trackHit() {
            //Withing 5 ticks?
            if (System.currentTimeMillis() - this.lastHit <= 120)
                this.hitCounter++;
            else
                this.hitCounter = 1;

            this.lastHit = System.currentTimeMillis();
            return this.hitCounter;
        }
    }
}
