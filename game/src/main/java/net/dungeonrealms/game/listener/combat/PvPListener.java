package net.dungeonrealms.game.listener.combat;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.handler.EnergyHandler;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.repairing.RepairAPI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;

/**
 * Created by Kieran Quigley (Proxying) on 03-Jul-16.
 */
public class PvPListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void playerMeleePlayer(EntityDamageByEntityEvent event) {
        if (!GameAPI.isPlayer(event.getDamager())) return;
        if (!GameAPI.isPlayer(event.getEntity())) return;

        Player damager = (Player) event.getDamager();
        Player receiver = (Player) event.getEntity();

        if (receiver.getGameMode() != GameMode.SURVIVAL) return;

        event.setDamage(0);
        event.setCancelled(true);

        boolean isDuel = DuelingMechanics.isDuelPartner(damager.getUniqueId(), receiver.getUniqueId());
        if (!isDuel) {
            if (CombatLog.inPVP(damager)) {
                CombatLog.updatePVP(damager);
            } else {
                CombatLog.addToPVP(damager);
            }
        }
        EnergyHandler.removeEnergyFromPlayerAndUpdate(damager.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(damager.getEquipment().getItemInMainHand()), isDuel);
        receiver.playEffect(EntityEffect.HURT);

        //KNOCKBACK
//        org.bukkit.util.Vector unitVector = receiver.getLocation().toVector()
//                .subtract(damager.getLocation().toVector()).normalize();
//        receiver.setVelocity(unitVector.multiply(0.5F));

        DamageAPI.newKnockbackEntity(damager, receiver, 0.275);
        receiver.setSprinting(false);

        GamePlayer damagerGP = GameAPI.getGamePlayer(damager);
        GamePlayer receiverGP = GameAPI.getGamePlayer(receiver);

        if (damagerGP == null || receiverGP == null) return;

        //Dont tag them if they are in a duel..
        if (!isDuel)
            damagerGP.setPvpTaggedUntil(System.currentTimeMillis() + 1000 * 10L);
        else
            //So you dont regenerate in a duel if you take damage.
            receiver.setMetadata("lastDamageTaken", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));

        if (!GameAPI.isWeapon(damager.getEquipment().getItemInMainHand())) {

            //Take durability if not in a duel.
            if (!isDuel) {
                for (ItemStack i : receiver.getInventory().getArmorContents()) {
                    RepairAPI.subtractCustomDurability(receiver, i, 1);
                }
            }

            HealthHandler.getInstance().handlePlayerBeingDamaged(receiver, damager, 1, 0, 0, !isDuel);
            return;
        }
//        Item.ItemType weaponType = new Attribute(damager.getInventory().getItemInMainHand()).getItemType();
//        Item.ItemTier tier = new Attribute(damager.getInventory().getItemInMainHand()).getItemTier();

        //Dont change alignments based on duel contact at all.
        if (!isDuel)
            KarmaHandler.getInstance().handleAlignmentChanges(damager);

        event.setCancelled(true);
        damager.updateInventory();

        double calculatedDamage = DamageAPI.calculateWeaponDamage(damager, receiver, !isDuel);
        if (!isDuel && checkChaoticPrevention(event, damager, receiver, damagerGP, receiverGP, calculatedDamage))
            return;

        double[] armorCalculation = DamageAPI.calculateArmorReduction(damager, receiver, calculatedDamage, null, !isDuel);
        double armorReducedDamage = armorCalculation[0];
        double finalDamage = calculatedDamage;
        if (armorReducedDamage == -1) {
            damager.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT DODGED* (" + receiver.getName() + ChatColor.RED + ")");
            receiver.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "                        *DODGE* (" + ChatColor.RED + damager.getName() + ChatColor.GREEN + ")");
            //The defender dodged the attack
            DamageAPI.createDamageHologram(damager, receiver.getLocation(), ChatColor.RED + "*DODGE*");
            receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.5F, 2.0F);
            finalDamage = 0;
            return;
        } else if (armorReducedDamage == -2) {
            damager.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT BLOCKED* (" + receiver.getName() + ChatColor.RED + ")");
            receiver.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "                        *BLOCK* (" + ChatColor.RED + damager.getName() + ChatColor.DARK_GREEN + ")");
            DamageAPI.createDamageHologram(damager, receiver.getLocation(), ChatColor.RED + "*BLOCK*");
            receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2F, 1.0F);
            finalDamage = 0;
            return;
        } else if (armorReducedDamage == -3) {
            //Reflect when its fixed. @TODO
            damager.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT REFLECTED* (" + receiver.getName() + ChatColor.RED + ")");
            receiver.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "                        *REFLECT* (" + ChatColor.RED + damager.getName() + ChatColor.GOLD + ")");
            DamageAPI.createDamageHologram(damager, receiver.getLocation(), ChatColor.RED + "*REFLECT*");
            receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2F, 1.0F);
            HealthHandler.getInstance().handlePlayerBeingDamaged(damager, receiver, finalDamage, -5, 0);
            finalDamage = 0;
            return;
        }

        if (finalDamage > 0)
            DamageAPI.createDamageHologram(damager, receiver.getLocation(), finalDamage);
        HealthHandler.getInstance().handlePlayerBeingDamaged(receiver, damager, finalDamage, armorCalculation[0], armorCalculation[1], !isDuel);

        DamageAPI.handlePolearmAOE(event, calculatedDamage / 2, damager);
    }

    private boolean checkChaoticPrevention(EntityDamageByEntityEvent event, Player damager, Player receiver, GamePlayer damagerGP, GamePlayer receiverGP, double calculatedDamage) {
        if (receiverGP.getPlayerAlignment() == KarmaHandler.EnumPlayerAlignments.LAWFUL) {
            if (damagerGP.getPlayerAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, damager.getUniqueId()).toString())) {
                    if (calculatedDamage >= HealthHandler.getInstance().getPlayerHPLive(receiver)) {
                        receiver.setFireTicks(0);
                        for (PotionEffect potionEffect : receiver.getActivePotionEffects()) {
                            receiver.removePotionEffect(potionEffect.getType());
                        }
                        event.setCancelled(true);
                        event.setDamage(0);
                        damager.updateInventory();
                        receiver.updateInventory();
                        event.getDamager().sendMessage(ChatColor.YELLOW + "Your Chaotic Prevention Toggle has activated preventing the death of " + receiver.getName() + "!");
                        event.getEntity().sendMessage(ChatColor.YELLOW + damager.getName() + " has their Chaotic Prevention Toggle ON, your life has been spared!");

                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Used to handle staff damage.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void playerRangedPlayer(EntityDamageByEntityEvent event) {
        if (!DamageAPI.isBowProjectile(event.getDamager()) && !DamageAPI.isStaffProjectile(event.getDamager())) return;
        if (!GameAPI.isPlayer(event.getEntity())) return;
        Projectile projectile = (Projectile) event.getDamager();
        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }

        event.setDamage(0);
        event.setCancelled(true);

        Player damager = (Player) projectile.getShooter();
        Player receiver = (Player) event.getEntity();

        if (damager.equals(receiver))
            return; // sometimes the projectile can be knocked back to the player at close range

        if (receiver.getGameMode() != GameMode.SURVIVAL) return;
        boolean isDuel = DuelingMechanics.isDuelPartner(damager.getUniqueId(), receiver.getUniqueId());
        if (!isDuel) {
            if (CombatLog.inPVP(damager)) {
                CombatLog.updatePVP(damager);
            } else {
                CombatLog.addToPVP(damager);
            }
        }

        receiver.playEffect(EntityEffect.HURT);
        DamageAPI.knockbackEntity(damager, receiver, 0.3);
        receiver.setSprinting(false);

        double calculatedDamage = DamageAPI.calculateProjectileDamage(damager, receiver, projectile);
        GamePlayer damagerGP = GameAPI.getGamePlayer(damager);
        GamePlayer receiverGP = GameAPI.getGamePlayer(receiver);
        if (receiverGP != null && damagerGP != null) {
            if (!isDuel)
                damagerGP.setPvpTaggedUntil(System.currentTimeMillis() + 1000 * 10L);

            if (!isDuel && checkChaoticPrevention(event, damager, receiver, damagerGP, receiverGP, calculatedDamage))
                return;
        }
        double[] armorCalculation = DamageAPI.calculateArmorReduction(damager, receiver, calculatedDamage, null, !isDuel);
        double finalDamage = calculatedDamage;
        double armorReducedDamage = armorCalculation[0];
        String defenderName = receiver.getName();
        String attackerName = damager.getName();
        if (armorReducedDamage == -1) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                damager.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT DODGED* (" + defenderName + ChatColor.RED + ")");
                receiver.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "                        *DODGE* (" + ChatColor.RED + attackerName + ChatColor.GREEN + ")");
                //The defender dodged the attack
                DamageAPI.createDamageHologram(damager, receiver.getLocation(), ChatColor.RED + "*DODGE*");
                receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.5F, 2.0F);
            }, 1L);
            finalDamage = 0;
            return;
        } else if (armorReducedDamage == -2) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                damager.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT BLOCKED* (" + defenderName + ChatColor.RED + ")");
                receiver.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "                        *BLOCK* (" + ChatColor.RED + attackerName + ChatColor.DARK_GREEN + ")");
                DamageAPI.createDamageHologram(damager, receiver.getLocation(), ChatColor.RED + "*BLOCK*");
                receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2F, 1.0F);
            }, 1L);
            finalDamage = 0;
            return;
        } else if (armorReducedDamage == -3) {
            //Reflect when its fixed. @TODO
            double damage = finalDamage;
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                damager.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT REFLECTED* (" + receiver.getName() + ChatColor.RED + ")");
                receiver.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "                        *REFLECT* (" + ChatColor.RED + damager.getName() + ChatColor.DARK_GREEN + ")");
                DamageAPI.createDamageHologram(damager, receiver.getLocation(), ChatColor.RED + "*REFLECT*");
                receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2F, 1.0F);
                //Reflect in pvp.
                HealthHandler.getInstance().handlePlayerBeingDamaged(damager, receiver, damage, -5, 0);
            }, 1L);
//            finalDamage = 0;
            return;
        } else {
            DamageAPI.createDamageHologram(damager, receiver.getLocation(), finalDamage);
        }
        HealthHandler.getInstance().handlePlayerBeingDamaged(receiver, damager, finalDamage, armorCalculation[0], armorCalculation[1], !isDuel);
    }
}
