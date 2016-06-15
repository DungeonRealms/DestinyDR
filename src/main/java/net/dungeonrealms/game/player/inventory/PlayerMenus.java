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
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.world.entities.types.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entities.types.mounts.EnumMounts;
import net.dungeonrealms.game.world.entities.types.pets.EnumPets;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
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
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
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

    private static ItemStack applySupportItemTags(ItemStack item, String playerName, UUID uuid) {
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("name", new NBTTagString(playerName));
        tag.set("uuid", new NBTTagString(uuid.toString()));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static void openSupportMenu(Player player, String playerName) {
        try {
            UUID uuid = Bukkit.getPlayer(playerName) != null ? Bukkit.getPlayer(playerName).getUniqueId() : UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
            DatabaseAPI.getInstance().requestPlayer(uuid);
            String playerRank = Rank.getInstance().getRank(uuid);
            if (!Rank.isDev(player) && (playerRank.equalsIgnoreCase("gm") || playerRank.equalsIgnoreCase("dev"))) {
                player.sendMessage(ChatColor.RED + "You " + ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "DO NOT" + ChatColor.RED + " have permission to manage this user.");
                return;
            }

            ItemStack item;

            Inventory inv = Bukkit.createInventory(null, 45, "Support Tools");

            item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                    ChatColor.WHITE + "Rank: " + Rank.rankFromPrefix(playerRank),
                    ChatColor.WHITE + "Level: " + DatabaseAPI.getInstance().getData(EnumData.LEVEL, uuid),
                    ChatColor.WHITE + "Experience: " + DatabaseAPI.getInstance().getData(EnumData.EXPERIENCE, uuid),
                    ChatColor.WHITE + "E-Cash: " + DatabaseAPI.getInstance().getData(EnumData.ECASH, uuid),
                    ChatColor.WHITE + "Bank Balance: " + DatabaseAPI.getInstance().getData(EnumData.GEMS, uuid),
                    ChatColor.WHITE + "Hearthstone Location: " + DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid),
                    ChatColor.WHITE + "Alignment: " + Utils.ucfirst(DatabaseAPI.getInstance().getData(EnumData.ALIGNMENT, uuid).toString())
            });
            inv.setItem(4, applySupportItemTags(item, playerName, uuid));

            // Rank Manager
            item = editItem(new ItemStack(Material.DIAMOND), ChatColor.GOLD + "Rank Manager", new String[]{
                    ChatColor.WHITE + "Modify the rank of " + playerName + ".",
                    ChatColor.WHITE + "Current rank: " + Rank.rankFromPrefix(playerRank)
            });
            inv.setItem(19, applySupportItemTags(item, playerName, uuid));

            // Level Manager
            item = editItem(new ItemStack(Material.EXP_BOTTLE), ChatColor.GOLD + "Level Manager", new String[]{
                    ChatColor.WHITE + "Manage the level/experience of " + playerName + ".",
                    ChatColor.WHITE + "Current level: " + DatabaseAPI.getInstance().getData(EnumData.LEVEL, uuid),
                    ChatColor.WHITE + "Current EXP: " + DatabaseAPI.getInstance().getData(EnumData.EXPERIENCE, uuid)
            });
            inv.setItem(22, applySupportItemTags(item, playerName, uuid));

            // E-Cash Manager
            item = editItem(new ItemStack(Material.GOLDEN_CARROT), ChatColor.GOLD + "E-Cash Manager", new String[]{
                    ChatColor.WHITE + "Manage the e-cash of " + playerName + ".",
                    ChatColor.WHITE + "Current E-Cash: " + DatabaseAPI.getInstance().getData(EnumData.ECASH, uuid)
            });
            inv.setItem(25, applySupportItemTags(item, playerName, uuid));

            // Bank Manager
            item = editItem(new ItemStack(Material.ENDER_CHEST), ChatColor.GOLD + "Bank Manager", new String[]{
                    ChatColor.WHITE + "Manage the bank of " + playerName + ".",
                    ChatColor.WHITE + "Current bank balance: " + DatabaseAPI.getInstance().getData(EnumData.GEMS, uuid)
            });
            inv.setItem(28, applySupportItemTags(item, playerName, uuid));

            // Hearthstone Manager
            item = editItem(new ItemStack(Material.QUARTZ_ORE), ChatColor.GOLD + "Hearthstone Manager", new String[]{
                    ChatColor.WHITE + "Manage the Hearthstone Location of " + playerName + ".",
                    ChatColor.WHITE + "Current location: " + DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid)
            });
            inv.setItem(31, applySupportItemTags(item, playerName, uuid));

            // Shop Packages
            item = editItem(new ItemStack(Material.BOOK_AND_QUILL), ChatColor.GOLD + "Cosmetics", new String[]{
                    ChatColor.WHITE + "Manage cosmetics of " + playerName + "."
            });
            inv.setItem(34, applySupportItemTags(item, playerName, uuid));

            // PLACEHOLDER
            /*item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()), ChatColor.GOLD + "PLACEHOLDER", new String[]{
                    ChatColor.WHITE + "This is a placeholder, it does nothing.",
                    "",
                    ChatColor.WHITE + "One day, a tool for support will go here."
            });
            inv.setItem(34, applySupportItemTags(item, playerName, uuid));*/

            player.openInventory(inv);
        } catch (IllegalArgumentException ex) {
            // This exception is thrown if the UUID doesn't exist in the database.
            player.sendMessage(ChatColor.RED + "Unable to identify anybody with the player name: " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.RED + "!");
            player.sendMessage(ChatColor.RED + "It's likely the user has never played on the Dungeon Realms servers before.");
        }
    }

    public static void openSupportRankMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Rank)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.GRAY.getData()), Rank.rankFromPrefix("default"), new String[]{
                ChatColor.WHITE + "Set user rank to: Default"
        });
        inv.setItem(20, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), Rank.rankFromPrefix("sub"), new String[]{
                ChatColor.WHITE + "Set user rank to: Subscriber",
                ChatColor.WHITE + "User will have access to the subscriber server."
        });
        inv.setItem(21, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.ORANGE.getData()), Rank.rankFromPrefix("sub+"), new String[]{
                ChatColor.WHITE + "Set user rank to: Subscriber+",
                ChatColor.WHITE + "User will have access to the subscriber server."
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.CYAN.getData()), Rank.rankFromPrefix("sub++"), new String[]{
                ChatColor.WHITE + "Set user rank to: Subscriber++",
                ChatColor.WHITE + "User will have access to the subscriber server."
        });
        inv.setItem(23, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.WHITE.getData()), Rank.rankFromPrefix("pmod"), new String[]{
                ChatColor.WHITE + "Set user rank to: Player Moderator",
                ChatColor.WHITE + "User will have access to the subscriber server.",
                ChatColor.WHITE + "User will have access to limited moderation tools."
        });
        inv.setItem(24, applySupportItemTags(item, playerName, uuid));

        // Ranks that can only be applied by developers.
        if (Rank.isDev(player)) {
            item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.CYAN.getData()), Rank.rankFromPrefix("builder"), new String[]{
                    ChatColor.WHITE + "Set user rank to: Builder",
                    ChatColor.WHITE + "User will have identical permissions as a Subscriber."
            });
            inv.setItem(29, applySupportItemTags(item, playerName, uuid));

            item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.RED.getData()), Rank.rankFromPrefix("youtube"), new String[]{
                    ChatColor.WHITE + "Set user rank to: YouTuber",
                    ChatColor.WHITE + "User will have identical permissions as a Subscriber.",
                    ChatColor.WHITE + "User will have access to a special 'YouTube' server."
            });
            inv.setItem(30, applySupportItemTags(item, playerName, uuid));

            item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getData()), Rank.rankFromPrefix("support"), new String[]{
                    ChatColor.WHITE + "Set user rank to: Support Agent",
                    ChatColor.WHITE + "User will " + ChatColor.BOLD + "NOT" + ChatColor.WHITE + " have access to moderation tools.",
                    ChatColor.WHITE + "User will have access to a special command set.",
                    ChatColor.WHITE + "User will have access a special 'Support' server."
            });
            inv.setItem(32, applySupportItemTags(item, playerName, uuid));

            item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.LIGHT_BLUE.getData()), Rank.rankFromPrefix("gm"), new String[]{
                    ChatColor.WHITE + "Set user rank to: Game Master",
                    ChatColor.WHITE + "User will " + ChatColor.BOLD + "NOT" + ChatColor.WHITE + " have access to support tools.",
                    ChatColor.WHITE + "User will have access to almost all commands.",
                    ChatColor.WHITE + "User will have access to the MASTER server."
            });
            inv.setItem(33, applySupportItemTags(item, playerName, uuid));
        }

        player.openInventory(inv);
    }

    public static void openSupportLevelMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Level)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()), ChatColor.GOLD + "PLACEHOLDER", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openSupportECashMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (E-Cash)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()), ChatColor.GOLD + "PLACEHOLDER", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openSupportBankMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Bank)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()), ChatColor.GOLD + "PLACEHOLDER", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openSupportHearthstoneMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Hearthstone)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("cyrennica") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Cyrennica", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(18, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("harrison_field") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Harrison Fields", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(19, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("dark_oak") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Dark Oak Tavern", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(20, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("gloomy_hollows") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Gloomy Hollows", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(21, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("tripoli") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Tripoli", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("trollsbane") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Trollsbans Tavern", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(23, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("crestguard") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Crestguard Keep", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(24, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, uuid).toString().equalsIgnoreCase("deadpeaks") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Deadpeaks Mountain", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(25, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openSupportCoemeticssMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Cosmetics)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        // Rank Manager
        item = editItem(new ItemStack(Material.EYE_OF_ENDER), ChatColor.GOLD + "Trail Manager", new String[]{
                ChatColor.WHITE + "Modify trails of " + playerName + "."
        });
        inv.setItem(19, applySupportItemTags(item, playerName, uuid));

        // Level Manager
        item = editItem(new ItemStack(Material.SADDLE), ChatColor.GOLD + "Mount / Mule Manager", new String[]{
                ChatColor.WHITE + "Manage mounts / mules of " + playerName + "."
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        // E-Cash Manager
        item = editItem(new ItemStack(Material.NAME_TAG), ChatColor.GOLD + "Pet Manager", new String[]{
                ChatColor.WHITE + "Manage pets of " + playerName + "."
        });
        inv.setItem(25, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openSupportTrailsMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Trails)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()), ChatColor.GOLD + "Trail", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(18, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openSupportMountsMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Mounts)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()), ChatColor.GOLD + "Mount", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(18, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openSupportPetsMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Pets)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()), ChatColor.GOLD + "Pet", new String[]{
                ChatColor.WHITE + "This is a placeholder, it does nothing.",
                "",
                ChatColor.WHITE + "One day, a tool for support will go here."
        });
        inv.setItem(18, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

}
