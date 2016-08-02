package net.dungeonrealms.game.world.entity.powermove;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.world.entity.PowerMove;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.EntityCreature;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
                        Vector unitVector = p.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
                        double e_y = entity.getLocation().getY();
                        double p_y = p.getLocation().getY();
                        Material m = p.getLocation().subtract(0, 1, 0).getBlock().getType();
                        if ((p_y - 1) <= e_y || m == Material.AIR) {
                            p.setVelocity(unitVector.multiply(3));
                        }
                        // * 4 for whirlwind
                        double multiplier = entity.hasMetadata("boss") ? 1.3 : 4;
                        double dmg = DamageAPI.calculateWeaponDamage(entity, p) * multiplier;
                        double[] result = DamageAPI.calculateArmorReduction(entity, p, dmg, null);
                        int armourReducedDamage = (int) result[0];
                        int totalArmor = (int) result[1];
                        HealthHandler.getInstance().handlePlayerBeingDamaged(p, entity, (dmg - armourReducedDamage), armourReducedDamage, totalArmor);
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
