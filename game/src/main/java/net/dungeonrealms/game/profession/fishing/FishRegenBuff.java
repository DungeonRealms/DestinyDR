package net.dungeonrealms.game.profession.fishing;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.mechanic.dot.DotManager;
import net.dungeonrealms.game.mechanic.dot.impl.HealingDot;
import net.dungeonrealms.game.profession.Fishing.FishBuffType;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.entity.Player;

public class FishRegenBuff extends FishBuff {

	public FishRegenBuff(NBTTagCompound tag) {
		super(tag, FishBuffType.REGEN);
	}

	public FishRegenBuff(FishingTier tier) {
		super(tier, FishBuffType.REGEN);
	}

	@Override
	public void applyBuff(Player player) {
		int toUse = getValue();
		if(toUse > 30) toUse = 23;
		DotManager.addDamageOverTime(player, new HealingDot(player, player, toUse,getDurations()[getTier().getTier() - 1]), true);
		//applyStatTemporarily(player, ArmorAttributeType.HEALTH_REGEN);
	}

	@Override
	protected int[] getChances() {
		return new int[]{0, 10, 5, 5, 5};
	}

	@Override
	protected String[] getNamePrefixes() {
		return new String[]{"Lesser", "Normal", "Mighty", "Enhanced", "Extreme"};
	}

	@Override
	protected int[] getDurations() {
		return new int[]{1, 1, 1, 1, 1};
	}

	private int[] getStartingHeal() {
		return new int[]{10, 15, 17, 20, 23};
	}

	@Override
	protected void generateVal() {
		int min = getStartingHeal()[getTier().getTier() -1] -1;
		int max = getStartingHeal()[getTier().getTier() -1] + 1;
		int real = Utils.randInt(min,max);
		//int decrement = getDurations()[getTier().getTier() - 1];
		//int toReturn = 0;
		//for(int k = real; k > 0; k-=decrement) toReturn += k;
		setValue(real);
	}
}
