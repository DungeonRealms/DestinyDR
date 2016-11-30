package net.dungeonrealms.api.creature;

import net.dungeonrealms.api.creature.lib.EnumEntityType;
import net.dungeonrealms.api.creature.lib.intelligence.EnumIntelligenceType;
import net.dungeonrealms.api.creature.lib.meta.LivingMeta;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.PathfinderGoalNearestAttackableTarget;

import java.util.List;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface ICreature extends IEntity {
    EnumEntityType getEntityType();

    EnumIntelligenceType getIntelligenceType();

    LivingMeta getLivingMeta();

    default void setMovementSpeed(double value) {
        this.getEntityInsentient().getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(value / 10);
    }

    default void setFollowingRange(double value) {
        this.getEntityInsentient().getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(value);
    }

    default void setMaxHealth(double value) {
        this.getEntityInsentient().getAttributeInstance(GenericAttributes.maxHealth).setValue(value);
    }

    default void addTargets(List<Class> classList) {
        for (Class clazz : classList)
            this.getEntityInsentient().goalSelector.a(1, new PathfinderGoalNearestAttackableTarget(this.getEntityCreature(), clazz, true));
    }

    default void setWeapon() {

    }

    default void setArmorContents() {

    }
}
