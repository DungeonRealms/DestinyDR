package net.dungeonrealms.game.world.entity.powermove.type;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.world.entity.PowerMove;
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

            @Override
            public void run() {
                if (first) {
                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1F, 4.0F);
                    first = false;
                }

                if (entity.isDead() || entity.getHealth() <= 0) {
                    chargedMonsters.remove(entity.getUniqueId());
                    chargingMonsters.remove(entity.getUniqueId());
                    this.cancel();
                    return;
                }

                if(step > 3) {
                    chargedMonsters.remove(entity.getUniqueId());
                    chargingMonsters.remove(entity.getUniqueId());
                    this.cancel();
                    step = 0;
                    return;
                }

                entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1F, 4.0F);

                step++;
                player.sendMessage(""+step);
                if (step <= 3) {
                    chargedMonsters.add(entity.getUniqueId());

                    for(int i = 0; i <damageable.size(); i ++) {
                        if(damageable.get(i) instanceof  Player) {
                            Location loca = damageable.get(i).getLocation();
                            loca.setY(loca.getY() + 5.0);
                            loca.getWorld().spawnFallingBlock(loca, Material.REDSTONE_BLOCK, (byte) 0);
                        }
                    }
                }

            }
        }.runTaskTimer(DungeonRealms.getInstance(),0, 20);
    }
}

