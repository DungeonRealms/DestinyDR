package net.dungeonrealms.game.mechanic.data;

import lombok.Getter;

/**
 * EnumUpgrade - Contains data about player attached upgradeables.
 *
 * Created April 4th, 2017.
 * @author Kneesnap
 */
@Getter
public enum EnumUpgrade {

	TIER_1(0),
	TIER_2(50),
	TIER_3(125),
	TIER_4(500),
	TIER_5(1500),
	TIER_6(3500),
	TIER_7(7500);
	
	private int bankCost;
	
	EnumUpgrade(int bank) {
		this.bankCost = bank;
	}
	
	public boolean hasBankUpgrade() {
		return getBankCost() > 0;
	}
	
	public int getId() {
		return ordinal();
	}
	
	public EnumUpgrade getNextUpgrade() {
		return getTier(getId() + 2);
	}
	
	public static EnumUpgrade getTier(int i) {
		return values()[i - 1];
	}
}
