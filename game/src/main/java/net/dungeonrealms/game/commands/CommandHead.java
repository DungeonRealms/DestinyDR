package net.dungeonrealms.game.commands;

import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * Created by Brad on 04/07/2016.
 */

public class CommandHead extends BasicCommand {
    public CommandHead(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player) || !Rank.isGM((Player) sender)) return false;
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Invalid usage! /head <name>");
            return false;
        }

        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(args[0]);
        meta.setDisplayName(ChatColor.RESET + args[0]);
        item.setItemMeta(meta);
        player.getInventory().addItem(item);

        player.sendMessage(ChatColor.GREEN + "Successfully given you the head of " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0] + ChatColor.GREEN + "." );
        return true;
    }
}