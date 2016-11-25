package net.dungeonrealms.vgame.world.entity.generic;

import net.dungeonrealms.api.creature.lib.craft.CreatureCow;
import net.dungeonrealms.api.creature.lib.craft.CreatureZombie;
import net.dungeonrealms.api.creature.lib.intelligence.EnumIntelligenceType;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Giovanni on 25-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class EntityBandit extends CreatureZombie
{
    public EntityBandit(World world)
    {
        super(world, EnumIntelligenceType.HOSTILE);

        addTarget(EntityHuman.class);
        addTarget(CreatureCow.class);
    }
}
