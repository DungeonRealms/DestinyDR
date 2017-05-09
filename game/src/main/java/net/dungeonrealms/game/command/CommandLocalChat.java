package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Created by Brad on 08/06/2015.
 */
public class CommandLocalChat extends BaseCommand {

    public CommandLocalChat(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) return false;

        if (args.length <= 0) {
            sender.sendMessage(ChatColor.RED + "/l <message>");
            return true;
        }
        
        return true;
    }
}
