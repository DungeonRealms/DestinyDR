package net.dungeonrealms.game.world.entity.powermove.type;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.world.entity.powermove.PowerMove;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by chase on 6/30/2016.
 */
public class PowerStrike extends PowerMove {

    public static CopyOnWriteArrayList<UUID> powerStrike = new CopyOnWriteArrayList<>();


    public PowerStrike() {
        super("powerstrike");
    }

    @Override
    public void schedulePowerMove(LivingEntity entity, Player attack) {
        chargingMonsters.add(entity.getUniqueId());
        new BukkitRunnable() {

            int step = 0;
            public boolean first = true;

            @Override
            public void run() {
                if (first) {
                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1F, 4.0F);
                    first = false;
                }

                if (entity.isDead() || entity.getHealth() <= 0) {
                    chargedMonsters.remove(entity.getUniqueId());
                    chargingMonsters.remove(entity.getUniqueId());
                    powerStrike.remove(entity.getUniqueId());
                    this.cancel();
                    return;
                }
                entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1F, 4.0F);

                step++;
                if (step == 5) {
                    powerStrike.add(entity.getUniqueId());
                    chargedMonsters.add(entity.getUniqueId());
                    chargingMonsters.remove(entity.getUniqueId());
                    this.cancel();
                }

            }
        }.runTaskTimer(DungeonRealms.getInstance(), 0, 20);
    }
}
