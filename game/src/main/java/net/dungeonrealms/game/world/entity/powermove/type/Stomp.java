package net.dungeonrealms.game.world.entity.powermove.type;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.listener.combat.AttackResult;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.PowerMove;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.DamageAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Stomp extends PowerMove {

    public Stomp() {super("stomp");}

    @Override
    public void schedulePowerMove(LivingEntity entity, Player player) {
        chargingMonsters.add(entity.getUniqueId());
        new BukkitRunnable() {
            public int step = 0;
            public boolean first = true;
            List<Entity> damageable = entity.getNearbyEntities(9.0, 9.0, 9.0);
            FallingBlock block;

            @Override
            public void run() {
                if (first) {
                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 1F, 4.0F);
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 60));
                    chargedMonsters.add(entity.getUniqueId());
                    first = false;
                }

                if (entity.isDead() || entity.getHealth() <= 0) {
                    chargedMonsters.remove(entity.getUniqueId());
                    chargingMonsters.remove(entity.getUniqueId());
                    this.cancel();
                    return;
                }

                step++;
                player.sendMessage("" + step);

                if(step < 3)
                    entity.setVelocity(new Vector(0, 1, 0));

                if(step == 5) {
                    entity.getWorld().createExplosion(entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), 10, false, false);
                    entity.getWorld().createExplosion(entity.getLocation().getX() + 5, entity.getLocation().getY(), entity.getLocation().getZ() + 5, 10, false, false);
                    entity.getWorld().createExplosion(entity.getLocation().getX() - 5, entity.getLocation().getY(), entity.getLocation().getZ() - 5, 10, false, false);
                    entity.getWorld().createExplosion(entity.getLocation().getX() + 5, entity.getLocation().getY(), entity.getLocation().getZ() - 5, 10, false, false);
                    entity.getWorld().createExplosion(entity.getLocation().getX() - 5, entity.getLocation().getY(), entity.getLocation().getZ() + 5, 10, false, false);
                    GameAPI.getNearbyPlayers(entity.getLocation(), 11).forEach(p -> {
                        Vector unitVector = p.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(6);

                        EntityMechanics.setVelocity(p, unitVector);

                        double multiplier = 8;
                        AttackResult res = new AttackResult(entity, p);
                        DamageAPI.calculateWeaponDamage(res, true);
                        res.setDamage(res.getDamage() * multiplier);
                        DamageAPI.applyArmorReduction(res, true);
                        HealthHandler.damageEntity(res);
                    });
                }


                if(step > 6) {
                    chargedMonsters.remove(entity.getUniqueId());
                    chargingMonsters.remove(entity.getUniqueId());
                    this.cancel();
                }

            }
        }.runTaskTimer(DungeonRealms.getInstance(),0, 10);
    }
}

