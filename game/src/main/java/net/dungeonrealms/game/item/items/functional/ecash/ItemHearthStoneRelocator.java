package net.dungeonrealms.game.item.items.functional.ecash;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;

public class ItemHearthStoneRelocator extends ItemGeneric {

	@Getter @Setter
	private TeleportLocation location;

	public ItemHearthStoneRelocator(ItemStack item) {
		super(item);
		setLocation(TeleportLocation.valueOf(getTagString("loc")));
	}

	public ItemHearthStoneRelocator(TeleportLocation loc) {
		super(ItemType.HEARTHSTONE_RELOCATE);
		setLocation(loc);
	}

	@Override
	public void updateItem() {
		setTagString("loc", getLocation().name());
		getMeta().setDisplayName(ChatColor.WHITE + getLocation().getDisplayName());
		super.updateItem();
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.BEACON);
	}

}
