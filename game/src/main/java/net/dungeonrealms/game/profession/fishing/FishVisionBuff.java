package net.dungeonrealms.game.profession.fishing;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.profession.Fishing.FishBuffType;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FishVisionBuff extends FishBuff {

	public FishVisionBuff(NBTTagCompound tag) {
		super(tag, FishBuffType.VISION);
	}
	
	public FishVisionBuff(FishingTier tier) {
		super(tier, FishBuffType.VISION);
	}
	
	@Override
	public void applyBuff(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, getDuration() * 20, 1));
	}

	@Override
	protected int[] getChances() {
		return new int[] {30, 25, 20, 20, 20};
	}

	@Override
	protected String[] getNamePrefixes() {
		return new String[] {"Extra", "Super", "Lasting", "Eagle", "Omniscient"};
	}

	@Override
	protected int[] getDurations() {
		return new int[] {30, 45, 60, Utils.randInt(40, 49), Utils.randInt(100, 110)};
	}
	
	@Override
	protected void generateVal() {}
}
