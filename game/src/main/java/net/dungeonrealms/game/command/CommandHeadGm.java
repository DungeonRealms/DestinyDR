package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Brad on 26/12/2016.
 */
public class CommandHeadGm extends BaseCommand {
    public CommandHeadGm() {
        super("headgm", "/<command> [password]", "Toggle extended GM access.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player) || !Rank.isHeadGM((Player) sender)) return false;

        // We have a security precaution in-place, the word for arg[0] must match otherwise the user doesn't know the key.
        // Without the correct key they won't be able to access to head GM tools and we can pretend they've not got access.
        // TODO: Why do we restrict this with a password if it's only for Head GMs?
        if (args.length > 0 && args[0].equalsIgnoreCase("generation")) {
        	boolean newState = !DungeonRealms.getInstance().isGMExtendedPermissions;
        	DungeonRealms.getInstance().isGMExtendedPermissions = newState;
        	sender.sendMessage((newState ? ChatColor.GREEN : ChatColor.RED) + "Extended GM Permissions - " + (newState ? "Enabled" : "Disabled"));
        }
        
        return true;
    }

}
