package net.dungeonrealms.game.command.donation;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.CategoryGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 5/18/2017.
 */
public class CommandUnlocks extends BaseCommand {

    public CommandUnlocks() {
        super("unlocks","/unlocks", "Open your unlockables menu");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) return true;
        new CategoryGUI((Player) sender).open((Player) sender, null);
        return false;
    }
}
