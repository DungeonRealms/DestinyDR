package net.dungeonrealms.game.profession.fishing;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.profession.Fishing.FishBuffType;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

public class FishSpeedBuff extends FishBuff {

	public FishSpeedBuff(NBTTagCompound tag) {
		super(tag, FishBuffType.SPEED);
	}
	
	public FishSpeedBuff(FishingTier tier) {
		super(tier, FishBuffType.SPEED);
	}

	@Override
	public void applyBuff(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, getDuration() * 20, getTier().getTier() >= 4 ? 2 : 1));
	}

	@Override
	protected int[] getChances() {
		return new int[] {25, 25, 25, 25, 15};
	}

	@Override
	protected String[] getNamePrefixes() {
		return new String[] {"Light", "Normal", "Lasting", "Bursting", "Amplifying"};
	}

	@Override
	protected int[] getDurations() {
		return new int[] {15, 20, 30, 15, 30};
	}

	@Override
	protected void generateVal() {
		
	}
}
