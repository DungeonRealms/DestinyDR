package net.dungeonrealms.game.profession.fishing;

import org.bukkit.entity.Player;

import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.profession.Fishing.FishBuffType;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

public class FishHealBuff extends FishBuff {

	public FishHealBuff(NBTTagCompound tag) {
		super(tag, FishBuffType.HEALTH);
	}
	
	public FishHealBuff(FishingTier tier) {
		super(tier, FishBuffType.HEALTH);
	}

	@Override
	public void applyBuff(Player player) {
		HealthHandler.heal(player, getValue() * HealthHandler.getMaxHP(player),true);
	}

	@Override
	protected int[] getChances() {
		return new int[] {10, 10, 10, 10, 10};
	}

	@Override
	protected String[] getNamePrefixes() {
		return new String[] {"Lesser", "Normal", "Mighty", "Enhanced", "Medicine"};
	}

	@Override
	protected int[] getDurations() {
		return null;
	}

	@Override
	protected void generateVal() {
		int t = getTier().getTier();
		if (t <= 2) {
			setValue(Utils.randInt(1, 1 + (2 * t)));
		} else if (t <= 4){
			setValue(Utils.randInt(4, 8));
		} else {
			setValue(Utils.randInt(6, 11));
		}
	}
}
