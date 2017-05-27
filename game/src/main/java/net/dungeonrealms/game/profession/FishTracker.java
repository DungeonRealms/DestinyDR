package net.dungeonrealms.game.profession;

import lombok.Getter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;

@Getter
public class FishTracker extends HashMap<Integer, Integer> {

    private static int MAX_FLAGS = 4, TEMP_BAN_FLAGS = 40;
    private int flagsThrown = 0;
    private int lastCatchSpeed;

    private int fishCaught = 0;

    private long sent = System.currentTimeMillis();

    public int trackFishCatch(Player pl, int ticksCaughtIn) {
        fishCaught++;
//        Bukkit.getLogger().info("Caught in " + ticksCaughtIn + " Last catch: " + lastCatchSpeed);
        if (ticksCaughtIn != lastCatchSpeed && lastCatchSpeed + 1 != ticksCaughtIn && lastCatchSpeed - 1 != ticksCaughtIn)
            flagsThrown = 0;
        else {
            flagsThrown++;
            Bukkit.getLogger().info("Flags: " + flagsThrown);
        }

        lastCatchSpeed = ticksCaughtIn;


        if (sent <= System.currentTimeMillis()) {
            if (isSuspiciousAutoFisher()) {
                sent = System.currentTimeMillis() + 10_000;
                GameAPI.sendStaffMessage(PlayerRank.TRIALGM, ChatColor.RED + pl.getName() + " has thrown " + getFlagsThrown() + " Auto Fishing flags with the delay of " + getLastCatchSpeed() + " ticks, caught " + getFishCaught() + " total fish.", true);
            } else if (isAutoFisher()) {
                sent = System.currentTimeMillis() + 20_000;
                GameAPI.sendWarning(ChatColor.RED + pl.getName() + " has thrown " + getFlagsThrown() + " Auto Fishing flags with the delay of " + getLastCatchSpeed() + " ticks, caught " + getFishCaught() + " total fish.");
            }
        }
        return flagsThrown;
    }

    public boolean isSuspiciousAutoFisher() {
        return flagsThrown >= MAX_FLAGS;
    }

    public boolean isAutoFisher() {
        return flagsThrown >= TEMP_BAN_FLAGS;
    }
}
