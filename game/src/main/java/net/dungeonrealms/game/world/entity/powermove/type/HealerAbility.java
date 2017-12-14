package net.dungeonrealms.game.world.entity.powermove.type;

import io.netty.util.internal.ConcurrentSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.entity.PowerMove;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.DamageAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class HealerAbility extends PowerMove {
    public static Set<Entity> spawnedMobs = new ConcurrentSet<>();
    public static int mobCount;
    public static int minionTier;

    public HealerAbility() {
        super("healerAbility");
    }

    @Override
    public void schedulePowerMove(LivingEntity ent, Player player) {
        chargedMonsters.add(ent.getUniqueId());

        new BukkitRunnable() {
            public boolean first = true;

            @Override
            public void run() {
                if (first) {
                    minionTier = EntityAPI.getTier(ent);
                    MinionType type = MinionType.getRandom();
                    mobCount = type.getGroupSize();
                    //Spawn mobs
                    for (int i = 0; i < type.getGroupSize(); i++)
                        spawnedMobs.add(spawnMinion(ent, type.getMonster(), type.getName(), minionTier));

                    first = false;
                }

                if (ent.isDead() || ent.getHealth() <= 0) {
                    chargedMonsters.remove(ent.getUniqueId());
                    chargingMonsters.remove(ent.getUniqueId());
                    this.cancel();
                    return;
                }

                if (mobCount > 0) {
                    ent.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, Integer.MAX_VALUE, 1));
                }

                if(mobCount <= 0) {
                    ent.removePotionEffect(PotionEffectType.LEVITATION);
                    chargedMonsters.remove(ent.getUniqueId());
                    chargingMonsters.remove(ent.getUniqueId());
                    this.cancel();
                }
            }
        }.runTaskTimer(DungeonRealms.getInstance(), 0, 20);
    }

    @AllArgsConstructor
    private enum MinionType {
        PROTECTOR(EnumMonster.Monk, minionTier, 12),
        ACOLYTE(EnumMonster.Acolyte, minionTier, 12),
        SACRIFICE(EnumMonster.Skeleton, minionTier, 12);

        @Getter
        private EnumMonster monster;
        @Getter
        private int tier;
        @Getter
        private int groupSize;

        public String getName() {
            return Utils.capitalize(name());
        }

        public static MinionType getRandom() {
            return values()[ThreadLocalRandom.current().nextInt(values().length)];
        }
    }

}
