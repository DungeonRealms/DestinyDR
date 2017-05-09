package net.dungeonrealms.game.item.event;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.item.ItemUsage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * ItemInventoryEvent - Handles an inventory interaction for a functional item.
 * 
 * This could be cleaned up a little more, but it's a million times better than the old system.
 * @author Kneesnap
 */
public class ItemInventoryEvent extends FunctionalItemEvent {
	
	@Getter //The inventory click event that prompted this event.
	private InventoryClickEvent event;
	
	@Getter
	@Setter //The item that is not the DR item being activated at this point. Will be null if the click is not SWAP_WITH_CURSOR
	private ItemStack swappedItem;
	
	//We use these instead of just raw Player#closeInventory, etc because that is unsafe and can cause dupes.
	private boolean closeInventory;
	private Inventory openInventory;
	
	//Whether we should avoid skipping the inventory update event. Only used for the double events. (Swap item calls 2 events)
	private boolean skipUpdate;

	public ItemInventoryEvent(InventoryClickEvent evt) {
		super((Player)evt.getWhoClicked());
		this.event = evt;

		if(evt.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
			setUsage(ItemUsage.INVENTORY_SWAP_PLACE);
			setVanillaItem(evt.getCursor());
			setSwappedItem(evt.getCurrentItem());
		} else if (evt.getAction() == InventoryAction.PLACE_ALL || evt.getAction() == InventoryAction.PLACE_SOME || evt.getAction() == InventoryAction.PLACE_ONE){
			setUsage(ItemUsage.INVENTORY_PLACE_ITEM);
			setVanillaItem(evt.getCursor());
		} else {
			setUsage(ItemUsage.INVENTORY_PICKUP_ITEM);
			setVanillaItem(evt.getCurrentItem());
		}
	}
	
	//This is a special case (D:). Since bukkit has the swap action as one action (SWAP_WITH_CURSOR)
	//we have to combine it into two seperate events ourselves. (We can't just have it as one single
	//event, as we have to do the checks for both items, not to mention the code.)
	//I added this hacky second ItemInventoryEvent that will always represent the picked up item being swapped.
	public ItemInventoryEvent(InventoryClickEvent evt, boolean differentiateConstructor) {
		super((Player)evt.getWhoClicked());
		this.event = evt;
		this.skipUpdate = true;
		
		setUsage(ItemUsage.INVENTORY_SWAP_PICKUP);
		setVanillaItem(evt.getCurrentItem());
		setSwappedItem(evt.getCursor());
	}

	@Override
	public void handle() {
		if (!(getItem() instanceof ItemInventoryListener))
			return;
		//Don't allow items that trigger by activation swapping if you're in another window. IE a trade window. So for instance you can't orb another player's item.
		if(!getEvent().getInventory().getTitle().equals("container.crafting") || getEvent().getSlotType() == SlotType.ARMOR)
			return;
		
		//Call the event
		((ItemInventoryListener) getItem()).onInventoryClick(this);
		
		//Update the result.
		if (getUsage() == ItemUsage.INVENTORY_PICKUP_ITEM || getUsage() == ItemUsage.INVENTORY_SWAP_PICKUP) {
			event.setCurrentItem(getResultItem());
			getPlayer().setItemOnCursor(getSwappedItem());
		} else {
			event.setCurrentItem(getSwappedItem());
			getPlayer().setItemOnCursor(getResultItem());
		}
		
		if (isCancelled())
			event.setCancelled(true);
		
		//These two things right here are because you should never call closeInventory or openInventory before the InventoryClickEvent has completely finished. (Finished in NMS) Otherwise it can lead to bad things such as dupes or unintended clicks, or errors.
		if (this.closeInventory)
			Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> getPlayer().closeInventory());
		
		if (this.openInventory != null)
			Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> getPlayer().openInventory(this.openInventory));
		
		if (this.skipUpdate)
			return;
		
		if (isCancelled())
			getPlayer().updateInventory();
		else
			Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> getPlayer().updateInventory());
	}
	
	public void openInventory(Inventory inv) {
		this.openInventory = inv;
	}
	
	public void closeInventory() {
		this.closeInventory = true;
	}
	
	public interface ItemInventoryListener {
		/**
		 * Called when an item is interacted with in the inventory.
		 */
		public abstract void onInventoryClick(ItemInventoryEvent evt);
	}
}
