package net.dungeonrealms.vgame.anticheat;

import net.dungeonrealms.DungeonRealms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by Matthew E on 10/29/2016 at 10:42 AM.
 */
public abstract class Watch {

    long checkTime;

    public Watch(long checkTime) {
        this.checkTime = checkTime;
        if (this.checkTime == 0L) {
            return;
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            Bukkit.getOnlinePlayers().forEach(player -> check(player));
        }, this.checkTime, this.checkTime);
    }

    public abstract void check(Player player);

    public long getCheckTime() {
        return checkTime;
    }
}
