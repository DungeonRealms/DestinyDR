package net.dungeonrealms.game.world.item.itemgenerator.engine;

import lombok.Getter;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;

import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class ItemModifier implements Comparable<ItemModifier> {
	
	private List<ModifierCondition> conditions = new ArrayList<>();
	private List<ItemType> possibleApplicants;
	
	@Getter
	private AttributeType currentAttribute;
	private AttributeType[] possibleAttributes;
	
	public ItemModifier(AttributeType attribute, ItemType... pI) {
		this(new AttributeType[] {attribute}, pI);
	}
	
	public ItemModifier(AttributeType[] attributes, ItemType... possibleItems){
        this.possibleApplicants = Arrays.asList(possibleItems);
        this.possibleAttributes = attributes;
        this.currentAttribute = attributes[0];
        ItemGenerator.modifiers.put(this.getClass(), this);
        ItemGenerator.modifierObjects.add(this);
    }
	
	@Override
	public int compareTo(ItemModifier other){
	    return other.getCurrentAttribute().getId() - getCurrentAttribute().getId();
	}
	
	public boolean canApply(ItemType type) {
		return possibleApplicants != null && possibleApplicants.contains(type);
	}
	
	protected void addCondition(ItemTier tier, ItemRarity rarity, ModifierRange range){
		conditions.add(new ModifierCondition(tier, rarity, range, -1));
	}
	
	protected void addCondition(ModifierCondition condition){
		conditions.add(condition);
	}
	
	public ModifierCondition tryModifier(ItemMeta meta, ItemTier tier, ItemRarity rarity) {
		for(ModifierCondition condition : conditions)
			if(condition.doesConclude(tier, rarity, meta))
				return condition;
		return null;
	}

	public void rollAttribute() {
		if (this.possibleAttributes.length > 1)
			this.currentAttribute = this.possibleAttributes[new Random().nextInt(this.possibleAttributes.length)];
	}
}
