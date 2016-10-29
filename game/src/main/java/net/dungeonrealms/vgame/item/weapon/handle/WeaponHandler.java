package net.dungeonrealms.vgame.item.weapon.handle;

import net.dungeonrealms.awt.SuperHandler;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.item.weapon.WeaponItem;
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
            {
                if (Game.getGame().getRegistryHandler().getWeaponRegistry().getMap().containsKey(player.getItemInHand()))
                {
                    double actualDamage = 0;
                    WeaponItem weaponItem = Game.getGame().getRegistryHandler().getWeaponRegistry().getMap().get(player.getItemInHand());
                    // Add default weapon damage, a random double between min and max
                    actualDamage += ThreadLocalRandom.current().nextDouble(weaponItem.getMinDmg(), weaponItem.getMaxDmg());

                    event.setDamage(Math.round(actualDamage));
                } else
                {
                    return;
                }
            }
        }
    }
}
