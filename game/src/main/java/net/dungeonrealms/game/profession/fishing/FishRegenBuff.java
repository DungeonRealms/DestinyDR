package net.dungeonrealms.game.profession.fishing;

import org.bukkit.entity.Player;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.profession.Fishing.FishBuffType;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

public class FishRegenBuff extends FishBuff {

	public FishRegenBuff(NBTTagCompound tag) {
		super(tag, FishBuffType.REGEN);
	}
	
	public FishRegenBuff(FishingTier tier) {
		super(tier, FishBuffType.REGEN);
	}

	@Override
	public void applyBuff(Player player) {
		applyStatTemporarily(player, ArmorAttributeType.HEALTH_REGEN);
	}

	@Override
	protected int[] getChances() {
		return new int[] {0, 10, 5, 5, 5};
	}

	@Override
	protected String[] getNamePrefixes() {
		return new String[] {"Lesser", "Normal", "Mighty", "Enhanced", "Extreme"};
	}

	@Override
	protected int[] getDurations() {
		return new int[] {0, 10, 10, 10, 10};
	}

	@Override
	protected void generateVal() {
		int min = getTier().getTier() >= 4 ? 10 : 5;
		int max = getTier().getTier() >= 4 ? 16 : (min + 4 * (getTier().getTier() - 1));
		setValue(Utils.randInt(min, max));
	}
}
