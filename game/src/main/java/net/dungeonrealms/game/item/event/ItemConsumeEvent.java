package net.dungeonrealms.game.item.event;

import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.mastery.Utils;

import org.bukkit.event.player.PlayerItemConsumeEvent;

public class ItemConsumeEvent extends FunctionalItemEvent {

	private PlayerItemConsumeEvent event;
	
	public ItemConsumeEvent(PlayerItemConsumeEvent event) {
		super(event.getPlayer(), event.getItem(), ItemUsage.CONSUME_ITEM);
		this.event = event;
	}

	@Override
	public void handle() {
		if (!(getItem() instanceof ItemConsumeListener))
			return;
		((ItemConsumeListener) getItem()).onConsume(this);
		
		event.setCancelled(true);
		
		//  REMOVE ITEM  //
		if (event.getItem().equals(event.getPlayer().getInventory().getItemInMainHand())) {
			event.getPlayer().getInventory().setItemInMainHand(getResultItem());
		} else if (event.getItem().equals(event.getPlayer().getInventory().getItemInOffHand())) {
			event.getPlayer().getInventory().setItemInOffHand(getResultItem());
		} else {
			Utils.log.info("Consumed item was not found in either hand for " + event.getPlayer().getName() + "?");
		}
		
		event.getPlayer().updateInventory();
	}
	
	public interface ItemConsumeListener {
		/**
		 * Called when this item is consumed.
		 */
		public abstract void onConsume(ItemConsumeEvent evt);
	}
}
