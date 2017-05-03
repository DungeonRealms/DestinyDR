package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent.ItemInventoryListener;
import net.dungeonrealms.game.mechanic.data.PouchTier;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

public class ItemGemPouch extends ItemMoney implements ItemInventoryListener {
	
	private PouchTier tier;
	
	public ItemGemPouch(PouchTier tier) {
		super(ItemType.GEM_POUCH, 0);
		this.tier = tier;
	}
	
	public ItemGemPouch(ItemStack item) {
		super(item);
		this.tier = PouchTier.getById(getTagInt(TIER));
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
		return GameAPI.getTierColor(tier.getId()) + tier.getName() + " Gem Pouch " + ChatColor.GREEN + getGemValue() + "g";
	}

	@Override
	protected String[] getLore() {
		return new String[] { "A small linen pouch that holds " + tier.getSize() + "g" };
	}
	
	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {
		if(evt.getEvent().isRightClick() && evt.getSwappedItem() == null) {
			//Lets players take gems from pouches.
			evt.setCancelled(true);
			int withdrawGems = Math.max(getGemValue(), 64);
			setGemValue(getGemValue() - withdrawGems);
			evt.setSwappedItem(new ItemGem(withdrawGems).generateItem());
			evt.setResultItem(generateItem());
			evt.getPlayer().playSound(evt.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
		} else if (ItemGem.isGem(evt.getSwappedItem())) {
			//Deposit gems.
			evt.setCancelled(true);
			ItemGem gem = new ItemGem(evt.getSwappedItem());
			int oldGemValue = getGemValue();
			
			setGemValue(Math.max(getGemValue() + gem.getGemValue(), getMaxStorage()));
			evt.setResultItem(generateItem());
			
			gem.setGemValue(gem.getGemValue() - (getGemValue() - oldGemValue));
			evt.setSwappedItem(gem.generateItem());
			evt.getPlayer().playSound(evt.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
		}
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
