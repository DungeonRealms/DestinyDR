package net.dungeonrealms.game.item.items.functional;

import lombok.Getter;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;

import org.bukkit.inventory.ItemStack;

public abstract class ItemMoney extends FunctionalItem {
	
	//Whether or not to write the gem value to NBT.
	//Gems have this disabled so they stack properly.
	//Their value is their size.
	private boolean writeNbt;
	
	@Getter
	private int gemValue;
	
	public ItemMoney(ItemType type, int gemAmount) {
		this(type, gemAmount, true);
	}
	
	public ItemMoney(ItemType type, int gemAmount, boolean nbtWrite) {
		super(type);
		this.writeNbt = nbtWrite;
		setGemValue(gemAmount);
	}
	
	public ItemMoney(ItemStack item) {
		super(item);
	}
	
	@Override
	public void loadItem() {
		this.writeNbt = hasTag("worth");
		if(this.writeNbt)
			setGemValue(getTagInt("worth"));
		super.loadItem();
	}
	
	@Override
	public void updateItem() {
		if (this.writeNbt)
			setTagInt("worth", getGemValue());
		super.updateItem();
	}
	
	/**
	 * Sets the gem value of this item.
	 */
	public void setGemValue(int value) {
		assert getMaxStorage() >= value && value >= 0;
		if (value == 0 && doesDestroyOnEmpty())
			setDestroyed(true);
		this.gemValue = value;
	}
	
	/**
	 * Gets the maximum amount of gems this item can represent.
	 */
	public abstract int getMaxStorage();
	
	/**
	 * Returns the stack that should replace this item when 
	 * a transaction occurs that completely removes this item,
	 * such as being deposited into the bank. Gem pouches use this to not disappear.
	 */
	protected boolean doesDestroyOnEmpty() {
		return true;
	}

	@Override
	public void onClick(ItemClickEvent evt) {}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {}
	
	public static boolean isMoney(ItemStack item) {
		return ItemGem.isGem(item) || ItemGemNote.isGemNote(item) || ItemGemPouch.isPouch(item);
	}
}
