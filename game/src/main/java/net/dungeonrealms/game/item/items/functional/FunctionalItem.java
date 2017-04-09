package net.dungeonrealms.game.item.items.functional;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.FunctionalItemEvent;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.core.ItemGeneric;

/**
 * A basic class for items that behave a reactionary behavior.
 * 
 * @author Kneesnap
 */
public abstract class FunctionalItem extends ItemGeneric {

	// Some basic predefined "event packs".
	protected static final ItemUsage[] INTERACT = {ItemUsage.LEFT_CLICK_AIR, ItemUsage.LEFT_CLICK_BLOCK, ItemUsage.LEFT_CLICK_ENTITY,
									ItemUsage.RIGHT_CLICK_AIR, ItemUsage.RIGHT_CLICK_BLOCK, ItemUsage.RIGHT_CLICK_ENTITY};
	protected static final ItemUsage[] INTERACT_LEFT_CLICK = {ItemUsage.LEFT_CLICK_AIR, ItemUsage.LEFT_CLICK_BLOCK, ItemUsage.LEFT_CLICK_ENTITY};
	protected static final ItemUsage[] INTERACT_RIGHT_CLICK = {ItemUsage.RIGHT_CLICK_AIR, ItemUsage.RIGHT_CLICK_BLOCK, ItemUsage.RIGHT_CLICK_ENTITY};
	protected static final ItemUsage[] INVENTORY_PICKUP = {ItemUsage.INVENTORY_PICKUP_ITEM, ItemUsage.INVENTORY_SWAP_PICKUP};
	protected static final ItemUsage[] INVENTORY_PLACE = {ItemUsage.INVENTORY_PLACE_ITEM, ItemUsage.INVENTORY_SWAP_PLACE};
	
	public FunctionalItem(ItemStack item) {
		super(item);
		setAntiDupe(true);
	}
	
	public FunctionalItem(ItemType type) {
		super(type);
		setAntiDupe(true);
	}
	
	@Override
	public void updateItem() {
		if (getLore() != null)
			for (String line : getLore())
				addLore(line);
		super.updateItem();
	}
	
	/**
	 * Handles a FunctionalItemEvent related to this item.
	 */
	public static void attemptUseItem(FunctionalItemEvent ice) {
		if(ice.getVanillaItem() == null || ice.getVanillaItem().getType() == Material.AIR)
			return;
		
		//Check that the FunctionalItem is not null, and that the usagetype supplied is allowed for this item.
		if(ice.getItem() == null || !Arrays.asList(ice.getItem().getUsage()).contains(ice.getUsage()))
			return;
		
		ice.handle();
	}
	
	/**
	 * Called when an item is clicked either in air or on a block.
	 */
	public abstract void onClick(ItemClickEvent evt);
	
	/**
	 * Called when this item is consumed.
	 */
	public abstract void onConsume(ItemConsumeEvent evt);
	
	/**
	 * Called when an item is interacted with in the inventory.
	 */
	public abstract void onInventoryClick(ItemInventoryEvent evt);
	
	/**
	 * Gets the display name for this item.
	 */
	protected abstract String getDisplayName();
	
	/**
	 * Gets the lore to set for this item, if any.
	 */
	protected abstract String[] getLore();
	
	/**
	 * Returns the ways to use this item that DR is listening for.
	 */
	protected abstract ItemUsage[] getUsage();
}
