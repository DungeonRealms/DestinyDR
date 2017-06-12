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
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.item.items.functional.PotionItem;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.world.item.Item.ItemTier;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ItemNameTag extends FunctionalItem implements ItemClickEvent.ItemClickListener, ItemInventoryEvent.ItemInventoryListener {

    public ItemNameTag(ItemStack item) {
        super(item);
    }

    public ItemNameTag() {
        super(ItemType.ITEM_NAME_TAG);
        setPermUntradeable(true);
        setUndroppable(true);
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
        return arr("Apply to an item to rename it!");
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

    private static final int MAX_LENGTH = 20;

    @Override
    public void onInventoryClick(ItemInventoryEvent evt) {
        InventoryClickEvent event = evt.getEvent();
        Player player = evt.getPlayer();
        
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;
        
        if (!canRenameItem(current)) {
        	player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "(!) " + ChatColor.RED + "You cannot rename this item!");
        	return;
        }

        evt.setUsed(true);
//        player.setItemOnCursor(null); // Use this instead of evt.setUsed(true) because Chat.listenForMessage force closes the inventory and would cause the item to drop.
        evt.setSwappedItem(null);
        player.updateInventory();
        
        Utils.sendCenteredMessage(player, ChatColor.GREEN.toString() + ChatColor.BOLD + "Item Rename Started");
        Utils.sendCenteredMessage(player, ChatColor.GRAY + "Are you sure you want to rename this item?");
        Utils.sendCenteredMessage(player, ChatColor.WHITE + Utils.getItemName(current));
        player.sendMessage("");
        Utils.sendCenteredMessage(player, ChatColor.GREEN + "Please enter the new desired name of this item.");
        Utils.sendCenteredMessage(player, ChatColor.GRAY + "Type " + ChatColor.RED + "cancel" + ChatColor.GRAY + " to cancel the item rename.");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADING, 3, .6F);
        
        Chat.listenForMessage(player, chat -> {
        	String itemName = Chat.checkForBannedWords(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', SQLDatabaseAPI.filterSQLInjection(chat.getMessage()))));
        	ItemTier tier = ItemTier.getByTier(new VanillaItem(current).getTagInt(TIER));
        	
        	if (Chat.containsIllegal(itemName)) {
        		player.sendMessage(ChatColor.RED + "Your desired name contained illegal characters!");
        		player.sendMessage(ChatColor.GRAY + "Remove them and try again.");
        		returnItem(player, item);
        		return;
        	}
        	
        	if (itemName.length() >= MAX_LENGTH) {
        		player.sendMessage(ChatColor.RED + "Your desired item name exceeds " + MAX_LENGTH + " characters!");
        		returnItem(player, item);
        		return;
        	}

        	if(itemName.equalsIgnoreCase("cancel")){
                returnItem(player, item);
        	    return;
            }
        	String finalItemName = (tier != null ? tier.getColor() : ChatColor.WHITE) + itemName;
        	
        	player.sendMessage("");
        	player.sendMessage("");
        	player.sendMessage("");
        	Utils.sendCenteredMessage(player, ChatColor.GREEN.toString() + ChatColor.BOLD + "Confirm Item Rename");
        	Utils.sendCenteredMessage(player, ChatColor.GRAY.toString() + "New Item Name: " + finalItemName);
        	player.sendMessage("");
        	Utils.sendCenteredMessage(player, ChatColor.GRAY + "Enter " + ChatColor.GREEN + ChatColor.BOLD + "CONFIRM" + ChatColor.GRAY + " to confirm.");
        	Utils.sendCenteredMessage(player, ChatColor.GRAY + "Enter " + ChatColor.RED + ChatColor.BOLD + "CANCEL" + ChatColor.GRAY + " to cancel the item rename.");
        	player.sendMessage("");
        	player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3, .85F);
        	
        	Chat.promptPlayerConfirmation(player, () -> {
        		ItemGeneric pi = (ItemGeneric) PersistentItem.constructItem(current);
        		pi.setCustomName(finalItemName);
        		
        		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5F);
        		Utils.sendCenteredMessage(player, ChatColor.GREEN.toString() + ChatColor.BOLD + "Item Rename Successful!");
        		Utils.sendCenteredMessage(player, ChatColor.GRAY.toString() + "New Item Name: " + finalItemName);
        		Achievements.giveAchievement(player, Achievements.EnumAchievements.RENAME_ITEM);
        		
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
