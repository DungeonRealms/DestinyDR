package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Opens the toggle menu.
 * 
 * Redone on May 8th, 2017.
 * @author Kneesnap
 */
public class CommandToggles extends BaseCommand {
    public CommandToggles() {
        super("toggles", "/<command> [args]", "View and manage your profile toggles.", "toggle");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player)
        	PlayerMenus.openToggleMenu((Player) sender);
        return true;
    }
}
