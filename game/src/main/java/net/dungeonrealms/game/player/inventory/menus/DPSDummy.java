package net.dungeonrealms.game.player.inventory.menus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.game.item.items.functional.ecash.ItemDPSDummy;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.util.EulerAngle;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Getter
public class DPSDummy {

    private Entity entity;
    private Location location;
    private UUID owner;
    private String ownerName;

    private Map<UUID, LinkedHashMap<Long, Double>> dummy = new HashMap<>();

    public DPSDummy(Entity entity, Location location, UUID owner, String ownerName) {
        this.entity = entity;
        this.location = location;
        this.owner = owner;
        this.ownerName = ownerName;
    }

    public void trackDamage(UUID uuid, double damage) {
        LinkedHashMap<Long, Double> map = dummy.computeIfAbsent(uuid, m -> new LinkedHashMap<>());
        if (!map.isEmpty()) {
            long newest = -1;
            for (Long time : map.keySet()) {
                if (time > newest)
                    newest = time;

            }
            //Its been 10 seconds since they damaged the dummy, reset DPS
            if (System.currentTimeMillis() - newest >= TimeUnit.SECONDS.toMillis(5))
                map.clear();
        }

        map.put(System.currentTimeMillis(), damage);

        if (entity.isValid()) {
            ArmorStand stand = (ArmorStand) entity;
            Random random = ThreadLocalRandom.current();
            stand.setHeadPose(new EulerAngle(0, random.nextInt(360), 0));
            stand.setLeftLegPose(new EulerAngle(random.nextInt(90), random.nextInt(360), random.nextInt(90) + 270));
            stand.setRightLegPose(new EulerAngle(random.nextInt(90), random.nextInt(360), random.nextInt(90) + 270));

            Bukkit.getLogger().info("Left: X" + stand.getLeftLegPose().getX() + " Y:" + stand.getLeftLegPose().getY() + " Z: " + stand.getLeftLegPose().getZ());
            Bukkit.getLogger().info("Right: X" + stand.getRightLegPose().getX() + " Y:" + stand.getRightLegPose().getY() + " Z: " + stand.getRightLegPose().getZ());

            //Below 90x above 270z
            stand.setLeftArmPose(new EulerAngle(random.nextInt(360), random.nextInt(360), random.nextInt(360)));
            stand.setRightArmPose(new EulerAngle(random.nextInt(360), random.nextInt(360), random.nextInt(360)));
            Bukkit.getLogger().info("Head: X" + stand.getHeadPose().getX() + " Y:" + stand.getHeadPose().getY() + " Z: " + stand.getHeadPose().getZ());
        }
    }

    public Map<UUID, Double> calculateDPS() {
        Map<UUID, Double> retr = new HashMap<>();

        dummy.forEach((id, map) -> {
            long totalHitTime = 0;

            double totalDamage = 0;
            long lastHit = -1;
            for (Map.Entry<Long, Double> entrySet : map.entrySet()) {
                long hitTime = entrySet.getKey();
                double damage = entrySet.getValue();

                if (lastHit != -1) {
                    long millisSinceHit = hitTime - lastHit;

                    totalHitTime += millisSinceHit;
                }

                totalDamage += damage;
                lastHit = hitTime;
            }


            int seconds = (int) Math.floor(totalHitTime / 1000D);
            System.out.println("Total Seconds: " + seconds + " Damage: " + totalDamage);
            if (seconds == 0) return;
            double damagePerSecond = totalDamage / seconds;
            retr.put(id, damagePerSecond);
        });
        return retr;
    }

    public void destroy() {
        ItemDPSDummy.dpsDummies.remove(entity);
        entity.remove();
        ParticleAPI.sendParticleToLocationAsync(ParticleAPI.ParticleEffect.CRIT, location.clone().add(0, 1, 0), .5F, .5F, .5F, 1F, 30);
    }
}
