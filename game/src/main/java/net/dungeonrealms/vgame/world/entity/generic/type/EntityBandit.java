package net.dungeonrealms.vgame.world.entity.generic.type;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.api.creature.lib.craft.CreatureCow;
import net.dungeonrealms.api.creature.lib.craft.CreaturePig;
import net.dungeonrealms.api.creature.lib.craft.CreatureZombie;
import net.dungeonrealms.api.creature.lib.intelligence.EnumIntelligenceType;
import net.dungeonrealms.vgame.world.entity.generic.IGameEntity;
import net.dungeonrealms.vgame.world.entity.generic.construct.EnumEntityTier;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.World;

import java.util.Arrays;

/**
 * Created by Giovanni on 25-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class EntityBandit extends CreatureZombie implements IGameEntity
{
    @Getter
    @Setter
    private EnumEntityTier entityTier;

    public EntityBandit(World world)
    {
        super(world, EnumIntelligenceType.HOSTILE);

        this.addTargets(Arrays.asList(EntityHuman.class, CreatureCow.class, CreaturePig.class));

    }
}
