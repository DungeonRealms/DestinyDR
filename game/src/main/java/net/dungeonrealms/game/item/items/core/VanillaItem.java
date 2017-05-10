package net.dungeonrealms.game.item.items.core;

import lombok.Setter;
import net.dungeonrealms.game.item.ItemType;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A generic item that can be constructed with any bukkit itemstack.
 * @author Kneesnap
 */
public class VanillaItem extends ItemGeneric {
	@Setter private String displayName;
	
	public VanillaItem(ItemStack item) {
		super(item);
		if (item == null || item.getType() == Material.AIR)
			return;
		ItemMeta meta = item.getItemMeta();
		if (meta.hasLore())
			meta.getLore().forEach(this::addLore); // Lore gets wiped when an item is generated, don't let that happen.
		if (meta.hasDisplayName())
			setDisplayName(meta.getDisplayName());
	}
	
	// Change visibility.
	public void addLore(String lore) {
		super.addLore(lore);
	}

	@Override
	public void updateItem() {
		if (displayName != null)
			getMeta().setDisplayName(displayName);
		super.updateItem();
	}
	
	@Override
	protected ItemStack getStack() {
		return getItem();
	}
	
	@Override
	public ItemType getItemType() {
		return null;
	}
}
