package net.dungeonrealms.game.item.event;

import java.util.Arrays;

import lombok.Getter;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemUsage;

/**
 * ItemClickEvent - For when an item is used in the world somehow.
 * 
 * Created March 27, 2017.
 * @author Kneesnap
 */
public class ItemClickEvent extends FunctionalItemEvent {
	
	@Getter
	private Block clickedBlock;
	
	@Getter
	private Entity clickedEntity;
	
	private Cancellable event;
	
	@Getter
	private EquipmentSlot hand;
	
	public ItemClickEvent(EntityDamageByEntityEvent evt) {
		this((Player)evt.getDamager(), ((Player)evt.getDamager()).getEquipment().getItemInMainHand(),
				EquipmentSlot.HAND, evt, ItemUsage.LEFT_CLICK_ENTITY);
		this.clickedEntity = evt.getEntity();
	}
	
	public ItemClickEvent(PlayerInteractEntityEvent evt) {
		this(evt.getPlayer(), GameAPI.getItem(evt.getPlayer(), evt.getHand()), evt.getHand(), evt, ItemUsage.RIGHT_CLICK_ENTITY);
		this.clickedEntity = evt.getRightClicked();
	}
	
	public ItemClickEvent(PlayerInteractEvent evt) {
		this(evt.getPlayer(), evt.getItem(), evt.getHand(), evt, ItemUsage.valueOf(evt.getAction().name()));
		this.clickedBlock = evt.hasBlock() ? evt.getClickedBlock() : null;
	}
	
	private ItemClickEvent(Player player, ItemStack item, EquipmentSlot hand, Cancellable event, ItemUsage usage) {
		super(player, item, usage);
		this.hand = hand;
		this.event = event;
		setCancelled(true); //By default we block the bukkit event for this so it doesn't mess up the world.
	}

	@Override
	public void handle() {
		getItem().onClick(this);
		
		GameAPI.setHandItem(getPlayer(), getResultItem(), getHand());
		
		if(isCancelled() && this.event != null)
			this.event.setCancelled(true);
	}
	
	/**
	 * Was the player interacting with a block?
	 */
	public boolean hasBlock() {
		return getClickedBlock() != null;
	}
	
	/**
	 * Was the player interacting with an entity?
	 */
	public boolean hasEntity() {
		return getClickedEntity() != null;
	}
	
	/**
	 * Was the left mouse button used?
	 */
	public boolean isLeftClick() {
		return Arrays.asList(ItemUsage.LEFT_CLICK_AIR, ItemUsage.LEFT_CLICK_BLOCK, ItemUsage.LEFT_CLICK_ENTITY).contains(getUsage());
	}
	
	/**
	 * Was the right mouse button used?
	 */
	public boolean isRightClick() {
		return Arrays.asList(ItemUsage.RIGHT_CLICK_AIR, ItemUsage.RIGHT_CLICK_BLOCK, ItemUsage.RIGHT_CLICK_ENTITY).contains(getUsage());
	}
	
	/**
	 * Is this event called while the player is sneaking?
	 */
	public boolean isSneaking() {
		return getPlayer().isSneaking();
	}
}
