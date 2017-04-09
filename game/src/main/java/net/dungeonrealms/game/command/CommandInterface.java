package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.inventory.NPCMenus;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Brad on 12/06/2016.
 */

public class CommandInterface extends BaseCommand {
    public CommandInterface() {
        super("interface", "/<command> <menu>", "Development command for accessing interfaces.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if (!Rank.isTrialGM(player)) return false;

        if (args.length >= 1) {

            switch (args[0].toLowerCase()) {
                case "bank":
                    Storage storage = BankMechanics.getStorage(player.getUniqueId());
                    if(storage == null){
                        player.sendMessage(ChatColor.RED + "Please wait while your storage is loaded.");
                        return true;
                    }
                    player.openInventory(storage.inv);
                    break;

                case "item":
                case "itemvendor":
                case "item_vendor":
                    NPCMenus.openItemVendorMenu(player);
                    break;

                case "food":
                case "foodvendor":
                case "food_vendor":
                    NPCMenus.openFoodVendorMenu(player);
                    break;

                case "skill":
                case "profession":
                case "skillvendor":
                case "professionvendor":
                case "skill_vendor":
                case "profession_vendor":
                    NPCMenus.openProfessionPurchaseMenu(player);
                    break;

                case "dungeoneer":
                    NPCMenus.openDungeoneerMenu(player);
                    break;

                case "hearthstone":
                    NPCMenus.openHearthstoneRelocateMenu(player);
                    break;

                case "wizard":
                    NPCMenus.openWizardMenu(player);
                    break;

                case "merchant":
                    NPCMenus.openMerchantMenu(player);
                    break;

                case "mount":
                    NPCMenus.openMountPurchaseMenu(player);
                    break;

                case "ecash":
                case "ecashvendor":
                case "ecash_vendor":
                    NPCMenus.openECashPurchaseMenu(player);
                    break;

                default:
                    player.sendMessage(ChatColor.RED + "The interface '" + args[0] + "' was not recognised or registered.");
                    return false;
            }

            player.sendMessage(ChatColor.GREEN + "Successfully opened " + ChatColor.BOLD + ChatColor.UNDERLINE.toString() + args[0].toUpperCase() + ChatColor.GREEN + ".");
        } else {
            player.sendMessage(ChatColor.RED + "Invalid usage! /interface <name>");
        }

        return true;
    }

}
