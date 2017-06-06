package net.dungeonrealms.game.mechanic.dot;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DotManager implements GenericMechanic {

    private static ConcurrentHashMap<Entity, DamageOverTime> currentDots = new ConcurrentHashMap<>();

    @Override
    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for(Map.Entry<Entity, DamageOverTime> set : currentDots.entrySet()) {
                Entity entity = set.getKey();
                DamageOverTime dot = set.getValue();
                if(dot == null || entity.isDead()) {
                    currentDots.remove(entity);
                    continue;
                }
                if(dot.isFinished()) {
                    dot.handleFinish();
                    currentDots.remove(entity);
                }
                dot.tick();
            }
        }, 5,5);
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
    public void stopInvocation() {
        currentDots.clear();
    }
}
