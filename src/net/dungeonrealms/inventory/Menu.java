package net.dungeonrealms.inventory;

import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.rank.Subscription;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Nick on 9/29/2015.
 */
public class Menu {

    public static void openPlayerGuildInventory(Player player) {
        UUID uuid = player.getUniqueId();
        Guild.GuildBlob g = Guild.getInstance().getGuild(uuid);
        if (g == null) {
            player.sendMessage(ChatColor.RED + "You are not in a guild! Or we're having trouble finding it.");
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 54, "Guild - " + ChatColor.translateAlternateColorCodes('&', g.getClanTag()));
        String name = g.getName();
        List<UUID> officers = g.getOfficers();
        List<UUID> members = g.getMembers();

    }

    public static void openPlayerPurchaseHistory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Purchase History");
        inv.setItem(4, Utils.getPlayerHead(player));

        int timeLeft = Subscription.getInstance().getHoursLeft(player);
        inv.setItem(10, timeLeft > 0 ?
                editItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Subscription Active", new String[]{
                        ChatColor.GRAY + "Your subscription is active another",
                        ChatColor.AQUA + String.valueOf(timeLeft) + ChatColor.GRAY + " hours"
                })
                :
                editItem(new ItemStack(Material.WOOL, 1, (short) 14), ChatColor.RED + "NO SUBSCRIPTION ACTIVE!", new String[]{
                        ChatColor.GRAY + "You do not have an active subscription!",
                        "",
                        ChatColor.AQUA + "www.dungeonrealms.net/buy"
                }));

        inv.setItem(27, editItem(new ItemStack(Material.WOOL, 1, (short) 5), ChatColor.GREEN + "Purchase History", new String[]{
                ChatColor.GRAY + "Latest purchases -->",
        }));
        List<String> purchases = (List<String>) DatabaseAPI.getInstance().getData(EnumData.PURCHASE_HISTORY, player.getUniqueId());
        int size = purchases.size();
        if (size == 0) {
            for (int i = 28; i < 35; i++) {
                inv.setItem(i, editItem(new ItemStack(Material.WOOL, 1, (short) 14), ChatColor.RED + "NO PURCHASE", new String[]{
                        ChatColor.GRAY + "You haven't made any purchases!"
                }));
            }
        } else {
            int i = 28;
            for (String s : purchases) {
                inv.setItem(i, editItem(new ItemStack(Material.BOOK_AND_QUILL), ChatColor.GREEN + s, new String[]{}));
                i++;
            }
        }
        player.openInventory(inv);
    }

    static ItemStack editItem(ItemStack itemStack, String name, String[] lore) {
        ItemStack item = itemStack;
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

}
