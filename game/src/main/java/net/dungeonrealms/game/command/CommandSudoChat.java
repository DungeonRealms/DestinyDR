package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.player.chat.Chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/23/2016
 */
public class CommandSudoChat extends BaseCommand {
    public CommandSudoChat() {
        super("sudochat", "/<command> <name> <type> <message>", "Sudo Chat command.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Rank.isDev(player)) return false;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Invalid usage! /sudochat <name> <type> <message>");
            return false;
        }

        if (Bukkit.getPlayer(args[0]) == null) {
            sender.sendMessage(ChatColor.RED + args[0] + " is not online.");
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        StringBuilder message = new StringBuilder(args[2]);
        for (int arg = 3; arg < args.length; arg++)
        	message.append(" ").append(args[arg]);

        Chat.sendChatMessage(player, message.toString(), args[1].equalsIgnoreCase("global"));

        return true;
    }
}
