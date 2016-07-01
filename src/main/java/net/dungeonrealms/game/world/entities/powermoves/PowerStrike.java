package net.dungeonrealms.game.world.entities.powermoves;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.world.entities.PowerMove;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
                    first = false;
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 4, 60));
                    API.getNearbyPlayers(entity.getLocation(), 3).stream().forEach(player -> player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + entity.getName() + ChatColor.YELLOW + " is charging " + ChatColor.RED + ChatColor.BOLD + "Power Strike"));

                }

                if (entity.isDead() || entity.getHealth() <= 0) {
                    this.cancel();
                    return;
                }

                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1F, 4.0F);
                step++;
                if (step == 5) {
                    powerStrike.add(entity.getUniqueId());
                    this.cancel();
                }

            }
        }.runTaskTimer(DungeonRealms.getInstance(), 0, 20);
    }
}
