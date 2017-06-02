package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.database.PlayerWrapper;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Kieran on 11/9/2015.
 */
public class CommandRoll extends BaseCommand {

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
            sender.sendMessage(ChatColor.RED + "Incorrect syntax. /roll <1-10000>");
            return false;
        }

        try {
            int max = Integer.parseInt(args[0]);
            if (max < 1 || max > 10000) {
                sender.sendMessage(ChatColor.RED + "Incorrect syntax. /roll <1-10000>");
                return false;
            }
            Player player = (Player) sender;

            int roll = ThreadLocalRandom.current().nextInt(max) + 1;

            String playerName = PlayerWrapper.getWrapper(player).getChatName();

            GameAPI.getNearbyPlayers(player.getLocation(), 20).forEach(player1 -> player1.sendMessage(playerName + ChatColor.GRAY + " has rolled a " + ChatColor.UNDERLINE + ChatColor.BOLD + roll + ChatColor.GRAY + " out of " + ChatColor.UNDERLINE + ChatColor.BOLD + max + ChatColor.GRAY + "."));
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "Incorrect syntax. /roll <1-10000>");
            return false;
        }

        return true;
    }
}
