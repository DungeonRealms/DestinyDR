package net.dungeonrealms.game.listener.combat;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.handlers.EnergyHandler;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.statistics.PlayerStatistics;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.PowerMove;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.entities.types.monsters.boss.Boss;
import net.dungeonrealms.game.world.items.Attribute;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.party.Affair;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Kieran Quigley (Proxying) on 03-Jul-16.
 */
public class PvEListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void playerMeleeMob(EntityDamageByEntityEvent event) {
        if (!GameAPI.isPlayer(event.getDamager())) return;
        if (event.getEntity() instanceof Player) return;
        if (Entities.PLAYER_PETS.containsValue(((CraftEntity) event.getEntity()).getHandle())) return;
        if (Entities.PLAYER_MOUNTS.containsValue(((CraftEntity) event.getEntity()).getHandle())) return;
        if (event.getEntity() instanceof LivingEntity) {
            if (!event.getEntity().hasMetadata("type")) return;
        } else {
            if (event.getEntity().hasMetadata("type")) {
                if (event.getEntity().getMetadata("type").get(0).asString().equals("buff")) return;
            } else {
                return;
            }
        }

        event.setDamage(0);

        Player damager = (Player) event.getDamager();
        LivingEntity receiver = (LivingEntity) event.getEntity();

        if (DamageAPI.isInvulnerable(receiver)) {
            if (receiver.hasMetadata("boss")) {
                if (receiver instanceof CraftLivingEntity) {
                    Boss b = (Boss) ((CraftLivingEntity) receiver).getHandle();
                    b.onBossHit(event);
                }
            }
            event.setCancelled(true);
            damager.updateInventory();
            return;
        }

        double finalDamage;

        if (CombatLog.isInCombat(damager)) {
            CombatLog.updateCombat(damager);
        } else {
            CombatLog.addToCombat(damager);
        }

        EnergyHandler.removeEnergyFromPlayerAndUpdate(damager.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(damager.getEquipment().getItemInMainHand()));

        DamageAPI.knockbackEntity(damager, receiver, 0.4);

        if (!GameAPI.isWeapon(damager.getEquipment().getItemInMainHand())) {
            HealthHandler.getInstance().handleMonsterBeingDamaged(receiver, damager, 1);
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
                return;
            default:
                break;
        }

        finalDamage = DamageAPI.calculateWeaponDamage(damager, receiver);
        double[] armorCalculation = DamageAPI.calculateArmorReduction(damager, receiver, finalDamage, null);
        double armorReducedDamage = armorCalculation[0];
        String defenderName;
        if (receiver.hasMetadata("customname")) {
            defenderName = receiver.getMetadata("customname").get(0).asString().trim();
        } else {
            defenderName = "Enemy";
        }
        if (armorReducedDamage == -1) {
            damager.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT DODGED* (" + defenderName + ChatColor.RED + ")");
            //The defender dodged the attack
            receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.5F, 2.0F);
            finalDamage = 0;
        } else if (armorReducedDamage == -2) {
            damager.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT BLOCKED* (" + defenderName + ChatColor.RED + ")");
            receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2F, 1.0F);
            finalDamage = 0;
        } else if (armorReducedDamage == -3) {
            //Reflect when its fixed. @TODO
        } else {
            finalDamage = finalDamage - armorCalculation[0];
        }
        HealthHandler.getInstance().handleMonsterBeingDamaged(receiver, damager, finalDamage);
        DamageAPI.handlePolearmAOE(event, finalDamage / 2, damager);

        if (!receiver.hasMetadata("tier")) return;
        if (PowerMove.chargedMonsters.contains(receiver.getUniqueId()) || PowerMove.chargingMonsters.contains(receiver.getUniqueId()))
            return;

        int mobTier = receiver.getMetadata("tier").get(0).asInt();
        Random rand = new Random();
        int powerChance = 0;
        if (receiver.hasMetadata("elite")) {
            switch (mobTier) {
                case 1:
                    powerChance = 5;
                    break;
                case 2:
                    powerChance = 7;
                    break;
                case 3:
                    powerChance = 10;
                    break;
                case 4:
                    powerChance = 13;
                    break;
                case 5:
                    powerChance = 20;
                    break;

            }
            if (rand.nextInt(100) <= powerChance) {
                receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1F, 4.0F);
                PowerMove.doPowerMove("whirlwind", receiver, null);
            }
        } else if (receiver.hasMetadata("boss")) {
            if (receiver instanceof CraftLivingEntity) {
                Boss b = (Boss) ((CraftLivingEntity) receiver).getHandle();
                b.onBossHit(event);
            }
        } else {
            switch (mobTier) {
                case 1:
                    powerChance = 5;
                    break;
                case 2:
                    powerChance = 7;
                    break;
                case 3:
                    powerChance = 10;
                    break;
                case 4:
                    powerChance = 13;
                    break;
                case 5:
                    powerChance = 20;
                    break;

            }
            if (rand.nextInt(100) <= powerChance) {
                PowerMove.doPowerMove("powerstrike", receiver, null);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void playerRangedMob(EntityDamageByEntityEvent event) {
        if (!DamageAPI.isBowProjectile(event.getDamager()) && !DamageAPI.isStaffProjectile(event.getDamager())) return;
        if (event.getEntity() instanceof Player) return;
        if (Entities.PLAYER_PETS.containsValue(((CraftEntity) event.getEntity()).getHandle())) return;
        if (Entities.PLAYER_MOUNTS.containsValue(((CraftEntity) event.getEntity()).getHandle())) return;
        Projectile projectile = (Projectile) event.getDamager();
        if (!(projectile.getShooter() instanceof Player)) {
            event.setDamage(0);
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof LivingEntity) {
            if (!event.getEntity().hasMetadata("type")) return;
        } else {
            if (event.getEntity().hasMetadata("type")) {
                if (event.getEntity().getMetadata("type").get(0).asString().equals("buff")) return;
            } else {
                return;
            }
        }

        event.setDamage(0);

        Player damager = (Player) projectile.getShooter();
        LivingEntity receiver = (LivingEntity) event.getEntity();

        if (DamageAPI.isInvulnerable(receiver)) {
            if (receiver.hasMetadata("boss")) {
                if (receiver instanceof CraftLivingEntity) {
                    Boss b = (Boss) ((CraftLivingEntity) receiver).getHandle();
                    b.onBossHit(event);
                }
            }
            event.setCancelled(true);
            damager.updateInventory();
            return;
        }

        double finalDamage;

        if (CombatLog.isInCombat(damager)) {
            CombatLog.updateCombat(damager);
        } else {
            CombatLog.addToCombat(damager);
        }

        DamageAPI.knockbackEntity(damager, receiver, 0.4);

        finalDamage = DamageAPI.calculateProjectileDamage(damager, receiver, projectile);
        double[] armorCalculation = DamageAPI.calculateArmorReduction(damager, receiver, finalDamage, null);
        double armorReducedDamage = armorCalculation[0];
        String defenderName;
        if (receiver.hasMetadata("customname")) {
            defenderName = receiver.getMetadata("customname").get(0).asString().trim();
        } else {
            defenderName = "Enemy";
        }
        if (armorReducedDamage == -1) {
            damager.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT DODGED* (" + defenderName + ChatColor.RED + ")");
            //The defender dodged the attack
            receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.5F, 2.0F);
            finalDamage = 0;
        } else if (armorReducedDamage == -2) {
            damager.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "                   *OPPONENT BLOCKED* (" + defenderName + ChatColor.RED + ")");
            receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2F, 1.0F);
            finalDamage = 0;
        } else if (armorReducedDamage == -3) {
            //Reflect when its fixed. @TODO
        } else {
            finalDamage = finalDamage - armorCalculation[0];
        }
        HealthHandler.getInstance().handleMonsterBeingDamaged(receiver, damager, finalDamage);

        if (!receiver.hasMetadata("tier")) return;
        if (PowerMove.chargedMonsters.contains(receiver.getUniqueId()) || PowerMove.chargingMonsters.contains(receiver.getUniqueId()))
            return;

        int mobTier = receiver.getMetadata("tier").get(0).asInt();
        Random rand = new Random();
        int powerChance = 0;
        if (receiver.hasMetadata("elite")) {
            switch (mobTier) {
                case 1:
                    powerChance = 5;
                    break;
                case 2:
                    powerChance = 7;
                    break;
                case 3:
                    powerChance = 10;
                    break;
                case 4:
                    powerChance = 13;
                    break;
                case 5:
                    powerChance = 20;
                    break;

            }
            if (rand.nextInt(100) <= powerChance) {
                receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1F, 4.0F);
                PowerMove.doPowerMove("whirlwind", receiver, null);
            }
        } else if (receiver.hasMetadata("boss")) {
            if (receiver instanceof CraftLivingEntity) {
                Boss b = (Boss) ((CraftLivingEntity) receiver).getHandle();
                b.onBossHit(event);
            }
        } else {
            switch (mobTier) {
                case 1:
                    powerChance = 5;
                    break;
                case 2:
                    powerChance = 7;
                    break;
                case 3:
                    powerChance = 10;
                    break;
                case 4:
                    powerChance = 13;
                    break;
                case 5:
                    powerChance = 20;
                    break;

            }
            if (rand.nextInt(100) <= powerChance) {
                PowerMove.doPowerMove("powerstrike", receiver, null);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMonsterDeath(EntityDeathEvent event) {
        if (!event.getEntity().hasMetadata("type")) return;
        if (!event.getEntity().getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) return;
        if (event.getEntity().hasMetadata("uuid") || event.getEntity().hasMetadata("boss")) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity monster = event.getEntity();
        Player killer = monster.getKiller();
        Player highestDamage = null;
        if (HealthHandler.getInstance().getMonsterTrackers().containsKey(monster.getUniqueId())) {
            highestDamage = HealthHandler.getInstance().getMonsterTrackers().get(monster.getUniqueId()).findHighestDamageDealer();
        }
        if (highestDamage == null || !highestDamage.isOnline()) {
            if (killer != null) {
                highestDamage = killer;
            } else {
                return;
            }
        }
        HealthHandler.getInstance().getMonsterTrackers().remove(monster.getUniqueId());
        ((DRMonster) ((CraftLivingEntity) monster).getHandle()).onMonsterDeath(highestDamage);
        int exp = GameAPI.getMonsterExp(highestDamage, monster);
        GamePlayer gamePlayer = GameAPI.getGamePlayer(highestDamage);
        if (gamePlayer == null) {
            return;
        }
        if (killer != null) {
            if (!highestDamage.getUniqueId().toString().equals(killer.getUniqueId().toString())) {
                killer.sendMessage(ChatColor.GRAY + highestDamage.getName() + " has dealt more damage to this mob than you.");
                killer.sendMessage(ChatColor.GRAY + "They have been awarded the XP.");
            }
        }
        if (Affair.getInstance().isInParty(highestDamage)) {
            List<Player> nearbyPlayers = GameAPI.getNearbyPlayers(highestDamage.getLocation(), 10);
            List<Player> nearbyPartyMembers = new ArrayList<>();
            if (!nearbyPlayers.isEmpty()) {
                for (Player player : nearbyPlayers) {
                    if (player.equals(highestDamage)) {
                        continue;
                    }
                    if (!GameAPI.isPlayer(highestDamage)) {
                        continue;
                    }
                    if (Affair.getInstance().areInSameParty(highestDamage, player)) {
                        nearbyPartyMembers.add(player);
                    }
                }
                if (nearbyPartyMembers.size() > 0) {
                    nearbyPartyMembers.add(highestDamage);
                    switch (nearbyPartyMembers.size()) {
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            exp *= 1.2;
                            break;
                        case 4:
                            exp *= 1.3;
                            break;
                        case 5:
                            exp *= 1.4;
                            break;
                        case 6:
                            exp *= 1.5;
                            break;
                        case 7:
                            exp *= 1.6;
                            break;
                        case 8:
                            exp *= 1.7;
                            break;
                        default:
                            break;
                    }
                    exp /= nearbyPartyMembers.size();
                    for (Player player : nearbyPartyMembers) {
                        GameAPI.getGamePlayer(player).addExperience(exp, true, true);
                    }
                } else {
                    gamePlayer.addExperience(exp, false, true);
                }
            } else {
                gamePlayer.addExperience(exp, false, true);
            }
        } else {
            gamePlayer.addExperience(exp, false, true);
        }
        PlayerStatistics playerStatistics = gamePlayer.getPlayerStatistics();
        switch (monster.getMetadata("tier").get(0).asInt()) {
            case 1:
                playerStatistics.setT1MobsKilled(playerStatistics.getT1MobsKilled() + 1);
                break;
            case 2:
                playerStatistics.setT2MobsKilled(playerStatistics.getT2MobsKilled() + 1);
                break;
            case 3:
                playerStatistics.setT3MobsKilled(playerStatistics.getT3MobsKilled() + 1);
                break;
            case 4:
                playerStatistics.setT4MobsKilled(playerStatistics.getT4MobsKilled() + 1);
                break;
            case 5:
                playerStatistics.setT5MobsKilled(playerStatistics.getT5MobsKilled() + 1);
                break;
            default:
                break;
        }
        switch (playerStatistics.getTotalMobKills()) {
            case 100:
                Achievements.getInstance().giveAchievement(highestDamage.getUniqueId(), Achievements.EnumAchievements.MONSTER_HUNTER_I);
                break;
            case 300:
                Achievements.getInstance().giveAchievement(highestDamage.getUniqueId(), Achievements.EnumAchievements.MONSTER_HUNTER_II);
                break;
            case 500:
                Achievements.getInstance().giveAchievement(highestDamage.getUniqueId(), Achievements.EnumAchievements.MONSTER_HUNTER_III);
                break;
            case 1000:
                Achievements.getInstance().giveAchievement(highestDamage.getUniqueId(), Achievements.EnumAchievements.MONSTER_HUNTER_IV);
                break;
            case 1500:
                Achievements.getInstance().giveAchievement(highestDamage.getUniqueId(), Achievements.EnumAchievements.MONSTER_HUNTER_V);
                break;
            case 2000:
                Achievements.getInstance().giveAchievement(highestDamage.getUniqueId(), Achievements.EnumAchievements.MONSTER_HUNTER_VI);
                break;
            default:
                break;
        }
    }
}
