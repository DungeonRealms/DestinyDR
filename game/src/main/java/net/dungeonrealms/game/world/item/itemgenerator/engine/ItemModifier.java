package net.dungeonrealms.game.world.item.itemgenerator.engine;

import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.Item.ItemType;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class ItemModifier implements Comparable<ItemModifier> {
	
	private List<ModifierCondition> conditions = new ArrayList<>();
	private List<ItemType> possibleApplicants;
	private int chance = 0; // chance of obtaining modifier on item
	private String NBTName; // internal name for NBT tags
	private String prefix; // prefix on lore
	private String suffix; // suffix on lore
	private int orderPriority = 10;
	private boolean includeOnReroll = true;
	protected String chosenStat; // for multiple possible stats
	
	public ItemModifier(List<ItemType> possibleApplicants, int chance, String NBTName, String prefix, String suffix){
		this.possibleApplicants = possibleApplicants;
		this.chance = chance;
		this.NBTName = NBTName;
		this.prefix = prefix;
		this.suffix = suffix;
		ItemGenerator.modifiers.put(this.getClass(), this);
		ItemGenerator.modifierObjects.add(this);
	}
	
	public ItemModifier(List<ItemType> possibleApplicants, int chance, String NBTName, String prefix, String suffix, boolean includeOnReroll){
        this.possibleApplicants = possibleApplicants;
        this.chance = chance;
        this.NBTName = NBTName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.includeOnReroll = includeOnReroll;
        ItemGenerator.modifiers.put(this.getClass(), this);
        ItemGenerator.modifierObjects.add(this);
    }
	
	public void setOrderPriority(int position){
		this.orderPriority = position;
	}
	
	public int getOrderPriority(){
		return orderPriority;
	}
	
	@Override
	public int compareTo(ItemModifier other){
	    return other.getOrderPriority() - orderPriority;
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
	
	public ModifierCondition tryModifier(ItemMeta meta, ItemTier tier, ItemRarity rarity, ItemType type, int mobTier, boolean override){
		for(ModifierCondition condition : conditions){
			if(condition.doesConclude(tier, rarity, meta)){
				String prefix = getPrefix(meta);
				String suffix = getSuffix(meta);
				
				if(condition.getReplacement() != null && condition.getReplacement().size() > 0){
					ItemModifier replacement = ItemGenerator.modifiers.get(condition.getReplacement().get(new Random().nextInt(condition.getReplacement().size())));
					prefix = replacement.getPrefix(meta);
					suffix = replacement.getSuffix(meta);
				}
				
				condition.setChosenPrefix(prefix);
				condition.setChosenSuffix(suffix);
				return condition;
			}
		}
		return null;
	}
	
	public ItemMeta applyModifier(ModifierCondition condition, ItemMeta meta){
		String random = condition.getRange().generateRandom();
		random = ((condition.getChosenPrefix() != null) ? condition.getChosenPrefix() : "") + random + ((condition.getChosenSuffix() != null) ? condition.getChosenSuffix() : "");
		
		List<String> lore = meta.getLore();
		lore.add(random);
		meta.setLore(lore);
		
		return meta;
	}
	
	public ModifierCondition tryModifier(ItemMeta meta, ItemTier tier, ItemRarity rarity, ItemType type, int mobTier){
		return tryModifier(meta, tier, rarity, type, mobTier, false);
	}
	
	public ModifierCondition tryModifier(ItemMeta meta, ItemTier tier, ItemRarity rarity, ItemType type){
		return tryModifier(meta, tier, rarity, type, -1, false);
	}

	public String getPrefix(ItemMeta meta) {
		return prefix;
	}

	public String getSuffix(ItemMeta meta) {
		return suffix;
	}
	
	public int getChance(){
		return chance;
	}

    public boolean isIncludeOnReroll() {
        return includeOnReroll;
    }

    public void setIncludeOnReroll(boolean includeOnReroll) {
        this.includeOnReroll = includeOnReroll;
    }

    public String getNBTName() {
        return NBTName;
    }

    // choose one stat for multiple stat modifiers
    public void chooseStat() {}
}
