package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.player.inventory.NPCMenus;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Created by Brad on 12/06/2016.
 */

public class CommandInterface extends BaseCommand {
    public CommandInterface(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        String message = ChatColor.translateAlternateColorCodes('&', String.join(" ", Arrays.asList(args)));

        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if (!Rank.isGM(player)) return false;

        if (args.length >= 1) {

            switch (args[0].toLowerCase()) {
                case "bank":
                    break;

                case "item":
                case "itemvendor":
                case "item_vendor":
                    player.sendMessage(ChatColor.RED + "Unavailable.");
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
