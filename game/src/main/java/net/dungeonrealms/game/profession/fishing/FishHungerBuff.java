package net.dungeonrealms.game.profession.fishing;

import org.bukkit.entity.Player;

import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.profession.Fishing.FishBuffType;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

public class FishHungerBuff extends FishBuff {

	public FishHungerBuff(FishingTier tier) {
		super(tier, FishBuffType.HUNGER);
	}
	
	public FishHungerBuff(NBTTagCompound tag) {
		super(tag, FishBuffType.HUNGER);
	}

	@Override
	public void applyBuff(Player player) {
		int amount = (int) (getValue() / 5);
		
		player.setFoodLevel(Math.min(player.getFoodLevel() + amount, 20));
		player.setSaturation(Math.min(player.getSaturation() + amount, 20));
	}

	@Override
	protected int[] getChances() {
		return new int[] {10, 25, 10, 10, 5};
	}

	@Override
	protected String[] getNamePrefixes() {
		return new String[] {"Minor", "Normal", "Great", "Immense", "Supreme"};
	}

	@Override
	protected int[] getDurations() {
		return null;
	}

	@Override
	protected void generateVal() {
		setValue(15 + (5 * getTier().getTier()));
	}
}
