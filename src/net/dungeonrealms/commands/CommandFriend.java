package net.dungeonrealms.commands;

import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by Nick on 10/22/2015.
 */
public class CommandFriend implements CommandExecutor {

    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "add":
                    if (Bukkit.getPlayer(args[1]) == null) {
                        player.sendMessage(ChatColor.RED + "That player is not valid!");
                        return false;
                    }
                    Player user = Bukkit.getPlayer(args[1]);

                    ArrayList<String> friends = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIENDS, user.getUniqueId());
                    ArrayList<String> friendRequest = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIEND_REQUSTS, user.getUniqueId());

                    boolean isFriend = friends.contains(player.getUniqueId().toString());
                    long frID = friendRequest.stream().filter(s1 -> s1.startsWith(player.getUniqueId().toString())).count();

                    if (isFriend || frID >= 0) {
                        player.sendMessage(ChatColor.RED + "That player is either; 1. Already a friend, or 2. Pending friendship!");
                        return true;
                    }



                    break;
            }
        } else {
            player.sendMessage(ChatColor.RED + "/friend add <playerName>");
        }

        return false;
    }
}
