package net.dungeonrealms.commands;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.mongo.DatabaseAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 11/8/2015.
 */
public class CommandStuck extends BasicCommand {
    public CommandStuck(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof ConsoleCommandSender) return false;

        Player player = (Player) sender;

        sender.sendMessage(ChatColor.GREEN + "Checking your status ...");

        if (!DatabaseAPI.getInstance().PLAYERS.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Seems one of the issues is we failed to retrieve your data from the database! Please rejoin!");
            return true;
        }

        if (player.getLocation().getBlock() != null && !player.getLocation().getBlock().getType().equals(Material.AIR)
                && !player.getLocation().getBlock().getType().equals(Material.WATER) &&
                !player.getLocation().getBlock().getType().equals(Material.STATIONARY_LAVA) &&
                !player.getLocation().getBlock().getType().equals(Material.GRASS) &&
                !player.getLocation().getBlock().getType().equals(Material.LONG_GRASS)

                ){
            player.sendMessage(ChatColor.GREEN + "It appears that you're stuck inside of a blocK?");
            player.teleport(player.getLocation().add(0, 2, 0));
        }

        player.sendMessage(ChatColor.GREEN + "You seem to be fine, maybe your issue is mental?");

        return true;
    }
}
