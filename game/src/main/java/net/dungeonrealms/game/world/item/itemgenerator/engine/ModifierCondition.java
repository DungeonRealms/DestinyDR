package net.dungeonrealms.game.world.item.itemgenerator.engine;

import net.dungeonrealms.game.world.item.Item;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModifierCondition {
	
	private Item.ItemTier tier;
	private Item.ItemRarity rarity;
	private ModifierRange range;
	private int chance = -1;
	private List<Class<? extends ItemModifier>> cantContain;
	private ModifierCondition bonus;
	private List<Class<? extends ItemModifier>> replacement;
	private String chosenPrefix;
	private String chosenSuffix;

	public ModifierCondition(Item.ItemTier tier, Item.ItemRarity rarity, ModifierRange range, int chance){
		this.tier = tier;
		this.rarity = rarity;
		this.range = range;
		this.chance = chance;
		this.cantContain = new ArrayList<>();
	}
	
	public ModifierCondition(Item.ItemTier tier, Item.ItemRarity rarity, ModifierRange range){
		this.tier = tier;
		this.rarity = rarity;
		this.range = range;
		this.cantContain = new ArrayList<>();
	}
	
	public void setChosenPrefix(String prefix){
		this.chosenPrefix = prefix;
	}
	
	public void setChosenSuffix(String suffix){
		this.chosenSuffix = suffix;
	}
	
	public String getChosenPrefix(){
		return chosenPrefix;
	}
	
	public String getChosenSuffix(){
		return chosenSuffix;
	}
	
	public ModifierCondition setBonus(ModifierCondition bonus){
		this.bonus = bonus;
		return this;
	}
	
	public ModifierCondition setReplacement(Class<? extends ItemModifier> replacement){
		this.replacement = new ArrayList<>();
		this.replacement.add(replacement);
		return this;
	}
	
	public ModifierCondition setReplacement(List<Class<? extends ItemModifier>> replacement){
		this.replacement = replacement;
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
	
	public List<Class<? extends ItemModifier>> getReplacement(){
		return replacement;
	}
	
	public List<Class<? extends ItemModifier>> getCantContain() {
	    return cantContain;
	}

	public boolean canApply(Set<ModifierCondition> conditions) {
		for(ModifierCondition mc : conditions){
			if(mc.equals(this)) continue;
			
			if(mc.getChosenPrefix() != null){
				if(mc.getChosenPrefix().equals(getChosenPrefix())){
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean checkCantContain(Class<? extends ItemModifier> mod) {
		return !this.getCantContain().contains(mod);
	}
	
}
