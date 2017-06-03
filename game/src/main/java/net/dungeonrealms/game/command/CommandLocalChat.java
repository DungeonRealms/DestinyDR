package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;

import net.dungeonrealms.game.player.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

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

        Chat.sendChatMessage((Player) sender, String.join(" ", args), false, true);
        return true;
    }
}
