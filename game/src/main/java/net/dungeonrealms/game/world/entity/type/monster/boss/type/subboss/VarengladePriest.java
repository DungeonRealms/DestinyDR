package net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss;

import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.MeleeSkeleton;
import net.minecraft.server.v1_9_R2.World;

/**
 * The Priest
 * 
 * Created April 29th, 2017.
 * @author Kneesnap
 */
public class VarengladePriest extends MeleeSkeleton implements DungeonBoss {

	public VarengladePriest(World world) {
		super(world);
	}

	@Override
	public BossType getBossType() {
		return BossType.BurickPriest;
	}

	@Override
	public String[] getItems() {
		return null;
	}
}
