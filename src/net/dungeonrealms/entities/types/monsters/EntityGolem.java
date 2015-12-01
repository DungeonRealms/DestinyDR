package net.dungeonrealms.entities.types.monsters;

import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.base.DRZombie;
import net.minecraft.server.v1_8_R3.World;

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
    protected void getRareDrop() {

    }

    @Override
    protected void setStats() {

    }

}
