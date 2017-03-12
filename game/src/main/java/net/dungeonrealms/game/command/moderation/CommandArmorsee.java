package net.dungeonrealms.game.command.moderation;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mastery.ItemSerialization;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
public class CommandArmorsee extends BaseCommand {
    public CommandArmorsee() {
        super("armorsee", "/<command> <player>", "View a player's armor inventory.", Collections.singletonList("mas"));
    }

    public static Map<UUID, UUID> offline_armor_watchers = new HashMap<>();

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
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            Inventory inv = Bukkit.createInventory(null, 9, player.getName() + " Armor");
            for (int i = 0; i < 4; i++) {
                ItemStack stack = player.getInventory().getArmorContents()[i];
                if (stack == null) continue;
                inv.addItem(stack);
            }
            inv.setItem(8, player.getEquipment().getItemInMainHand());
            sender.openInventory(inv);
        }
        else {
            if (DatabaseAPI.getInstance().getUUIDFromName(playerName).equals("")) {
                sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + playerName + ChatColor.RED + " does not exist in our database.");
                return true;
            }

            UUID p_uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
            Inventory inventoryView = Bukkit.createInventory(null, 9, playerName + "'s Offline Armor View (Last slot is offhand)");

            List<String> playerArmor = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ARMOR, p_uuid);
            if (playerArmor == null) return true;
            int i = -1;
            ItemStack[] armorContents = new ItemStack[4];
            ItemStack offHand = new ItemStack(Material.AIR);
            for (String armor : playerArmor) {
                i++;
                if (i <= 3) { //Normal armor piece
                    if (armor.equals("null") || armor.equals("")) {
                        inventoryView.addItem(new ItemStack(Material.AIR));
                    } else {
                        inventoryView.addItem(ItemSerialization.itemStackFromBase64(armor));
                    }

                } else {
                    if (armor.equals("null") || armor.equals("")) {
                        inventoryView.addItem(new ItemStack(Material.AIR));
                    } else {
                        inventoryView.addItem(ItemSerialization.itemStackFromBase64(armor));
                    }
                }
            }

            offline_armor_watchers.put(sender.getUniqueId(), p_uuid);
            sender.openInventory(inventoryView);
        }

        return false;
    }
}
