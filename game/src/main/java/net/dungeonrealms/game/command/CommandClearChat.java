package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.database.PlayerWrapper;

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
        	Bukkit.broadcastMessage(" ");
        
        Bukkit.broadcastMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Chat cleared by " + PlayerWrapper.getWrapper((Player) sender).getChatName());
        
        return true;
    }
}
