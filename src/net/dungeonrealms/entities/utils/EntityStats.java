package net.dungeonrealms.entities.utils;

import net.dungeonrealms.DungeonRealms;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Random;

/**
 * Created by Chase on Sep 18, 2015
 */
public class EntityStats {

    public static class Stats {
        public int def;
        public int hp;
        public int atk;
        public int spd;

        public Stats(int def, int hp, int atk, int spd) {
            this.def = def;
            this.hp = hp;
            this.atk = atk;
            this.spd = spd;
        }

        public static Stats getRandomStats(int lvl) {
            Random random = new Random();
            int lvldef;
            int lvlatk;
            int lvlhp;
            int lvlspd;
            if (random.nextBoolean())
                lvldef = (lvl * 100) + random.nextInt(20);
            else
                lvldef = (lvl * 100) - random.nextInt(20);
            if (random.nextBoolean())
                lvlhp = (lvl * 100) + random.nextInt(20);
            else
                lvlhp = (lvl * 100) - random.nextInt(20);

            if (random.nextBoolean())
                lvlatk = (lvl * 100) + random.nextInt(20);
            else
                lvlatk = (lvl * 100) - random.nextInt(20);
            if (random.nextBoolean())
                lvlspd = (lvl * 100) + random.nextInt(20);
            else
                lvlspd = (lvl * 100) - random.nextInt(20);

            return new Stats(lvldef, lvlhp, lvlatk, lvlspd);
        }
    }

    public static Stats getMonsterStats(Entity entity) {
        int hp = entity.getBukkitEntity().getMetadata("hp").get(0).asInt();
        int def = entity.getBukkitEntity().getMetadata("def").get(0).asInt();
        int spd = entity.getBukkitEntity().getMetadata("spd").get(0).asInt();
        int atk = entity.getBukkitEntity().getMetadata("atk").get(0).asInt();
        return new Stats(def, hp, atk, spd);
    }

    public static void setMonsterStats(Entity entity, int lvl) {
        Stats stat = Stats.getRandomStats(lvl);
        entity.getBukkitEntity().setMetadata("hp", new FixedMetadataValue(DungeonRealms.getInstance(), stat.hp));
        entity.getBukkitEntity().setMetadata("def", new FixedMetadataValue(DungeonRealms.getInstance(), stat.def));
        entity.getBukkitEntity().setMetadata("atk", new FixedMetadataValue(DungeonRealms.getInstance(), stat.atk));
        entity.getBukkitEntity().setMetadata("spd", new FixedMetadataValue(DungeonRealms.getInstance(), stat.spd));
    }

}