package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemGear;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandFix extends BaseCommand {
    public CommandFix() {
        super("fix", "/command <usage>", "Fix command");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (!Rank.isGM(player)) return true;

        int repaired = 0;
        for (ItemStack item : player.getInventory()) {
            if (!ItemGear.isCustomTool(item))
            	continue;
            ItemGear gear = (ItemGear)PersistentItem.constructItem(item);
            gear.repair();
            player.getInventory().setItem(player.getInventory().first(item), gear.generateItem());
            repaired++;
        }

        player.sendMessage(ChatColor.RED + "Repaired " + ChatColor.BOLD + repaired + ChatColor.RED + " items.");
        return false;
    }
}
