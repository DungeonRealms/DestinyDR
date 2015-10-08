package net.dungeonrealms.entities.utils;

import java.util.Random;

import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.DungeonRealms;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.Entity;

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
                    lvlhp = (lvl * 10) + (random.nextInt(30) - 20);
                    lvlatk = (lvl + 5) + (random.nextInt(5) - 3);
                    lvlspd = (lvl + 5) + (random.nextInt(5) - 3);
                    break;
                case 2:
                    lvldef = (lvl + 20) + (random.nextInt(20) - 10);
                    lvlhp = (lvl * 15) + (random.nextInt(50) - 35);
                    lvlatk = (lvl + 20) + (random.nextInt(20) - 10);
                    lvlspd = (lvl + 20) + (random.nextInt(20) - 10);
                    break;
                case 3:
                    lvldef = (lvl + 40) + (random.nextInt(35) - 20);
                    lvlhp = (lvl * 20) + (random.nextInt(75) - 50);
                    lvlatk = (lvl + 40) + (random.nextInt(35) - 20);
                    lvlspd = (lvl + 40) + (random.nextInt(35) - 20);
                    break;
                case 4:
                    lvldef = (lvl + 60) + (random.nextInt(55) - 35);
                    lvlhp = (lvl * 50) + (random.nextInt(100) - 70);
                    lvlatk = (lvl + 60) + (random.nextInt(55) - 35);
                    lvlspd = (lvl + 60) + (random.nextInt(55) - 35);
                    break;
                case 5:
                    lvldef = (lvl + 85) + (random.nextInt(80) - 50);
                    lvlhp = (lvl * 75) + (random.nextInt(150) - 100);
                    lvlatk = (lvl + 85) + (random.nextInt(80) - 50);
                    lvlspd = (lvl + 85) + (random.nextInt(80) - 50);
                    break;
                default:
                    lvldef = (lvl + 40) + (random.nextInt(35) - 20);
                    lvlhp = (lvl * 20) + (random.nextInt(75) - 50);
                    lvlatk = (lvl + 40) + (random.nextInt(35) - 20);
                    lvlspd = (lvl + 40) + (random.nextInt(35) - 20);
                    break;
            }
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

    public static void setMonsterElite(Entity ent,int lvl, int tier) {
    	String name = ChatColor.GREEN.toString() + "* " + ent.getCustomName() + ChatColor.GREEN.toString() + " *";
    	Stats stat = Stats.getRandomStats(lvl,tier);
    	stat.atk += 10;
    	stat.hp += 100;
    	stat.def += 20;
    	//TODO Idk some type of stat increase.
        ent.getBukkitEntity().setMetadata("elite", new FixedMetadataValue(DungeonRealms.getInstance(), 1));
        ent.setCustomName(name);
        setMonsterState(ent, stat);
    }

    public static void setMonsterState(Entity entity, Stats stat){
        entity.getBukkitEntity().setMetadata("hp", new FixedMetadataValue(DungeonRealms.getInstance(), stat.hp));
        entity.getBukkitEntity().setMetadata("def", new FixedMetadataValue(DungeonRealms.getInstance(), stat.def));
        entity.getBukkitEntity().setMetadata("atk", new FixedMetadataValue(DungeonRealms.getInstance(), stat.atk));
        entity.getBukkitEntity().setMetadata("spd", new FixedMetadataValue(DungeonRealms.getInstance(), stat.spd));
    }
    
    public static void setMonsterRandomStats(Entity entity, int lvl, int tier) {
        Stats stat = Stats.getRandomStats(lvl, tier);
        entity.getBukkitEntity().setMetadata("hp", new FixedMetadataValue(DungeonRealms.getInstance(), stat.hp));
        entity.getBukkitEntity().setMetadata("def", new FixedMetadataValue(DungeonRealms.getInstance(), stat.def));
        entity.getBukkitEntity().setMetadata("atk", new FixedMetadataValue(DungeonRealms.getInstance(), stat.atk));
        entity.getBukkitEntity().setMetadata("spd", new FixedMetadataValue(DungeonRealms.getInstance(), stat.spd));
    }

}