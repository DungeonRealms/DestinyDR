package net.dungeonrealms.commands;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.handlers.FriendHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 10/22/2015.
 */
public class CommandFriend extends BasicCommand {

    public CommandFriend(String command, String usage, String description) {
        super(command, usage, description);
    }

    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        if (args.length > 0) {
            switch (args[0]) {
                case "add":
                    if (Bukkit.getPlayer(args[1]) != null) {
                        Player wantingToAdd = Bukkit.getPlayer(args[1]);
                        FriendHandler.getInstance().sendRequest(player, wantingToAdd);
                    } else {
                        player.sendMessage(ChatColor.RED + "That player isn't online!");
                    }
                    break;
                case "remove":
                    break;
            }
        } else {
            player.sendMessage(ChatColor.RED + "Syntax Error (/friend add <playerName>)");
        }

        return false;
    }
}
