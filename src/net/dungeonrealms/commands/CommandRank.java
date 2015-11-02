package net.dungeonrealms.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.rank.Rank;

/**
 * Created by Nick on 9/27/2015.
 */
public class CommandRank extends BasicCommand {

    public CommandRank(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "[WARNING] " + ChatColor.YELLOW + "You do not have permissions for this!");
            return false;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("set")) {
                if (Bukkit.getPlayer(args[1]) != null) {
                    Rank.getInstance().setRank(Bukkit.getPlayer(args[1]).getUniqueId(), args[2]);
                }
                DatabaseAPI.getInstance().update(Bukkit.getPlayer(args[1]).getUniqueId(), EnumOperators.$SET, EnumData.RANK, args[2], true);
            } else if (args[0].equals("create")) {
                if (args[1] == null || args[2] == null || args[3] == null) return false;
                boolean didCreate = Rank.getInstance().createNewRank(args[1], args[2], args[3]);
                if (didCreate) {
                    player.sendMessage(ChatColor.GREEN + "[RANK] " + ChatColor.YELLOW + "Created a new rank " + args[1]);
                } else {
                    player.sendMessage(ChatColor.RED + "[RANK] " + ChatColor.YELLOW + "That rank already exist!?");
                }
            } else if (args[0].equalsIgnoreCase("addpermission") || args[0].equalsIgnoreCase("addp")) {
                if (args[1] == null || args[2] == null) return false;
                Rank.getInstance().addPermission(args[1], args[2]);
                player.sendMessage(ChatColor.GREEN + "[RANK] " + ChatColor.YELLOW + "Added permission " + args[2] + " for rank " + args[1]);
            }
        } else {
            player.sendMessage(ChatColor.RED + "[ERROR] " + ChatColor.YELLOW + "Arguments not long enough.");
        }

        return false;
    }
}
