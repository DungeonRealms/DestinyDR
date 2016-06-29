package net.dungeonrealms.game.handlers;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Kieran Quigley (Proxying) on 19-Jun-16.
 */
public class ProtectionHandler implements GenericMechanic, Listener {

    private static ProtectionHandler instance = null;

    public static ProtectionHandler getInstance() {
        if (instance == null) {
            instance = new ProtectionHandler();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
    public void startInitialization() {
    }

    @Override
    public void stopInvocation() {

    }

    public void handleLogin(Player player) {
        long firstJoin = (long) DatabaseAPI.getInstance().getData(EnumData.FIRST_LOGIN, player.getUniqueId());
        if ((System.currentTimeMillis() - firstJoin) <= (24 * 3600000)) {
            if ((int) DatabaseAPI.getInstance().getData(EnumData.PLAYER_KILLS, player.getUniqueId()) == 0) {
                //Still under 24 hour newbie protection.
                long hours = (System.currentTimeMillis() - firstJoin) / 3600000;
                int remainingHours = 24 - (int) hours;
                player.sendMessage("");
                player.sendMessage(ChatColor.RED + "You have " + ChatColor.BOLD + remainingHours + "h " + ChatColor.RED + "left in your 'Newbie Protection'. After this time expires, you will lose items as you normally would when PK'd.");
                player.setMetadata("newbie_protection", new FixedMetadataValue(DungeonRealms.getInstance(), true));
            }
        }
    }

    public void removePlayerProtection(Player player) {
        if (player.hasMetadata("newbie_protection")) {
            player.removeMetadata("newbie_protection", DungeonRealms.getInstance());
        }
    }

    public boolean hasNewbieProtection(Player player) {
        return player.hasMetadata("newbie_protection");
    }
}
