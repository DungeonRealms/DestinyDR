package net.dungeonrealms.inventory;

import com.mongodb.Block;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.types.mounts.EnumMounts;
import net.dungeonrealms.entities.types.pets.EnumPets;
import net.dungeonrealms.mastery.ItemSerialization;
import net.dungeonrealms.mongo.Database;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumGuildData;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
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
@SuppressWarnings({"unchecked", "chasesTouch"})
public class Menu {

    public static void openMailInventory(Player player) {
        UUID uuid = player.getUniqueId();
        ArrayList<String> mail = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MAILBOX, uuid);

        if (mail.size() <= 0) {
            Inventory noMailInv = Bukkit.createInventory(null, 0, ChatColor.RED + "You have no mail! :-(");
            player.openInventory(noMailInv);
            return;

        }

        Inventory inv = Bukkit.createInventory(null, 45, "Mailbox");
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");

        for (String s : mail) {
            String from = s.split(",")[0];
            long unix = Long.valueOf(s.split(",")[1]);
            String serializedItem = s.split(",")[2];
            Date sentDate = new Date(unix * 1000);
            String loginTime = sdf.format(sentDate);
            inv.addItem(editItem(ItemSerialization.itemStackFromBase64(serializedItem), "", new String[]{
                    ChatColor.GRAY + "From: " + ChatColor.AQUA + from,
                    ChatColor.GRAY + "Sent: " + ChatColor.AQUA + sentDate,
            }));
        }

        player.openInventory(inv);

    }

    public static void openGuildManagement(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, "Guild Management");
        UUID uuid = player.getUniqueId();
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        List<String> officers = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.OFFICERS, guildName);
        List<String> members = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.MEMBERS, guildName);

        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));


        inv.setItem(8, editItem(new ItemStack(Material.BEDROCK), ChatColor.RED + "Delete Guild " + ChatColor.RED.toString() + ChatColor.BOLD + "WARNING", new String[]{
                ChatColor.RED + "This action CANNOT be undone!",
                ChatColor.RED + "Guild deletion isn't automatic! Clicking",
                ChatColor.RED + "this item will send a request to be evaluated!",
                "",
                ChatColor.RED + "You must be ranked " + ChatColor.GREEN + "Guild Owner",
        }));

        inv.setItem(10, editItem("Nemanja011sl", ChatColor.GREEN + "Invite a player", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to add a player!",
                "",
                ChatColor.RED + "You must be ranked " + ChatColor.GREEN + "Officer" + ChatColor.RED + "!",
        }));

        inv.setItem(11, editItem("rarest_of_pepes", ChatColor.GREEN + "Remove a player", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to remove a player!",
                "",
                ChatColor.RED + "You must be ranked " + ChatColor.GREEN + "Officer" + ChatColor.RED + "!",
        }));

        inv.setItem(13, editItem("TeaZ", ChatColor.GREEN + "Promote a player", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to promote a player!",
                "",
                ChatColor.RED + "You must be ranked " + ChatColor.GREEN + "Owner/CoOwner" + ChatColor.RED + "!",
        }));

        inv.setItem(14, editItem("Arcaniax", ChatColor.GREEN + "Demote a player", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to demote a player!",
                "",
                ChatColor.RED + "You must be ranked " + ChatColor.GREEN + "Owner/CoOwner" + ChatColor.RED + "!",
        }));

        inv.setItem(36, editItem(new ItemStack(Material.WOOL, 1, (short) 5), ChatColor.GREEN + "Pick a Guild Icon!", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to edit the Guild Icon!",
                "",
                ChatColor.RED + "You must be ranked " + ChatColor.AQUA + "Owner" + ChatColor.RED + "!"
        }));

        player.openInventory(inv);
    }

    public static void openGuildMembers(Player player) {
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()));
        String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, guildName);
        List<String> members = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.MEMBERS, guildName);


        if (members.size() >= 50) {
            Inventory tooMuch = Bukkit.createInventory(null, 0, ChatColor.RED + "Too many members to list!");
            player.openInventory(tooMuch);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "Guild - " + ChatColor.translateAlternateColorCodes('&', clanTag) + ChatColor.RESET + " - Members");

        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (String s : members) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                inv.addItem(editItem(API.getNameFromUUID(s), ChatColor.GREEN + "Member " + API.getNameFromUUID(s), new String[]{}));
            }, 0l);
        }

        player.openInventory(inv);
    }

    public static void openGuildOfficers(Player player) {
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()));
        String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, guildName);
        List<String> members = (List<String>) DatabaseAPI.getInstance().getData(EnumGuildData.OFFICERS, guildName);


        if (members.size() >= 50) {
            Inventory tooMuch = Bukkit.createInventory(null, 0, ChatColor.RED + "Too many officers to list!");
            player.openInventory(tooMuch);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "Guild - " + ChatColor.translateAlternateColorCodes('&', clanTag) + ChatColor.RESET + " - Officers");

        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (String s : members) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                inv.addItem(editItem(API.getNameFromUUID(s), ChatColor.GREEN + "Officer " + API.getNameFromUUID(s), new String[]{}));
            }, 0l);
        }

        player.openInventory(inv);
    }

    public static void openPlayerGuildLogBankClicks(Player player) {
        UUID uuid = player.getUniqueId();

        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        List<String> bankClicks = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumGuildData.BANK_CLICK, guildName);
        String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));

        Inventory inv = Bukkit.createInventory(null, 36, "Guild - " + ChatColor.translateAlternateColorCodes('&', clanTag) + ChatColor.RESET + " - (Bank Logs)");

        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");

        //TODO: Finish this once Vault is created!
        /**
         * int plN = 1;
         for (String log : bankClicks) {
         if (plN > 34) break;

         String invitee = log.split(",")[0];
         Date loginDate = new Date(Long.valueOf(log.split(",")[1]) * 1000);
         String officer = log.split(",")[2];

         String loginTime = sdf.format(loginDate);
         inv.setItem(plN, editItem(invitee, ChatColor.GREEN + officer + " invited " + invitee, new String[]{
         ChatColor.GREEN + loginTime
         }));
         plN++;

         }
         */

        player.openInventory(inv);
    }

    public static void openPlayerGuildLogInvitations(Player player) {
        UUID uuid = player.getUniqueId();

        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        List<String> playerInvites = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumGuildData.PLAYER_INVITES, guildName);
        String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));

        Inventory inv = Bukkit.createInventory(null, 36, "Guild - " + ChatColor.translateAlternateColorCodes('&', clanTag) + ChatColor.RESET + " - (Invite Logs)");

        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");

        int plN = 1;
        for (String log : playerInvites) {
            if (plN > 34) break;

            String invitee = log.split(",")[0];
            Date loginDate = new Date(Long.valueOf(log.split(",")[1]) * 1000);
            String officer = log.split(",")[2];

            String loginTime = sdf.format(loginDate);
            inv.setItem(plN, editItem(invitee, ChatColor.GREEN + officer + " invited " + invitee, new String[]{
                    ChatColor.GREEN + loginTime
            }));
            plN++;

        }

        player.openInventory(inv);
    }

    public static void openPlayerGuildLogLogins(Player player) {
        UUID uuid = player.getUniqueId();

        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        List<String> loginLogs = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumGuildData.PLAYER_LOGINS, guildName);
        String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));

        Inventory inv = Bukkit.createInventory(null, 36, "Guild - " + ChatColor.translateAlternateColorCodes('&', clanTag) + ChatColor.RESET + " - (Login Logs)");

        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");

        int plN = 1;
        for (String log : loginLogs) {
            if (plN > 34) break;
            Date loginDate = new Date(Long.valueOf(log.split(",")[1]) * 1000);
            String loginTime = sdf.format(loginDate);
            inv.setItem(plN, editItem(log.split(",")[0], ChatColor.GREEN + log.split(",")[0], new String[]{
                    ChatColor.GREEN + loginTime
            }));
            plN++;

        }

        player.openInventory(inv);
    }

    public static void openPlayerGuildLog(Player player) {
        UUID uuid = player.getUniqueId();

        String owner = (String) DatabaseAPI.getInstance().getData(EnumGuildData.OWNER, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        long origin = (long) DatabaseAPI.getInstance().getData(EnumGuildData.CREATION_UNIX_DATA, guildName);
        int netLevel = (int) DatabaseAPI.getInstance().getData(EnumGuildData.LEVEL, guildName);
        double experience = Double.valueOf(String.valueOf(DatabaseAPI.getInstance().getData(EnumGuildData.EXPERIENCE, guildName)));

        Date creationDate = new Date(origin * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
        String date = sdf.format(creationDate);

        Inventory inv = Bukkit.createInventory(null, 18, "Guild - " + ChatColor.translateAlternateColorCodes('&', clanTag) + ChatColor.RESET + " - (Logs)");

        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        inv.setItem(4, editItem(new ItemStack(Material.BOOK_AND_QUILL), ChatColor.GREEN + guildName + "'s Logs", new String[]{
                ChatColor.GRAY + "Guild Master: " + ChatColor.AQUA + Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName(),
                ChatColor.GRAY + "Created: " + ChatColor.AQUA + date,
                ChatColor.GRAY + "Guild Level: " + ChatColor.AQUA + netLevel,
                ChatColor.GRAY + "Guild Experience: " + ChatColor.AQUA + experience,
        }));
        inv.setItem(12, editItem("Olaf_C", ChatColor.GREEN + "Player Logins", new String[]{}));
        inv.setItem(13, editItem("Shrek", ChatColor.GREEN + "Guild Invitations", new String[]{}));
        inv.setItem(14, editItem("Laserpanda", ChatColor.GREEN + "Guild Vault Entries", new String[]{}));

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

                Material m = Material.valueOf(((String) ((Document) info).get("icon")));

                inv.addItem(editItem(new ItemStack(m), ChatColor.GREEN + String.valueOf(((Document) info).get("name")), new String[]{
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

    public static void openPlayerGuildInventory(Player player) {
        UUID uuid = player.getUniqueId();

        String owner = (String) DatabaseAPI.getInstance().getData(EnumGuildData.OWNER, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumGuildData.NAME, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
        String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, guildName);
        Integer netLevel = (Integer) DatabaseAPI.getInstance().getData(EnumGuildData.LEVEL, guildName);

        Inventory inv = Bukkit.createInventory(null, 54, "Guild - " + ChatColor.translateAlternateColorCodes('&', clanTag));

        inv.setItem(0, editItem("PC", ChatColor.GREEN + "Guild Logs", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click" + ChatColor.GRAY + " to view Guild Logs!",
        }));

        inv.setItem(1, editItem("MHF_WSkeleton ", ChatColor.GREEN + "Guild Management", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click" + ChatColor.GRAY + " for guild Management.",
                "",
                ChatColor.RED + "You must " + ChatColor.GREEN + "Officer " + ChatColor.RED + "or higher!"
        }));

        inv.setItem(4, editItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Guild Name" + ChatColor.GRAY + ": " + ChatColor.RESET + guildName, new String[]{
                ChatColor.GRAY + "ClanTag: " + ChatColor.translateAlternateColorCodes('&', clanTag),
                ChatColor.GRAY + "Guild Master: " + ChatColor.AQUA + Bukkit.getOfflinePlayer(UUID.fromString(owner)).getName(),
                ChatColor.GRAY + "Level: " + ChatColor.AQUA + netLevel
        }));

        inv.setItem(8, editItem(new ItemStack(Material.DIAMOND_SWORD), ChatColor.RED + "Guild Wars", new String[]{
                ChatColor.RED + "This feature is upcoming!",
        }));
        inv.setItem(17, editItem("Seska_Rotan", ChatColor.GREEN + "Guild Ranking", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click" + ChatColor.GRAY + " to view highest Guilds!",
        }));

        inv.setItem(53, editItem("CruXXx", ChatColor.GREEN + "Guild Spoils", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click" + ChatColor.GRAY + " to claim Guild Spoils!",
        }));

        inv.setItem(18, editItem("Turmfalke2000", ChatColor.GREEN + "Guild Officers", new String[]{}));
        inv.setItem(27, editItem("Miner", ChatColor.GREEN + "Guild Members", new String[]{}));

        player.openInventory(inv);

    }

    public static void openPlayerPetMenu(Player player) {
        UUID uuid = player.getUniqueId();

        List<String> playerPets = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PETS, uuid);

        if (playerPets.size() <= 0) {
            Inventory noPets = Bukkit.createInventory(null, 0, ChatColor.RED + "You currently have no Pets!");
            player.openInventory(noPets);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 18, "Pet Selection");
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (String petType : playerPets) {
            ItemStack itemStack = new ItemStack(Material.MONSTER_EGG, 1, (short) EnumPets.getByName(petType).getEggShortData());
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("petType", new NBTTagString(petType));
            nmsStack.setTag(tag);
            inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStack), ChatColor.GREEN + petType.toUpperCase(), new String[]{
            }));
        }

        player.openInventory(inv);
    }

    public static void openPlayerMountMenu(Player player) {
        UUID uuid = player.getUniqueId();

        List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, uuid);

        if (playerMounts.size() <= 0) {
            Inventory noMounts = Bukkit.createInventory(null, 0, ChatColor.RED + "You currently have no Mounts!");
            player.openInventory(noMounts);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 18, "Mount Selection");
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (String mountType : playerMounts) {
            ItemStack itemStack = EnumMounts.getByName(mountType).getSelectionItem();
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("mountType", new NBTTagString(mountType));
            nmsStack.setTag(tag);
            inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStack), ChatColor.GREEN + mountType.toUpperCase(), new String[]{
            }));
        }

        player.openInventory(inv);
    }

    public static ItemStack editItem(String playerName, String name, String[] lore) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(playerName);
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack editItem(ItemStack itemStack, String name, String[] lore) {
        ItemStack item = itemStack;
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

}
