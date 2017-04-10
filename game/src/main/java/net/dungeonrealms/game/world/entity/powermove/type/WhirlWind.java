package net.dungeonrealms.game.world.entity.powermove.type;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.listener.combat.AttackResult;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.powermove.PowerMove;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.EntityCreature;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Created by chase on 6/30/2016.
 */
public class WhirlWind extends PowerMove {


    public WhirlWind() {
        super("whirlwind");
    }

    @Override
    public void schedulePowerMove(LivingEntity entity, Player attack) {
        chargingMonsters.add(entity.getUniqueId());
        new BukkitRunnable() {

            public int step = 0;
            public boolean first = true;

            @Override
            public void run() {

                if (first) {
                    first = false;
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 5, 60));
                }

                if (entity.isDead() || entity.getHealth() <= 0) {
                    this.cancel();
                    chargingMonsters.remove(entity.getUniqueId());
                    return;
                }


                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1, 4);

                entity.getWorld().playEffect(entity.getLocation(), Effect.EXPLOSION_LARGE, 1, 40);
                step++;
                if (step == 5) {
                    GameAPI.getNearbyPlayers(entity.getLocation(), 8).forEach(p -> {
                        Vector unitVector = p.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(3);

                        if (Double.isNaN(unitVector.getX()) || Double.isNaN(unitVector.getY()) || Double.isNaN(unitVector.getZ())) {
                            Bukkit.getLogger().info("SERVER CRASH PREVENTED: " + p.getName() + " ENTITY CAUSING: " + entity.toString() + " To set: " + unitVector.toString() + " AT " + p.getLocation());
                            return;
                        }
                        double e_y = entity.getLocation().getY();
                        double p_y = p.getLocation().getY();
                        Material m = p.getLocation().subtract(0, 1, 0).getBlock().getType();
                        if ((p_y - 1) <= e_y || m == Material.AIR)
                            EntityMechanics.setVelocity(p, unitVector);
                        
                        // * 4 for whirlwind
                        double multiplier = entity.hasMetadata("boss") ? 1.3 : 4;
                        AttackResult res = new AttackResult(entity, p);
                        DamageAPI.calculateWeaponDamage(res, true);
                        res.setDamage(res.getDamage() * multiplier);
                        DamageAPI.applyArmorReduction(res, true);
                        HealthHandler.damageEntity(res);
                    });

                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 0.5F);
                    entity.getWorld().playEffect(entity.getLocation(), Effect.EXPLOSION_HUGE, 1, 40);
                    entity.removePotionEffect(PotionEffectType.SLOW);
                    this.cancel();
                    chargingMonsters.remove(entity.getUniqueId());
                }
            }
        }.runTaskTimer(DungeonRealms.getInstance(), 0, 20);
        new BukkitRunnable() {

            public int step = 0;

            public float yaw = 0;

            @Override
            public void run() {
                if (entity.isDead() || entity.getHealth() <= 0) {
                    this.cancel();
                    chargingMonsters.remove(entity.getUniqueId());
                    return;
                }

                Location loc = entity.getLocation();
                yaw += 20;

                if (yaw > 360) {
                    yaw = 0;
                }
                loc.setYaw(yaw);
//                EntityLiving el = (EntityLiving) ((CraftEntity) entity).getHandle();
//                el.yaw = yaw;
                if (!(((CraftEntity) entity).getHandle() instanceof EntityCreature)) {
                    chargedMonsters.remove(entity.getUniqueId());
                    return;
                }

                EntityCreature ec = (EntityCreature) ((CraftEntity) entity).getHandle();
                ec.setGoalTarget(null);
//                ec.yaw = yaw;
                entity.teleport(loc);
                step++;
                if (step == (5 * 20)) {
                    this.cancel();
                    chargingMonsters.remove(entity.getUniqueId());
                }
            }
        }.runTaskTimer(DungeonRealms.getInstance(), 0, 1);
    }
}
