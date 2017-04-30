package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemRealmChest extends FunctionalItem {

	public ItemRealmChest(ItemStack item) {
		super(item);
	}
	
	public ItemRealmChest() {
		super(ItemType.REALM_CHEST);
	}

	@Override
	public void onClick(ItemClickEvent evt) {}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {}

	@Override
	protected String getDisplayName() {
		return ChatColor.GREEN + "Realm Chest";
	}

	@Override
	protected String[] getLore() {
		return new String[] { "This chest can only be placed in realms." };
	}

	@Override
	protected ItemUsage[] getUsage() {
		return null;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.CHEST);
	}
}
