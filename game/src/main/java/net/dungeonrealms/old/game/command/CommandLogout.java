package net.dungeonrealms.old.game.command;

import net.dungeonrealms.old.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.old.game.mastery.GamePlayer;
import net.dungeonrealms.old.game.player.combat.CombatLog;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Chase on Nov 18, 2015
 */
public class CommandLogout extends BaseCommand {
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

                GamePlayer gp = GameAPI.getGamePlayer(player);

                gp.setAbleToSuicide(false);
                gp.setAbleToDrop(false);
                player.setMetadata("sharding", new FixedMetadataValue(DungeonRealms.getInstance(), true));

                Location startingLocation = player.getLocation();
                if (GameAPI.isInSafeRegion(startingLocation)) {
                    BungeeUtils.sendNetworkMessage("BungeeCord", "KickPlayer", player.getName(), org.bukkit.ChatColor.RED + "You were logged out");
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
                        player.removeMetadata("sharding", DungeonRealms.getInstance());
                        gp.setAbleToSuicide(true);
                        gp.setAbleToDrop(true);
                        return;
                    }
                    player.sendMessage(ChatColor.RED + "Logging out in ... " + ChatColor.BOLD + taskTimer[0] + "s");
                    taskTimer[0]--;
                    if (taskTimer[0] == 0) {
                        BungeeUtils.sendNetworkMessage("BungeeCord", "KickPlayer", player.getName(), org.bukkit.ChatColor.RED + "You were logged out");
                    }
                }, 0, 20L);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Bukkit.getScheduler().cancelTask(taskID), 6 * 20L);
            }
        }
        return true;
    }
}
