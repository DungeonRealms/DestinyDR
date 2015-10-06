package net.dungeonrealms.entities.types.monsters;

import net.dungeonrealms.entities.types.RangedEntityBlaze;
import net.dungeonrealms.enums.EnumEntityType;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 4, 2015
 */
public class BasicEntityBlaze extends RangedEntityBlaze {

	/**
	 * @param world
	 * @param mobName
	 * @param mobHead
	 * @param tier
	 */
	public BasicEntityBlaze(World world, String mobName, String mobHead, int tier) {
		super(world, mobName, mobHead, tier, EnumEntityType.HOSTILE_MOB, true);
	}

	public BasicEntityBlaze(World world) {
		super(world);
	}

	@Override
	protected Item getLoot() {
		return null;
	}

	@Override
	protected void getRareDrop() {

	}

	@Override
	protected void setStats() {

	}

	@Override
	public String getPrefix() {
		return null;
	}

	@Override
	public String getSuffix() {
		return null;
	}

}
