package net.dungeonrealms.game.world.entities.types.monsters.StaffMobs;

import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRBlaze;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class BasicEntityBlaze extends DRBlaze {

	/**
	 * @param world
	 * @param mons
	 * @param tier
	 */
	public BasicEntityBlaze(World world, EnumMonster mons, int tier) {
		super(world, mons, tier, EnumEntityType.HOSTILE_MOB, true);
	}

	public BasicEntityBlaze(World world) {
		super(world);
	}

	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}


	@Override
	protected void setStats() {

	}

}
