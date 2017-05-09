package net.dungeonrealms.game.command.test;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/18/2016
 */
public class CommandTestDupe extends BaseCommand {

    public CommandTestDupe(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;

        if (!Rank.isDev(player)) return false;

        final ItemStack targetItem = player.getInventory().getItemInMainHand();

        if (targetItem == null || targetItem.getType() == Material.AIR) return true;

        final ItemStack duplicatedItem = new ItemStack(targetItem);

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "No inventory space.");
            return true;
        }


        player.sendMessage(ChatColor.RED + "Item duplicated");
        player.getInventory().setItem(player.getInventory().firstEmpty(), duplicatedItem);
        return true;
    }

}
