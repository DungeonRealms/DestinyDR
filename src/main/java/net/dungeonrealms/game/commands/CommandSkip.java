package net.dungeonrealms.game.commands;

import net.dungeonrealms.API;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.handlers.TutorialIslandHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 12/2/2015.
 */
public class CommandSkip extends BasicCommand {

    public CommandSkip(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (API.getRegionName(player.getLocation()).equalsIgnoreCase("tutorial_island")) {
            TutorialIslandHandler.getInstance().getSkipList().add(player.getUniqueId());
            if (!TutorialIslandHandler.getInstance().getSkipList().contains(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.RED
                        + "If you skip this tutorial you will not recieve " + ChatColor.UNDERLINE + "ANY"
                        + ChatColor.RED + " of the item rewards for completing it.");
                player.sendMessage(ChatColor.GRAY + "If you're sure you still want to skip it, type '" + ChatColor.GREEN
                        + ChatColor.BOLD + "Y" + ChatColor.GRAY + "' to finish the tutorial. Otherwise, just type '"
                        + ChatColor.RED + "cancel" + ChatColor.GRAY + "' to continue with the tutorial.");
            }
        }
        else {
            player.sendMessage(ChatColor.RED + "You are not on the tutorial island.");
        }


        return false;
    }
}
