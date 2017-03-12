package net.dungeonrealms.game.command.moderation;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mastery.ItemSerialization;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by Brad on 25/12/2016.
 */
public class CommandInvsee extends BaseCommand {
    public CommandInvsee() {
        super("invsee", "/<command> <player>", "View a player's inventory.", Collections.singletonList("mis"));
    }

    public static Map<UUID, UUID> offline_inv_watchers = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player sender = (Player) s;

        if (!Rank.isGM(sender)) return false;

        if (args.length == 0) {
            s.sendMessage(usage);
            return true;
        }

        String playerName = args[0];
        if (Bukkit.getPlayer(playerName) != null) {
            sender.openInventory(Bukkit.getPlayer(playerName).getInventory());
        } else {

            if (DatabaseAPI.getInstance().getUUIDFromName(playerName).equals("")) {
                sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + playerName + ChatColor.RED + " does not exist in our database.");
                return true;
            }

            UUID p_uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
            Inventory inventoryView = Bukkit.createInventory(null, 36, playerName + "'s Offline Inventory View");

            String playerInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY, p_uuid);
            if (playerInv != null && playerInv.length() > 0 && !playerInv.equalsIgnoreCase("null")) {
                ItemStack[] items = ItemSerialization.fromString(playerInv, 36).getContents();
                inventoryView.setContents(items);
            }

            offline_inv_watchers.put(sender.getUniqueId(), p_uuid);
            sender.openInventory(inventoryView);
        }
        return false;
    }
}
