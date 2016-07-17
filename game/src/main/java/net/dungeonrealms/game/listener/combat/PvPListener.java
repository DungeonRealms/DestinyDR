package net.dungeonrealms.game.listener.combat;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.handlers.EnergyHandler;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.items.Attribute;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

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

        if (CombatLog.isInCombat(damager)) {
            CombatLog.updateCombat(damager);
        } else {
            CombatLog.addToCombat(damager);
        }

        EnergyHandler.removeEnergyFromPlayerAndUpdate(damager.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(damager.getEquipment().getItemInMainHand()));

        if (!GameAPI.isWeapon(damager.getEquipment().getItemInMainHand())) {
            for (ItemStack i : receiver.getInventory().getArmorContents()) {
                RepairAPI.subtractCustomDurability(receiver, i, 1);
            }
            HealthHandler.getInstance().handlePlayerBeingDamaged(receiver, damager, 1, 0, 0);
            return;
        }

        Item.ItemType weaponType = new Attribute(damager.getInventory().getItemInMainHand()).getItemType();
        Item.ItemTier tier = new Attribute(damager.getInventory().getItemInMainHand()).getItemTier();

        switch (weaponType) {
            case BOW:
                switch (tier) {
                    case TIER_1:
                        DamageAPI.knockbackEntity(damager, receiver, 1.2);
                        event.setCancelled(true);
                        damager.updateInventory();
                        return;
                    case TIER_2:
                        DamageAPI.knockbackEntity(damager, receiver, 1.5);
                        event.setCancelled(true);
                        damager.updateInventory();
                        return;
                    case TIER_3:
                        DamageAPI.knockbackEntity(damager, receiver, 1.8);
                        event.setCancelled(true);
                        damager.updateInventory();
                        return;
                    case TIER_4:
                        DamageAPI.knockbackEntity(damager, receiver, 2.0);
                        event.setCancelled(true);
                        damager.updateInventory();
                        return;
                    case TIER_5:
                        DamageAPI.knockbackEntity(damager, receiver, 2.2);
                        event.setCancelled(true);
                        damager.updateInventory();
                        return;
                    default:
                        return;
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


        double calculatedDamage = DamageAPI.calculateWeaponDamage(damager, receiver);
        if (GameAPI.getGamePlayer(receiver) != null && GameAPI.getGamePlayer(damager) != null) {
            if (GameAPI.getGamePlayer(receiver).getPlayerAlignment() == KarmaHandler.EnumPlayerAlignments.LAWFUL) {
                if (GameAPI.getGamePlayer(damager).getPlayerAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                    if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, damager.getUniqueId()).toString())) {
                        if (calculatedDamage >= HealthHandler.getInstance().getPlayerHPLive(receiver)) {
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

            double[] armorCalculation = DamageAPI.calculateArmorReduction(damager, receiver, calculatedDamage, null);
            double armorReducedDamage = armorCalculation[0];
            double finalDamage = calculatedDamage - armorCalculation[0];
            if (armorReducedDamage == -1) {
                    damager.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT DODGED* (" + receiver.getName() + ChatColor.RED + ")");
                    receiver.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "                        *DODGE* (" + ChatColor.RED + damager.getName() + ChatColor.GREEN + ")");
                    //The defender dodged the attack
                    receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.5F, 2.0F);
                finalDamage = 0;
            } else if (armorReducedDamage == -2) {
                    damager.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT BLOCKED* (" + receiver.getName() + ChatColor.RED + ")");
                    receiver.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "                        *BLOCK* (" + ChatColor.RED + damager.getName() + ChatColor.DARK_GREEN + ")");
                    receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2F, 1.0F);
                finalDamage = 0;
            } else if (armorReducedDamage == -3) {
                //Reflect when its fixed. @TODO
            } else {
                finalDamage = finalDamage - armorCalculation[0];
                calculatedDamage = calculatedDamage - armorCalculation[0];
            }
            HealthHandler.getInstance().handlePlayerBeingDamaged(receiver, damager, finalDamage, armorCalculation[0], armorCalculation[1]);

            DamageAPI.handlePolearmAOE(event, calculatedDamage / 2, damager);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void playerRangedPlayer(EntityDamageByEntityEvent event) {
        if (!DamageAPI.isBowProjectile(event.getDamager()) && !DamageAPI.isStaffProjectile(event.getDamager())) return;
        if (!GameAPI.isPlayer(event.getEntity())) return;
        Projectile projectile = (Projectile) event.getDamager();
        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }

        event.setDamage(0);

        Player damager = (Player) projectile.getShooter();
        Player receiver = (Player) event.getEntity();

        if (receiver.getGameMode() != GameMode.SURVIVAL) return;
        if (CombatLog.isInCombat(damager)) {
            CombatLog.updateCombat(damager);
        } else {
            CombatLog.addToCombat(damager);
        }

        double calculatedDamage = DamageAPI.calculateProjectileDamage(damager, receiver, projectile);
        if (GameAPI.getGamePlayer(receiver) != null && GameAPI.getGamePlayer(damager) != null) {
            if (GameAPI.getGamePlayer(receiver).getPlayerAlignment() == KarmaHandler.EnumPlayerAlignments.LAWFUL) {
                if (GameAPI.getGamePlayer(damager).getPlayerAlignment() != KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
                    if (Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.TOGGLE_CHAOTIC_PREVENTION, damager.getUniqueId()).toString())) {
                        if (calculatedDamage >= HealthHandler.getInstance().getPlayerHPLive(receiver)) {
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
        double[] armorCalculation = DamageAPI.calculateArmorReduction(damager, receiver, calculatedDamage, null);
        double finalDamage = calculatedDamage - armorCalculation[0];
        double armorReducedDamage = armorCalculation[0];
        String defenderName = receiver.getName();
        String attackerName = damager.getName();
        if (armorReducedDamage == -1) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                damager.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT DODGED* (" + defenderName + ChatColor.RED + ")");
                receiver.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "                        *DODGE* (" + ChatColor.RED + attackerName + ChatColor.GREEN + ")");
                //The defender dodged the attack
                receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.5F, 2.0F);
            }, 1L);
            finalDamage = 0;
        } else if (armorReducedDamage == -2) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                damager.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT BLOCKED* (" + defenderName + ChatColor.RED + ")");
                receiver.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "                        *BLOCK* (" + ChatColor.RED + attackerName + ChatColor.DARK_GREEN + ")");
                receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2F, 1.0F);
            }, 1L);
            finalDamage = 0;
        } else if (armorReducedDamage == -3) {
            //Reflect when its fixed. @TODO
        } else {
            finalDamage = finalDamage - armorCalculation[0];
        }
        HealthHandler.getInstance().handlePlayerBeingDamaged(receiver, damager, finalDamage, armorCalculation[0], armorCalculation[1]);
    }
}
