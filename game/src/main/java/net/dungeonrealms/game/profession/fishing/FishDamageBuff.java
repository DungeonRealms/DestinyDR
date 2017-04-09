package net.dungeonrealms.game.profession.fishing;

import org.bukkit.entity.Player;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.profession.Fishing.FishBuffType;
import net.dungeonrealms.game.world.item.Item.WeaponAttributeType;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

public class FishDamageBuff extends FishBuff {

	public FishDamageBuff(NBTTagCompound tag) {
		super(tag, FishBuffType.DAMAGE);
	}
	
	public FishDamageBuff(FishingTier tier) {
		super(tier, FishBuffType.DAMAGE);
	}

	@Override
	public void applyBuff(Player player) {
		applyStatTemporarily(player, WeaponAttributeType.DAMAGE);
	}

	@Override
	protected int[] getChances() {
		return new int[] {15, 10, 10, 10, 10};
	}

	@Override
	protected String[] getNamePrefixes() {
		return new String[] {"Lesser", "Normal", "Greater", "Ancient", "Legendary"};
	}

	@Override
	protected int[] getDurations() {
		return new int[] {20, 25, 30, Utils.randInt(40, 50), Utils.randInt(50, 60)};
	}

	@Override //TODO: Make this better
	protected void generateVal() {
		int t = getTier().getTier();
		if (t == 1) {
			setValue(Utils.randInt(1, 2));
		} else if (t == 2) {
			setValue(Utils.randInt(1, 4));
		} else if (t == 3) {
			setValue(Utils.randInt(3, 6));
		} else if (t == 4) {
			setValue(Utils.randInt(5, 11));
		} else if (t == 5) {
			setValue(Utils.randInt(11, 16));
		}
	}
}
