package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kneesnap on 11/17/2017.
 */
public class CommandDiscord extends BaseCommand {
    public CommandDiscord() {
        super("discord");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        JSONMessage message = new JSONMessage();
        message.addText(ChatColor.GRAY + "Join our discord server: ");
        message.addText("HERE");
        message.addURL(ChatColor.BOLD.toString() + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://dungeonrealms.net/discord");
        message.sendToPlayer((Player) sender);
        return false;
    }
}
