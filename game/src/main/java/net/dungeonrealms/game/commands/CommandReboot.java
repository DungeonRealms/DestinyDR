package net.dungeonrealms.game.commands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.commands.BasicCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


/**
 * Created by Kieran Quigley (Proxying) on 08-Jun-16.
 */
public class CommandReboot extends BasicCommand {
    public CommandReboot(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        long timeDifference = System.currentTimeMillis() - DungeonRealms.getServerStart();
        timeDifference = (4 * 60 * 60 * 1000) - timeDifference;
        commandSender.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + "Next Scheduled Reboot:" + ChatColor.YELLOW + " " + (int) (timeDifference / 3600000 % 24) + ChatColor.BOLD + "h " + ChatColor.YELLOW + (int) (timeDifference / 60000 % 60) + ChatColor.BOLD + "m " + ChatColor.YELLOW + (int) (timeDifference / 1000 % 60) + ChatColor.BOLD + "s");
        return true;
    }
}
