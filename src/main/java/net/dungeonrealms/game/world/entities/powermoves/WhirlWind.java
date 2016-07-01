package net.dungeonrealms.game.world.entities.powermoves;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.world.entities.PowerMove;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.minecraft.server.v1_9_R2.EntityCreature;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

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

            int step = 0;

            double yaw = entity.getLocation().getYaw();
            Location loc = entity.getLocation();

            public boolean first = true;

            @Override
            public void run() {

                if (first) {
                    first = false;
                    EntityCreature ec = (EntityCreature) ((CraftEntity) entity).getHandle();
                    ec.setGoalTarget(null);
                    ec.yaw = (float) yaw;
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 4, 60));
                    API.getNearbyPlayers(entity.getLocation(), 3).stream().forEach(player -> player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + entity.getName() + ChatColor.YELLOW + " is charging a whirlwind attack"));

                }

                if (entity.isDead() || entity.getHealth() <= 0) {
                    this.cancel();
                    chargingMonsters.remove(entity.getUniqueId());
                    return;
                }


                yaw += 20;
                if (yaw > 360) {
                    yaw = 0;
                }

                step++;
                if (step == 5 * 20) {
                    API.getNearbyPlayers(entity.getLocation(), 3).stream().forEach(p -> {
                        org.bukkit.util.Vector unitVector = p.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
                        double e_y = entity.getLocation().getY();
                        double p_y = p.getLocation().getY();
                        Material m = p.getLocation().subtract(0, 1, 0).getBlock().getType();
                        if ((p_y - 1) <= e_y || m == Material.AIR) {
                            p.setVelocity(unitVector.multiply(3));
                        }
                        double dmg = DamageAPI.calculateWeaponDamage(entity, p) * 4;
                        double[] result = DamageAPI.calculateArmorReduction(entity, p, dmg, null);
                        int armourReducedDamage = (int) result[0];
                        int totalArmor = (int) result[1];
                        // * 4 for whirlwind
                        HealthHandler.getInstance().handlePlayerBeingDamaged(p, entity, (dmg - armourReducedDamage) * 4, armourReducedDamage, totalArmor);
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.3f, 1);
                        ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.LARGE_SMOKE, p.getLocation().add(0, 1, 0), new Random().nextFloat(),
                                new Random().nextFloat(), new Random().nextFloat(), 0.3F, 40);

                    });
                    entity.removePotionEffect(PotionEffectType.SLOW);
                    this.cancel();
                    chargingMonsters.remove(entity.getUniqueId());
                }

            }
        }.runTaskTimer(DungeonRealms.getInstance(), 0, 1);

    }
}
