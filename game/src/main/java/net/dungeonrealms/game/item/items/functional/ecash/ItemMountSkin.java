package net.dungeonrealms.game.item.items.functional.ecash;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import net.md_5.bungee.api.ChatColor;

public class ItemMountSkin extends ItemGeneric {

	@Getter @Setter
	private EnumMountSkins skin;
	
	public ItemMountSkin(ItemStack item) {
		super(item);
		setSkin(EnumMountSkins.valueOf(getTagString("skinType")));
	}
	
	public ItemMountSkin(EnumMountSkins s) {
		super(ItemType.MOUNT_SKIN_SELECTOR);
		setSkin(s);
	}
	
	@Override
	public void updateItem() {
		addLore(ChatColor.RED + "Requires a mount to purchase");
		setTagString("skinType", getSkin().name());
		getMeta().setDisplayName(ChatColor.GREEN + getSkin().getDisplayName() + " Skin");
		super.updateItem();
	}
	
	@Override
	protected ItemStack getStack() {
		return getSkin().getSelectionItem();
	}
}
