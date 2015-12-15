package net.dungeonrealms.game.commands;

import net.dungeonrealms.bounty.Bounty;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Created by Nick on 12/14/2015.
 */
public class CommandBounty extends BasicCommand {

    public CommandBounty(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        Player player = (Player) sender;

        if (args[0].equalsIgnoreCase("set")) {
            if (args.length == 3) {
                if (Bukkit.getPlayer(args[1]) != null) {
                    if (!Bounty.getInstance().hasBountyOn(Bukkit.getPlayer(args[1]).getUniqueId())) {
                        player.sendMessage(ChatColor.GREEN + "You can set bounty on this person!");
                    } else {
                        player.sendMessage(ChatColor.RED + "That player already has a bounty on them!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + args[1] + " isn't online!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Try /bounty set <userName> <amount>");
            }
        }

        if (args[0].equalsIgnoreCase("onme")) {
            Inventory inv = Bukkit.createInventory(null, 54, "Bounties on you");

            if (Bounty.getInstance().hasBountyOn(player.getUniqueId())) {
                Bounty.getInstance().getBountiesOn(player.getUniqueId(), e -> {
                    for (Map.Entry<String, Integer> e1 : e.entrySet()) {
                        inv.addItem(new ItemBuilder().setItem(new ItemStack(Material.SKULL_ITEM), ChatColor.GREEN.toString() + ChatColor.BOLD + "#" + String.valueOf(e.size()), new String[]{
                                ChatColor.GREEN.toString() + ChatColor.BOLD + "ACTIVE",
                                "",
                                ChatColor.AQUA + "Placed By: " + ChatColor.GRAY + e1.getKey(),
                                ChatColor.AQUA + "Reward: " + ChatColor.GREEN + String.valueOf(e1.getValue() + ChatColor.GREEN.toString() + ChatColor.BOLD + " GEMS"),
                        }).build());
                    }
                    player.openInventory(inv);
                });
            } else {
                player.sendMessage(ChatColor.RED + "You have no bounties placed on you!");
            }
        }

        return false;
    }
}
