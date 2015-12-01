package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.guild.Guild;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 10/2/2015.
 */
public class CommandGuild extends BasicCommand {

    public CommandGuild(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "[WARNING] " + ChatColor.YELLOW + "You do not have permissions for this!");
            return false;
        }
        if (Guild.getInstance().isGuildNull(player.getUniqueId())) {
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
            PlayerMenus.openPlayerGuildInventory(player);
        }

        return true;
    }
}
