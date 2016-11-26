package net.dungeonrealms.api.creature.lib.craft;

import lombok.Getter;
import net.dungeonrealms.api.creature.ICreature;
import net.dungeonrealms.api.creature.lib.EnumEntityType;
import net.dungeonrealms.api.creature.lib.damage.EnumDamageSource;
import net.dungeonrealms.api.creature.lib.intelligence.EnumIntelligenceType;
import net.dungeonrealms.api.creature.lib.meta.LivingMeta;
import net.minecraft.server.v1_9_R2.*;

/**
 * Created by Giovanni on 26-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CreatureEndCrystal extends EntityEnderCrystal implements ICreature
{
    @Getter
    private EnumEntityType entityType = EnumEntityType.CRYSTAL;

    @Getter
    private Entity entity;

    @Getter
    private EntityCreature entityCreature;

    @Getter
    private EntityInsentient entityInsentient;

    @Getter
    private EnumIntelligenceType intelligenceType;

    @Getter
    private EnumDamageSource damageSource = EnumDamageSource.CREATURE;

    @Getter
    private LivingMeta livingMeta;

    public CreatureEndCrystal(World world, EnumIntelligenceType intelligenceType)
    {
        super(world);
        this.intelligenceType = intelligenceType;

        this.entity = this;
    }
}
