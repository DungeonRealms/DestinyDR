package net.dungeonrealms.entities.types.monsters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.enums.EnumMonster;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 3, 2015
 */
public class EntityWitherSkeleton extends MeleeEntityZombie {

    /**
     * @param world
     * @param mobName
     * @param mobHead
     * @param tier
     */
    public EntityWitherSkeleton(World world, EnumMonster mon, int tier) {
        super(world, mon, tier, EnumEntityType.HOSTILE_MOB, true);
        int level = Utils.getRandomFromTier(tier);
        MetadataUtils.registerEntityMetadata(this, entityType, tier, level);
        EntityStats.setMonsterStats(this, level, tier);
    }

    public EntityWitherSkeleton(World world) {
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
