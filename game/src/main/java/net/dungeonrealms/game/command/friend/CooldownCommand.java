package net.dungeonrealms.game.command.friend;

import net.dungeonrealms.DungeonRealms;
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
        if (isOnCooldown(player)) {
            player.sendMessage(ChatColor.RED + "Please wait before using another friend command.");
            return true;
        }
        return false;
    }

    default boolean isOnCooldown(Player player) {
        return player.hasMetadata(getName() + "cmd_cooldown") && player.getMetadata(getName() + "cmd_cooldown").get(0).asLong() > System.currentTimeMillis();
    }
}
