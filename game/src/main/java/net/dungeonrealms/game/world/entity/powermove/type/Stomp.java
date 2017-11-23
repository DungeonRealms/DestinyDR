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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Stomp extends PowerMove {

    public Stomp() {super("stomp");}

    @Override
    public void schedulePowerMove(LivingEntity ent, Player player) {
        chargingMonsters.add(ent.getUniqueId());
        new BukkitRunnable() {
            public  boolean first = true;
            List<Entity> damageable = ent.getNearbyEntities(9.0, 9.0, 9.0);

            @Override
            public void run() {
                if (first) {
                    first = false;
                    for (int i = 0; i < damageable.size(); i++) {
                        Location location = damageable.get(i).getLocation();
                        if (damageable instanceof Player) {
                            ent.getWorld().playSound(ent.getLocation(), Sound.BLOCK_ANVIL_LAND, 1F, 4.0F);
                            FallingBlock lava = ent.getWorld().spawnFallingBlock(location, Material.WOOL, (byte) 0);
                        }

                    }
                }
                if (ent.isDead() || ent.getHealth() <= 0) {
                    this.cancel();
                    chargingMonsters.remove(ent.getUniqueId());
                    return;
                }
            }
        }.runTaskTimer(DungeonRealms.getInstance(),0, 1);
    }
}

