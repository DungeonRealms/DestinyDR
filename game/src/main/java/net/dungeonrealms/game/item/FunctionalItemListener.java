package net.dungeonrealms.game.item;

import java.util.Arrays;
import java.util.List;

import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class FunctionalItemListener implements Listener {

	private static final List<InventoryAction> CHECKED_CLICKS = Arrays.asList(InventoryAction.SWAP_WITH_CURSOR,
			InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ONE,
			InventoryAction.PLACE_ALL, InventoryAction.PLACE_SOME, InventoryAction.PLACE_ONE, InventoryAction.MOVE_TO_OTHER_INVENTORY,
			InventoryAction.HOTBAR_MOVE_AND_READD, InventoryAction.HOTBAR_SWAP);
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent evt) {
		if(evt.getAction() != Action.PHYSICAL)
			FunctionalItem.attemptUseItem(new ItemClickEvent(evt));
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent evt) {
		if(!CHECKED_CLICKS.contains(evt.getAction()))
			return;
		
		FunctionalItem.attemptUseItem(new ItemInventoryEvent(evt));


		//Two events get called for this, one for the item we're placing, and the one we're pulling.
		if (evt.getAction() == InventoryAction.SWAP_WITH_CURSOR)
			FunctionalItem.attemptUseItem(new ItemInventoryEvent(evt, true));
	}
	
	@EventHandler
	public void onPlayerConsume(PlayerItemConsumeEvent evt) {
		FunctionalItem.attemptUseItem(new ItemConsumeEvent(evt));
	}
	
	@EventHandler
	public void onEntityAttack(EntityDamageByEntityEvent evt) {
		if (evt.getDamager() instanceof Player)
			FunctionalItem.attemptUseItem(new ItemClickEvent(evt));
	}
	
	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent evt) {
		FunctionalItem.attemptUseItem(new ItemClickEvent(evt));
	}
}