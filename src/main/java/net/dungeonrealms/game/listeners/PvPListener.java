package net.dungeonrealms.game.listeners;

import net.dungeonrealms.API;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.handlers.EnergyHandler;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.handlers.ProtectionHandler;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.world.items.Attribute;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.party.Affair;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * Created by Kieran Quigley (Proxying) on 03-Jul-16.
 */
public class PvPListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void playerMeleePlayer(EntityDamageByEntityEvent event) {
        if (!API.isPlayer(event.getDamager())) return;
        if (!API.isPlayer(event.getEntity())) return;

        Player damager = (Player) event.getDamager();
        Player receiver = (Player) event.getEntity();

        if (API.isNonPvPRegion(damager.getLocation()) || API.isNonPvPRegion(receiver.getLocation())) {
            event.setDamage(0);
            event.setCancelled(true);
            damager.updateInventory();
            receiver.updateInventory();
            return;
        }

        if (API.isNonPvPRegion(damager.getLocation()) || API.isNonPvPRegion(receiver.getLocation())) {
            if (DuelingMechanics.isDueling(damager.getUniqueId())) { //TODO: Check if you can attack players that are dueling.
                if (DuelingMechanics.isDueling(receiver.getUniqueId())) {
                    if (!DuelingMechanics.isDuelPartner(damager.getUniqueId(), receiver.getUniqueId())) {
                        event.setDamage(0);
                        event.setCancelled(true);
                        damager.updateInventory();
                        receiver.updateInventory();
                    }
                }
            } else {
                event.setDamage(0);
                event.setCancelled(true);
                damager.updateInventory();
                receiver.updateInventory();
            }
            return;
        }

        if (!Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_PVP, damager.getUniqueId()).toString())) {
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, damager.getUniqueId()).toString())) {
                damager.sendMessage(org.bukkit.ChatColor.YELLOW + "You have toggle PvP disabled. You currently cannot attack players.");
            }
            event.setCancelled(true);
            event.setDamage(0);
            damager.updateInventory();
            receiver.updateInventory();
            return;
        }

        if (ProtectionHandler.getInstance().hasNewbieProtection(receiver)) {
            damager.sendMessage(ChatColor.RED + "The player you are attempting to attack has newbie protection! You cannot attack them.");
            event.getEntity().sendMessage(ChatColor.GREEN + "Your " + ChatColor.UNDERLINE + "NEWBIE " + "PROTECTION" + ChatColor.GREEN + " has prevented " + damager.getName() +
                    ChatColor.GREEN + " from attacking you!");
            event.getEntity();
            event.setCancelled(true);
            event.setDamage(0);
            damager.updateInventory();
            receiver.updateInventory();
            return;
        }

        double finalDamage;

        if (Affair.getInstance().areInSameParty(damager, receiver)) {
            event.setCancelled(true);
            event.setDamage(0);
            damager.updateInventory();
            receiver.updateInventory();
            return;
        }

        if (!GuildDatabaseAPI.get().isGuildNull(damager.getUniqueId()) && !GuildDatabaseAPI.get().isGuildNull(receiver.getUniqueId())) {
            if (GuildDatabaseAPI.get().getGuildOf(damager.getUniqueId()).equals(GuildDatabaseAPI.get().getGuildOf(receiver.getUniqueId()))) {
                event.setCancelled(true);
                event.setDamage(0);
                damager.updateInventory();
                receiver.updateInventory();
                return;
            }
        }

        if (damager.hasPotionEffect(PotionEffectType.SLOW_DIGGING) || EnergyHandler.getPlayerCurrentEnergy(damager) <= 0) {
            event.setCancelled(true);
            event.setDamage(0);
            damager.playSound(damager.getLocation(), Sound.ENTITY_WOLF_PANT, 12F, 1.5F);
            try {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CRIT, event.getEntity().getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.75F, 40);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            damager.updateInventory();
            receiver.updateInventory();
            return;
        }

        if (CombatLog.isInCombat(damager)) {
            CombatLog.updateCombat(damager);
        } else {
            CombatLog.addToCombat(damager);
        }

        EnergyHandler.removeEnergyFromPlayerAndUpdate(damager.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(damager.getEquipment().getItemInMainHand()));

        if (!API.isWeapon(damager.getEquipment().getItemInMainHand())) {
            event.setDamage(1);
            return;
        }

        Item.ItemType weaponType = new Attribute(damager.getInventory().getItemInMainHand()).getItemType();
        Item.ItemTier tier = new Attribute(damager.getInventory().getItemInMainHand()).getItemTier();

        switch (weaponType) {
            case BOW:
                switch (tier) {
                    case TIER_1:
                        DamageAPI.knockbackEntity(damager, receiver, 1.2);
                        break;
                    case TIER_2:
                        DamageAPI.knockbackEntity(damager, receiver, 1.5);
                        break;
                    case TIER_3:
                        DamageAPI.knockbackEntity(damager, receiver, 1.8);
                        break;
                    case TIER_4:
                        DamageAPI.knockbackEntity(damager, receiver, 2.0);
                        break;
                    case TIER_5:
                        DamageAPI.knockbackEntity(damager, receiver, 2.2);
                        break;
                    default:
                        break;
                }
            case STAFF:
                event.setDamage(0);
                event.setCancelled(true);
                damager.updateInventory();
                receiver.updateInventory();
                return;
            default:
                break;
        }

        finalDamage = DamageAPI.calculateWeaponDamage(damager, receiver);

        if (API.getGamePlayer(receiver) != null && API.getGamePlayer(damager) != null) {
            if (API.getGamePlayer(receiver).getPlayerAlignment() == KarmaHandler.EnumPlayerAlignments.LAWFUL) {
                if (API.getGamePlayer(damager).getPlayerAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                    if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, damager.getUniqueId()).toString())) {
                        if (finalDamage >= HealthHandler.getInstance().getPlayerHPLive(receiver)) {
                            event.setCancelled(true);
                            event.setDamage(0);
                            damager.updateInventory();
                            receiver.updateInventory();
                            event.getDamager().sendMessage(ChatColor.YELLOW + "Your Chaotic Prevention Toggle has activated preventing the death of " + receiver.getName() + "!");
                            event.getEntity().sendMessage(ChatColor.YELLOW + damager.getName() + " has their Chaotic Prevention Toggle ON, your life has been spared!");
                            return;
                        }
                    }
                }
            }
        }

        event.setDamage(finalDamage);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void playerRangedPlayer(EntityDamageByEntityEvent event) {
        if (!DamageAPI.isBowProjectile(event.getDamager()) && !DamageAPI.isStaffProjectile(event.getDamager())) return;
        if (!API.isPlayer(event.getEntity())) return;
        Projectile projectile = (Projectile) event.getDamager();
        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }
        Player damager = (Player) projectile.getShooter();
        Player receiver = (Player) event.getEntity();

        if (API.isNonPvPRegion(damager.getLocation()) || API.isNonPvPRegion(receiver.getLocation())) {
            event.setDamage(0);
            event.setCancelled(true);
            damager.updateInventory();
            receiver.updateInventory();
            return;
        }

        if (API.isNonPvPRegion(damager.getLocation()) || API.isNonPvPRegion(receiver.getLocation())) {
            if (DuelingMechanics.isDueling(damager.getUniqueId())) { //TODO: Check if you can attack players that are dueling.
                if (DuelingMechanics.isDueling(receiver.getUniqueId())) {
                    if (!DuelingMechanics.isDuelPartner(damager.getUniqueId(), receiver.getUniqueId())) {
                        event.setDamage(0);
                        event.setCancelled(true);
                        damager.updateInventory();
                        receiver.updateInventory();
                        return;
                    }
                }
            } else {
                event.setDamage(0);
                event.setCancelled(true);
                damager.updateInventory();
                receiver.updateInventory();
                return;
            }
        }

        if (ProtectionHandler.getInstance().hasNewbieProtection(receiver)) {
            damager.sendMessage(ChatColor.RED + "The player you are attempting to attack has newbie protection! You cannot attack them.");
            event.getEntity().sendMessage(ChatColor.GREEN + "Your " + ChatColor.UNDERLINE + "NEWBIE " + "PROTECTION" + ChatColor.GREEN + " has prevented " + damager.getName() +
                    ChatColor.GREEN + " from attacking you!");
            event.getEntity();
            event.setCancelled(true);
            event.setDamage(0);
            damager.updateInventory();
            receiver.updateInventory();
            return;
        }

        if (!Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_PVP, damager.getUniqueId()).toString())) {
            if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, damager.getUniqueId()).toString())) {
                damager.sendMessage(org.bukkit.ChatColor.YELLOW + "You have toggle PvP disabled. You currently cannot attack players.");
            }
            event.setCancelled(true);
            event.setDamage(0);
            damager.updateInventory();
            receiver.updateInventory();
            return;
        }

        double finalDamage;

        if (Affair.getInstance().areInSameParty(damager, receiver)) {
            event.setCancelled(true);
            event.setDamage(0);
            damager.updateInventory();
            receiver.updateInventory();
            return;
        }

        if (!GuildDatabaseAPI.get().isGuildNull(damager.getUniqueId()) && !GuildDatabaseAPI.get().isGuildNull(receiver.getUniqueId())) {
            if (GuildDatabaseAPI.get().getGuildOf(damager.getUniqueId()).equals(GuildDatabaseAPI.get().getGuildOf(receiver.getUniqueId()))) {
                event.setCancelled(true);
                event.setDamage(0);
                damager.updateInventory();
                receiver.updateInventory();
                return;
            }
        }

        if (CombatLog.isInCombat(damager)) {
            CombatLog.updateCombat(damager);
        } else {
            CombatLog.addToCombat(damager);
        }

        finalDamage = DamageAPI.calculateProjectileDamage(damager, receiver, projectile);

        if (API.getGamePlayer(receiver) != null && API.getGamePlayer(damager) != null) {
            if (API.getGamePlayer(receiver).getPlayerAlignment() == KarmaHandler.EnumPlayerAlignments.LAWFUL) {
                if (API.getGamePlayer(damager).getPlayerAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                    if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, damager.getUniqueId()).toString())) {
                        if (finalDamage >= HealthHandler.getInstance().getPlayerHPLive(receiver)) {
                            event.setCancelled(true);
                            event.setDamage(0);
                            damager.updateInventory();
                            receiver.updateInventory();
                            event.getDamager().sendMessage(ChatColor.YELLOW + "Your Chaotic Prevention Toggle has activated preventing the death of " + receiver.getName() + "!");
                            event.getEntity().sendMessage(ChatColor.YELLOW + damager.getName() + " has their Chaotic Prevention Toggle ON, your life has been spared!");
                            return;
                        }
                    }
                }
            }
        }

        event.setDamage(finalDamage);
    }
}
