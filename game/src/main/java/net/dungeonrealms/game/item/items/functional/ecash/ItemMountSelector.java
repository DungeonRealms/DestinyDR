package net.dungeonrealms.game.item.items.functional.ecash;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.mechanic.data.HorseTier;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.inventory.ItemStack;

public class ItemMountSelector extends ItemGeneric {
	
	@Getter @Setter
	private HorseTier tier;
	
	public ItemMountSelector(ItemStack item) {
		super(item);
	}
	
	public ItemMountSelector(HorseTier tier) {
		super(ItemType.MOUNT_SELECTOR);
		setTier(tier);
	}
	
	@Override
	public void loadItem() {
		setTier(HorseTier.getByTier(getTagInt(TIER)));
		super.loadItem();
	}
	
	@Override
	public void updateItem() {
		if (getTier().getSpeed() != 100)
			addLore("Speed " + getTier().getSpeed() + "%");
		if (getTier().getJump() != 100)
			addLore("Jump " + getTier().getJump() + "%");
		addLore(ChatColor.ITALIC + getTier().getDescription());
		addLore(ChatColor.RED + "" + ChatColor.BOLD + "REQ: " + ChatColor.RESET + ChatColor.AQUA + HorseTier.getByTier(getTier().getId() - 1).getDescription());
		setTagInt(TIER, getTier().getId());
		super.updateItem();
	}

	@Override
	protected ItemStack getStack() {
		return getTier().getMount().getSelectionItem();
	}
}
