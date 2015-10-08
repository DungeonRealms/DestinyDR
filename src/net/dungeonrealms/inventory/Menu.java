package net.dungeonrealms.inventory;

import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumGuildData;
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

        String owner = (String) DatabaseAPI.getInstance().getData(EnumGuildData.OWNER, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        List<String> officers = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.OFFICERS, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        List<String> members = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.MEMBERS, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));

        Inventory inv = Bukkit.createInventory(null, 54, "Guild - " + ChatColor.translateAlternateColorCodes('&', clanTag));

        inv.setItem(4, editItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Guild Name" + ChatColor.GRAY + ": " + ChatColor.RESET + guildName, new String[]{
                ChatColor.GRAY + "ClanTag: " + ChatColor.AQUA + clanTag,
                ChatColor.GRAY + "Guild Master: " + ChatColor.AQUA + Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName(),
        }));

        inv.setItem(8, editItem(new ItemStack(Material.DIAMOND_SWORD), ChatColor.RED + "Guild Wars", new String[]{
                ChatColor.RED + "This feature is upcoming!",
        }));
        inv.setItem(7, editItem(new ItemStack(Material.CHEST), ChatColor.GREEN + "Guild Bank", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click" + ChatColor.GRAY + " to open your Guilds Bank!",
        }));

        inv.setItem(18, editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), ChatColor.GREEN + "Guild Officers", new String[]{}));
        int oi = 19;
        for (String oUuid : officers) {
            if (oi > 26) break;
            inv.setItem(oi, editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), Bukkit.getServer().getOfflinePlayer(UUID.fromString(oUuid)).getName(), new String[]{

            }));
            oi++;
        }

        inv.setItem(27, editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), ChatColor.GREEN + "Guild Members", new String[]{}));
        int moi = 28;
        for (String mUuid : members) {
            if (moi > 35) break;
            inv.setItem(oi, editItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), Bukkit.getServer().getOfflinePlayer(UUID.fromString(mUuid)).getName(), new String[]{

            }));
            moi++;
        }

        player.openInventory(inv);

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
