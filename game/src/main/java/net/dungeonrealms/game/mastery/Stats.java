package net.dungeonrealms.game.mastery;

import lombok.Getter;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.item.Item.WeaponAttributeType;

@Getter
public enum Stats {
	
	STRENGTH(ArmorAttributeType.STRENGTH, new String[] {"Adds armor, block chance, axe ", "damage, and polearm damage."}, new StatBoost(ArmorAttributeType.ARMOR, .03f), new StatBoost(ArmorAttributeType.BLOCK, .017f)),
	DEXTERITY(ArmorAttributeType.DEXTERITY, new String[] {"Adds DPS%, dodge chance, armor", "penetration, and bow damage."}, new StatBoost(ArmorAttributeType.DAMAGE, .03f), new StatBoost(ArmorAttributeType.DODGE, .017f), new StatBoost(WeaponAttributeType.ARMOR_PENETRATION, .02F)),
	VITALITY(ArmorAttributeType.VITALITY, new String[] {"Adds health, hp regen,", "elemental resistance and", "sword damage."}, new StatBoost(ArmorAttributeType.HEALTH_POINTS, .034f), new StatBoost(ArmorAttributeType.HEALTH_REGEN, .03f)),
	INTELLECT(ArmorAttributeType.INTELLECT, new String[] {"Adds energy regeneration, critical,",  "hit chance, and staff damage."}, new StatBoost(ArmorAttributeType.ENERGY_REGEN, .015f), new StatBoost(WeaponAttributeType.CRITICAL_HIT, .025f));
	
	private ArmorAttributeType type;
	private String[] description;
	private StatBoost[] statBoosts;
	
	Stats(ArmorAttributeType type, String[] lore, StatBoost... boosts) {
		this.statBoosts = boosts;
		this.description = lore;
		this.type = type;
	}
	
	public String getDBField() {
		return "attributes." + name().toLowerCase();
	}
}
