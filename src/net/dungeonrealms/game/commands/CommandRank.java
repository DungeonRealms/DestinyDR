package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Created by Nick on 9/27/2015.
 */
public class CommandRank extends BasicCommand {

    public CommandRank(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {

        if (!(s instanceof ConsoleCommandSender)) return false;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("set")) {
                if (Bukkit.getPlayer(args[1]) != null) {
                    Rank.getInstance().setRank(Bukkit.getPlayer(args[1]).getUniqueId(), args[2]);
                    DatabaseAPI.getInstance().update(Bukkit.getPlayer(args[1]).getUniqueId(), EnumOperators.$SET, EnumData.RANK, args[2], true);
                } else {
                    Utils.log.warning("Unable to rank: " + args[1] + " due to them not being online!");
                }
            } else if (args[0].equals("create")) {
                if (args[1] == null || args[2] == null || args[3] == null) return false;
                boolean didCreate = Rank.getInstance().createNewRank(args[1], args[2], args[3]);
                if (didCreate) {
                    s.sendMessage(ChatColor.GREEN + "[RANK] " + ChatColor.YELLOW + "Created a new rank " + args[1]);
                } else {
                    s.sendMessage(ChatColor.RED + "[RANK] " + ChatColor.YELLOW + "That rank already exist!?");
                }
            } else if (args[0].equalsIgnoreCase("addpermission") || args[0].equalsIgnoreCase("addp")) {
                if (args[1] == null || args[2] == null) return false;
                Rank.getInstance().addPermission(args[1], args[2]);
                s.sendMessage(ChatColor.GREEN + "[RANK] " + ChatColor.YELLOW + "Added permission " + args[2] + " for rank " + args[1]);
            }
        } else {
            s.sendMessage(ChatColor.RED + "[ERROR] " + ChatColor.YELLOW + "Arguments not long enough.");
        }

        return false;
    }
}
