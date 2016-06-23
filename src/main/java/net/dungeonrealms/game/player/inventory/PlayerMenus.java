package net.dungeonrealms.game.player.inventory;

import net.dungeonrealms.API;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.handlers.MailHandler;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mechanics.PlayerManager;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.world.entities.types.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entities.types.mounts.EnumMounts;
import net.dungeonrealms.game.world.entities.types.pets.EnumPets;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
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

            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
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

            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
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

        inv.setItem(8, editItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Tax", new String[]{
                ChatColor.GRAY + "Tax: " + ChatColor.AQUA + "5 GEMS"
        }));

        int slot = 9;
        for (String s : mail) {
            String from = s.split(",")[0];
            long unix = Long.valueOf(s.split(",")[1]);
            String serializedItem = s.split(",")[2];
            Date sentDate = new Date(unix * 1000);
            ItemStack item = ItemSerialization.itemStackFromBase64(serializedItem);

            ItemStack mailTemplateItem = MailHandler.getInstance().setItemAsMail(editItem(item, new String[]{
                    ChatColor.GRAY + "From: " + ChatColor.AQUA + from,
                    ChatColor.GRAY + "Sent: " + ChatColor.AQUA + sentDate,
                    "",
                    ChatColor.GRAY.toString() + ChatColor.UNDERLINE + "Left-Click:" + ChatColor.GREEN + " Receive item."
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
            String petName;
            if (pet.contains("@")) {
                petType = pet.split("@")[0];
                petName = pet.split("@")[1];
            } else {
                petType = pet;
                petName = EnumPets.getByName(petType).getDisplayName();
            }
            EnumPets pets = EnumPets.getByName(petType);
            if (pets == null) {
                //UH OH BOYZ. HOW'D THAT GET HERE? SOMEONE EDITED MONGO WRONGLY
                continue;
            }
            ItemStack itemStack = new ItemStack(Material.MONSTER_EGG, 1, (short) pets.getEggShortData());
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("petType", new NBTTagString(petType));
            tag.set("petName", new NBTTagString(petName));
            nmsStack.setTag(tag);
            inv.addItem(editItemWithShort(CraftItemStack.asBukkitCopy(nmsStack), (short) pets.getEggShortData(), pets.getDisplayName(), new String[]{
                    ChatColor.GREEN + "Left Click: " + ChatColor.WHITE + "Summon Pet",
                    ChatColor.GREEN + "Right Click: " + ChatColor.WHITE + "Rename Pet",
                    "",
                    ChatColor.GREEN + "Name: " + ChatColor.WHITE + petName,
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

        if (playerMounts.contains(EnumMounts.TIER4_HORSE.getRawName())) {
            mountType = EnumMounts.TIER4_HORSE.getRawName();
        } else if (playerMounts.contains(EnumMounts.TIER3_HORSE.getRawName())) {
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

        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
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
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
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
            itemStack.setDurability(EnumMountSkins.getByName(skinType).getShortID());
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("skinType", new NBTTagString(skinType));
            nmsStack.setTag(tag);
            inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStack), ChatColor.GREEN + EnumMountSkins.getByName(skinType).getDisplayName(), new String[]{
            }));
        }

        player.openInventory(inv);
    }

    public static void openPlayerAchievementsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Achievements");
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        inv.setItem(2, editItem(new ItemStack(Material.MAP), ChatColor.GOLD + "Exploration", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Achievements related to Exploration",
                "",
        }));
        inv.setItem(3, editItem(new ItemStack(Material.GOLD_SWORD), ChatColor.GOLD + "Combat", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Achievements related to Combat",
                "",
        }));
        inv.setItem(4, editItem(new ItemStack(Material.ARMOR_STAND), ChatColor.GOLD + "Character", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Achievements related to Character customization",
                "",
        }));
        inv.setItem(5, editItem(new ItemStack(Material.EMERALD), ChatColor.GOLD + "Currency", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Achievements related to Currency",
                "",
        }));
        inv.setItem(6, editItem(new ItemStack(Material.WRITTEN_BOOK), ChatColor.GOLD + "Social", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Social Achievements",
                "",
        }));
        inv.setItem(7, editItem(new ItemStack(Material.NETHER_STAR), ChatColor.GOLD + "Realm", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Achievements related to your Realm",
                "",
        }));
        player.openInventory(inv);
    }

    public static void openExplorationAchievementMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Exploration Achievements");
        UUID uuid = player.getUniqueId();
        List<String> playerAchievements = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ACHIEVEMENTS, uuid);

        boolean noAchievements;
        noAchievements = (playerAchievements == null || playerAchievements.size() <= 0);
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (Achievements.EnumAchievements achievement : Achievements.EnumAchievements.values()) {
            if (achievement.getMongoName().contains(".explorer_")) {
                if (noAchievements || !playerAchievements.contains(achievement.getMongoName())) {
                    if (achievement.getHide()) continue;

                    inv.addItem(editItem(new ItemStack(Material.MAGMA_CREAM), ChatColor.RED + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.RED.toString() + ChatColor.BOLD + "Incomplete",
                    }));
                } else {
                    inv.addItem(editItem(new ItemStack(Material.SLIME_BALL), ChatColor.GREEN + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "Complete",
                    }));
                }
            }
        }
        player.openInventory(inv);
    }

    public static void openSocialAchievementMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Social Achievements");
        UUID uuid = player.getUniqueId();
        List<String> playerAchievements = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ACHIEVEMENTS, uuid);

        boolean noAchievements;
        noAchievements = (playerAchievements == null || playerAchievements.size() <= 0);
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (Achievements.EnumAchievements achievement : Achievements.EnumAchievements.values()) {
            if (achievement.getMongoName().contains(".social_")) {
                if (noAchievements || !playerAchievements.contains(achievement.getMongoName())) {
                    if (achievement.getHide()) continue;

                    inv.addItem(editItem(new ItemStack(Material.MAGMA_CREAM), ChatColor.RED + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.RED.toString() + ChatColor.BOLD + "Incomplete",
                    }));
                } else {
                    inv.addItem(editItem(new ItemStack(Material.SLIME_BALL), ChatColor.GREEN + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "Complete",
                    }));
                }
            }
        }
        player.openInventory(inv);
    }

    public static void openCurrencyAchievementMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Currency Achievements");
        UUID uuid = player.getUniqueId();
        List<String> playerAchievements = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ACHIEVEMENTS, uuid);

        boolean noAchievements;
        noAchievements = (playerAchievements == null || playerAchievements.size() <= 0);
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (Achievements.EnumAchievements achievement : Achievements.EnumAchievements.values()) {
            if (achievement.getMongoName().contains(".currency_")) {
                if (noAchievements || !playerAchievements.contains(achievement.getMongoName())) {
                    if (achievement.getHide()) continue;

                    inv.addItem(editItem(new ItemStack(Material.MAGMA_CREAM), ChatColor.RED + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.RED.toString() + ChatColor.BOLD + "Incomplete",
                    }));
                } else {
                    inv.addItem(editItem(new ItemStack(Material.SLIME_BALL), ChatColor.GREEN + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "Complete",
                    }));
                }
            }
        }
        player.openInventory(inv);
    }

    public static void openCombatAchievementMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Combat Achievements");
        UUID uuid = player.getUniqueId();
        List<String> playerAchievements = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ACHIEVEMENTS, uuid);

        boolean noAchievements;
        noAchievements = (playerAchievements == null || playerAchievements.size() <= 0);
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (Achievements.EnumAchievements achievement : Achievements.EnumAchievements.values()) {
            if (achievement.getMongoName().contains(".combat_")) {
                if (noAchievements || !playerAchievements.contains(achievement.getMongoName())) {
                    if (achievement.getHide()) continue;

                    inv.addItem(editItem(new ItemStack(Material.MAGMA_CREAM), ChatColor.RED + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.RED.toString() + ChatColor.BOLD + "Incomplete",
                    }));
                } else {
                    inv.addItem(editItem(new ItemStack(Material.SLIME_BALL), ChatColor.GREEN + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "Complete",
                    }));
                }
            }
        }
        player.openInventory(inv);
    }

    public static void openRealmAchievementMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Realm Achievements");
        UUID uuid = player.getUniqueId();
        List<String> playerAchievements = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ACHIEVEMENTS, uuid);

        boolean noAchievements;
        noAchievements = (playerAchievements == null || playerAchievements.size() <= 0);
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (Achievements.EnumAchievements achievement : Achievements.EnumAchievements.values()) {
            if (achievement.getMongoName().contains(".realm_")) {
                if (noAchievements || !playerAchievements.contains(achievement.getMongoName())) {
                    if (achievement.getHide()) continue;

                    inv.addItem(editItem(new ItemStack(Material.MAGMA_CREAM), ChatColor.RED + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.RED.toString() + ChatColor.BOLD + "Incomplete",
                    }));
                } else {
                    inv.addItem(editItem(new ItemStack(Material.SLIME_BALL), ChatColor.GREEN + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "Complete",
                    }));
                }
            }
        }
        player.openInventory(inv);
    }

    public static void openCharacterAchievementMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Character Achievements");
        UUID uuid = player.getUniqueId();
        List<String> playerAchievements = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ACHIEVEMENTS, uuid);

        boolean noAchievements;
        noAchievements = (playerAchievements == null || playerAchievements.size() <= 0);
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (Achievements.EnumAchievements achievement : Achievements.EnumAchievements.values()) {
            if (achievement.getMongoName().contains(".character_")) {
                if (noAchievements || !playerAchievements.contains(achievement.getMongoName())) {
                    if (achievement.getHide()) continue;

                    inv.addItem(editItem(new ItemStack(Material.MAGMA_CREAM), ChatColor.RED + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.RED.toString() + ChatColor.BOLD + "Incomplete",
                    }));
                } else {
                    inv.addItem(editItem(new ItemStack(Material.SLIME_BALL), ChatColor.GREEN + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "Complete",
                    }));
                }
            }
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
        inv.setItem(6, editItem(new ItemStack(Material.EYE_OF_ENDER), ChatColor.GOLD + "Effects", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Stand out amongst the rest",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "with powerful effects.",
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
        inv.setItem(16, editItem(new ItemStack(Material.LEASH), ChatColor.GOLD + "Storage Mule", new String[]{
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
        inv.setItem(24, editItem(new ItemStack(Material.TORCH), ChatColor.GOLD + "Achievements", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Check your progress.",
                "",
                ChatColor.WHITE + "Use: View achievements."
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

    public static ItemStack editItem(ItemStack itemStack, String[] lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }

    /**
     * @param player
     */
    public static void openToggleMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, ((int) Math.ceil((1 + PlayerManager.PlayerToggles.values().length) / 9.0) * 9), "Toggles");

        int i = 0;
        for (PlayerManager.PlayerToggles playerToggles : PlayerManager.PlayerToggles.values()) {
            boolean isToggled = (boolean) DatabaseAPI.getInstance().getData(playerToggles.getDbField(), player.getUniqueId());
            inv.setItem(i, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, (short) (isToggled ? 10 : 8)), (isToggled ? ChatColor.GREEN : ChatColor.RED) + "/" + playerToggles.getCommandName(), new String[]{(ChatColor.GRAY + playerToggles.getDescription())}).build());
            i++;
        }

        inv.setItem(i, ItemManager.createItem(Material.BARRIER, ChatColor.YELLOW + "Back", new String[]{ChatColor.AQUA + "Back to the Profile Menu!"}));
        player.openInventory(inv);
    }

    /*
     * -- Customer Support --
     */



}
