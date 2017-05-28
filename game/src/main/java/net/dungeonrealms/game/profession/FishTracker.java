package net.dungeonrealms.game.profession;

import lombok.Getter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.game.mastery.MetadataUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Getter
public class FishTracker extends HashMap<Integer, Integer> {

    private static int MAX_FLAGS = 10, TEMP_BAN_FLAGS = 40, MAX_WURST_FLAGS = 10;

    private int flagsThrown = 0;

    private int lastCatchSpeed;

    private int fishCaught = 0, wurstFlags = 0;

    private long sent = System.currentTimeMillis(), wurstLastSet;

    public int trackFishCatch(Player pl, int ticksCaughtIn) {
        fishCaught++;
        if (ticksCaughtIn != lastCatchSpeed && lastCatchSpeed + 1 != ticksCaughtIn && lastCatchSpeed - 1 != ticksCaughtIn)
            flagsThrown = 0;
        else {
            flagsThrown++;
            Bukkit.getLogger().info("Flags: " + flagsThrown);
        }

        lastCatchSpeed = ticksCaughtIn;


        if (sent <= System.currentTimeMillis()) {
            if (isAutoFisher()) {
                sent = System.currentTimeMillis() + 20_000;
                GameAPI.sendWarning(ChatColor.RED + pl.getName() + " has thrown " + getFlagsThrown() + " (" + wurstFlags + ") Auto Fishing flags with the delay of " + getLastCatchSpeed() + " ticks, caught " + getFishCaught() + " total fish.");
                MetadataUtils.Metadata.AUTO_FISHING.set(pl, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30));
            } else if (isSuspiciousAutoFisher()) {
                sent = System.currentTimeMillis() + 10_000;
                GameAPI.sendStaffMessage(PlayerRank.TRIALGM, ChatColor.RED + pl.getName() + " has thrown " + getFlagsThrown() + " (" + wurstFlags + ") Auto Fishing flags with the delay of " + getLastCatchSpeed() + " ticks, caught " + getFishCaught() + " total fish.", true);
                MetadataUtils.Metadata.AUTO_FISHING.set(pl, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60));
            }
        }
        return flagsThrown;
    }

    long worstTrapStart = 0;

    public void startWurstTrap() {
        this.worstTrapStart = System.currentTimeMillis();
    }

    public void trackWurstTrap(Player pl, long t) {
        if (worstTrapStart != 0 && t - worstTrapStart <= 100) {
            long time = t - worstTrapStart;
            this.wurstFlags++;
            wurstLastSet = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(15);
            Bukkit.getLogger().info("Accepted wurst trap for " + pl.getName() + " in " + time + "ms or " + time / 50 + " ticks later.");
        }
        this.worstTrapStart = t;
    }

    public boolean isSuspiciousAutoFisher() {
        return flagsThrown >= MAX_FLAGS;
    }

    public int getWurstFlags() {
        if (wurstLastSet <= System.currentTimeMillis())
            this.wurstFlags = 0;
        return wurstFlags;
    }

    public boolean isAutoFisher() {
        return flagsThrown >= TEMP_BAN_FLAGS || getWurstFlags() >= MAX_WURST_FLAGS;
    }
}
