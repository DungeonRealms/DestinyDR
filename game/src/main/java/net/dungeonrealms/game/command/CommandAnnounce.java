package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * Created by Brad on 17/03/2017.
 */
public class CommandAnnounce extends BaseCommand {

    public CommandAnnounce() {
        super("announce", "/<command> [args]", "Sends a pre-defined broadcast to all shards.", Collections.singletonList("ann"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ((sender instanceof Player && !Rank.isGM((Player) sender)) || sender instanceof BlockCommandSender)
            return false;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Invalid usage! /announce <announcement>");
            return false;
        }

        ChatColor color = ChatColor.GOLD;
        String message;
        switch (args[0].toLowerCase()) {
            case "discord":
                message = "Come join our Discord server! https://discord.gg/KP5wWrC";
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid announcement!");
                return false;
        }

        message = color + ChatColor.BOLD.toString() + " >> " + color + message;
        GameAPI.sendNetworkMessage("Broadcast", message);
        return true;
    }
}
