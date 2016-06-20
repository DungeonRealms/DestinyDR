package net.dungeonrealms.game.handlers;

import lombok.Getter;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

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

    @Getter
    private List<String> protected_Players = new ArrayList<>();

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
            //Still under 24 hour newbie protection.
            long hours = (System.currentTimeMillis() - firstJoin) / 3600000;
            int remainingHours = 24 - (int) hours;
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "You have " + ChatColor.BOLD + remainingHours + "h " + ChatColor.RED + "left in your 'Newbie Protection'. After this time expires, you will lose items as you normally would when PK'd.");
            //TODO: Way to check if they have PVP'd.
            //TODO: Something inside damage check to see if they are inside this (below) list. If so, cancel their death damage.
            protected_Players.add(player.getName());
        }
    }

    public void removePlayerProtection(Player player) {
        protected_Players.remove(player.getName());
        //TODO: Database update to show they have removed their protection early.
    }
}
