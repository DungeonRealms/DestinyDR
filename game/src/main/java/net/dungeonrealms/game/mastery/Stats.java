package net.dungeonrealms.game.mastery;

import lombok.Getter;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.item.Item.WeaponAttributeType;

public enum Stats {
	
	STRENGTH(ArmorAttributeType.STRENGTH, EnumData.STRENGTH, new String[] {"Adds armor, block chance, axe ", "damage, and polearm damage."}, new StatBoost(ArmorAttributeType.ARMOR, .03f), new StatBoost(ArmorAttributeType.BLOCK, .017f)),
	DEXTERITY(ArmorAttributeType.DEXTERITY, EnumData.DEXTERITY, new String[] {"Adds DPS%, dodge chance, armor", "penetration, and bow damage."}, new StatBoost(ArmorAttributeType.DAMAGE, .03f), new StatBoost(ArmorAttributeType.DODGE, .017f), new StatBoost(WeaponAttributeType.ARMOR_PENETRATION, .02F)),
	VITALITY(ArmorAttributeType.VITALITY, EnumData.VITALITY, new String[] {"Adds health, hp regen,", "elemental resistance and", "sword damage."}, new StatBoost(ArmorAttributeType.HEALTH_POINTS, .034f), new StatBoost(ArmorAttributeType.HEALTH_REGEN, .03f)),
	INTELLECT(ArmorAttributeType.INTELLECT, EnumData.INTELLECT, new String[] {"Adds energy regeneration, critical,",  "hit chance, and staff damage."}, new StatBoost(ArmorAttributeType.ENERGY_REGEN, .015f), new StatBoost(WeaponAttributeType.CRITICAL_HIT, .025f));
	
	@Getter
	private ArmorAttributeType type;
	@Getter
	private String[] description;
	@Getter
	private EnumData data;
	@Getter
	private StatBoost[] statBoosts;
	
	Stats(ArmorAttributeType type, EnumData data, String[] lore, StatBoost... boosts) {
		this.statBoosts = boosts;
		this.data = data;
		this.description = lore;
		this.type = type;
	}
}
