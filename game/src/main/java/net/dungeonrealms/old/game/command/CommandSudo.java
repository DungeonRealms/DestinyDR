package net.dungeonrealms.old.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/23/2016
 */
public class CommandSudo extends BaseCommand {
    public CommandSudo(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Rank.isDev(player)) return false;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Invalid usage! /sudo <name> <command>");
            return false;
        }


        if (Bukkit.getPlayer(args[0]) == null) {

            sender.sendMessage(ChatColor.RED + args[0] + " is not online.");
            return true;
        }

        StringBuilder sudoCommand = new StringBuilder(args[1]);
        for (int arg = 2; arg < args.length; arg++) sudoCommand.append(" ").append(args[arg]);

        Bukkit.getPlayer(args[0]).performCommand(sudoCommand.toString());
        return true;
    }
}
