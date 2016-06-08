package net.dungeonrealms.game.player.inventory;

import net.dungeonrealms.API;
import net.dungeonrealms.game.handlers.MailHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.world.entities.types.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entities.types.mounts.EnumMounts;
import net.dungeonrealms.game.world.entities.types.pets.EnumPets;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
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
public class PlayerMenus {

    public static void openFriendsMenu(Player player) {
        UUID uuid = player.getUniqueId();
        ArrayList<String> friendRequest = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIENDS, uuid);

        Inventory inv = Bukkit.createInventory(null, 54, "Friends");

        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to go back!"
        }));


        int slot = 9;
        for (String s : friendRequest) {
            String name = API.getNameFromUUID(UUID.fromString(s));
            ItemStack stack = editItem(name, name, new String[]{
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Right-Click " + ChatColor.GRAY + "to delete!"
            });

            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("info", new NBTTagString(s));
            nmsStack.setTag(tag);


            inv.setItem(slot, CraftItemStack.asBukkitCopy(nmsStack));

            if (slot >= 54) break;
            slot++;
        }

        player.openInventory(inv);

    }

    public static void openFriendInventory(Player player) {
        UUID uuid = player.getUniqueId();
        ArrayList<String> friendRequest = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIEND_REQUSTS, uuid);

        Inventory inv = Bukkit.createInventory(null, 45, "Friend Management");

        inv.setItem(0, editItem(new ItemStack(Material.BOOK_AND_QUILL), ChatColor.GREEN + "Add Friend", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to add friend!"
        }));

        inv.setItem(1, editItem(new ItemStack(Material.CHEST), ChatColor.GREEN + "View Friend", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to view friends!"
        }));

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");

        int slot = 9;
        for (String s : friendRequest) {
            String from = s.split(",")[0];
            String name = API.getNameFromUUID(UUID.fromString(from));

            long unix = Long.valueOf(s.split(",")[1]);
            Date sentDate = new Date(unix * 1000);
            String date = sdf.format(sentDate);

            ItemStack stack = editItem(name, name, new String[]{
                    ChatColor.GRAY + "Sent: " + date,
                    "",
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to accept!",
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Right-Click " + ChatColor.GRAY + "to deny!"
            });

            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("info", new NBTTagString(s));
            nmsStack.setTag(tag);

            inv.setItem(slot, CraftItemStack.asBukkitCopy(nmsStack));

            if (slot >= 44) break;
            slot++;
        }

        player.openInventory(inv);

    }

    public static void openMailInventory(Player player) {
        UUID uuid = player.getUniqueId();
        ArrayList<String> mail = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MAILBOX, uuid);

        Inventory inv = Bukkit.createInventory(null, 45, "Mailbox");
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");

        inv.setItem(0, editItem(new ItemStack(Material.BOOK_AND_QUILL), ChatColor.GREEN + "Send Mail", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to send mail!"
        }));
        inv.setItem(8, editItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Tax", new String[]{
                ChatColor.GRAY + "Tax: " + ChatColor.AQUA + "1 GEM"
        }));

        int slot = 9;
        for (String s : mail) {
            String from = s.split(",")[0];
            long unix = Long.valueOf(s.split(",")[1]);
            String serializedItem = s.split(",")[2];
            Date sentDate = new Date(unix * 1000);
            String loginTime = sdf.format(sentDate);

            ItemStack mailTemplateItem = MailHandler.getInstance().setItemAsMail(editItem(new ItemStack(Material.CHEST), ChatColor.GREEN + "Mail Item", new String[]{
                    ChatColor.GRAY + "From: " + ChatColor.AQUA + from,
                    ChatColor.GRAY + "Sent: " + ChatColor.AQUA + sentDate,
                    "",
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to open!"
            }), s);

            inv.setItem(slot, mailTemplateItem);
            if (slot >= 44) break;
            slot++;
        }

        player.openInventory(inv);

    }

    public static void openPlayerPetMenu(Player player) {
        UUID uuid = player.getUniqueId();

        List<String> playerPets = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PETS, uuid);

        if (playerPets.size() <= 0) {
            Inventory noPets = Bukkit.createInventory(null, 0, ChatColor.RED + "You have no Pets!");
            player.openInventory(noPets);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Pet Selection");
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        inv.setItem(26, editItem(new ItemStack(Material.LEASH), ChatColor.GREEN + "Dismiss Pet", new String[]{}));

        for (String pet : playerPets) {
            String petType;
            String particleType = "";
            String petName;
            if (pet.contains("-")) {
                petType = pet.split("-")[0];
                particleType = pet.split("-")[1];
                petName = ParticleAPI.ParticleEffect.getChatColorByName(particleType) + particleType + " " + ChatColor.GREEN + EnumPets.getByName(petType).getDisplayName();
            } else {
                petType = pet;
                petName = ChatColor.GREEN + EnumPets.getByName(petType).getDisplayName();
            }
            ItemStack itemStack = new ItemStack(Material.MONSTER_EGG, 1, (short) EnumPets.getByName(petType).getEggShortData());
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("petType", new NBTTagString(petType));
            if (!particleType.equalsIgnoreCase("")) {
                tag.set("particleType", new NBTTagString(particleType));
            }
            nmsStack.setTag(tag);
            inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStack), petName, new String[]{
            }));
        }

        player.openInventory(inv);
    }

    public static void openPlayerMountMenu(Player player) {
        UUID uuid = player.getUniqueId();

        List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, uuid);
        int count = 0;
        if (playerMounts.size() > 0) {
            for (String mount : playerMounts) {
                if (!mount.equalsIgnoreCase("MULE")) {
                    count++;
                }
            }
        }
        if (count <= 0) {
            Inventory noMounts = Bukkit.createInventory(null, 0, ChatColor.RED + "You have no Mounts!");
            player.openInventory(noMounts);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Mount Selection");
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        inv.setItem(26, editItem(new ItemStack(Material.LEASH), ChatColor.GREEN + "Dismiss Mount", new String[]{}));

        ItemStack itemStack;
        String mountType = null;

        if (playerMounts.contains(EnumMounts.TIER3_HORSE.getRawName())) {
            mountType = EnumMounts.TIER3_HORSE.getRawName();
        } else if (playerMounts.contains(EnumMounts.TIER2_HORSE.getRawName())) {
            mountType = EnumMounts.TIER2_HORSE.getRawName();
        } else if (playerMounts.contains(EnumMounts.TIER1_HORSE.getRawName())) {
            mountType = EnumMounts.TIER1_HORSE.getRawName();
        }

        if (mountType == null) {
            Inventory noMounts = Bukkit.createInventory(null, 0, ChatColor.RED + "You have no Mounts!");
            player.openInventory(noMounts);
            return;
        }

        itemStack = EnumMounts.getByName(mountType).getSelectionItem();

        if (itemStack == null) {
            Inventory noMounts = Bukkit.createInventory(null, 0, ChatColor.RED + "You have no Mounts!");
            player.openInventory(noMounts);
            return;
        }

        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("mountType", new NBTTagString(mountType));
        nmsStack.setTag(tag);
        inv.addItem(editItemWithShort(CraftItemStack.asBukkitCopy(nmsStack), EnumMounts.getByName(mountType).getShortID(), ChatColor.GREEN + EnumMounts.getByName(mountType).getDisplayName(), new String[]{
        }));

        player.openInventory(inv);
    }

    public static void openPlayerParticleMenu(Player player) {
        UUID uuid = player.getUniqueId();

        List<String> playerTrails = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PARTICLES, uuid);

        if (playerTrails == null || playerTrails.size() <= 0) {
            Inventory noTrails = Bukkit.createInventory(null, 0, ChatColor.RED + "You have no Player Trails!");
            player.openInventory(noTrails);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Player Trail Selection");
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        inv.setItem(26, editItem(new ItemStack(Material.ARMOR_STAND), ChatColor.GREEN + "Turn off Trail", new String[]{}));

        for (String trailType : playerTrails) {
            ItemStack itemStack = ParticleAPI.ParticleEffect.getByName(trailType).getSelectionItem();
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("playerTrailType", new NBTTagString(trailType));
            nmsStack.setTag(tag);
            inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStack), ChatColor.GREEN + ParticleAPI.ParticleEffect.getByName(trailType).getDisplayName(), new String[]{
            }));
        }

        player.openInventory(inv);
    }

    public static void openPlayerMountSkinMenu(Player player) {
        UUID uuid = player.getUniqueId();

        List<String> playerMountSkins = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNT_SKINS, uuid);

        if (playerMountSkins == null || playerMountSkins.size() <= 0) {
            Inventory noSkins = Bukkit.createInventory(null, 0, ChatColor.RED + "You have no Mount Skins!");
            player.openInventory(noSkins);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Mount Skin Selection");
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        inv.setItem(26, editItem(new ItemStack(Material.ARMOR_STAND), ChatColor.GREEN + "Turn off Mount Skin", new String[]{}));

        for (String skinType : playerMountSkins) {
            ItemStack itemStack = EnumMountSkins.getByName(skinType).getSelectionItem();
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("skinType", new NBTTagString(skinType));
            nmsStack.setTag(tag);
            inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStack), ChatColor.GREEN + EnumMountSkins.getByName(skinType).getDisplayName(), new String[]{
            }));
        }

        player.openInventory(inv);
    }

    public static void openPlayerProfileMenu(Player player) {

        Inventory inv = Bukkit.createInventory(null, 27, "Profile");
        inv.setItem(0, editItem(new ItemStack(Material.EXP_BOTTLE), ChatColor.GOLD + "Stat Distribution", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Place stat points on different attributes. ",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View Attributes."
        }));
        inv.setItem(1, editItem("Shrek", ChatColor.GOLD + "Friend List", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Add or remove friends.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View Friend list."
        }));
        inv.setItem(4, editItem(Utils.getPlayerHead(player), ChatColor.GREEN + "Player Profile", new String[]{
        }));
        inv.setItem(6, editItem(new ItemStack(Material.EYE_OF_ENDER), ChatColor.GOLD + "Trails", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Stand out amongst the rest",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "with a powerful trail.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View obtained trails.",
                ChatColor.WHITE + "Middle-Click:" + ChatColor.GREEN + " Receive trail item."
        }));
        inv.setItem(7, editItem(new ItemStack(Material.SADDLE), ChatColor.GOLD + "Mounts", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Travel Andalucia quickly.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View obtained mounts.",
                ChatColor.WHITE + "Middle Click:" + ChatColor.GREEN + " Receive Saddle."
        }));
        inv.setItem(8, editItem(new ItemStack(Material.NAME_TAG), ChatColor.GOLD + "Pets", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Travel with a cute companion.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View available pets.",
                ChatColor.WHITE + "Middle-Click:" + ChatColor.GREEN + " Receive Pet Leash."
        }));
        inv.setItem(16, editItem(new ItemStack(Material.CHEST), ChatColor.GOLD + "Storage Mule", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Inventory getting full on your travels?",
				ChatColor.GRAY.toString() + ChatColor.ITALIC + "purchase a Mule from the Animal Tamer.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " Spawn Storage Mule.",
                ChatColor.WHITE + "Middle-Click:" + ChatColor.GREEN + " Receive Mule Leash."
        }));
        inv.setItem(17, editItem(new ItemStack(Material.ARMOR_STAND), ChatColor.GOLD + "Mount Skins", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Equip your mount with a fancy skin.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View obtained mount skins."
        }));
        inv.setItem(18, editItem(new ItemStack(Material.EMERALD), ChatColor.GOLD + "E-Cash Vendor", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "E-Cash is obtained by voting and online store purchase.",
				ChatColor.GRAY + "http://dungeonrealms.net/shop",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " Open the E-Cash Vendor."
        }));
        inv.setItem(26, editItem(new ItemStack(Material.REDSTONE_COMPARATOR), ChatColor.GOLD + "Toggles", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Adjust preferences here.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " Open toggles menu."
        }));

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
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }

    public static ItemStack editItemWithShort(ItemStack itemStack, short shortID, String name, String[] lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        itemStack.setDurability(shortID);
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }

    /**
     * @param player
     */
    public static void openToggleMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Toggles");
        boolean toggle1 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, player.getUniqueId());
        boolean toggle2 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, player.getUniqueId());
        boolean toggle3 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DUEL, player.getUniqueId());
        boolean toggle4 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId());
        boolean toggle5 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_PVP, player.getUniqueId());
        boolean toggle6 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_RECEIVE_MESSAGE, player.getUniqueId());
        boolean toggle7 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_TRADE, player.getUniqueId());
        boolean toggle8 = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_TRADE_CHAT, player.getUniqueId());
        ItemStack stack1 = null;
        ItemStack stack2 = null;
        ItemStack stack3 = null;
        ItemStack stack4 = null;
        ItemStack stack5 = null;
        ItemStack stack6 = null;
        ItemStack stack7 = null;
        ItemStack stack8 = null;

        if (toggle1)
            stack1 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle CHAOTIC PREVENTION", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
        else
            stack1 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle CHAOTIC PREVENTION", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();

        if (toggle2)
            stack2 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle DEBUG", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
        else
            stack2 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle DEBUG", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();

        if (toggle3)
            stack3 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle DUEL", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
        else
            stack3 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle DUEL", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();

        if (toggle4)
            stack4 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle GLOBAL CHAT", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
        else
            stack4 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle GLOBAL CHAT", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();

        if (toggle5)
            stack5 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle PVP", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
        else
            stack5 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle PVP", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();

        if (toggle6)
            stack6 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle RECEIVE MESSAGE", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
        else
            stack6 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle RECEIVE MESSAGE", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();

        if (toggle7)
            stack7 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle TRADE", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
        else
            stack7 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle TRADE", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();

        if (toggle8)
            stack8 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), "Toggle TRADE CHAT", new String[]{(ChatColor.YELLOW + "Currently ON")}).build();
        else
            stack8 = new ItemBuilder().setItem(new ItemStack(Material.WOOL, 1, DyeColor.SILVER.getData()), "Toggle TRADE CHAT", new String[]{(ChatColor.YELLOW + "Currently OFF")}).build();

        ItemStack back = ItemManager.createItem(Material.BARRIER, ChatColor.YELLOW + "Back", new String[]{ChatColor.AQUA + "Back to the Profile Menu!"});

        inv.setItem(0, stack1);
        inv.setItem(1, stack2);
        inv.setItem(2, stack3);
        inv.setItem(3, stack4);
        inv.setItem(4, stack5);
        inv.setItem(5, stack6);
        inv.setItem(6, stack7);
        inv.setItem(7, stack8);
        inv.setItem(8, back);
        player.openInventory(inv);
    }

}
