package net.dungeonrealms.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.commands.generic.BasicCommand;

/**
 * Created by Nick on 9/11/2015.
 */
public class CommandList extends BasicCommand  {

    public CommandList(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command cmd, String string, String[] args) {

        if (commandSender instanceof ConsoleCommandSender) {
            return false;
        }
        if (!(commandSender.isOp())) {
            commandSender.sendMessage("You're not OP.");
            return false;
        }

        StringBuilder players = new StringBuilder();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (players.length() > 0) {
                players.append(ChatColor.AQUA).append(", ").append(ChatColor.GOLD);
            }
            players.append(player.getDisplayName());
        }

        String onlinePlayers = Bukkit.getOnlinePlayers().size() + "";
        commandSender.sendMessage(ChatColor.GREEN + "Players Online: " + ChatColor.LIGHT_PURPLE + onlinePlayers + ChatColor.GRAY + "/" + ChatColor.LIGHT_PURPLE + Bukkit.getMaxPlayers());
        commandSender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + players.toString() + ChatColor.GRAY + "]");

        return true;
    }
}
