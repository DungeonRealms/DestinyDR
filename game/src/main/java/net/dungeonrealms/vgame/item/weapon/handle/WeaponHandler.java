package net.dungeonrealms.vgame.item.weapon.handle;

import net.dungeonrealms.api.creature.EnumCreatureType;
import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.old.game.handler.EnergyHandler;
import net.dungeonrealms.old.game.party.Party;
import net.dungeonrealms.old.game.party.PartyMechanics;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.item.EnumItemType;
import net.dungeonrealms.vgame.item.weapon.WeaponItem;
import net.dungeonrealms.vgame.item.weapon.attribute.EnumWeaponAttribute;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

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

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            if (event.getDamager() instanceof Player)
            {
                if (PartyMechanics.getInstance().areInSameParty((Player) event.getDamager(), (Player) event.getEntity())) // In a party?
                {
                    event.setCancelled(true);
                    event.setDamage(0);
                }
            }
        } else
        {
            if (event.getDamager() instanceof Player)
            {
                Player player = (Player) event.getDamager();
                if (!player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)
                        || EnergyHandler.getPlayerCurrentEnergy(player) <= 0) // Do they have the energy?
                {
                    if (player.getItemInHand() != null)
                        if (Game.getGame().getRegistryHandler().getWeaponRegistry().getMap().containsKey(player.getItemInHand())) // Check if it's a weapon.
                        {
                            WeaponItem weaponItem = Game.getGame().getRegistryHandler().getWeaponRegistry().getMap().get(player.getItemInHand());
                            if (weaponItem.getItemType() != EnumItemType.BOW || weaponItem.getItemType() != EnumItemType.STAFF)
                            {
                                // Calculate the weapon damage based upon the damaged creature
                                event.setDamage(Math.round(weaponItem.calculateDamage(event.getEntity() instanceof Player ? EnumCreatureType.PLAYER : EnumCreatureType.ENTITY)));
                                // Remove energy
                                EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(weaponItem.getItemStack()));
                            } else
                            {
                                event.setDamage(0); // Don't damage an entity if they just punch with a bow/staff
                            }
                        }
                } else
                {
                    // Well that sucks m8
                    event.setCancelled(true);
                    event.setDamage(0);
                    player.playSound(player.getLocation(), Sound.ENTITY_WOLF_PANT, 12f, 1f);
                    player.getWorld().spigot().playEffect(player.getLocation(), Effect.CRIT, 0, 0, 0.5f, 0.5f, 0.5f, 0.1f, 5, 1);
                }
            }
        }
    }
}
