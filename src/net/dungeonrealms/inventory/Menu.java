package net.dungeonrealms.inventory;

import com.mongodb.Block;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.Database;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumGuildData;
import net.dungeonrealms.rank.Subscription;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Nick on 9/29/2015.
 */
public class Menu {

    public static void openGuildManagement(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, "Guild Management");
        UUID uuid = player.getUniqueId();

        String owner = (String) DatabaseAPI.getInstance().getData(EnumGuildData.OWNER, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, guildName);
        List<String> officers = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.OFFICERS, guildName);
        List<String> members = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.MEMBERS, guildName);

        player.openInventory(inv);
    }

    public static void openGuildRankingBoard(Player player) {

        Inventory inv = Bukkit.createInventory(null, 18, "Top Guilds");

        inv.addItem(editItem("Mapparere", ChatColor.GREEN + "Top Ranked Guilds", new String[]{
                ChatColor.GRAY + "Filter:" + ChatColor.AQUA + " Level"
        }));

        Block<Document> printDocumentBlock = document -> {
            Object info = document.get("info");

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () ->
            {
                Date creationDate = new Date(Long.valueOf(String.valueOf(((Document) info).get("unixCreation"))) * 1000);
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
                String date = sdf.format(creationDate);

                inv.addItem(editItem(new ItemStack(Material.DIAMOND), ChatColor.GREEN + String.valueOf(((Document) info).get("name")), new String[]{
                        ChatColor.GRAY + "ClanTag: " + ChatColor.translateAlternateColorCodes('&', String.valueOf(((Document) info).get("clanTag"))),
                        ChatColor.GRAY + "Level: " + ChatColor.AQUA + String.valueOf(((Document) info).get("netLevel")),
                        ChatColor.GRAY + "Officers: " + ChatColor.AQUA + ((ArrayList<String>) ((Document) info).get("officers")).size(),
                        ChatColor.GRAY + "Members: " + ChatColor.AQUA + ((ArrayList<String>) ((Document) info).get("members")).size(),
                        ChatColor.GRAY + "Created: " + ChatColor.AQUA + date,
                }));

            }, 0l);
        };
        SingleResultCallback<Void> callbackWhenFinished = (result, t) -> {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.openInventory(inv), 1l);
        };

        Database.guilds.find(Filters.exists("info.netLevel")).sort(Sorts.descending("info.netLevel")).limit(16).forEach(printDocumentBlock, callbackWhenFinished);
    }

    public static void openPlayerGuildLog(Player player) {
        UUID uuid = player.getUniqueId();

        String owner = (String) DatabaseAPI.getInstance().getData(EnumGuildData.OWNER, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        Long origin = (Long) DatabaseAPI.getInstance().getData(EnumGuildData.CREATION_UNIX_DATA, guildName);
        int netLevel = (int) DatabaseAPI.getInstance().getData(EnumGuildData.LEVEL, guildName);
        double experience = Double.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumGuildData.EXPERIENCE, guildName)));

        Date creationDate = new Date(origin * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
        String date = sdf.format(creationDate);

        List<String> logs = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumGuildData.PLAYER_LOGINS, guildName);

        Inventory inv = Bukkit.createInventory(null, 36, "Guild - " + ChatColor.translateAlternateColorCodes('&', clanTag) + ChatColor.RESET + " - (Logs)");

        inv.setItem(0, editItem("Sloggy_Whopper ", ChatColor.GREEN + "Back", new String[]{}));

        inv.setItem(4, editItem(new ItemStack(Material.BOOK_AND_QUILL), ChatColor.GREEN + guildName + "'s Logs", new String[]{
                ChatColor.GRAY + "Guild Master: " + ChatColor.AQUA + Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName(),
                ChatColor.GRAY + "Created: " + ChatColor.AQUA + date,
                ChatColor.GRAY + "Guild Level: " + ChatColor.AQUA + netLevel,
                ChatColor.GRAY + "Guild Experience: " + ChatColor.AQUA + experience,
        }));
        inv.setItem(9, editItem("Olaf_C", ChatColor.GREEN + "Player Logins", new String[]{}));
        inv.setItem(18, editItem("Shrek", ChatColor.GREEN + "Guild Invitations", new String[]{}));
        inv.setItem(27, editItem("Laserpanda", ChatColor.GREEN + "Guild Vault Entries", new String[]{}));

        int plN = 10;
        for (String log : logs) {
            if (plN > 17) break;
            Date loginDate = new Date(Long.valueOf(log.split(",")[1]) * 1000);
            String loginTime = sdf.format(loginDate);
            inv.setItem(plN, editItem(log.split(",")[0], ChatColor.GREEN + log.split(",")[0], new String[]{
                    ChatColor.GREEN + loginTime
            }));
            plN++;
        }

        int giN = 19;
        for (String log : logs) {
            if (giN > 26) break;
            Date loginDate = new Date(Long.valueOf(log.split(",")[1]) * 1000);
            String loginTime = sdf.format(loginDate);
            inv.setItem(giN, editItem(log.split(",")[0], ChatColor.GREEN + log.split(",")[0], new String[]{
                    ChatColor.GREEN + loginTime
            }));
            giN++;
        }

        int gveI = 28;
        for (String log : logs) {
            if (gveI > 35) break;
            Date loginDate = new Date(Long.valueOf(log.split(",")[1]) * 1000);
            String loginTime = sdf.format(loginDate);
            inv.setItem(gveI, editItem(log.split(",")[0], ChatColor.GREEN + log.split(",")[0], new String[]{
                    ChatColor.GREEN + loginTime
            }));
            gveI++;
        }


        player.openInventory(inv);

    }

    public static void openPlayerGuildInventory(Player player) {
        UUID uuid = player.getUniqueId();

        String owner = (String) DatabaseAPI.getInstance().getData(EnumGuildData.OWNER, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, guildName);
        List<String> officers = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.OFFICERS, guildName);
        List<String> members = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.MEMBERS, guildName);
        Integer netLevel = (Integer) DatabaseAPI.getInstance().getData(EnumGuildData.LEVEL, guildName);

        Inventory inv = Bukkit.createInventory(null, 54, "Guild - " + ChatColor.translateAlternateColorCodes('&', clanTag));

        inv.setItem(0, editItem("PC", ChatColor.GREEN + "Guild Logs", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click" + ChatColor.GRAY + " to view Guild Logs!",
        }));

        inv.setItem(1, editItem("MHF_WSkeleton ", ChatColor.GREEN + "Guild Mechanics", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click" + ChatColor.GRAY + " for guild mechanics."
        }));

        inv.setItem(4, editItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Guild Name" + ChatColor.GRAY + ": " + ChatColor.RESET + guildName, new String[]{
                ChatColor.GRAY + "ClanTag: " + ChatColor.translateAlternateColorCodes('&', clanTag),
                ChatColor.GRAY + "Guild Master: " + ChatColor.AQUA + Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName(),
                ChatColor.GRAY + "Level: " + ChatColor.AQUA + netLevel
        }));

        inv.setItem(8, editItem(new ItemStack(Material.DIAMOND_SWORD), ChatColor.RED + "Guild Wars", new String[]{
                ChatColor.RED + "This feature is upcoming!",
        }));
        inv.setItem(7, editItem(new ItemStack(Material.CHEST), ChatColor.GREEN + "Guild Vault", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click" + ChatColor.GRAY + " to open your Guilds Vault!",
        }));
        inv.setItem(17, editItem("Seska_Rotan", ChatColor.GREEN + "Guild Ranking", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click" + ChatColor.GRAY + " to view highest Guilds!",
        }));

        inv.setItem(53, editItem("CruXXx", ChatColor.GREEN + "Guild Spoils", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click" + ChatColor.GRAY + " to claim Guild Spoils!",
        }));

        inv.setItem(18, editItem("Turmfalke2000", ChatColor.GREEN + "Guild Officers", new String[]{}));
        int oi = 19;
        for (String oUuid : officers) {
            if (oi > 26) break;
            inv.setItem(oi, editItem(Bukkit.getServer().getOfflinePlayer(UUID.fromString(oUuid)).getName(), Bukkit.getServer().getOfflinePlayer(UUID.fromString(oUuid)).getName(), new String[]{

            }));
            oi++;
        }

        inv.setItem(27, editItem("Miner", ChatColor.GREEN + "Guild Members", new String[]{}));
        int moi = 28;
        for (String mUuid : members) {
            if (moi > 35) break;
            inv.setItem(oi, editItem(Bukkit.getServer().getOfflinePlayer(UUID.fromString(mUuid)).getName(), Bukkit.getServer().getOfflinePlayer(UUID.fromString(mUuid)).getName(), new String[]{

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

    static ItemStack editItem(String playerName, String name, String[] lore) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(playerName);
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
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
