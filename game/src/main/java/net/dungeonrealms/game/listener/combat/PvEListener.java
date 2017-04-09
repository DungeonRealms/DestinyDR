package net.dungeonrealms.game.listener.combat;


import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievementMonsterKill;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.handler.EnergyHandler;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.item.items.core.ItemWeaponBow;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.data.EnumTier;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.statistics.PlayerStatistics;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveKill;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.powermove.PowerMove;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.boss.DungeonBoss;
import net.dungeonrealms.game.world.item.DamageAPI;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Kieran Quigley (Proxying) on 03-Jul-16.
 */
public class PvEListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void playerAttackMob(EntityDamageByEntityEvent event) {
    	
    	Player damager = null;
    	Projectile projectile = null;
    	LivingEntity receiver = (LivingEntity) event.getEntity();
    	
        if (GameAPI.isPlayer(event.getDamager())) {
        	damager = (Player) event.getDamager();
        } else if(DamageAPI.isBowProjectile(event.getDamager()) || DamageAPI.isStaffProjectile(event.getDamager())) {
        	ProjectileSource shooter = ((Projectile)event.getDamager()).getShooter();
        	if(!(shooter instanceof Player))
        		return;
        	damager = (Player) shooter;
        	projectile = (Projectile) event.getDamager();
        } else {
        	return;
        }
        
        //  THIS ONLY HANDLES PvE  //
        if (event.getEntity() instanceof Player) return;
        
        //  NO HITTING PETS  //
        if (EntityMechanics.PLAYER_PETS.containsValue(((CraftEntity) event.getEntity()).getHandle())) return;
        if (EntityMechanics.PLAYER_MOUNTS.containsValue(((CraftEntity) event.getEntity()).getHandle())) return;
        
        //  ONLY HANDLE MOB ATTACKS  //
        if (event.getEntity() instanceof LivingEntity) {
            if (!event.getEntity().hasMetadata("type"))
            	return;
        } else if (EnumEntityType.BUFF.isType(event.getEntity())){
            return;
        }

        event.setDamage(0);

        if (DamageAPI.isInvulnerable(receiver)) {
            if (receiver.hasMetadata("boss")) {
                if (receiver instanceof CraftLivingEntity) {
                    DungeonBoss b = (DungeonBoss) ((CraftLivingEntity) receiver).getHandle();
                    b.onBossAttack(event);
                }
            }
            event.setCancelled(true);
            damager.updateInventory();
            return;
        }

        CombatLog.updateCombat(damager);
        
        ItemStack held = damager.getEquipment().getItemInMainHand();
        EnergyHandler.removeEnergyFromPlayerAndUpdate(damager.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(held));
        
        if (!receiver.hasMetadata("boss"))
            DamageAPI.knockbackEntity(damager, receiver, 0.4);
        
        if (!ItemWeapon.isWeapon(held)) {
        	AttackResult res = new AttackResult(damager, receiver);
        	res.setDamage(1);
        	HealthHandler.damageMonster(res);
            checkPowerMove(event, receiver);
            return;
        }

        if(!(receiver instanceof Player) && ItemWeaponBow.isBow(held)) {
        	event.setCancelled(true);
        	DamageAPI.knockbackEntity(damager, receiver, 1.2);
        	damager.updateInventory();
        	return;
        }
        //  CALCULATE DAMAGE  //
        AttackResult res = new AttackResult(damager, receiver, projectile);
    	DamageAPI.calculateWeaponDamage(res, true);
    	DamageAPI.applyArmorReduction(res, true);
        
        res.applyDamage();
        
        //  EXTRA WEAPON ONLY DAMAGE  //
        DamageAPI.handlePolearmAOE(event, res.getDamage() / 2, damager);
        checkPowerMove(event, receiver);
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
        if (HealthHandler.getMonsterTrackers().containsKey(monster.getUniqueId()))
            highestDamage = HealthHandler.getMonsterTrackers().get(monster.getUniqueId()).findHighestDamageDealer();
        
        if (highestDamage == null || !highestDamage.isOnline()) {
            if (killer != null) {
                highestDamage = killer;
            } else {
                return;
            }
        }
        for(int i = 0; i < 3; i++)
        	DamageAPI.createDamageHologram(killer, monster.getLocation(), ChatColor.RED + "☠");
        HealthHandler.getMonsterTrackers().remove(monster.getUniqueId());
        DRMonster drMonster = ((DRMonster) ((CraftLivingEntity) monster).getHandle());
        drMonster.onMonsterDeath(highestDamage);
        
        //Handle Quest Kill Objective
        //This has to be declared a second time as final to be used in .forEach
        final Player questReward = highestDamage;
        for(Quest quest : Quests.getInstance().questStore.getList())
			quest.getStageList().stream().filter(stage -> stage.getObjective() instanceof ObjectiveKill)
				.forEach(stage -> ((ObjectiveKill)stage.getObjective()).handleKill(questReward, event.getEntity(), drMonster));
        
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
                    //  ADD BOOST  //
                    if (nearbyPartyMembers.size() > 2 && nearbyPartyMembers.size() <= 8)
                    	exp *= 1.1 + (((double)nearbyPartyMembers.size() - 2) / 10);
                    //  DISTRIBUTE EVENLY  //
                    exp /= nearbyPartyMembers.size();
                    for (Player player : nearbyPartyMembers)
                        GameAPI.getGamePlayer(player).addExperience(exp, true, true);
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
        
        for (EnumAchievementMonsterKill ach : EnumAchievementMonsterKill.values())
        	if (playerStatistics.getTotalMobKills() == ach.getKillRequirement())
        		Achievements.getInstance().giveAchievement(highestDamage.getUniqueId(), ach.getAchievement());
    }

    private static void checkPowerMove(EntityDamageByEntityEvent event, LivingEntity receiver) {
        if (!receiver.hasMetadata("tier")) return;
        if (PowerMove.chargedMonsters.contains(receiver.getUniqueId()) || PowerMove.chargingMonsters.contains(receiver.getUniqueId()))
            return;

        int mobTier = receiver.getMetadata("tier").get(0).asInt();
        Random rand = new Random();
        int powerChance = EnumTier.getById(mobTier).getPowerMoveChance();
        if (receiver.hasMetadata("elite")) {
            if (rand.nextInt(100) <= powerChance) {
                receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1F, 4.0F);
                PowerMove.doPowerMove("whirlwind", receiver, null);
            }
        } else if (receiver.hasMetadata("boss")) {
            if (receiver instanceof CraftLivingEntity) {
                DungeonBoss b = (DungeonBoss) ((CraftLivingEntity) receiver).getHandle();
                b.onBossAttack(event);
            }
            else
                return;
            powerChance = 3;
            if (rand.nextInt(100) <= powerChance) {
                receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1F, 4.0F);
                PowerMove.doPowerMove("whirlwind", receiver, null);
            }
        } else {
            if (rand.nextInt(100) <= powerChance)
                PowerMove.doPowerMove("powerstrike", receiver, null);
        }
    }
}
