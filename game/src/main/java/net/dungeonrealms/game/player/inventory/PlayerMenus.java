package net.dungeonrealms.game.player.inventory;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.handler.MailHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveOpenProfile;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
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

import java.util.*;

/**
 * Created by Nick on 9/29/2015.
 */
public class PlayerMenus {
//TODO: DisplayItem Lore on stuff here.

    public static void openFriendsMenu(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        if(wrapper == null)return;

        Map<UUID, Integer> friends = wrapper.getFriendsList();
//        ArrayList<String> friends = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIENDS, uuid);

        Inventory inv = Bukkit.createInventory(null, 54, "Friends");

        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to go back!",
                ChatColor.GRAY + "Display Item"
        }));


        int slot = 9;
        for (Map.Entry<UUID, Integer> s : friends.entrySet()) {
            String name = SQLDatabaseAPI.getInstance().getUsernameFromUUID(s.getKey());
            ItemStack stack = editItem(name, name, new String[]{
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Right-Click " + ChatColor.GRAY + "to delete!",
                    ChatColor.GRAY + "Display Item"
            });

            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("info", new NBTTagString(s.getKey().toString()));
            nmsStack.setTag(tag);


            inv.setItem(slot, CraftItemStack.asBukkitCopy(nmsStack));

            if (slot >= 54) break;
            slot++;
        }

        player.openInventory(inv);

    }

    public static void openFriendInventory(Player player) {
        UUID uuid = player.getUniqueId();
//        ArrayList<String> friendRequest = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIEND_REQUESTS, uuid);

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        if(wrapper == null)return;

        Inventory inv = Bukkit.createInventory(null, 45, "Friend Management");

        inv.setItem(0, editItem(new ItemStack(Material.BOOK_AND_QUILL), ChatColor.GREEN + "Add Friend", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to add friend!",
                ChatColor.GRAY + "Display Item"
        }));

        inv.setItem(1, editItem(new ItemStack(Material.CHEST), ChatColor.GREEN + "View Friend", new String[]{
                ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to view friends!",
                ChatColor.GRAY + "Display Item"
        }));

        int slot = 9;
        for (Map.Entry<UUID, Integer> from : wrapper.getPendingFriends().entrySet()) {
            String name = SQLDatabaseAPI.getInstance().getUsernameFromUUID(from.getKey());
//            String name = DatabaseAPI.getInstance().getOfflineName(UUID.fromString(from));
            ItemStack stack = editItem(name, name, new String[]{
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to accept!",
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Right-Click " + ChatColor.GRAY + "to deny!",
                    ChatColor.GRAY + "Display Item"
            });

            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("info", new NBTTagString(from.getKey().toString()));
            nmsStack.setTag(tag);

            inv.setItem(slot, CraftItemStack.asBukkitCopy(nmsStack));

            if (slot >= 44) break;
            slot++;
        }

        player.openInventory(inv);

    }


    public static void openPlayerPetMenu(Player player) {
        UUID uuid = player.getUniqueId();

        if (GameAPI.getGamePlayer(player) != null && GameAPI.getGamePlayer(player).isJailed()) {
            Inventory jailed = Bukkit.createInventory(null, 0, ChatColor.RED + "You are jailed");
            player.openInventory(jailed);
            return;
        }

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return;

        Set<String> playerPets = wrapper.getPetsUnlocked();

        if (Rank.isSubscriber(player))
            for (EnumPets p : EnumPets.values()) {
                if (p == EnumPets.BABY_HORSE)
                    continue;

                playerPets.add(p.getRawName());
            }


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
                    ChatColor.GRAY + "Display Item"
            }));
        }

        player.openInventory(inv);
    }

    public static void openPlayerMountMenu(Player player) {
        UUID uuid = player.getUniqueId();

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return;

        if (GameAPI.getGamePlayer(player) != null && GameAPI.getGamePlayer(player).isJailed()) {
            Inventory jailed = Bukkit.createInventory(null, 0, ChatColor.RED + "You are jailed");
            player.openInventory(jailed);
            return;
        }

        HashSet<String> playerMounts = wrapper.getMountsUnlocked();
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
                ChatColor.GRAY + "Display Item"
        }));

        player.openInventory(inv);
    }

    public static void openPlayerParticleMenu(Player player) {
        UUID uuid = player.getUniqueId();

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return;

        Set<String> playerTrails = wrapper.getParticlesUnlocked();

        if (Rank.isSubscriber(player))
            for (ParticleAPI.ParticleEffect effect : ParticleAPI.ParticleEffect.values())
                playerTrails.add(effect.getRawName());

        if (playerTrails == null || playerTrails.size() <= 0) {
            Inventory noTrails = Bukkit.createInventory(null, 0, ChatColor.RED + "You have no Player Effects!");
            player.openInventory(noTrails);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Player Effect Selection");
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));
        inv.setItem(26, editItem(new ItemStack(Material.ARMOR_STAND), ChatColor.GREEN + "Turn off Effect", new String[]{}));

        for (String trailType : playerTrails) {
            ItemStack itemStack = ParticleAPI.ParticleEffect.getByName(trailType).getSelectionItem();
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("playerTrailType", new NBTTagString(trailType));
            nmsStack.setTag(tag);
            inv.addItem(editItem(CraftItemStack.asBukkitCopy(nmsStack), ChatColor.GREEN + ParticleAPI.ParticleEffect.getByName(trailType).getDisplayName(), new String[]{
                    ChatColor.GRAY + "Display Item"
            }));
        }

        player.openInventory(inv);
    }

    public static void openPlayerMountSkinMenu(Player player) {
        UUID uuid = player.getUniqueId();

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return;

        HashSet<String> playerMountSkins = wrapper.getMountSkins();

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
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Achievements related to exploration.",
                "",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(3, editItem(new ItemStack(Material.GOLD_SWORD), ChatColor.GOLD + "Combat", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Achievements related to combat.",
                "",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(4, editItem(new ItemStack(Material.ARMOR_STAND), ChatColor.GOLD + "Character", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Achievements related to character customization.",
                "",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(5, editItem(new ItemStack(Material.EMERALD), ChatColor.GOLD + "Currency", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Achievements related to currency.",
                "",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(6, editItem(new ItemStack(Material.WRITTEN_BOOK), ChatColor.GOLD + "Social", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Social Achievements",
                "",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(7, editItem(new ItemStack(Material.NETHER_STAR), ChatColor.GOLD + "Realm", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Achievements related to your realm.",
                "",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(8, editItem(new ItemStack(Material.GOLD_INGOT), ChatColor.GOLD + "Event", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Achievements related to event participation.",
                "",
                ChatColor.GRAY + "Display Item"
        }));
        player.openInventory(inv);
    }

    public static void openExplorationAchievementMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "Exploration Achievements");
        UUID uuid = player.getUniqueId();
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return;
        List<String> playerAchievements = wrapper.getAchievements();

        boolean noAchievements;
        noAchievements = (playerAchievements == null || playerAchievements.size() <= 0);
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (Achievements.EnumAchievements achievement : Achievements.EnumAchievements.values()) {
            if (achievement.getDBName().contains(".explorer_")) {
                if (noAchievements || !playerAchievements.contains(achievement.getDBName())) {
                    if (achievement.getHide()) continue;

                    inv.addItem(editItem(new ItemStack(Material.MAGMA_CREAM), ChatColor.RED + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.RED.toString() + ChatColor.BOLD + "Incomplete",
                            ChatColor.GRAY + "Display Item"
                    }));
                } else {
                    inv.addItem(editItem(new ItemStack(Material.SLIME_BALL), ChatColor.GREEN + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "Complete",
                            ChatColor.GRAY + "Display Item"
                    }));
                }
            }
        }
        player.openInventory(inv);
    }

    public static void openSocialAchievementMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "Social Achievements");
        UUID uuid = player.getUniqueId();
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return;
        List<String> playerAchievements = wrapper.getAchievements();

        boolean noAchievements;
        noAchievements = (playerAchievements == null || playerAchievements.size() <= 0);
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (Achievements.EnumAchievements achievement : Achievements.EnumAchievements.values()) {
            if (achievement.getDBName().contains(".social_")) {
                if (noAchievements || !playerAchievements.contains(achievement.getDBName())) {
                    if (achievement.getHide()) continue;

                    inv.addItem(editItem(new ItemStack(Material.MAGMA_CREAM), ChatColor.RED + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.RED.toString() + ChatColor.BOLD + "Incomplete",
                            ChatColor.GRAY + "Display Item"
                    }));
                } else {
                    inv.addItem(editItem(new ItemStack(Material.SLIME_BALL), ChatColor.GREEN + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "Complete",
                            ChatColor.GRAY + "Display Item"
                    }));
                }
            }
        }
        player.openInventory(inv);
    }

    public static void openCurrencyAchievementMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "Currency Achievements");
        UUID uuid = player.getUniqueId();
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return;
        List<String> playerAchievements = wrapper.getAchievements();

        boolean noAchievements;
        noAchievements = (playerAchievements == null || playerAchievements.size() <= 0);
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (Achievements.EnumAchievements achievement : Achievements.EnumAchievements.values()) {
            if (achievement.getDBName().contains(".currency_")) {
                if (noAchievements || !playerAchievements.contains(achievement.getDBName())) {
                    if (achievement.getHide()) continue;

                    inv.addItem(editItem(new ItemStack(Material.MAGMA_CREAM), ChatColor.RED + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.RED.toString() + ChatColor.BOLD + "Incomplete",
                            ChatColor.GRAY + "Display Item"
                    }));
                } else {
                    inv.addItem(editItem(new ItemStack(Material.SLIME_BALL), ChatColor.GREEN + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "Complete",
                            ChatColor.GRAY + "Display Item"
                    }));
                }
            }
        }
        player.openInventory(inv);
    }

    public static void openCombatAchievementMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "Combat Achievements");
        UUID uuid = player.getUniqueId();
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return;
        List<String> playerAchievements = wrapper.getAchievements();

        boolean noAchievements;
        noAchievements = (playerAchievements == null || playerAchievements.size() <= 0);
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (Achievements.EnumAchievements achievement : Achievements.EnumAchievements.values()) {
            if (achievement.getDBName().contains(".combat_")) {
                if (noAchievements || !playerAchievements.contains(achievement.getDBName())) {
                    if (achievement.getHide()) continue;

                    inv.addItem(editItem(new ItemStack(Material.MAGMA_CREAM), ChatColor.RED + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.RED.toString() + ChatColor.BOLD + "Incomplete",
                            ChatColor.GRAY + "Display Item"
                    }));
                } else {
                    inv.addItem(editItem(new ItemStack(Material.SLIME_BALL), ChatColor.GREEN + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "Complete",
                            ChatColor.GRAY + "Display Item"
                    }));
                }
            }
        }
        player.openInventory(inv);
    }

    public static void openRealmAchievementMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Realm Achievements");
        UUID uuid = player.getUniqueId();
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return;
        List<String> playerAchievements = wrapper.getAchievements();

        boolean noAchievements;
        noAchievements = (playerAchievements == null || playerAchievements.size() <= 0);
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (Achievements.EnumAchievements achievement : Achievements.EnumAchievements.values()) {
            if (achievement.getDBName().contains(".realm_")) {
                if (noAchievements || !playerAchievements.contains(achievement.getDBName())) {
                    if (achievement.getHide()) continue;

                    inv.addItem(editItem(new ItemStack(Material.MAGMA_CREAM), ChatColor.RED + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.RED.toString() + ChatColor.BOLD + "Incomplete",
                            ChatColor.GRAY + "Display Item"
                    }));
                } else {
                    inv.addItem(editItem(new ItemStack(Material.SLIME_BALL), ChatColor.GREEN + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "Complete",
                            ChatColor.GRAY + "Display Item"
                    }));
                }
            }
        }
        player.openInventory(inv);
    }

    public static void openEventAchievementMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Event Achievements");
        UUID uuid = player.getUniqueId();
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return;
        List<String> playerAchievements = wrapper.getAchievements();

        boolean noAchievements;
        noAchievements = (playerAchievements == null || playerAchievements.size() <= 0);
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (Achievements.EnumAchievements achievement : Achievements.EnumAchievements.values()) {
            if (achievement.getDBName().contains(".event")) {
                if (noAchievements || !playerAchievements.contains(achievement.getDBName())) {
                    if (achievement.getHide()) continue;

                    inv.addItem(editItem(new ItemStack(Material.MAGMA_CREAM), ChatColor.RED + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.RED.toString() + ChatColor.BOLD + "Incomplete",
                            ChatColor.GRAY + "Display Item"
                    }));
                } else {
                    inv.addItem(editItem(new ItemStack(Material.SLIME_BALL), ChatColor.GREEN + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "Complete",
                            ChatColor.GRAY + "Display Item"
                    }));
                }
            }
        }
        player.openInventory(inv);
    }

    public static void openCharacterAchievementMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, "Character Achievements");
        UUID uuid = player.getUniqueId();
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return;
        List<String> playerAchievements = wrapper.getAchievements();

        boolean noAchievements;
        noAchievements = (playerAchievements == null || playerAchievements.size() <= 0);
        inv.setItem(0, editItem(new ItemStack(Material.BARRIER), ChatColor.GREEN + "Back", new String[]{}));

        for (Achievements.EnumAchievements achievement : Achievements.EnumAchievements.values()) {
            if (achievement.getDBName().contains(".character_")) {
                if (noAchievements || !playerAchievements.contains(achievement.getDBName())) {
                    if (achievement.getHide()) continue;

                    inv.addItem(editItem(new ItemStack(Material.MAGMA_CREAM), ChatColor.RED + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.RED.toString() + ChatColor.BOLD + "Incomplete",
                            ChatColor.GRAY + "Display Item"
                    }));
                } else {
                    inv.addItem(editItem(new ItemStack(Material.SLIME_BALL), ChatColor.GREEN + achievement.getName(), new String[]{
                            "",
                            ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                            ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP",
                            "",
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "Complete",
                            ChatColor.GRAY + "Display Item"
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
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View Attributes.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(1, editItem("Shrek", ChatColor.GOLD + "Friend List", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Add or remove friends.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View Friend list.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(4, editItem(Utils.getPlayerHead(player), ChatColor.GREEN + "Player Profile", new String[]{
        }));
        inv.setItem(6, editItem(new ItemStack(Material.EYE_OF_ENDER), ChatColor.GOLD + "Effects", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Stand out amongst the rest",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "with powerful effects.",
                "",
                ChatColor.WHITE + "Left-Click:" + ChatColor.GREEN + " View obtained effect.",
                ChatColor.WHITE + "Right-Click:" + ChatColor.GREEN + " Receive effect item.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(7, editItem(new ItemStack(Material.SADDLE), ChatColor.GOLD + "Mounts", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Travel Andalucia quickly.",
                "",
                ChatColor.WHITE + "Left-Click:" + ChatColor.GREEN + " View obtained mounts.",
                ChatColor.WHITE + "Right-Click:" + ChatColor.GREEN + " Receive Saddle.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(8, editItem(new ItemStack(Material.NAME_TAG), ChatColor.GOLD + "Pets", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Travel with a cute companion.",
                "",
                ChatColor.WHITE + "Left-Click:" + ChatColor.GREEN + " View available pets.",
                ChatColor.WHITE + "Right-Click:" + ChatColor.GREEN + " Receive Pet Leash.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(16, editItem(new ItemStack(Material.LEASH), ChatColor.GOLD + "Storage Mule", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Inventory getting full on your travels?",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "purchase a Mule from the Animal Tamer.",
                "",
                ChatColor.WHITE + "Left-Click:" + ChatColor.GREEN + " Spawn Storage Mule.",
                ChatColor.WHITE + "Right-Click:" + ChatColor.GREEN + " Receive Mule Leash.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(17, editItem(new ItemStack(Material.ARMOR_STAND), ChatColor.GOLD + "Mount Skins", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Equip your mount with a fancy skin.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View obtained mount skins.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(18, editItem(new ItemStack(Material.EMERALD), ChatColor.GOLD + "E-Cash Vendor", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "E-Cash is obtained by voting and online store purchase.",
                ChatColor.GRAY + "http://dungeonrealms.net/shop",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " Open the E-Cash Vendor.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(24, editItem(new ItemStack(Material.COMPASS), ChatColor.GOLD + "Achievements", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Check your progress.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " View achievements.",
                ChatColor.GRAY + "Display Item"
        }));
        inv.setItem(26, editItem(new ItemStack(Material.REDSTONE_COMPARATOR), ChatColor.GOLD + "Toggles", new String[]{
                "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Adjust preferences here.",
                "",
                ChatColor.WHITE + "Use:" + ChatColor.GREEN + " Open toggles menu.",
                ChatColor.GRAY + "Display Item"
        }));

        player.openInventory(inv);
        
        Quests.getInstance().triggerObjective(player, ObjectiveOpenProfile.class);
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
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return;
        Inventory inv = Bukkit.createInventory(null, ((int) Math.ceil((1 + PlayerManager.PlayerToggles.values().length) / 9.0) * 9), "Toggles");

        int i = 0;
        for (PlayerManager.PlayerToggles playerToggles : PlayerManager.PlayerToggles.values()) {
            boolean isToggled = playerToggles.getToggleState(player);
            inv.setItem(i, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, (short) (isToggled ? 10 : 8)), (isToggled ? ChatColor.GREEN : ChatColor.RED) + "/" + playerToggles.getCommandName(), new String[]{
                    ChatColor.GRAY + playerToggles.getDescription(),
                    ChatColor.GRAY + "Display Item"
            }).build());
            i++;
        }

        inv.setItem(i, ItemManager.createItem(Material.BARRIER, ChatColor.YELLOW + "Back", new String[]{
                ChatColor.AQUA + "Back to the Profile Menu!",
                ChatColor.GRAY + "Display Item"}));
        player.openInventory(inv);
    }

    /**
     * Opens the GM Toggles menu.
     * (user must be GM)
     *
     * @param player
     */
    public static void openGameMasterTogglesMenu(Player player) {
        if (!Rank.isTrialGM(player)) return;
        boolean isToggled = false;

        Inventory inv = Bukkit.createInventory(null, 9, "Game Master Toggles");
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (gp == null) return;

        // Invisible
        isToggled = GameAPI._hiddenPlayers.contains(player);
        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, (short) (isToggled ? 10 : 8)), (isToggled ? ChatColor.GREEN : ChatColor.RED) + "Invisible Mode", new String[]{
                ChatColor.GRAY + "Toggling this will make you invisible to players and mobs.",
                ChatColor.GRAY + "Display Item"}).build());

        // Allow Fight
        isToggled = !gp.isInvulnerable() && gp.isTargettable();
        inv.setItem(1, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, (short) (isToggled ? 10 : 8)), (isToggled ? ChatColor.GREEN : ChatColor.RED) + "Allow Combat", new String[]{
                ChatColor.GRAY + "Toggling this will make you vulnerable to attacks but also allow outgoing damage.",
                ChatColor.GRAY + "Display Item"}).build());

        // Stream Mode
        isToggled = gp.isStreamMode();
        inv.setItem(2, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, (short) (isToggled ? 10 : 8)), (isToggled ? ChatColor.GREEN : ChatColor.RED) + "Stream Mode", new String[]{
                ChatColor.GRAY + "Disable sensitive messages from being displayed.",
                ChatColor.GRAY + "Display Item"}).build());

        player.openInventory(inv);
    }

    /**
     * Opens the Head GM Toggles menu.
     * (user must be Head GM)
     *
     * @param player
     */
    public static void openHeadGameMasterTogglesMenu(Player player) {
        if (!Rank.isHeadGM(player)) return;
        boolean isToggled = false;

        Inventory inv = Bukkit.createInventory(null, 9, "Head Game Master Toggles");
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (gp == null) return;

        // Game Master Extended Permissions
        isToggled = DungeonRealms.getInstance().isGMExtendedPermissions;
        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, (short) (isToggled ? 10 : 8)), (isToggled ? ChatColor.GREEN : ChatColor.RED) + "Game Master Extended Permissions", new String[]{
                ChatColor.GRAY + "Toggling this will allow GMs to have extended permissions.",
                ChatColor.GRAY + "This should be used for events and grants access to features such as adding items.",
                ChatColor.GRAY + "Display Item"}).build());

        player.openInventory(inv);
    }

}
