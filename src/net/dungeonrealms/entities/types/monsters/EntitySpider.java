package net.dungeonrealms.entities.types.monsters;

import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.entities.EnumEntityType;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 2, 2015
 */
public class EntitySpider extends MeleeEntityZombie {

    /**
     * @param world
     * @param mobName
     * @param tier
     */
    public EntitySpider(World world, EnumMonster monst, int tier) {
        super(world, monst, tier, EnumEntityType.HOSTILE_MOB, true);
    }

    public EntitySpider(World world) {
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

}
