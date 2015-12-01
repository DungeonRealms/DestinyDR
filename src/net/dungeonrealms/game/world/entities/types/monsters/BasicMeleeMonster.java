package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRZombie;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 2, 2015
 */
public class BasicMeleeMonster extends DRZombie {

    /**
     * @param world
     * @param mobName
     * @param mobHead
     * @param tier
     */
	public EnumMonster monsterType;
	
    public BasicMeleeMonster(World world, EnumMonster type, int tier) {
        super(world, type, tier, EnumEntityType.HOSTILE_MOB, true);
    }

    public BasicMeleeMonster(World world) {
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
