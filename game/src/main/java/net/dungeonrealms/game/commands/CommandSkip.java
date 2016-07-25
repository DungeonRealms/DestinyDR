package net.dungeonrealms.game.commands;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.player.chat.Chat;
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
            return true;
        }

        Player player = (Player) sender;

        if (GameAPI.getRegionName(player.getLocation()).equalsIgnoreCase("tutorial_island")) {
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.RED
                    + "If you skip this tutorial you will not recieve " + ChatColor.UNDERLINE + "ANY"
                    + ChatColor.RED + " of the item rewards for completing it.");
            player.sendMessage(ChatColor.GRAY + "If you're sure you still want to skip it, type '" + ChatColor.GREEN
                    + ChatColor.BOLD + "Y" + ChatColor.GRAY + "' to finish the tutorial. Otherwise, just type '"
                    + ChatColor.RED + "cancel" + ChatColor.GRAY + "' to continue with the tutorial.");

            Chat.getInstance().listenForMessage(player, chat -> {
                if (chat.getMessage().equalsIgnoreCase("y")) {
                    player.teleport(new Location(Bukkit.getWorlds().get(0), -378, 85, 362));
                    ItemManager.giveStarter(player);
                }
            }, p -> p.sendMessage(ChatColor.RED + "Tutorial Skip - " + ChatColor.BOLD + "CANCELLED"));
        } else {
            player.sendMessage(ChatColor.RED + "You are not on the tutorial island.");
        }

        return true;
    }
}
