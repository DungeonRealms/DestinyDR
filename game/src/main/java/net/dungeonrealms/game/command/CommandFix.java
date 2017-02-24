package net.dungeonrealms.game.command;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.miscellaneous.Repair;
import net.dungeonrealms.game.world.item.repairing.RepairAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandFix extends BaseCommand {
    public CommandFix() {
        super("fix", "/command <usage", "Fix command");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (!Rank.isGM(player)) return true;

        int repaired = 0;
        for (ItemStack item : player.getInventory()) {
            if (item == null || item.getType() == Material.AIR) continue;

            double customDura = RepairAPI.getItemDurabilityValue(item);
            if (customDura <= 0) {
                continue;
            }

            if (customDura > 0 && customDura < 100) {
                System.out.println("Found percent: " + customDura + " for " + item.getType());
            }

            RepairAPI.setCustomItemDurability(item, 1500);
            repaired++;
        }

        player.sendMessage(ChatColor.RED + "Repaired " + ChatColor.BOLD + repaired + ChatColor.RED + " items.");
        return false;
    }
}
