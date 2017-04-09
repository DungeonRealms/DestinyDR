package net.dungeonrealms.game.item.items.functional.ecash;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.world.entity.type.mounts.mule.MuleTier;

public class ItemMuleMount extends FunctionalItem {

	@Getter @Setter
	private MuleTier tier;
	
	public ItemMuleMount() {
		super(ItemType.MULE);
		setUntradeable(true);
		setTier(MuleTier.ADVENTURER);
	}
	
	public ItemMuleMount(ItemStack item) {
		super(item);
	}
	
	@Override
	public void loadItem() {
		setTier(MuleTier.valueOf(getTagString(TIER)));
		super.loadItem();
	}
	
	@Override
	public void updateItem() {
		setTagString(TIER, getTier().name());
		super.updateItem();
	}

	@Override
	public void onClick(ItemClickEvent evt) {
		ItemMount.attemptSummonMount(evt.getPlayer(), true);
	}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}
	
	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {}

	@Override
	protected String getDisplayName() {
		return getTier().getName();
	}

	@Override
	protected String[] getLore() {
		return new String[] {
				ChatColor.RED + "Storage Size: " + getTier().getSize() + " Items",
				ChatColor.RED + "An old worn mule."
		};
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT_RIGHT_CLICK;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.LEASH);
	}
}
