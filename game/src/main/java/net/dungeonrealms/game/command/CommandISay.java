package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;


/**
 * Created by Chase on Dec 14, 2015
 */
public class CommandISay extends BaseCommand {
    public CommandISay() {
        super("isay", "/<command> [args]", "Prints message to players in dungeon world from command block.");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.asList(args)));

        message = message.replaceAll("&u", ChatColor.UNDERLINE.toString());
        message = message.replaceAll("&s", ChatColor.BOLD.toString());
        message = message.replaceAll("&i", ChatColor.ITALIC.toString());
        message = message.replaceAll("&m", ChatColor.MAGIC.toString());

        if (commandSender instanceof Player) {
            if (!Rank.isDev((Player) commandSender))
                return false;
            Bukkit.broadcastMessage(message);
        } else if (commandSender instanceof BlockCommandSender) {
            BlockCommandSender block = (BlockCommandSender) commandSender;
            for (Player p : block.getBlock().getWorld().getPlayers())
                p.sendMessage(message);
        }
        return true;
    }

}
