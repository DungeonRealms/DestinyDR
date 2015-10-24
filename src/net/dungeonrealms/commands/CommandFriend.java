package net.dungeonrealms.commands;

import net.dungeonrealms.API;
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
            switch (args[1].toLowerCase()) {
                case "add":
                    if (args.length != 1) {
                        player.sendMessage(ChatColor.RED + "/friend add <playerName>");
                        return false;
                    }
                    if (API.isOnline(Bukkit.getPlayer(args[2]).getUniqueId())) {
                        Player wantingToAdd = Bukkit.getPlayer(args[2]);
                        FriendHandler.getInstance().sendRequest(player, wantingToAdd);
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot send friend requests to offline players!");
                    }

                    break;
            }
        } else {
            player.sendMessage(ChatColor.RED + "/friend add <playerName>");
        }

        return false;
    }
}
