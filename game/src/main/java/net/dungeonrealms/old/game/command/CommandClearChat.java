package net.dungeonrealms.old.game.command;

import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.common.old.game.database.player.rank.Rank;
import net.dungeonrealms.old.game.player.chat.GameChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/20/2016
 */

public class CommandClearChat extends BaseCommand {

    public CommandClearChat(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && !Rank.isGM((Player) sender)) return true;

        for (int i = 0; i < 100; i++)
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(" ");

                if (i == 99)
                    p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Chat cleared by " + GameChat.getFormattedName((Player) sender));
            }

        return false;
    }
}
