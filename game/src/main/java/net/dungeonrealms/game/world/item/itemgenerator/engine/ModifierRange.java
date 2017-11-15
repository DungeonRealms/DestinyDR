package net.dungeonrealms.game.world.item.itemgenerator.engine;

import net.dungeonrealms.game.mastery.Utils;
import net.minecraft.server.v1_9_R2.NBTBase;
import net.minecraft.server.v1_9_R2.NBTBase.NBTNumber;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagInt;
import net.minecraft.server.v1_9_R2.NBTTagIntArray;
import net.minecraft.server.v1_9_R2.NBTTagList;
import lombok.Getter;

//TODO: This is bad, might be worthwhile to redo this.
public class ModifierRange implements Cloneable {

	@Getter private ModifierType modifierType;
	@Getter private int low, lowHigh, highLow, high;
	@Getter private int valLow, valHigh;
	private boolean halve;
	
	public ModifierRange(NBTBase loadFrom) {
		if (loadFrom instanceof NBTNumber) {
			valLow = ((NBTNumber)loadFrom).d();
			this.modifierType = ModifierType.STATIC;
		} else if (loadFrom instanceof NBTTagList) {
			NBTTagList range = (NBTTagList)loadFrom;
			valLow = range.c(0);
			valHigh = range.c(1);
			this.modifierType = ModifierType.RANGE;
		} else if (loadFrom instanceof NBTTagIntArray) {
			NBTTagIntArray arr = (NBTTagIntArray) loadFrom;
			valLow = arr.c()[0];
			valHigh = arr.c()[1];
			this.modifierType = ModifierType.RANGE;
		} else {
			Utils.log.info("Tried to load unknown NBT type as ModifierRange!. Id = " + loadFrom.getTypeId());
		}
	}

	public ModifierRange(int val) {
		this(ModifierType.STATIC, val, val);
	}
	
    public ModifierRange(ModifierType type, int low, int high){
		this(type, low,0,0, high);
		setVal(low, high);
	}
	
	public ModifierRange(ModifierType type, int low, int high, boolean halve){
        this(type, low, high);
        this.halve = halve;
    }

//	public ModifierRange(ModifierType type, int low, int lowHigh, int highLow){
//		this.modifierType = type;
//		this.low = low;
//		this.lowHigh = lowHigh;
//		this.highLow = highLow;
//
//		if (high < low || (lowHigh < low && lowHigh > 0)) {
////			Utils.printTrace();
////			Utils.log.info("Received a modifier range with a negative value? Type = " + type.name() + ", Bounds = (" + low + "," + lowHigh + "," + high + ")");
//		}
//	}

	public ModifierRange(ModifierType type, int low, int lowHigh, int highLow, int high) {
		this.modifierType = type;
		this.low = low;
		this.lowHigh = lowHigh;
		this.highLow = highLow;
		this.high = high;
	}
	
	/**
	 * Generates the random values for this range.
	 * Should only be called when trying to generate the stat values, not to get a random value in their range.
	 */
	public void generateRandom() {

//	 if (modifierType == ModifierType.RANGE || modifierType == ModifierType.TRIPLE) {
//			valLow = Utils.randInt(low, high); //or low if < 0?
//			valHigh = highLow;
//
//			if (modifierType == ModifierType.TRIPLE)
//				valLow = lowHigh < low ? low : Utils.randInt(low, lowHigh); //Or low if < 0.
//
//			if (highLow - valLow > 0) {
//				valHigh = Utils.randInt(valLow, highLow);
//
//				if(modifierType == ModifierType.TRIPLE) {
//					double percent = (valLow / ((double)valHigh)) * 100.0;
//					if(percent >= 90) {
//						valHigh *= 1.2;
//					}
//				}
//			}
//		}
		valLow = Utils.randInt(low, high); //or low if < 0?
		valHigh = high;

		if (modifierType == ModifierType.RANGE || modifierType == ModifierType.QUAD) {
				valLow = Utils.randInt(low, lowHigh);
				valHigh = Utils.randInt(highLow, high);

		} else {
			if (halve)
				valLow = valLow >= 2 ? valLow / 2 : 1;
		}
	}
	
	/**
	 * Gets a random value in a range.
	 */
	public double getValueInRange() {
		assert modifierType != ModifierType.STATIC;
		return Utils.randInt(getValLow(), getValHigh());
	}
	
	/**
	 * Gets the middle value between ranged ranges.
	 */
	public double getMiddle() {
		assert modifierType != ModifierType.STATIC;
		return (double)(getValLow() + getValHigh()) / 2;
	}
	
	/**
	 * Gets a static attribute value.
	 * 
	 * If it's a ranged value, return the highest possible value. (Not Recommended)
	 * This functionality is mainly present for easy number displaying, such as for handling armor changes.
	 */
	public int getValue() {

		return modifierType == ModifierType.RANGE ? getValHigh() : getValLow();
	}
	
	/**
	 * Sets the static attribute value.
	 * @param val
	 */
	public void setVal(int val) {

		this.valLow = val;
	}
	
	/**
	 * Set the two bounds for a ranged value.
	 */
	public void setVal(int low, int high) {
		this.valLow = low;
		this.valHigh = high;
	}
	
	/**
	 * Return this range as an int[] array.
	 */
	public int[] toArr() {
		return modifierType == ModifierType.STATIC ? new int[] {0, getValue()} : new int[] {getValLow(), getValHigh()};
	}
	
	/**
	 * Get this value as a string.
	 */
	@Override
	public String toString() {
		return modifierType == ModifierType.STATIC ? String.valueOf(getValue()) : getValLow() + " - " + getValHigh();
	}
	
	/**
	 * Create a clone of this range.
	 */
	@Override
	public ModifierRange clone() {
		ModifierRange mr = new ModifierRange(modifierType, low, lowHigh, highLow, high);
		mr.setVal(valLow, valHigh);
		return mr;

	}
	
	/**
	 * Save this range to an NBT Tag Compound.
	 */
	public void save(NBTTagCompound tag, String tagName) {
		if (modifierType == ModifierType.RANGE || modifierType == ModifierType.QUAD) {
			NBTTagList range = new NBTTagList();
			range.add(new NBTTagInt(getValLow()));
			range.add(new NBTTagInt(getValHigh()));
			tag.set(tagName, range);
		} else {
			tag.setInt(tagName, getValue());
		}
	}
}
