package net.dungeonrealms.game.profession.fishing;

import org.bukkit.entity.Player;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.profession.Fishing.FishBuffType;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

public class FishArmorBuff extends FishBuff {

	public FishArmorBuff(NBTTagCompound tag) {
		super(tag, FishBuffType.ARMOR);
	}
	
	public FishArmorBuff(FishingTier tier) {
		super(tier, FishBuffType.ARMOR);
	}
	
	@Override
	public void applyBuff(Player player) {
		applyStatTemporarily(player, ArmorAttributeType.ARMOR);
	}

	@Override
	protected int[] getChances() {
		return new int[] {10, 10, 10, 10, 10};
	}

	@Override
	protected String[] getNamePrefixes() {
		return new String[] {"Weak", "Minor", "Mighty", "Fortified", "Legendary"};
	}

	@Override
	protected int[] getDurations() {
		return new int[] {20, 25, 30, 45, 60};
	}
	
	@Override
	public String getNamePrefix() {
		return getTier() == FishingTier.TIER_5 ? "Hardened " : "";
	}

	@Override
	protected void generateVal() {
		int t = getTier().getTier();
		if (t == 1) {
			setValue(2);
		} else if (t == 2) {
			setValue(3);
		} else if (t == 3) {
			setValue(5);
		} else if (t == 4) {
			setValue(8);
		} else if (t == 5) {
			setValue(9);
		}
	}
}
