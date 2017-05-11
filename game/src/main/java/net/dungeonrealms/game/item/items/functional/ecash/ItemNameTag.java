package net.dungeonrealms.game.item.items.functional.ecash;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.item.items.functional.PotionItem;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemNameTag extends FunctionalItem implements ItemClickEvent.ItemClickListener, ItemInventoryEvent.ItemInventoryListener {

    public ItemNameTag(ItemStack item) {
        super(ItemType.ITEM_NAME_TAG);
    }

    public ItemNameTag() {
        super(ItemType.ITEM_NAME_TAG);
        setPermUntradeable(true);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.NAME_TAG, 1);
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.GREEN + ChatColor.BOLD.toString() + "Item Name Tag";
    }

    @Override
    protected String[] getLore() {
        return new String[]{ChatColor.GRAY + "Apply to an item to rename it!"};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return INVENTORY_PLACE;
    }

    @Override
    public void onClick(ItemClickEvent evt) {
        //In world right clicked?
        evt.setCancelled(true);
    }

    private static final int MAX_LENGTH = 20;

    @Override
    public void onInventoryClick(ItemInventoryEvent evt) {
        InventoryClickEvent event = evt.getEvent();
        Player player = evt.getPlayer();
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
            //Applying this item?
            ItemStack current = event.getCurrentItem();
            if (current == null || current.getType() == Material.AIR) return;

            if (!canRenameItem(current)) {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "(!) " + ChatColor.RED + "You cannot rename this item!");
                return;
            }
            evt.setCancelled(true);
            evt.setUsed(false);
            evt.setResultItem(null);
            evt.setSwappedItem(null);
            player.setItemOnCursor(null);
            player.updateInventory();

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.closeInventory(), 1);

            Utils.sendCenteredMessage(player, ChatColor.GREEN.toString() + ChatColor.BOLD + "Item Rename Started");
            Utils.sendCenteredMessage(player, ChatColor.GRAY + "Are you sure you want to rename this item?");
            Utils.sendCenteredMessage(player, ChatColor.WHITE + Utils.getItemName(current));
            player.sendMessage("");
            Utils.sendCenteredMessage(player, ChatColor.GREEN + "Please enter the new desired name of this item.");
            Utils.sendCenteredMessage(player, ChatColor.GRAY + "Type " + ChatColor.RED +
                    "cancel" + ChatColor.GRAY + " to cancel the item rename.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADING, 3, .6F);
            Chat.listenForMessage(player, chat -> {
                String msg = Chat.checkForBannedWords(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', SQLDatabaseAPI.filterSQLInjection(chat.getMessage()))));
                Item.ItemTier tier = Item.ItemTier.getByTier(new VanillaItem(current).getTagInt(TIER));

                if (Chat.containsIllegal(msg)) {
                    player.sendMessage(ChatColor.RED + "Your desired name contained illegal characters!");
                    player.sendMessage(ChatColor.GRAY + "Remove them and try again.");
                    returnItem(player, item);
                    return;
                }
                ChatColor color = ChatColor.WHITE;
                if (tier != null) {
                    color = tier.getColor();
                }

                String newItemName = color + msg;

                if (newItemName.length() - 2 >= MAX_LENGTH) {
                    player.sendMessage(ChatColor.RED + "Your desired item name exceeds " + MAX_LENGTH + " characters!");
                    returnItem(player, item);
                    return;
                }
                player.sendMessage("");
                player.sendMessage("");
                player.sendMessage("");
                Utils.sendCenteredMessage(player, ChatColor.GREEN.toString() + ChatColor.BOLD + "Confirm Item Rename");
                Utils.sendCenteredMessage(player, ChatColor.GRAY.toString() + "New Item Name: " + newItemName);
                player.sendMessage("");
                Utils.sendCenteredMessage(player, ChatColor.GRAY + "Enter " + ChatColor.GREEN + ChatColor.BOLD + "CONFIRM" + ChatColor.GRAY + " to confirm.");
                Utils.sendCenteredMessage(player, ChatColor.GRAY + "Enter " + ChatColor.RED + ChatColor.BOLD + "CANCEL" + ChatColor.GRAY + " to cancel the item rename.");
                player.sendMessage("");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3, .85F);
                Chat.listenForMessage(player, confirm -> {

                    if (confirm.getMessage().toLowerCase().equals("confirm")) {
                        ItemStack newItem = current.clone();
                        ItemMeta im = newItem.getItemMeta();
                        im.setDisplayName(newItemName);
                        newItem.setItemMeta(im);

                        PersistentItem item = PersistentItem.constructItem(newItem);
                        item.setCustomDisplayName(newItemName);
                        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
                            GameAPI.giveOrDropItem(player, item.generateItem());
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5F);
                            Utils.sendCenteredMessage(player, ChatColor.GREEN.toString() + ChatColor.BOLD + "Item Rename Successful!");
                            Utils.sendCenteredMessage(player, ChatColor.GRAY.toString() + "New Item Name: " + newItemName);
                            Achievements.giveAchievement(player, Achievements.EnumAchievements.RENAME_ITEM);
                            Quest.spawnFirework(player.getLocation(), FireworkEffect.builder().flicker(true).trail(true).withColor(Color.RED).build());
                        });
                    } else {
                        returnItem(player, current);
                    }
                }, denied -> returnItem(player, current));
            }, orElse -> returnItem(player, current));
        }
    }

    public void returnItem(Player player, ItemStack current) {
        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
            player.sendMessage(ChatColor.RED + "Item renaming " + ChatColor.BOLD + "CANCELLED" + ChatColor.RED + "!");
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
            GameAPI.giveOrDropItem(player, current);
            GameAPI.giveOrDropItem(player, generateItem());
        });
    }

    public boolean canRenameItem(ItemStack item) {
        return ItemArmor.isCustomTool(item) || item.getType().isEdible() || PotionItem.isPotion(item);
    }
}
