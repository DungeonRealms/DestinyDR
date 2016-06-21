package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.player.rank.Rank;
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
public class CommandISay extends BasicCommand {
    public CommandISay(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.asList(args)));
        message = message.replaceAll("_", " ");
        message = message.replaceAll("&0", ChatColor.BLACK.toString());
        message = message.replaceAll("&1", ChatColor.DARK_BLUE.toString());
        message = message.replaceAll("&2", ChatColor.DARK_GREEN.toString());
        message = message.replaceAll("&3", ChatColor.DARK_AQUA.toString());
        message = message.replaceAll("&4", ChatColor.DARK_RED.toString());
        message = message.replaceAll("&5", ChatColor.DARK_PURPLE.toString());
        message = message.replaceAll("&6", ChatColor.GOLD.toString());
        message = message.replaceAll("&7", ChatColor.GRAY.toString());
        message = message.replaceAll("&8", ChatColor.DARK_GRAY.toString());
        message = message.replaceAll("&9", ChatColor.BLUE.toString());
        message = message.replaceAll("&a", ChatColor.GREEN.toString());
        message = message.replaceAll("&b", ChatColor.AQUA.toString());
        message = message.replaceAll("&c", ChatColor.RED.toString());
        message = message.replaceAll("&d", ChatColor.LIGHT_PURPLE.toString());
        message = message.replaceAll("&e", ChatColor.YELLOW.toString());
        message = message.replaceAll("&f", ChatColor.WHITE.toString());

        message = message.replaceAll("&u", ChatColor.UNDERLINE.toString());
        message = message.replaceAll("&s", ChatColor.BOLD.toString());
        message = message.replaceAll("&i", ChatColor.ITALIC.toString());
        message = message.replaceAll("&m", ChatColor.MAGIC.toString());
        //This is autistic. Whoever placed the command blocks with these incorrect color codes should be banned.

        if (commandSender instanceof Player) {
            if (!Rank.isGM((Player) commandSender)) {
                return false;
            }
            Bukkit.broadcastMessage(message);
        } else if (commandSender instanceof BlockCommandSender) {
            BlockCommandSender block = (BlockCommandSender) commandSender;
            for (Player p : block.getBlock().getWorld().getPlayers()) {
                p.sendMessage(message);
            }
        }
        return true;
    }

}
