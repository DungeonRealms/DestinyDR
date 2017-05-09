package net.dungeonrealms.game.mastery;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierRange;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierType;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

public class AttributeList extends HashMap<AttributeType, ModifierRange> {
	private static final long serialVersionUID = 1L;

	public AttributeList() {
		
	}
	
	/**
	 * Does this item have a specified attribute?
	 */
	public boolean hasAttribute(AttributeType t) {
		return this.containsKey(t) && get(t) != null && get(t).getValue() != 0;
	}
	
	/**
	 * Get a specifier attribute's value.
	 */
	public ModifierRange getAttribute(AttributeType t) {
		return hasAttribute(t) ? this.get(t) : new ModifierRange(t.isRange() ? ModifierType.RANGE : ModifierType.STATIC, 0, 0);
	}
	
	/**
	 * Gets a list of all the attributes this has.
	 * Basically keySet() except it checks the value is not 0
	 */
	public List<AttributeType> getAttributes() {
		return keySet().stream().filter(this::hasAttribute).collect(Collectors.toList());
	}
	
	/**
	 * Multiplies the given attribute value(s). Fails silently if not found.
	 */
	public void multiplyStat(AttributeType t, double multiplier) {
		if(!hasAttribute(t))
			return;
		ModifierRange range = getAttribute(t);
		int newVal = (int) Math.round(range.getValue() * multiplier) + 1;
		if(t.isRange()) {
			int highVal = (int) Math.round(range.getValHigh() * multiplier) + 1;
			range.setVal(newVal, highVal);
		} else {
			range.setVal(newVal);
		}
	}
	
	public void addStats(ItemStack item) {
		if(!ItemGear.isCustomTool(item))
			return;
		addStats(((ItemGear)PersistentItem.constructItem(item)).getAttributes());
	}
	
	/**
	 * Add all the stats from another list to this one.
	 */
	public void addStats(AttributeList list) {
		for (AttributeType t : list.keySet())
			addStat(t, list.getAttribute(t));
	}
	
	public void removeStats(ItemStack item) {
		if(!ItemGear.isCustomTool(item))
			return;
		removeStats(((ItemGear)PersistentItem.constructItem(item)).getAttributes());
	}
	
	/**
	 * Subtracts all the current stats based on another list.
	 */
	public void removeStats(AttributeList list) {
		for(AttributeType type : list.keySet())
			subtractStat(type, list.getAttribute(type));
	}
	
	/**
	 * Subtracts a value from a given attribute.
	 * Fails silently if attribute is not found.
	 */
	public void subtractStat(AttributeType t, ModifierRange mr) {
		ModifierRange take = mr.clone();
		System.out.println("Attempting to remove " + t.getNBTName() + ", " + mr.toString());
		take.setVal(-take.getValLow(), -take.getValHigh());
		System.out.println("Doing this by adding " + take.toString());
		addStat(t, take);
	}
	
	public void substractStat(AttributeType t, int remove) {
		addStat(t, -remove);
	}
	
	/**
	 * Adds a value to the given attribute.
	 * Fails silently if attribute is not found.
	 */
	public void addStat(AttributeType t, ModifierRange range) {
		ModifierRange mr = getAttribute(t);
		
		System.out.println("Adding " + range.toString() + " to " + t.getNBTName());
		if(t.isRange()) {
			setStatRange(t, range.getValLow() + mr.getValLow(), range.getValHigh() + mr.getValHigh());
		} else {
			setStat(t, range.getValue() + mr.getValue());
		}
		System.out.println("Ok! Is now " + getAttribute(t).toString());
	}
	
	public void addStat(AttributeType t, int inc) {
		addStat(t, new ModifierRange(inc));
	}
	
	/**
	 * Sets the value of a static attribute type.
	 * Does not work on ranged attributes.
	 */
	public void setStat(AttributeType t, int val) {
		assert !t.isRange();
		this.put(t, new ModifierRange(val));
	}
	
	/**
	 * Sets the value range of an attribute type.
	 * Only works with ranged / triple attributes.
	 */
	public void setStatRange(AttributeType t, int low, int high) {
		assert t.isRange();
		this.put(t, new ModifierRange(ModifierType.RANGE, low, high));
	}
	
	/**
	 * Apply stat boosts for STR / DEX / VIT / INT
	 */
	public void applyStatBonuses() {
		for(Stats stat : Stats.values()) {
        	if(!hasAttribute(stat.getType()))
        		continue;
        	for (StatBoost boost : stat.getStatBoosts())
        		if(hasAttribute(boost.getType()))
        			addStat(boost.getType(), (int) (getAttribute(stat.getType()).getValue() + boost.getMultiplier()));
        }
	}

	public void save(NBTTagCompound tag) {
		NBTTagCompound attr = new NBTTagCompound();
		for (AttributeType type : getAttributes())
			getAttribute(type).save(attr, type.getNBTName());
		tag.set("itemAttributes", attr);
	}
	
	public void load(NBTTagCompound tag, AttributeType[] types) {
		NBTTagCompound attr = tag.getCompound("itemAttributes");
		
		for (String key : attr.c()) {
			//  FIND ATTRIBUTE TYPE  //
			AttributeType aType = null;
			for (AttributeType type : types) 
				if(type.getNBTName().equals(key))
					aType = type;
			
			if (aType != null) {
				this.put(aType, new ModifierRange(attr.get(key)));
			} else {
				Utils.log.info("Could not find Item attribute " + key + " for " + key.toString());
			}
		}
	}
	
	@Override
	public String toString() {
		String values = "";
		for (AttributeType type : keySet())
			values += "," + type.getNBTName() + "->" + getAttribute(type).toString();
		return "[AttributeList (" + size() + ")" + values + "]";
	}
}
