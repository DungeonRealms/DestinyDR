package net.dungeonrealms.game.item.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import lombok.Getter;
import lombok.Setter;

/**
 * FunctionalItemEvent. A simple event for whenever a function that an item performs should be triggered.
 * 
 * NOTE: This is not a bukkit event. Don't try to treat it like one :P
 * This is done so we can have the code for using each of the items within its own class.
 * 
 * Created March 27, 2017.
 * @author Kneesnap
 */
public abstract class FunctionalItemEvent {
	
	@Getter //Get the player who triggered this event.
	private Player player;
	
	@Getter //Get the vanilla item.
	private ItemStack vanillaItem;
	
	@Getter
	private FunctionalItem item;

	@Getter //What usages will trigger the code?
	private ItemUsage usage;
	
	@Getter @Setter
	private boolean used;
	
	@Getter @Setter //This marks whether we should cancel the original Bukkit event or not.
	private boolean cancelled;
	
	public FunctionalItemEvent(Player player) {
		this(player, null, null);
	}
	
	public FunctionalItemEvent(Player player, ItemStack item, ItemUsage usage) {
		this.player = player;
		setVanillaItem(item);
		setUsage(usage);
	}
	
	public PlayerWrapper getWrapper() {
		return PlayerWrapper.getWrapper(getPlayer());
	}
	
	protected void setVanillaItem(ItemStack item) {
		setResultItem(item);
		
		PersistentItem pi = PersistentItem.constructItem(getVanillaItem());
		if (pi instanceof FunctionalItem)
			this.item = (FunctionalItem) pi;
	}
	
	protected void setUsage(ItemUsage usage) {
		this.usage = usage;
	}
	
	public void setResultItem(ItemStack item) {
		this.vanillaItem = item;
	}
	
	protected ItemStack getResultItem() {
		if(isUsed()) {
			int amt = getVanillaItem().getAmount();
			if(amt > 1) {
				ItemStack item = getVanillaItem().clone();
				item.setAmount(amt - 1);
				return item;
			}
			return null;
		}
		return getVanillaItem();
	}
	
	public abstract void handle();
}
