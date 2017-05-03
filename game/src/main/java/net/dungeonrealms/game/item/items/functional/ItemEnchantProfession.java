package net.dungeonrealms.game.item.items.functional;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.item.Item.ProfessionAttribute;

public abstract class ItemEnchantProfession extends ItemEnchantScroll {

	private AttributeList attributes = new AttributeList();
	
	public ItemEnchantProfession(ItemType type, String enchantType) {
		this(type, enchantType, null);
	}
	
	public ItemEnchantProfession(ItemType type, String enchantType, ProfessionItem pi) {
		super(null, type, enchantType);
		if (pi != null)
			this.attributes.addStats(pi.getAttributes());
	}
	
	public ItemEnchantProfession(ItemStack stack) {
		super(stack);
		this.attributes.load(getTag(), getValues());
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
