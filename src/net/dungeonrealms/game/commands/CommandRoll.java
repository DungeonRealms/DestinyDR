package net.dungeonrealms.game.commands;

import net.dungeonrealms.API;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

/**
 * Created by Kieran on 11/9/2015.
 */
public class CommandRoll extends BasicCommand {

    public CommandRoll(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command!");
            return false;
        }
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Incorrect syntax. /Roll <1-10000>");
            return false;
        }

        try {
            int max = Integer.parseInt(args[0]);
            if (max < 1 || max > 10000) {
                sender.sendMessage(ChatColor.RED + "Incorrect syntax. /Roll <1-10000>");
                return false;
            }
            Player player = (Player) sender;

            int roll = new Random().nextInt(max) + 1;

            API.getNearbyPlayers(player.getLocation(), 20).stream().forEach(player1 -> player1.sendMessage(ChatColor.AQUA.toString() + ChatColor.UNDERLINE + player.getName() +
                    ChatColor.GRAY + " has rolled a " + ChatColor.RED + roll + ChatColor.GRAY + " out of " + ChatColor.RED + max + ChatColor.GRAY + "."));
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Incorrect syntax. /Roll <1-10000>");
            return false;
        }

        return true;
    }
}
