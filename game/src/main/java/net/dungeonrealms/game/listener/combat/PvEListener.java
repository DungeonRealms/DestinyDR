package net.dungeonrealms.game.listener.combat;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievementMonsterKill;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.handler.EnergyHandler;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.item.items.core.ItemWeaponBow;
import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mechanic.data.EnumTier;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.mechanic.rifts.Rift;
import net.dungeonrealms.game.mechanic.rifts.RiftMechanics;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveKill;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.PowerMove;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.dungeonrealms.game.world.item.DamageAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Kieran Quigley (Proxying) on 03-Jul-16.
 */
public class PvEListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void playerAttackMob(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof LivingEntity))
            return;

        Player damager = null;
        Projectile projectile = null;
        LivingEntity receiver = (LivingEntity) event.getEntity();

        if (GameAPI.isPlayer(event.getDamager())) {
            damager = (Player) event.getDamager();
        } else if (DamageAPI.isBowProjectile(event.getDamager()) || DamageAPI.isStaffProjectile(event.getDamager())) {
            ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if (!(shooter instanceof Player))
                return;
            damager = (Player) shooter;
            projectile = (Projectile) event.getDamager();
        } else {
            return;
        }


        //  THIS ONLY HANDLES PvE  //
        if (event.getEntity() instanceof Player)
            return;

        // Don't attack pets!
        if (PetUtils.getPets().containsValue(event.getEntity()) || MountUtils.getMounts().containsValue(event.getEntity()))
            return;

        //  ONLY HANDLE MOB ATTACKS  //
        if (!Metadata.ENTITY_TYPE.has(event.getEntity()))
            return;

        event.setDamage(0);

        if (DamageAPI.isInvulnerable(receiver)) {
            if (EntityAPI.isBoss(receiver))
                ((DungeonBoss) EntityAPI.getMonster(receiver)).onBossAttacked(damager);
            event.setCancelled(true);
            damager.updateInventory();
            return;
        }

        CombatLog.updateCombat(damager);

        boolean dpsDummy = EnumEntityType.DPS_DUMMY.isType(event.getEntity());
        ItemStack held = damager.getEquipment().getItemInMainHand();
        EnergyHandler.removeEnergyFromPlayerAndUpdate(damager, EnergyHandler.getWeaponSwingEnergyCost(held), dpsDummy);

        if (!EntityAPI.isBoss(receiver))
            DamageAPI.knockbackEntity(damager, receiver, 0.4);

        AttackResult res = null;
        if (ProfessionItem.isProfessionItem(held)) {
            event.setCancelled(true);
            event.setDamage(0.0);
        }
        //1 damage for melee staffing..
        if (!ItemWeapon.isWeapon(held) || !DamageAPI.isStaffProjectile(event.getDamager()) && ItemWeaponStaff.isStaff(held)) {
            res = new AttackResult(damager, receiver);
            res.setDamage(1);
            if (dpsDummy) {
                res.applyDamage();
            } else {
                HealthHandler.damageMonster(res);
                checkPowerMove(event, receiver);
            }
            return;
        }

        if (!(receiver instanceof Player) && ItemWeaponBow.isBow(held)) {
            //Why cancel the event?
            int tier = new ItemWeaponBow(damager.getInventory().getItemInMainHand()).getTier().getId();
            DamageAPI.knockbackEntity(damager, receiver, .45 + .2D * tier);
            damager.updateInventory();
        }


        //  CALCULATE DAMAGE  //
        res = new AttackResult(damager, receiver, projectile);

        DamageAPI.calculateWeaponDamage(res, !dpsDummy);
        DamageAPI.applyArmorReduction(res, true);

        res.applyDamage();

        //  EXTRA WEAPON ONLY DAMAGE  //
        DamageAPI.handlePolearmAOE(event, res.getDamage() / 2, damager);
        checkPowerMove(event, receiver);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMonsterDeath(EntityDeathEvent event) {
        LivingEntity monster = event.getEntity();
        if (!EnumEntityType.HOSTILE_MOB.isType(monster))
            return;

        Player killer = monster.getKiller();
        Player highestDamage = null;
        if (HealthHandler.getMonsterTrackers().containsKey(monster.getUniqueId()))
            highestDamage = HealthHandler.getMonsterTrackers().get(monster.getUniqueId()).findHighestDamageDealer();

        if (highestDamage == null || !highestDamage.isOnline()) {
            if (killer != null) {
                highestDamage = killer;
            } else if (!EntityAPI.isBoss(monster)) { // We can skip the kill hook, unless it's a boss.
                return;
            }
        }

        for (int i = 0; i < 3; i++)
            DamageAPI.createDamageHologram(killer, monster.getLocation(), ChatColor.RED + "☠");
        HealthHandler.getMonsterTrackers().remove(monster.getUniqueId());

        DRMonster drMonster = EntityAPI.getMonster(monster);
        EntityAPI.getEntityAttributes().remove(drMonster);
        drMonster.onMonsterDeath(highestDamage);

        Rift active = RiftMechanics.getInstance().getActiveRift();
        //Handle rift mob deaths?
        if (active != null) {
            if (active.isRiftMinion(event.getEntity()))
                active.onRiftMinionDeath(event.getEntity(), event);
        }
        //Handle Quest Kill Objective
        //This has to be declared a second time as final to be used in .forEach
        final Player questReward = highestDamage;
        for (Quest quest : Quests.getInstance().questStore.getList())
            quest.getStageList().stream().filter(stage -> stage.getObjective() instanceof ObjectiveKill)
                    .forEach(stage -> ((ObjectiveKill) stage.getObjective()).handleKill(questReward, event.getEntity(), drMonster));

        if (EntityAPI.isBoss(monster))
            return; // We don't need to run the code past here for bosses.

        int exp = GameAPI.getMonsterExp(highestDamage, monster);
        int eliteBonusXP = (int) (exp * 0.3);
        if (EntityAPI.isElite(monster)) exp += eliteBonusXP;
        GamePlayer gamePlayer = GameAPI.getGamePlayer(highestDamage);

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(highestDamage);
        if (gamePlayer == null || wrapper == null) {
            return;
        }
        if (killer != null) {
            if (!highestDamage.getUniqueId().toString().equals(killer.getUniqueId().toString())) {
                killer.sendMessage(ChatColor.GRAY + highestDamage.getName() + " has dealt more damage to this mob than you.");
                killer.sendMessage(ChatColor.GRAY + "They have been awarded the XP.");
            }
        }

        boolean added = false;
        if (Affair.isInParty(highestDamage)) {
            List<Player> nearbyPlayers = GameAPI.getNearbyPlayers(highestDamage.getLocation(), 10);
            List<Player> nearbyPartyMembers = new ArrayList<>();
            if (!nearbyPlayers.isEmpty()) {
                for (Player player : nearbyPlayers)
                    if (!player.equals(highestDamage) && Affair.areInSameParty(highestDamage, player))
                        nearbyPartyMembers.add(player);

                if (nearbyPartyMembers.size() > 0) {
                    nearbyPartyMembers.add(highestDamage);
                    //  ADD BOOST  //
                    if (nearbyPartyMembers.size() > 2 && nearbyPartyMembers.size() <= 8)
                        exp *= 1.1 + (((double) nearbyPartyMembers.size() - 2) / 10);

                    //  DISTRIBUTE EVENLY  //
                    exp /= nearbyPartyMembers.size();
                    for (Player player : nearbyPartyMembers)
                        PlayerWrapper.getWrapper(player).addExperience(exp, true, true, true);
                    added = true;
                }
            }
        }

        if (!added) {
            wrapper.addExperience(exp, false, true, true);
            if (EntityAPI.isElite(monster) && killer != null)
                killer.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "        " + ChatColor.GOLD
                        .toString() + ChatColor.BOLD + "ELITE BUFF >> " + ChatColor.YELLOW.toString() + ChatColor.BOLD
                        + "+" + ChatColor.YELLOW + Math.round(eliteBonusXP) + ChatColor.BOLD + " EXP " +
                        ChatColor.GRAY + "[" + Math.round(eliteBonusXP) + ChatColor.BOLD + "/" +
                        ChatColor.GRAY + Math.round(wrapper.getEXPNeeded(wrapper.getLevel())) + " EXP]");
        }

        StatColumn[] tierStats = new StatColumn[]{StatColumn.T1_MOB_KILLS, StatColumn.T2_MOB_KILLS, StatColumn.T3_MOB_KILLS, StatColumn.T4_MOB_KILLS, StatColumn.T5_MOB_KILLS};
        wrapper.getPlayerGameStats().addStat(tierStats[EntityAPI.getTier(monster) - 1]);

        for (EnumAchievementMonsterKill ach : EnumAchievementMonsterKill.values())
            if (wrapper.getPlayerGameStats().getTotalMobKills() == ach.getKillRequirement())
                Achievements.giveAchievement(highestDamage, ach.getAchievement());
    }

    private static void checkPowerMove(EntityDamageByEntityEvent event, LivingEntity receiver) {
        if (!EntityAPI.isMonster(receiver))
            return;

        if (PowerMove.chargedMonsters.contains(receiver.getUniqueId()) || PowerMove.chargingMonsters.contains(receiver.getUniqueId()))
            return;

        int mobTier = EntityAPI.getTier(receiver);
        Random rand = ThreadLocalRandom.current();
        int powerChance = EnumTier.getById(mobTier).getPowerMoveChance();
        if (EntityAPI.isElite(receiver)) {
            if (rand.nextInt(100) <= powerChance) {
                receiver.getWorld().playSound(receiver.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1F, 4.0F);
                PowerMove.doPowerMove("whirlwind", receiver, null);
            }
        } else if (EntityAPI.isBoss(receiver)) {
            if (event.getDamager() instanceof Player)
                ((DungeonBoss) EntityAPI.getMonster(receiver)).onBossAttacked((Player) event.getDamager());
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
