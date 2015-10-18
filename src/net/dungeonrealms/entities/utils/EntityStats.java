package net.dungeonrealms.entities.utils;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.handlers.HealthHandler;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.entity.LivingEntity;
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

        public static Stats getRandomStats(int lvl, int tier) {
            int lvldef;
            int lvlatk;
            int lvlhp;
            int lvlspd;
            Random random = new Random();
            switch (tier) {
                case 1:
                    lvldef = (lvl + 5) + (random.nextInt(5) - 3);
                    lvlhp = Math.abs((lvl * 2) + (random.nextInt(30) - 15));
                    lvlatk = (lvl + 5) + (random.nextInt(5) - 3);
                    lvlspd = (lvl + 5) + (random.nextInt(5) - 3);
                    break;
                case 2:
                    lvldef = (lvl + 20) + (random.nextInt(20) - 10);
                    lvlhp = Math.abs((lvl * 5) + (random.nextInt(50) - 35));
                    lvlatk = (lvl + 20) + (random.nextInt(20) - 10);
                    lvlspd = (lvl + 20) + (random.nextInt(20) - 10);
                    break;
                case 3:
                    lvldef = (lvl + 40) + (random.nextInt(35) - 20);
                    lvlhp = Math.abs((lvl * 10) + (random.nextInt(75) - 50));
                    lvlatk = (lvl + 40) + (random.nextInt(35) - 20);
                    lvlspd = (lvl + 40) + (random.nextInt(35) - 20);
                    break;
                case 4:
                    lvldef = (lvl + 60) + (random.nextInt(55) - 35);
                    lvlhp = Math.abs((lvl * 20) + (random.nextInt(100) - 70));
                    lvlatk = (lvl + 60) + (random.nextInt(55) - 35);
                    lvlspd = (lvl + 60) + (random.nextInt(55) - 35);
                    break;
                case 5:
                    lvldef = (lvl + 85) + (random.nextInt(80) - 50);
                    lvlhp = Math.abs((lvl * 50) + (random.nextInt(150) - 100));
                    lvlatk = (lvl + 85) + (random.nextInt(80) - 50);
                    lvlspd = (lvl + 85) + (random.nextInt(80) - 50);
                    break;
                default:
                    lvldef = (lvl + 40) + (random.nextInt(35) - 20);
                    lvlhp = Math.abs((lvl * 50) + (random.nextInt(75) - 50));
                    lvlatk = (lvl + 40) + (random.nextInt(35) - 20);
                    lvlspd = (lvl + 40) + (random.nextInt(35) - 20);
                    break;
            }
            return new Stats(lvldef, lvlhp, lvlatk, lvlspd);
        }
    }

    public static Stats getMonsterStats(Entity entity) {
        int hp = entity.getBukkitEntity().getMetadata("maxHP").get(0).asInt();
        int def = entity.getBukkitEntity().getMetadata("def").get(0).asInt();
        int spd = entity.getBukkitEntity().getMetadata("spd").get(0).asInt();
        int atk = entity.getBukkitEntity().getMetadata("attack").get(0).asInt();
        return new Stats(def, hp, atk, spd);
    }

    public static void setMonsterElite(Entity entity ,int lvl, int tier) {
    	String name = ChatColor.BOLD.toString() + ChatColor.GREEN.toString() + "* " + entity.getCustomName() + ChatColor.GREEN.toString() + " *";
    	Stats stat = Stats.getRandomStats(lvl,tier);
    	stat.atk *= 2.5;
    	stat.hp *= 2.5;
    	stat.def *= 2.5;
        stat.spd *= 2.5;
    	//TODO Named Elites 5* as Strong? Bosses 10?
        entity.setCustomName(name);
        entity.getBukkitEntity().setMetadata("elite", new FixedMetadataValue(DungeonRealms.getInstance(), 1));
        entity.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), stat.hp));
        entity.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), HealthHandler.getInstance().getMonsterMaxHPOnSpawn((LivingEntity) entity.getBukkitEntity())));
        HealthHandler.getInstance().setMonsterHPLive((LivingEntity) entity.getBukkitEntity(), HealthHandler.getInstance().getMonsterMaxHPLive((LivingEntity) entity.getBukkitEntity()));
        entity.getBukkitEntity().setMetadata("def", new FixedMetadataValue(DungeonRealms.getInstance(), stat.def));
        entity.getBukkitEntity().setMetadata("attack", new FixedMetadataValue(DungeonRealms.getInstance(), stat.atk));
        entity.getBukkitEntity().setMetadata("spd", new FixedMetadataValue(DungeonRealms.getInstance(), stat.spd));
    }
    
    public static void setMonsterRandomStats(Entity entity, int lvl, int tier) {
        Stats stat = Stats.getRandomStats(lvl, tier);
        entity.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), stat.hp));
        entity.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), HealthHandler.getInstance().getMonsterMaxHPOnSpawn((LivingEntity) entity.getBukkitEntity())));
        HealthHandler.getInstance().setMonsterHPLive((LivingEntity) entity.getBukkitEntity(), HealthHandler.getInstance().getMonsterMaxHPLive((LivingEntity) entity.getBukkitEntity()));
        entity.getBukkitEntity().setMetadata("def", new FixedMetadataValue(DungeonRealms.getInstance(), stat.def));
        entity.getBukkitEntity().setMetadata("attack", new FixedMetadataValue(DungeonRealms.getInstance(), stat.atk));
        entity.getBukkitEntity().setMetadata("spd", new FixedMetadataValue(DungeonRealms.getInstance(), stat.spd));
    }

}