package net.dungeonrealms.game.commands.menualias;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.player.inventory.NPCMenus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Brad on 29/06/2016.
 */

public class CommandEcash extends BasicCommand {

    public CommandEcash(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;
        NPCMenus.openECashPurchaseMenu(player);

        return true;
    }
}
