package net.dungeonrealms.game.command.menu;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.player.inventory.menus.guis.TogglesGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGM extends BaseCommand {
    public CommandGM() {
        super("gm", "Legacy command for GMs.", "Way to /toggles.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && Rank.isTrialGM((Player) sender)) {
            new TogglesGUI((Player) sender, null).open((Player) sender, null);
            return true;
        }
        return false;
    }
}
