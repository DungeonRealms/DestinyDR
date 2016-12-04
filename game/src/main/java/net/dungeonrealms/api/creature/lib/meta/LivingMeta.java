package net.dungeonrealms.api.creature.lib.meta;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import net.dungeonrealms.frontend.Game;
import net.dungeonrealms.frontend.vgame.player.GamePlayer;
import net.minecraft.server.v1_9_R2.DamageSource;
import net.minecraft.server.v1_9_R2.EntityInsentient;

import java.util.UUID;

/**
 * Created by Giovanni on 24-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class LivingMeta {

    private BiMap<UUID, Double> damage;

    private EntityInsentient insentient;

    public LivingMeta(EntityInsentient entityInsentient) {
        this.insentient = entityInsentient;
        this.damage = HashBiMap.create();
    }

    public LivingMeta resetDamage() {
        this.damage.clear();
        this.insentient.setHealth(this.insentient.getMaxHealth());
        return this;
    }

    public LivingMeta setHealth(float value) {
        if (value < this.insentient.getMaxHealth() && value > 0)
            this.insentient.setHealth(value);
        return this;
    }

    public LivingMeta damage(UUID damageSource, double value) {
        this.damage.put(damageSource, value);
        this.insentient.damageEntity(DamageSource.GENERIC, Float.parseFloat(String.valueOf(value)));
        return this;
    }

    public GamePlayer getLargestDamageSource() {
        UUID damageSource = damage.inverse().get(Lists.newArrayList(this.damage.values()).get(0));
        for (GamePlayer gamePlayer : Game.getGame().getRegistryRegistry().getPlayerRegistry().getOnlinePlayers().values()) {
            if (gamePlayer.getPlayer().getUniqueId().equals(damageSource)) {
                return gamePlayer;
            }
        }
        return null;
    }
}
