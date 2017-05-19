package net.dungeonrealms.game.mastery;

import lombok.Getter;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.item.Item.WeaponAttributeType;

@Getter
public enum Stats {
	
	STRENGTH(ArmorAttributeType.STRENGTH, "Strength",new String[] {"Adds armor, block chance, axe ", "damage, and polearm damage."},11, new StatBoost(ArmorAttributeType.ARMOR, .03f), new StatBoost(ArmorAttributeType.BLOCK, .017f)),
	DEXTERITY(ArmorAttributeType.DEXTERITY, "Dexterity",new String[] {"Adds DPS%, dodge chance, armor", "penetration, and bow damage."},12, new StatBoost(ArmorAttributeType.DAMAGE, .03f), new StatBoost(ArmorAttributeType.DODGE, .017f), new StatBoost(WeaponAttributeType.ARMOR_PENETRATION, .02F)),
	VITALITY(ArmorAttributeType.VITALITY, "Vitality",new String[] {"Adds health, hp regen,", "elemental resistance and", "sword damage."},13, new StatBoost(ArmorAttributeType.HEALTH_POINTS, .034f), new StatBoost(ArmorAttributeType.HEALTH_REGEN, .03f)),
	INTELLECT(ArmorAttributeType.INTELLECT, "Intellect",new String[] {"Adds energy regeneration, critical,",  "hit chance, and staff damage."},14, new StatBoost(ArmorAttributeType.ENERGY_REGEN, .015f), new StatBoost(WeaponAttributeType.CRITICAL_HIT, .025f));
	
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
