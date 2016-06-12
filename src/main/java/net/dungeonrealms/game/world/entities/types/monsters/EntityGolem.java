package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRZombie;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Chase on Sep 28, 2015
 */
public class EntityGolem extends DRZombie {

    public EntityGolem(World world, int tier, EnumEntityType entityType) {
        super(world, EnumMonster.Golem, tier, entityType, true);
    }

    /**
     * @param world
     */
    public EntityGolem(World world) {
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
