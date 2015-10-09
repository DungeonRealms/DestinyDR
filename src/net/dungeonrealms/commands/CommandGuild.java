package net.dungeonrealms.commands;

import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.inventory.Menu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 10/2/2015.
 */
public class CommandGuild implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        if (!Guild.getInstance().isInGuild(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are not in a guild, or we're having trouble finding it.");
            return true;
        }

        if (args.length > 0) {
            switch (args[0]) {
                case "invite":
                    break;
                case "remove":
                    break;
                case "kick":
                    break;
                case "chat":
                    break;
            }
        } else {
            Menu.openPlayerGuildInventory(player);
        }

        return true;
    }
}
