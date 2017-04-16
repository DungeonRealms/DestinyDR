package net.dungeonrealms.game.handler;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
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

//    public void handleLogin(Player player) {
//        PlayerWrapper
//        long firstJoin = (long) DatabaseAPI.getInstance().getData(EnumData.FIRST_LOGIN, player.getUniqueId());
//        if ((System.currentTimeMillis() - firstJoin) < (24 * 3600000)) {
//            if ((int) DatabaseAPI.getInstance().getData(EnumData.PLAYER_KILLS, player.getUniqueId()) == 0) {
//                Still under 24 hour newbie protection.
//                long hours = (System.currentTimeMillis() - firstJoin) / 3600000;
//                int remainingHours = 24 - (int) hours;
//                player.sendMessage("");
//                player.sendMessage(ChatColor.RED + "You have " + ChatColor.BOLD + remainingHours + "h " + ChatColor.RED + "left in your 'Newbie Protection'. After this time expires, you will lose items as you normally would when PK'd.");
//                player.setMetadata("newbie_protection", new FixedMetadataValue(DungeonRealms.getInstance(), true));
//            }
//        }
//    }

    public void removePlayerProtection(Player player) {
        if (player.hasMetadata("newbie_protection")) {
            player.removeMetadata("newbie_protection", DungeonRealms.getInstance());
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "You have forfeited your 'Newbie protection' by killing another Player.");
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 1F, 1F);
        }
    }

    public boolean hasNewbieProtection(Player player) {
        return player.hasMetadata("newbie_protection");
    }
}
