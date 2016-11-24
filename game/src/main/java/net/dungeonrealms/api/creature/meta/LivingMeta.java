package net.dungeonrealms.api.creature.meta;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.api.creature.damage.IDamageSource;
import net.dungeonrealms.vgame.player.GamePlayer;
import net.minecraft.server.v1_9_R2.EntityInsentient;

/**
 * Created by Giovanni on 24-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class LivingMeta
{
    private BiMap<IDamageSource, Double> damage;

    @Getter
    private double attackDamage;

    private EntityInsentient insentient;

    public LivingMeta(EntityInsentient entityInsentient)
    {
        this.insentient = entityInsentient;
        this.damage = HashBiMap.create();
    }

    public LivingMeta resetDamage()
    {
        this.damage.clear();
        this.insentient.setHealth(this.insentient.getMaxHealth());
        return this;
    }

    public LivingMeta damage(IDamageSource damageSource, double value)
    {
        this.damage.put(damageSource, value);
        return this;
    }

    public GamePlayer getLargestDamageSource()
    {
        IDamageSource damageSource = damage.inverse().get(Lists.newArrayList(this.damage.values()).get(0));
        if (damageSource instanceof GamePlayer)
        {
            return (GamePlayer) damageSource;
        }
        return null;
    }
}
