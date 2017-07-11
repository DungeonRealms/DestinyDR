package net.dungeonrealms.game.item.items.functional.ecash;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.util.ChatUtil;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.item.items.functional.PotionItem;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.Quest;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemLoreBook extends FunctionalItem implements ItemInventoryEvent.ItemInventoryListener, ItemClickEvent.ItemClickListener {
    public ItemLoreBook(ItemStack item) {
        super(item);
    }

    public ItemLoreBook() {
        super(ItemType.ITEM_LORE_BOOK);
        setPermUntradeable(true);
        setUndroppable(true);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.ENCHANTED_BOOK, 1, (short) 3);
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.GOLD + "Item Lore Book";
    }

    @Override
    protected String[] getLore() {
        return arr(ChatColor.GOLD.toString() + "Uses: " + ChatColor.GRAY + "1",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "Apply to any tradeable item to",
                ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "add a custom line of lore text.");
    }

    @Override
    protected ItemUsage[] getUsage() {
        return INVENTORY_PLACE;
    }

    @Override
    public void onClick(ItemClickEvent evt) {
        //In world right clicked?
        evt.setCancelled(true);
        evt.getPlayer().updateInventory();
    }

    private static final int MAX_LENGTH = 40;

    @Override
    public void onInventoryClick(ItemInventoryEvent evt) {
        InventoryClickEvent event = evt.getEvent();
        Player player = evt.getPlayer();

        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;

        if (!canRenameItem(current)) {
            evt.setCancelled(true);
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "(!) " + ChatColor.RED + "You cannot add custom lore to this item!");
            return;
        }

        if (current.getAmount() > 1) {
            evt.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " use this on stacked items.");
            return;
        }


        ItemGeneric generic = (ItemGeneric) PersistentItem.constructItem(current);

        if (generic == null) {
            evt.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot modify this item!");
            return;
        }

        if (generic.isUntradeable() || generic.isPermanentUntradeable()) {
            evt.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot modify this item!");
            return;
        }

        List<String> currentLore = generic.getCustomLore();

        if (currentLore != null) {
            //Allow 1 custom lore line on Soulbounds?*
            if (currentLore.size() >= 1 && !generic.hasTag("customId") || currentLore.size() >= 2) {
                evt.setCancelled(true);
                player.sendMessage(ChatColor.RED + "This item already has custom lore!");
                return;
            }
        }

        evt.setUsed(true);
//        player.setItemOnCursor(null); // Use this instead of evt.setUsed(true) because Chat.listenForMessage force closes the inventory and would cause the item to drop.
        evt.setSwappedItem(null);
        player.updateInventory();

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "Please enter a " + ChatColor.UNDERLINE + "1 LINE" + ChatColor.GOLD
                + " description to add to this item.");
        player.sendMessage(ChatColor.RED + "This opperation is non-refundable and non-reversable, type 'cancel' to void it.");
        player.sendMessage("");


        //        Utils.sendCenteredMessage(player, ChatColor.GREEN.toString() + ChatColor.BOLD + "Item Rename Started");
//        Utils.sendCenteredMessage(player, ChatColor.GRAY + "Are you sure you want to rename this item?");
//        Utils.sendCenteredMessage(player, ChatColor.WHITE + Utils.getItemName(current));
//        player.sendMessage("");
//        Utils.sendCenteredMessage(player, ChatColor.GREEN + "Please enter the new desired name of this item.");
//        Utils.sendCenteredMessage(player, ChatColor.GRAY + "Type " + ChatColor.RED + "cancel" + ChatColor.GRAY + " to cancel the item rename.");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADING, 3, .6F);

        Chat.listenForMessage(player, chat -> {
            String itemName = Chat.checkForBannedWords(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', SQLDatabaseAPI.filterSQLInjection(chat.getMessage()))));
            if (ChatUtil.containsIllegal(itemName)) {
                player.sendMessage(ChatColor.RED + "Your desired lore contained illegal characters!");
                player.sendMessage(ChatColor.GRAY + "Remove them and try again.");
                returnItem(player, current);
                return;
            }

            if (itemName.length() >= MAX_LENGTH) {
                player.sendMessage(ChatColor.RED + "Your desired item lore exceeds " + MAX_LENGTH + " characters!");
                returnItem(player, current);
                return;
            }

            if (itemName.equalsIgnoreCase("cancel")) {
                returnItem(player, current);
                return;
            }
            String finalItemName = ChatColor.GOLD + ChatColor.ITALIC.toString() + itemName;

            player.sendMessage("");
            player.sendMessage("");
            player.sendMessage("");
            Utils.sendCenteredMessage(player, ChatColor.GREEN.toString() + ChatColor.BOLD + "Confirm Custom Item Lore");
            Utils.sendCenteredMessage(player, ChatColor.GRAY.toString() + "New Item Lore: " + finalItemName);
            player.sendMessage("");
            Utils.sendCenteredMessage(player, ChatColor.GRAY + "Enter " + ChatColor.GREEN + ChatColor.BOLD + "CONFIRM" + ChatColor.GRAY + " to confirm.");
            Utils.sendCenteredMessage(player, ChatColor.GRAY + "Enter " + ChatColor.RED + ChatColor.BOLD + "CANCEL" + ChatColor.GRAY + " to cancel this operation.");
            player.sendMessage("");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3, .85F);

            Chat.promptPlayerConfirmation(player, () -> {
                ItemGeneric pi = (ItemGeneric) PersistentItem.constructItem(current);
//                pi.setCustomName(finalItemName);
                List<String> lore = pi.getCustomLore();
                if (lore == null) lore = Lists.newArrayList();
                lore.add(finalItemName);
                pi.setCustomLore(lore);

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5F);
                Utils.sendCenteredMessage(player, ChatColor.GREEN.toString() + ChatColor.BOLD + "Custom Item Lore added successfully!");
                Utils.sendCenteredMessage(player, ChatColor.GRAY.toString() + "New Item Lore: " + finalItemName);
                Achievements.giveAchievement(player, Achievements.EnumAchievements.RENAME_LORE);

                Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
                    GameAPI.giveOrDropItem(player, pi.generateItem());
                    Quest.spawnFirework(player.getLocation(), FireworkEffect.builder().flicker(true)
                            .trail(true).withColor(Color.RED).build());
                });

            }, () -> returnItem(player, current));
        }, orElse -> returnItem(player, current));
    }

    public void returnItem(Player player, ItemStack current) {
        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
            player.sendMessage(ChatColor.RED + "Custom Lore Addition " + ChatColor.BOLD + "CANCELLED" + ChatColor.RED + "!");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
            GameAPI.giveOrDropItem(player, current);
            GameAPI.giveOrDropItem(player, generateItem());
        });
    }

    public boolean canRenameItem(ItemStack item) {
        return ItemArmor.isCustomTool(item) || item.getType().isEdible() || PotionItem.isPotion(item);
    }
}
