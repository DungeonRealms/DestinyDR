package net.dungeonrealms.game.listeners;

import net.dungeonrealms.API;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.handlers.EnergyHandler;
import net.dungeonrealms.game.handlers.ProtectionHandler;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.party.Affair;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * Created by Alan Lu (dartaran) on 03-Jul-16.
 */
public class PlayerDamageRestrictionListener implements Listener {

    /**
     * Checks if a player can be damaged and if they can damage.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAttemptAttackEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity receiver = event.getEntity();
        boolean isAttackerPlayer = false;
        boolean isDefenderPlayer = false;
        Player pDamager = null;
        Player pReceiver = null;

        if (API.isPlayer(damager)) {
            isAttackerPlayer = true;
            pDamager = (Player) event.getDamager();
        }
        else if ((damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player && (DamageAPI.isStaffProjectile(damager) ||
                DamageAPI.isBowProjectile(damager)))) {
            isAttackerPlayer = true;
            pDamager = (Player) ((Projectile) damager).getShooter();
        }

        if (API.isPlayer(receiver)) {
            isDefenderPlayer = true;
            pReceiver = (Player) event.getEntity();
        }
        if (!isAttackerPlayer && !isDefenderPlayer) {
            return;
        }

        if (isAttackerPlayer && !isDefenderPlayer || (isDefenderPlayer && !isAttackerPlayer)) {
            if (API.isInSafeRegion(damager.getLocation()) || API.isInSafeRegion(receiver.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }

        if (isAttackerPlayer) {
            if (pDamager.hasPotionEffect(PotionEffectType.SLOW_DIGGING) || EnergyHandler.getPlayerCurrentEnergy(pDamager) <= 0) {
                event.setCancelled(true);
                event.setDamage(0);
                pDamager.playSound(pDamager.getLocation(), Sound.ENTITY_WOLF_PANT, 12F, 1.5F);
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CRIT, event.getEntity().getLocation().add(0, 1, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.75F, 40);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return;
            }
        }

        if (isDefenderPlayer) {
            if (API.getGamePlayer(pReceiver).isInvulnerable() || API.getGamePlayer(pReceiver).isTargettable()) {
                event.setCancelled(true);
                event.setDamage(0);
                return;
            }
        }

        if (isAttackerPlayer && isDefenderPlayer) {

            if (API.isNonPvPRegion(pDamager.getLocation()) || API.isNonPvPRegion(pReceiver.getLocation())) {
                if (DuelingMechanics.isDueling(pDamager.getUniqueId())) { //TODO: Check if you can attack players that are dueling.
                    if (DuelingMechanics.isDueling(pReceiver.getUniqueId())) {
                        if (!DuelingMechanics.isDuelPartner(pDamager.getUniqueId(), pReceiver.getUniqueId())) {
                            event.setDamage(0);
                            event.setCancelled(true);
                            pDamager.updateInventory();
                            pReceiver.updateInventory();
                        }
                    }
                } else {
                    event.setDamage(0);
                    event.setCancelled(true);
                    pDamager.updateInventory();
                    pReceiver.updateInventory();
                }
                return;
            }

            if (!Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_PVP, pDamager.getUniqueId()).toString())) {
                if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, pDamager.getUniqueId()).toString())) {
                    pDamager.sendMessage(org.bukkit.ChatColor.YELLOW + "You have toggle PvP disabled. You currently cannot attack players.");
                }
                event.setCancelled(true);
                event.setDamage(0);
                pDamager.updateInventory();
                pReceiver.updateInventory();
                return;
            }

            if (ProtectionHandler.getInstance().hasNewbieProtection(pReceiver)) {
                pDamager.sendMessage(ChatColor.RED + "The player you are attempting to attack has newbie protection! You cannot attack them.");
                event.getEntity().sendMessage(ChatColor.GREEN + "Your " + ChatColor.UNDERLINE + "NEWBIE " + "PROTECTION" + ChatColor.GREEN + " has prevented " + pDamager.getName() +
                        ChatColor.GREEN + " from attacking you!");
                event.getEntity();
                event.setCancelled(true);
                event.setDamage(0);
                pDamager.updateInventory();
                pReceiver.updateInventory();
                return;
            }

            if (Affair.getInstance().areInSameParty(pDamager, pReceiver)) {
                event.setCancelled(true);
                event.setDamage(0);
                pDamager.updateInventory();
                pReceiver.updateInventory();
                return;
            }

            if (!GuildDatabaseAPI.get().isGuildNull(pDamager.getUniqueId()) && !GuildDatabaseAPI.get().isGuildNull(pReceiver.getUniqueId())) {
                if (GuildDatabaseAPI.get().getGuildOf(pDamager.getUniqueId()).equals(GuildDatabaseAPI.get().getGuildOf(pReceiver.getUniqueId()))) {
                    event.setCancelled(true);
                    event.setDamage(0);
                    pDamager.updateInventory();
                    pReceiver.updateInventory();
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void cancelAllVanillaDamageFailsafe(EntityDamageByEntityEvent event) {
        event.setDamage(0);
    }
}
