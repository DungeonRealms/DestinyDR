package net.dungeonrealms.game.command.menu;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kieran Quigley (Proxying) on 29-May-16.
 */
public class CommandProfile extends BaseCommand {
    public CommandProfile(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            return false;
        }
        if (args.length == 0) {
            PlayerMenus.openPlayerProfileMenu((Player) sender);
            return true;
        } else {
            return true;
        }
    }
}
