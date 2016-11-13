package test;

import net.dungeonrealms.api.creature.EnumCreatureType;
import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.item.EnumItemType;
import net.dungeonrealms.vgame.item.weapon.WeaponItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Created by Giovanni on 9-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GameTest implements SuperHandler.ListeningHandler
{
    @Override
    public void prepare()
    {
        Game.getGame().getServer().getPluginManager().registerEvents(this, Game.getGame());
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event)
    {
        if (event.getDamager() instanceof Player)
        {
            Player player = (Player) event.getDamager();
            {
                if (player.getItemInHand() != null)
                    if (Game.getGame().getRegistryHandler().getWeaponRegistry().getMap().containsKey(player.getItemInHand())) // Check if it's a weapon.
                    {
                        WeaponItem weaponItem = Game.getGame().getRegistryHandler().getWeaponRegistry().getMap().get(player.getItemInHand());
                        if (weaponItem.getItemType() != EnumItemType.BOW || weaponItem.getItemType() != EnumItemType.STAFF)
                        {
                            // Calculate the weapon damage based upon the damaged creature
                            event.setDamage(Math.round(weaponItem.calculateDamage(event.getEntity() instanceof Player ? EnumCreatureType.PLAYER : EnumCreatureType.ENTITY)));
                            player.sendMessage("" + event.getDamage());
                        } else
                        {
                            event.setDamage(0); // Don't damage an entity if they just punch with a bow/staff
                        }
                    }
            }
        }
    }
}
