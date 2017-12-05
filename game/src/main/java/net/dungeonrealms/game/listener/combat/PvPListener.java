package net.dungeonrealms.game.listener.combat;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.EnergyHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.item.items.functional.accessories.Trinket;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.world.item.DamageAPI;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

public class PvPListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void playerAttackPlayer(EntityDamageByEntityEvent event) {
        boolean isProjectile = DamageAPI.isBowProjectile(event.getDamager()) || DamageAPI.isStaffProjectile(event.getDamager());
        boolean isMarksmanProjectile = DamageAPI.isMarksmanBowProjectile(event.getDamager());
        boolean isPlayer = GameAPI.isPlayer(event.getDamager()) && GameAPI.isPlayer(event.getEntity());

        Projectile projectile = isProjectile ? (Projectile) event.getDamager() : null;

        //  DONT HANDLE IF IT'S NOT PLAYER VS PLAYER  //
        if (!isProjectile && !isPlayer)
            return;

        if (isProjectile && !(projectile.getShooter() instanceof Player)) return; //Shooter is not a player

        Player attacker = isProjectile ? (Player) projectile.getShooter() : (Player) event.getDamager();
        if (attacker != null && MetadataUtils.Metadata.SHARDING.has(attacker)) {
            event.setCancelled(true);
            event.setDamage(0);
            return;
        }

        if (event.getEntity() instanceof Player) {

            Player defender = (Player) event.getEntity();

            if (MetadataUtils.Metadata.SHARDING.has(event.getEntity())) {
                event.setCancelled(true);
                return;
            }

            // Apply speed from speed trinket
            int speedChance = 0;
            int rand = Utils.randInt(1, 100);
            if (Trinket.hasActiveTrinket(attacker, Trinket.COMBAT_SPEED)) {
                speedChance = 10;
                if (rand <= speedChance) {
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1));
                }
            }

            // Projectiles can be knocked back into the player.
            if (attacker.equals(event.getEntity()) || defender.hasMetadata("NPC")) {
                return;
            }

            if (defender.getGameMode() != GameMode.SURVIVAL)
                return;

            boolean isDuel = DuelingMechanics.isDuelPartner(attacker.getUniqueId(), defender.getUniqueId());

            if (!isDuel)
                CombatLog.updatePVP(attacker);

            defender.playEffect(EntityEffect.HURT);
//            attacker.playEffect(EntityEffect.HURT);
            //decrement the knockback?
            if (defender.isOnGround())
                DamageAPI.knockbackPlayerPVP(attacker, defender);

//            defender.setSprinting(false);
//            Utils.stopSprint(attacker, false);
            /*int foodLevel = defender.getFoodLevel();
            if(foodLevel > 1) {
                defender.setFoodLevel(1);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> defender.setFoodLevel(foodLevel), 40L);
            }*/


            GamePlayer damagerGP = GameAPI.getGamePlayer(attacker);

            //Dont tag them if they are in a duel..
            if (!isDuel) {
                damagerGP.setPvpTaggedUntil(System.currentTimeMillis() + 1000 * 10L);
            } else {
                // Marks the player as not able to regen health while in a duel.
                defender.setMetadata("lastDamageTaken", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
            }
            //Make it seem like we are taking damage.
//            GameAPI.getNearbyPlayersAsync(defender.getLocation(), 20).forEach(pl -> ((CraftPlayer) pl).getHandle().playerConnection.sendPacket(new PacketPlayOutAnimation(defend, 1)));
            event.setDamage(0.0D);
            event.setCancelled(true);

            ItemStack held = attacker.getEquipment().getItemInMainHand();
            AttackResult res = projectile != null ? new AttackResult(attacker, defender, projectile) : new AttackResult(attacker, defender);

            if (!isProjectile && !ItemWeapon.isWeapon(held)) {
                res.setDamage(1);
                res.applyDamage();
                return;
            }

            //  ALIGNMENT IGNORES DUELS  //
            if (!isDuel)
                if (!(attacker.hasMetadata("duelCooldown") && attacker.getMetadata("duelCooldown").get(0).asLong() > System.currentTimeMillis()))
                    KarmaHandler.update(attacker);

            DamageAPI.calculateWeaponDamage(res, !isDuel);
            DamageAPI.applyArmorReduction(res, !isDuel);

            if (!isDuel && res.checkChaoticPrevention())
                return;

            res.applyDamage();

            if (!isProjectile) {
                //float energyToRemove = EnergyHandler.handleAirSwingItem(held);
                float energyToRemove = EnergyHandler.getWeaponSwingEnergyCost(held);
                EnergyHandler.removeEnergyFromPlayerAndUpdate(attacker, energyToRemove, isDuel);
            }

            if (!isProjectile)
                DamageAPI.handlePolearmAOE(event, Math.max(1, res.getDamage() / 2), attacker);

            GamePlayer defenderGP = GameAPI.getGamePlayer(defender);

            if(isMarksmanProjectile && !defenderGP.isMarksmanTagged()) {
                defenderGP.setMarksmanTaggedUntil(System.currentTimeMillis() + 1000 * 8L);
                if(defenderGP.isMarksmanTagged()) {
                    defender.setGlowing(true);
                }
            }

        }
    }
}
