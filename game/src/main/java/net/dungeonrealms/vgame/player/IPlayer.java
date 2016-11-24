package net.dungeonrealms.vgame.player;

import net.dungeonrealms.api.creature.damage.IDamageSource;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Giovanni on 8-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface IPlayer extends IDamageSource
{
    Player getPlayer();

    default void blind(int par1)
    {
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * par1, 4));
    }

    default void confuse(int par1)
    {
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * par1, 2));
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * par1, 2));
    }

    default void freeze(int par1)
    {
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * par1, 4));
    }
}
