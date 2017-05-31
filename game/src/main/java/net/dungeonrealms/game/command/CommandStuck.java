package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.combat.CombatLog;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 11/8/2015.
 */
public class CommandStuck extends BaseCommand {
    public CommandStuck(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof ConsoleCommandSender) return false;

        Player player = (Player) sender;

        if (CombatLog.isInCombat(player)) {
            player.sendMessage(ChatColor.RED + "You cannot use this while in combat!");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "Checking your status ...");

        if (!PlayerWrapper.getPlayerWrappers().containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Seems one of the issues is we failed to retrieve your data from the database! Please rejoin!");
            return true;
        }

        if (player.getLocation().getBlock() != null && !player.getLocation().getBlock().getType().equals(Material.AIR)
                && !player.getLocation().getBlock().getType().equals(Material.WATER) &&
                !player.getLocation().getBlock().getType().equals(Material.STATIONARY_LAVA) &&
                !player.getLocation().getBlock().getType().equals(Material.GRASS) &&
                !player.getLocation().getBlock().getType().equals(Material.LONG_GRASS)
                ) {
            player.sendMessage(ChatColor.GREEN + "It appears that you're stuck inside of a block?");
            Location loc = player.getLocation();
            while (!loc.getBlock().getType().equals(Material.AIR)) {
                loc.add(0, 2, 0);
            }
            GameAPI.teleport(player, loc);
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "You seem to be fine.");

        return true;
    }
}
