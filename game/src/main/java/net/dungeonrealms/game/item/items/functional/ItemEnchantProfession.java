package net.dungeonrealms.game.item.items.functional;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.item.Item.ProfessionAttribute;

public abstract class ItemEnchantProfession extends ItemEnchantScroll {

	private AttributeList attributes = new AttributeList();
	
	public ItemEnchantProfession(ItemType type, String enchantType) {
		super(null, type, enchantType);
	}
	
	public ItemEnchantProfession(ItemStack stack) {
		super(stack);
	}
	
	@Override
	public void loadItem() {
		this.attributes.load(getTag(), getValues());
		super.loadItem();
	}
	
	@Override
	public void updateItem() {
		this.attributes.save(getTag());
		super.updateItem();
	}
	
	@Override
	public void enchant(Player player, ItemGear gear) {
		gear.getAttributes().addStats(attributes);
	}
	
	protected void add(ProfessionAttribute attr) {
		this.attributes.setStat(attr, Utils.randInt(attr.getPercentRange()[0], attr.getPercentRange()[1]));
	}
	
	/**
	 * Get a list of possible attribute types.
	 */
	protected abstract ProfessionAttribute[] getValues();
}
