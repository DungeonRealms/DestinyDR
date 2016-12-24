package net.dungeonrealms.common.game.util;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/13/2016
 */

public class Cooldown {

    private static Map<UUID, Cooldown> cooldowns = new WeakHashMap<>();

    private long endTime;

    public Cooldown(long cooldownInMilliseconds) {
        this.endTime = System.currentTimeMillis() + (cooldownInMilliseconds);
    }

    public boolean isOver() {
        return this.endTime < System.currentTimeMillis();
    }

    public int getTimeLeft() {
        return (int) (this.endTime - System.currentTimeMillis());
    }


    public static void addCooldown(UUID uuid, long cooldown) {
        cooldowns.put(uuid, new Cooldown(cooldown));
    }

    public static boolean hasCooldown(UUID uuid) {
        return cooldowns.containsKey(uuid) && !cooldowns.get(uuid).isOver();
    }


    public static Cooldown getCooldown(UUID uuid) {
        return cooldowns.get(uuid);
    }

}
