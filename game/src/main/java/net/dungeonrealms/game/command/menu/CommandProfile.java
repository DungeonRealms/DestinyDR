package net.dungeonrealms.game.command.menu;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.player.inventory.menus.guis.PlayerProfileGUI;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.CategoryGUI;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.PendingPurchasesGUI;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
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

        Player player = (Player)sender;
        new PlayerProfileGUI(player).open(player, null);
        return true;
    }
}
