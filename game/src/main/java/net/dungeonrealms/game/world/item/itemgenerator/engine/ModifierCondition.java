package net.dungeonrealms.game.world.item.itemgenerator.engine;

import net.dungeonrealms.game.world.item.Item;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ModifierCondition {
	
	private Item.ItemTier tier;
	private Item.ItemRarity rarity;
	private ModifierRange range;
	private int chance = -1;
	private List<Class<? extends ItemModifier>> cantContain;
	private ModifierCondition bonus;

	public ModifierCondition(Item.ItemTier tier, Item.ItemRarity rarity, ModifierRange range){
		this(tier, rarity, range, 0);
	}
	
	public ModifierCondition(Item.ItemTier tier, Item.ItemRarity rarity, ModifierRange range, int chance){
		this.tier = tier;
		this.rarity = rarity;
		this.range = range;
		this.chance = chance;
		this.cantContain = new ArrayList<>();
	}
	
	public ModifierCondition setBonus(ModifierCondition bonus){
		this.bonus = bonus;
		return this;
	}
	
	public ModifierCondition setCantContain(Class<? extends ItemModifier> cantContain){
		this.cantContain.add(cantContain);
		return this;
	}
	
	public ModifierCondition setCantContain(List<Class<? extends ItemModifier>> cantContain){
		this.cantContain = cantContain;
		return this;
	}
	
	public boolean doesConclude(Item.ItemTier tier, Item.ItemRarity rarity, ItemMeta meta) {
        return !(this.tier != null && this.tier != tier) && !(this.rarity != null && this.rarity != rarity);
    }
	
	public ModifierRange getRange(){
		return range;
	}
	
	public int getChance(){
		return chance;
	}
	
	public ModifierCondition getBonus(){
		return bonus;
	}
	
	public List<Class<? extends ItemModifier>> getCantContain() {
	    return cantContain;
	}
	
	public boolean checkCantContain(Class<? extends ItemModifier> mod) {
		return !this.getCantContain().contains(mod);
	}
	
}
