package net.dungeonrealms.game.player.inventory;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.player.rank.Subscription;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
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

import java.util.*;

/**
 * Created by Brad on 16/06/2016.
 */
public class SupportMenus {

    private static ItemStack applySupportItemTags(ItemStack item, String playerName, UUID uuid) {
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("name", new NBTTagString(playerName));
        tag.set("uuid", new NBTTagString(uuid.toString()));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    private static ItemStack addNbtTag(ItemStack item, String tagId, String tagValue) {
        if (tagId == null || tagValue == null || Objects.equals(tagId, "") || Objects.equals(tagValue, "")) return item;

        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set(tagId, new NBTTagString(tagValue));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static void openMainMenu(Player player, String playerName) {
        // @todo: Redo the loading of player data, looks like whoever change how the DB works messed up this entire system.
        // @todo: As ranks no longer are read properly therefore leaving data [temporarily] outdated.

        try {
            Player online = Bukkit.getPlayer(playerName);
//            UUID uuid = online != null && online.getDisplayName().equalsIgnoreCase(playerName) ? online.getUniqueId() : UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));

            SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, (uuid) -> {
                // Always grab new data, unless they're logged in (which shouldn't ever be the case)
                if (online == null && PlayerWrapper.getPlayerWrappers().containsKey(uuid)) {
                    PlayerWrapper.getPlayerWrappers().remove(uuid);
                }
                PlayerWrapper.getPlayerWrapper(uuid, (wrapper) -> {
                    //Load if doesnt exist?
//                DatabaseAPI.getInstance().requestPlayer(uuid, false);
                    String playerRank = Rank.getInstance().getRank(uuid);
                    if (!Rank.isDev(player) && (playerRank.equalsIgnoreCase("gm") || playerRank.equalsIgnoreCase("dev"))) {
                        player.sendMessage(ChatColor.RED + "You " + ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "DO NOT" + ChatColor.RED + " have permission to manage this user.");
                        return;
                    }

                    ItemStack item;

                    Inventory inv = Bukkit.createInventory(null, 45, "Support Tools");

                    item = editItem(playerName, ChatColor.GREEN + playerName + ChatColor.WHITE + " (" + uuid.toString() + ")", new String[]{
                            ChatColor.WHITE + "Rank: " + Rank.rankFromPrefix(playerRank) +
                                    (playerRank.equalsIgnoreCase("sub") || playerRank.equalsIgnoreCase("sub+") ?
                                            ChatColor.WHITE + " (" + Subscription.getInstance().checkSubscription(uuid) + " days remaining)" : ""),
                            ChatColor.WHITE + "Level: " + wrapper.getLevel(),
                            ChatColor.WHITE + "Experience: " + wrapper.getExperience(),
                            ChatColor.WHITE + "E-Cash: " + wrapper.getEcash(),
                            ChatColor.WHITE + "Bank Balance: " + wrapper.getGems(),
                            ChatColor.WHITE + "Hearthstone Location: " + Utils.ucfirst(wrapper.getHearthstone()).replace("_", " "),
                            ChatColor.WHITE + "Alignment: " + Utils.ucfirst(wrapper.getPlayerAlignment().name()),
                            //ChatColor.WHITE + "Last Logout: " + Utils.formatTimeAgo((int) (System.currentTimeMillis() / 1000) - Integer.valueOf(DatabaseAPI.getInstance().getData(EnumData.LAST_LOGOUT, uuid).toString())) + " ago", @todo: Fix a bug with this.
                            ChatColor.WHITE + "Join Date: " + Utils.getDate(wrapper.getFirstLogin() * 1000)
                    });
                    inv.setItem(4, applySupportItemTags(item, playerName, uuid));

                    // Rank Manager
                    if (!playerName.equalsIgnoreCase(player.getDisplayName())) {
                        item = editItem(new ItemStack(Material.DIAMOND), ChatColor.GOLD + "Rank Manager", new String[]{
                                ChatColor.WHITE + "Modify the rank of " + playerName + ".",
                                ChatColor.WHITE + "Current rank: " + Rank.rankFromPrefix(playerRank)
                        });
                    } else {
                        item = editItem(new ItemStack(Material.BARRIER), ChatColor.RED + "Rank Manager", new String[]{
                                ChatColor.RED + "You cannot change the rank of your own account."
                        });
                    }
                    inv.setItem(19, applySupportItemTags(item, playerName, uuid));

                    // Level Manager
                    item = editItem(new ItemStack(Material.EXP_BOTTLE), ChatColor.GOLD + "Level Manager", new String[]{
                            ChatColor.WHITE + "Manage the level/experience of " + playerName + ".",
                            ChatColor.WHITE + "Current level: " + wrapper.getLevel(),
                            ChatColor.WHITE + "Current EXP: " + wrapper.getExperience()
                    });
                    inv.setItem(22, applySupportItemTags(item, playerName, uuid));

                    // E-Cash Manager
                    item = editItem(new ItemStack(Material.GOLDEN_CARROT), ChatColor.GOLD + "E-Cash Manager", new String[]{
                            ChatColor.WHITE + "Manage the e-cash of " + playerName + ".",
                            ChatColor.WHITE + "Current E-Cash: " + wrapper.getEcash()
                    });
                    inv.setItem(25, applySupportItemTags(item, playerName, uuid));

                    // Bank Manager
                    item = editItem(new ItemStack(Material.ENDER_CHEST), ChatColor.GOLD + "Bank Manager", new String[]{
                            ChatColor.WHITE + "Manage the bank of " + playerName + ".",
                            ChatColor.WHITE + "Current bank balance: " + wrapper.getGems()
                    });
                    inv.setItem(28, applySupportItemTags(item, playerName, uuid));

                    // Hearthstone Manager
                    item = editItem(new ItemStack(Material.QUARTZ_ORE), ChatColor.GOLD + "Hearthstone Manager", new String[]{
                            ChatColor.WHITE + "Manage the Hearthstone Location of " + playerName + ".",
                            ChatColor.WHITE + "Current location: " + Utils.ucfirst(wrapper.getHearthstone()).replace("_", " ")
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

                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.openInventory(inv));
                });
            });
        } catch (IllegalArgumentException ex) {
            // This exception is thrown if the UUID doesn't exist in the database.
            player.sendMessage(ChatColor.RED + "Unable to identify anybody with the player name: " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.RED + "!");
            player.sendMessage(ChatColor.RED + "It's likely the user has never played on the Dungeon Realms servers before.");
        }
    }

    public static void openRankMenu(Player player, String playerName, UUID uuid) {
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

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.YELLOW.getData()), Rank.rankFromPrefix("sub++"), new String[]{
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
            item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getData()), Rank.rankFromPrefix("builder"), new String[]{
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

    public static void openRankSubscriptionMenu(Player player, String playerName, UUID uuid, String rank) {
        rank = rank.toUpperCase();
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Subscription)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), ChatColor.GOLD + "Extend Subscription", new String[]{
                ChatColor.WHITE + "This will add to the subscription length of: " + playerName
        });
        inv.setItem(21, addNbtTag(applySupportItemTags(item, playerName, uuid), "rank", rank));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getData()), ChatColor.GOLD + "Set Subscription", new String[]{
                ChatColor.WHITE + "This will set the subscription length of: " + playerName
        });
        inv.setItem(22, addNbtTag(applySupportItemTags(item, playerName, uuid), "rank", rank));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.RED.getData()), ChatColor.GOLD + "Remove Gems", new String[]{
                ChatColor.WHITE + "This will remove from the subscription length of: " + playerName
        });
        inv.setItem(23, addNbtTag(applySupportItemTags(item, playerName, uuid), "rank", rank));

        player.openInventory(inv);
    }

    public static void openLevelMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Level)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), ChatColor.GOLD + "Add Experience", new String[]{
                ChatColor.WHITE + "This will add experience to: " + playerName
        });
        inv.setItem(21, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getData()), ChatColor.GOLD + "Set Experience", new String[]{
                ChatColor.WHITE + "This will set the experience of: " + playerName
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.RED.getData()), ChatColor.GOLD + "Remove Experience", new String[]{
                ChatColor.WHITE + "This will remove experience from: " + playerName
        });
        inv.setItem(23, applySupportItemTags(item, playerName, uuid));


        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), ChatColor.GOLD + "Add Level(s)", new String[]{
                ChatColor.WHITE + "This will add level(s) to: " + playerName
        });
        inv.setItem(30, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getData()), ChatColor.GOLD + "Set Level", new String[]{
                ChatColor.WHITE + "This will set the level of: " + playerName
        });
        inv.setItem(31, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.RED.getData()), ChatColor.GOLD + "Remove Level(s)", new String[]{
                ChatColor.WHITE + "This will remove level(s) from: " + playerName
        });
        inv.setItem(32, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openECashMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (E-Cash)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), ChatColor.GOLD + "Add Amount", new String[]{
                ChatColor.WHITE + "This will add the specified balance to: " + playerName
        });
        inv.setItem(21, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getData()), ChatColor.GOLD + "Set Amount", new String[]{
                ChatColor.WHITE + "This will set the specified balance to: " + playerName
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.RED.getData()), ChatColor.GOLD + "Remove Amount", new String[]{
                ChatColor.WHITE + "This will remove the specified balance to: " + playerName
        });
        inv.setItem(23, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.ORANGE.getData()), ChatColor.GOLD + "Package: 500 E-Cash", new String[]{
                ChatColor.WHITE + "This will add 500 E-Cash to " + playerName + " as part of the shop package."
        });
        inv.setItem(29, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.ORANGE.getData()), ChatColor.GOLD + "Package: 2500 E-Cash", new String[]{
                ChatColor.WHITE + "This will add 2500 E-Cash to " + playerName + " as part of the shop package."
        });
        inv.setItem(30, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.ORANGE.getData()), ChatColor.GOLD + "Package: 5000 E-Cash", new String[]{
                ChatColor.WHITE + "This will add 5000 E-Cash to " + playerName + " as part of the shop package."
        });
        inv.setItem(32, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.ORANGE.getData()), ChatColor.GOLD + "Package: 9999 E-Cash", new String[]{
                ChatColor.WHITE + "This will add 9999 E-Cash to " + playerName + " as part of the shop package."
        });
        inv.setItem(33, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openBankMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Bank)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()), ChatColor.GOLD + "Add Gems", new String[]{
                ChatColor.WHITE + "This will add gems to: " + playerName
        });
        inv.setItem(21, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getData()), ChatColor.GOLD + "Set Gems", new String[]{
                ChatColor.WHITE + "This will set the gems of: " + playerName
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, DyeColor.RED.getData()), ChatColor.GOLD + "Remove Gems", new String[]{
                ChatColor.WHITE + "This will remove gems from: " + playerName
        });
        inv.setItem(23, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openHearthstoneMenu(Player player, String playerName, UUID uuid) {

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        if(wrapper == null)return;

        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Hearthstone)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (wrapper.getHearthstone().equalsIgnoreCase("cyrennica") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Cyrennica", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Cyrennica"
        });
        inv.setItem(18, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (wrapper.getHearthstone().equalsIgnoreCase("harrison_field") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Harrison Fields", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Harrison Fields"
        });
        inv.setItem(19, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (wrapper.getHearthstone().equalsIgnoreCase("dark_oak") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Dark Oak Tavern", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Dark Oak Tavern"
        });
        inv.setItem(20, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (wrapper.getHearthstone().equalsIgnoreCase("gloomy_hollows") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Gloomy Hollows", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Gloomy Hollows"
        });
        inv.setItem(21, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (wrapper.getHearthstone().equalsIgnoreCase("tripoli") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Tripoli", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Tripoli"
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (wrapper.getHearthstone().equalsIgnoreCase("trollsbane") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Trollsbans Tavern", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Trollsbane Tavern"
        });
        inv.setItem(23, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (wrapper.getHearthstone().equalsIgnoreCase("crestguard") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Crestguard Keep", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Crestguard Keep"
        });
        inv.setItem(24, applySupportItemTags(item, playerName, uuid));

        item = editItem(new ItemStack(Material.WOOL, 1, (wrapper.getHearthstone().equalsIgnoreCase("deadpeaks") ? DyeColor.LIME.getData() : DyeColor.RED.getData())), ChatColor.GOLD + "Deadpeaks Mountain", new String[]{
                ChatColor.WHITE + "Set user hearthstone to: Deadpeaks Mountain"
        });
        inv.setItem(25, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openCosmeticsMenu(Player player, String playerName, UUID uuid) {
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Cosmetics)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        // Trail Manager
        item = editItem(new ItemStack(Material.EYE_OF_ENDER), ChatColor.GOLD + "Trail Manager", new String[]{
                ChatColor.WHITE + "Modify trails of " + playerName + "."
        });
        inv.setItem(19, applySupportItemTags(item, playerName, uuid));

        // Pet Manager
        item = editItem(new ItemStack(Material.NAME_TAG), ChatColor.GOLD + "Pet Manager", new String[]{
                ChatColor.WHITE + "Manage pets of " + playerName + "."
        });
        inv.setItem(22, applySupportItemTags(item, playerName, uuid));

        // Mule Manager
        item = editItem(new ItemStack(Material.SADDLE), ChatColor.GOLD + "Mount / Mule Manager", new String[]{
                ChatColor.WHITE + "Manage mounts / mules of " + playerName + "."
        });
        inv.setItem(25, applySupportItemTags(item, playerName, uuid));

        player.openInventory(inv);
    }

    public static void openTrailsMenu(Player player, String playerName, UUID uuid) {

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Trails)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        Set<String> unlockedPlayerTrails = wrapper.getTrails();
        int i = 18;
        for (ParticleAPI.ParticleEffect trailType : ParticleAPI.ParticleEffect.values()) {
            boolean hasUnlockedPlayerTrail = false;
            for (String unlockedTrails : unlockedPlayerTrails) {
                if (unlockedTrails.equalsIgnoreCase(trailType.getRawName())) {
                    hasUnlockedPlayerTrail = true;
                    break;
                }
            }
            item = editItem(trailType.getSelectionItem(), (hasUnlockedPlayerTrail ? ChatColor.GREEN : ChatColor.RED) + trailType.getDisplayName(), new String[]{
                    ChatColor.WHITE + "Click to " + (hasUnlockedPlayerTrail ? "lock" : "unlock") + " the " + trailType.getDisplayName().toLowerCase() + " player trail."
            });

            item = addNbtTag(item, "trail", trailType.getRawName());

            inv.setItem(i, applySupportItemTags(item, playerName, uuid));
            i++;
        }

        player.openInventory(inv);
    }

    public static void openMountsMenu(Player player, String playerName, UUID uuid) {
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

    public static void openPetsMenu(Player player, String playerName, UUID uuid) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        if(wrapper == null)return;
        ItemStack item;
        Inventory inv = Bukkit.createInventory(null, 45, "Support Tools (Pets)");

        item = editItem(playerName, ChatColor.GREEN + playerName, new String[]{
                ChatColor.WHITE + "Return to Menu"
        });
        inv.setItem(4, applySupportItemTags(item, playerName, uuid));

        Set<String> unlockedPlayerPets = wrapper.getPetsUnlocked();
        int i = 18;
        for (EnumPets petType : EnumPets.values()) {
            boolean hasUnlockedPet = false;
            for (String unlockedPets : unlockedPlayerPets) {
                if (unlockedPets.equalsIgnoreCase(petType.getRawName())) {
                    hasUnlockedPet = true;
                    break;
                }
            }

            item = editItemWithShort(applySupportItemTags(addNbtTag(new ItemStack(Material.MONSTER_EGG, 1, (short) petType.getEggShortData()), "pet", petType.getRawName()), playerName, uuid), (short) petType.getEggShortData(), (hasUnlockedPet ? ChatColor.GREEN : ChatColor.RED) + petType.getDisplayName(), new String[]{
                    ChatColor.WHITE + "Click to " + (hasUnlockedPet ? "lock" : "unlock") + " the " + petType.getDisplayName().toLowerCase() + " pet."
            });

            inv.setItem(i, item);
            i++;
        }

        player.openInventory(inv);
    }

    private static ItemStack editItem(String playerName, String name, String[] lore) {
        return PlayerMenus.editItem(playerName, name, lore);
    }

    private static ItemStack editItem(ItemStack itemStack, String name, String[] lore) {
        return PlayerMenus.editItem(itemStack, name, lore);
    }

    private static ItemStack editItemWithShort(ItemStack itemStack, short shortID, String name, String[] lore) {
        return PlayerMenus.editItemWithShort(itemStack, shortID, name, lore);
    }

}
