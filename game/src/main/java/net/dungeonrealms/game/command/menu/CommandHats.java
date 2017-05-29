package net.dungeonrealms.game.command.menu;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.HatGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Simple hat Shortcut for the players.
 */
public class CommandHats extends BaseCommand {
    public CommandHats() {
        super("hats", "/<command>", "Hat command", null, Lists.newArrayList("hat"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player)
            new HatGUI((Player) sender).open((Player) sender, null);
        return false;
    }
}
