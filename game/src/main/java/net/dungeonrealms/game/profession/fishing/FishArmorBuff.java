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
		return new int[] {20, 25, 30, Utils.randInt(40, 49), Utils.randInt(50, 60)};
	}
	
	@Override
	public String getNamePrefix() {
		return getTier() == FishingTier.TIER_5 ? "Hardened " : "";
	}

	@Override
	protected void generateVal() {
		int t = getTier().getTier();
		int min = 1;
		int max = 0;
		if (t == 1) {
			max = 2;
		} else if (t == 2) {
			max = 3;
		} else if (t == 3) {
			min = 3;
			max = 6;
		} else if (t == 4) {
			min = 5;
			max = 9;
		} else if (t == 5) {
			min = 5;
			max = 11;
		}
		setValue(Utils.randInt(min, max));
	}
}
