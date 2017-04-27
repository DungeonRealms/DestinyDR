package net.dungeonrealms.game.command.friend;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.util.TimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.concurrent.TimeUnit;

public interface CooldownCommand {

    public String getName();

    default long getCooldown() {
        return TimeUnit.SECONDS.toMillis(5);
    }

    default void triggerCooldown(Player player) {
        player.setMetadata(getName() + "cmd_cooldown", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis() + getCooldown()));
    }

    default boolean checkCooldown(Player player) {

        String cooldown = getCooldownString(player);

        if (cooldown != null) {
            player.sendMessage(ChatColor.RED + "Please wait " + cooldown +  " before using this command again.");
            return true;
        }
        return false;
    }

    default String getCooldownString(Player player) {
        Long cooldown = player.hasMetadata(getName() + "cmd_cooldown") ? player.getMetadata(getName() + "cmd_cooldown").get(0).asLong() : null;

        if (cooldown == null) return null;

        if (System.currentTimeMillis() > cooldown) return null;

        long timeLeft = cooldown - System.currentTimeMillis();

        return TimeUtil.formatDifference(timeLeft / 1000L);
    }

    default boolean isOnCooldown(Player player) {
        return player.hasMetadata(getName() + "cmd_cooldown") && player.getMetadata(getName() + "cmd_cooldown").get(0).asLong() > System.currentTimeMillis();
    }
}
