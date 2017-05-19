package net.dungeonrealms.game.command.donation;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.PendingPurchasesGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 5/18/2017.
 */
public class CommandMailbox extends BaseCommand {

    public CommandMailbox() {
        super("mailbox", "/mailbox", "Opens up your mailbox");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Players only!");
            return false;
        }

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper((Player)sender);
        if(wrapper.getPendingPurchaseablesUnlocked().size() <= 0) {
            sender.sendMessage(ChatColor.RED + "You do not have anything in your mailbox!");
            return true;
        }

        new PendingPurchasesGUI((Player)sender, ShopMenu.fitSize(wrapper.getPendingPurchaseablesUnlocked().size() + 2)).open((Player)sender,null);
        return false;
    }
}
