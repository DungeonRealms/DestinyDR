package net.dungeonrealms.game.profession.fishing;

import org.bukkit.entity.Player;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.profession.Fishing.FishBuffType;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

public class FishBlockBuff extends FishBuff {

	public FishBlockBuff(NBTTagCompound tag) {
		super(tag, FishBuffType.BLOCK);
	}
	
	public FishBlockBuff(FishingTier tier) {
		super(tier, FishBuffType.BLOCK);
	}
	
	@Override
	public void applyBuff(Player player) {
		applyStatTemporarily(player, ArmorAttributeType.BLOCK);
	}

	@Override
	protected int[] getChances() {
		return new int[] {5, 5, 5, 5, 5};
	}

	@Override
	protected String[] getNamePrefixes() {
		return new String[] {"Lesser", "Lesser", "Greater", "Greater", "Greater"};
	}

	@Override
	protected int[] getDurations() {
		return new int[] {25, 25, 30, Utils.randInt(40, 49), Utils.randInt(50, 60)};
	}

	@Override
	protected void generateVal() {
		if (getTier().getTier() <= 2) {
			setValue(Utils.randInt(1, 5));
		} else {
			setValue(Utils.randInt(4, 9));
		}
	}
}
