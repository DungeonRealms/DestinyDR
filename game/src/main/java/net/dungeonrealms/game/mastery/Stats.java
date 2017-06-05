package net.dungeonrealms.game.mastery;

import lombok.Getter;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.item.Item.WeaponAttributeType;

@Getter
public enum Stats {
	
	STRENGTH(ArmorAttributeType.STRENGTH, "Strength", new String[] {"Adds armor, block chance, axe ", "damage, and polearm damage."}, 11, new StatBoost(ArmorAttributeType.ARMOR, .0003f), new StatBoost(ArmorAttributeType.BLOCK, .0002f)),
	DEXTERITY(ArmorAttributeType.DEXTERITY, "Dexterity", new String[] {"Adds DPS%, dodge chance, armor", "penetration, and bow damage."}, 12, new StatBoost(ArmorAttributeType.DAMAGE, .0003f), new StatBoost(ArmorAttributeType.DODGE, .0002f), new StatBoost(WeaponAttributeType.ARMOR_PENETRATION, .0002F)),
	VITALITY(ArmorAttributeType.VITALITY, "Vitality", new String[] {"Adds health, hp regen,", "elemental resistance and", "sword damage."}, 13, new StatBoost(ArmorAttributeType.HEALTH_POINTS, .0003f), new StatBoost(ArmorAttributeType.HEALTH_REGEN, .003f)),
	INTELLECT(ArmorAttributeType.INTELLECT, "Intellect", new String[] {"Adds energy regeneration, critical,",  "hit chance, and staff damage."}, 14, new StatBoost(ArmorAttributeType.ENERGY_REGEN, .00015f));
	
	private ArmorAttributeType type;
	private String[] description;
	private StatBoost[] statBoosts;
	private int guiSlot;
	private String displayName;

	Stats(ArmorAttributeType type, String displayName,String[] lore, int guiSlot,StatBoost... boosts) {
		this.statBoosts = boosts;
		this.description = lore;
		this.type = type;
		this.guiSlot = guiSlot;
		this.displayName = displayName;
	}


	public String getDBField() {
		return "attributes." + name().toLowerCase();
	}
}
