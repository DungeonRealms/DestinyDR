package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.mechanic.data.EnumTier;
import net.dungeonrealms.game.mechanic.data.PouchTier;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

public class ItemGemPouch extends ItemMoney {
	
	private PouchTier tier;
	
	public ItemGemPouch(PouchTier tier) {
		super(ItemType.GEM_POUCH, 0);
		this.tier = tier;
	}
	
	public ItemGemPouch(ItemStack item) {
		super(item);
	}
	
	@Override
	public void loadItem() {
		this.tier = PouchTier.getById(getTagInt(TIER));
		super.loadItem();
	}
	
	@Override
	public void updateItem() {
		setTagInt(TIER, tier.getId());
		super.updateItem();
	}
	
	@Override
	protected boolean doesDestroyOnEmpty() {
		return false;
	}

	@Override
	public int getMaxStorage() {
		return tier.getSize();
	}

	@Override
	protected String getDisplayName() {
		return EnumTier.getById(tier.getId()).getColor() + tier.getName() + " Gem Pouch " + ChatColor.GREEN + getGemValue() + "g";
	}

	@Override
	protected String[] getLore() {
		return new String[] { "A small linen pouch that holds " + tier.getSize() + "g" };
	}
	
	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {
		//Lets players take gems from pouches.
		//TODO: Allow putting gems into pouches.
		if(!evt.getEvent().isRightClick())
			return;
		
		evt.setCancelled(true);
		int withdrawGems = Math.max(getGemValue(), 64);
		setGemValue(getGemValue() - withdrawGems);
		evt.setSwappedItem(new ItemGem(withdrawGems).generateItem());
		evt.setResultItem(generateItem());
        evt.getPlayer().playSound(evt.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
	}

	@Override
	protected ItemUsage[] getUsage() {
		return new ItemUsage [] {ItemUsage.INVENTORY_SWAP_PICKUP};
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.INK_SACK);
	}
	
	public static boolean isPouch(ItemStack item) {
		return isType(item, ItemType.GEM_POUCH);
	}
}
