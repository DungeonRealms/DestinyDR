/**
 *
 */
package net.dungeonrealms.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumOperators;

/**
 * Created by Chase on Sep 22, 2015
 */
public class CommandSet implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender)
            return false;
        Player player = (Player) s;
        if (args.length > 0) {
            switch (args[0]) {
                case "level":
                    int lvl = Integer.parseInt(args[1]);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "info.netLevel", lvl, true);
                    s.sendMessage("Level set to " + lvl);
                    break;
                case "gems":
                    int gems = Integer.parseInt(args[1]);
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "info.gems", gems, true);
                    s.sendMessage("Gems set to " + gems);
                    break;
                case "inv":
               	 DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "inventory.player", "", true);
               	 DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "inventory.storage", "", true);
               	 break;
            }
        }
        return true;
    }
}
