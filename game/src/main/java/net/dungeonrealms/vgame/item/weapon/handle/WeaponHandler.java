package net.dungeonrealms.vgame.item.weapon.handle;

import net.dungeonrealms.api.creature.EnumCreatureType;
import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.item.EnumItemType;
import net.dungeonrealms.vgame.item.weapon.WeaponItem;
import net.dungeonrealms.vgame.item.weapon.attribute.EnumWeaponAttribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class WeaponHandler implements SuperHandler.ListeningHandler
{
    @Override
    public void prepare()
    {
        Game.getGame().getServer().getPluginManager().registerEvents(this, Game.getGame());
    }

    // TODO
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event)
    {
        if (event.getDamager() instanceof Player)
        {
            Player player = (Player) event.getDamager();
            if (player.getItemInHand() != null)
                if (Game.getGame().getRegistryHandler().getWeaponRegistry().getMap().containsKey(player.getItemInHand())) // Check if it's a weapon.
                {
                    WeaponItem weaponItem = Game.getGame().getRegistryHandler().getWeaponRegistry().getMap().get(player.getItemInHand());
                    if (!(weaponItem.getType() == EnumItemType.BOW))
                    {
                        double actualDamage = 0;
                        // Calculate the weapon damage based upon the damaged creature
                        actualDamage += weaponItem.calculateDamage(event.getEntity() instanceof Player ? EnumCreatureType.PLAYER : EnumCreatureType.ENTITY);

                        event.setDamage(Math.round(actualDamage));
                    } else
                    {
                        event.setDamage(0); // Don't damage an entity if they just punch with a bow
                    }
                } else
                {
                    return;
                }
        }
    }
}
