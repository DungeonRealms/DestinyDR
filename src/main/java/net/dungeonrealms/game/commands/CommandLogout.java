package net.dungeonrealms.game.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.network.NetworkAPI;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.md_5.bungee.api.ChatColor;

/**
 * Created by Chase on Nov 18, 2015
 */
public class CommandLogout extends BasicCommand {
    public CommandLogout(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof Player) {
            Player player = (Player) s;

            if (DatabaseAPI.getInstance().PLAYERS.containsKey(player.getUniqueId())) {
                if (CombatLog.isInCombat(player)) {
                    player.sendMessage(ChatColor.RED + "You can not use /logout while in combat.");
                    return true;
                }

                Location startingLocation = player.getLocation();
                if (API.isInSafeRegion(startingLocation)) {
                    NetworkAPI.getInstance().sendNetworkMessage("BungeeCord", "KickPlayer", player.getName(), org.bukkit.ChatColor.RED + "You were logged out");
                    return true;
                }

                player.sendMessage(ChatColor.RED + "You will be " + ChatColor.BOLD + "LOGGED OUT" + ChatColor.RED + " of the game world shortly.");
                final int[] taskTimer = {5};
                int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                    if (taskTimer[0] <= 0) {
                        return;
                    }
                    if (startingLocation.distanceSquared(player.getLocation()) >= 2.0D || CombatLog.isInCombat(player)) {
                        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Logout - CANCELLED");
                        return;
                    }
                    player.sendMessage(ChatColor.RED + "Logging out in ... " + ChatColor.BOLD + taskTimer[0] + "s");
                    taskTimer[0]--;
                    if (taskTimer[0] == 0) {
                        NetworkAPI.getInstance().sendNetworkMessage("BungeeCord", "KickPlayer", player.getName(), org.bukkit.ChatColor.RED + "You were logged out");
                    }
                }, 0, 20L);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Bukkit.getScheduler().cancelTask(taskID), 6 * 20L);
            }
        }
        return true;
    }
}
