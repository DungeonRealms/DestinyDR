package net.dungeonrealms.game.world.spawning;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.BasicEntityBlaze;
import net.dungeonrealms.game.world.entities.types.monsters.BasicEntitySkeleton;
import net.dungeonrealms.game.world.entities.types.monsters.BasicMageMonster;
import net.dungeonrealms.game.world.entities.types.monsters.BasicMeleeMonster;
import net.dungeonrealms.game.world.entities.types.monsters.EntityBandit;
import net.dungeonrealms.game.world.entities.types.monsters.EntityFireImp;
import net.dungeonrealms.game.world.entities.types.monsters.EntityGolem;
import net.dungeonrealms.game.world.entities.types.monsters.EntityPirate;
import net.dungeonrealms.game.world.entities.types.monsters.EntityRangedPirate;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRMagma;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRPigman;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRSilverfish;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRSpider;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRWitherSkeleton;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Sep 28, 2015
 */
public class SpawningMechanics implements GenericMechanic {

    public static ArrayList<MobSpawner> ALLSPAWNERS = new ArrayList<>();
    public static ArrayList<String> SPAWNER_CONFIG = new ArrayList<>();
    public static ArrayList<MobSpawner> BanditTroveSpawns = new ArrayList<>();
    private static SpawningMechanics instance;


    public static void initSpawners() {
        ALLSPAWNERS.forEach(MobSpawner::init);
    }

    public static void killAll() {
        ALLSPAWNERS.stream().forEach(mobSpawner -> {
            mobSpawner.kill();
            mobSpawner.armorstand.getBukkitEntity().remove();
            mobSpawner.armorstand.getWorld().removeEntity(mobSpawner.armorstand);
        });
    }

    public static void loadSpawners() {
    	Utils.log.info("LOADING ALL DUNGEON REALMS MONSTERS...");
        SPAWNER_CONFIG = (ArrayList<String>) DungeonRealms.getInstance().getConfig().getStringList("spawners");
        for (String line : SPAWNER_CONFIG) {
            if (line == null || line.equalsIgnoreCase("null"))
                continue;
            String[] coords = line.split("=")[0].split(",");
            double x, y, z;
            x = Double.parseDouble(coords[0]);
            y = Double.parseDouble(coords[1]);
            z = Double.parseDouble(coords[2]);
            String tierString = line.substring(line.indexOf(":"), line.indexOf(";"));
            tierString = tierString.substring(1);
            int tier = Integer.parseInt(tierString);
            String stringAmount = line.split(";")[1].replace("-", "");
            stringAmount = stringAmount.replace("+", "");
            int spawnAmount = Integer.parseInt(stringAmount);
            String monster = line.split("=")[1].split(":")[0];
            String spawnRange = String.valueOf(line.charAt(line.length() - 1));
            MobSpawner spawner;
            if(spawnRange.equalsIgnoreCase("+"))
             spawner = new MobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, ALLSPAWNERS.size(), "high");
            else
             spawner = new MobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, ALLSPAWNERS.size(), "low");
            ALLSPAWNERS.add(spawner);
        }
        ArrayList<String> BANDIT_CONFIG = (ArrayList<String>) DungeonRealms.getInstance().getConfig().getStringList("banditTrove");
        Utils.log.info("LOADING DUNGEON SPAWNS...");
        for(String line : BANDIT_CONFIG){
            if (line == null || line.equalsIgnoreCase("null"))
                continue;
            String[] coords = line.split("=")[0].split(",");
            double x, y, z;
            x = Double.parseDouble(coords[0]);
            y = Double.parseDouble(coords[1]);
            z = Double.parseDouble(coords[2]);
            String tierString = line.substring(line.indexOf(":"), line.indexOf(";"));
            tierString = tierString.substring(1);
            int tier = Integer.parseInt(tierString);
            String stringAmount = line.split(";")[1].replace("-", "");
            stringAmount = stringAmount.replace("+", "");
            int spawnAmount = Integer.parseInt(stringAmount);
            String monster = line.split("=")[1].split(":")[0];
            MobSpawner spawner;
            spawner = new MobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, BanditTroveSpawns.size(), "high");
            BanditTroveSpawns.add(spawner);
        }
        Utils.log.info("FINISHED LOADING DUNGEON SPAWNS");
        SpawningMechanics.initSpawners();
        Bukkit.getWorlds().get(0).getEntities().forEach(entity -> {
            ((CraftEntity) entity).getHandle().damageEntity(DamageSource.GENERIC, 20f);
            entity.remove();
        });
        Bukkit.getWorlds().get(0).getLivingEntities().forEach(entity -> ((CraftEntity) entity).getHandle().damageEntity(DamageSource.GENERIC, 20f));
    }

    public static void loadSpawner(String line) {
        String[] coords = line.split("=")[0].split(",");
        double x, y, z;
        x = Double.parseDouble(coords[0]);
        y = Double.parseDouble(coords[1]);
        z = Double.parseDouble(coords[2]);
        String tierString = line.substring(line.indexOf(":"), line.indexOf(";"));
        tierString = tierString.substring(1);
        int tier = Integer.parseInt(tierString);
        int spawnAmount = Integer.parseInt(line.split(";")[1]);
        String monster = line.split("=")[1].split(":")[0];
        String spawnRange = String.valueOf(line.charAt(line.length() - 1));
        MobSpawner spawner;
        if(spawnRange.equalsIgnoreCase("+"))
         spawner = new MobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, ALLSPAWNERS.size(), "high");
        else
         spawner = new MobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, ALLSPAWNERS.size(), "low");
        ALLSPAWNERS.add(spawner);
        spawner.init();
    }
    
    public static void remove(MobSpawner mobSpawner) {
        ALLSPAWNERS.remove(mobSpawner);
    }

    /**
     * @param monsEnum
     * @return
     */
    public static Entity getMob(World world, int tier, EnumMonster monsEnum) {
        EnumEntityType type = EnumEntityType.HOSTILE_MOB;
        Entity entity;
        switch (monsEnum) {
            case Bandit:
                entity = new EntityBandit(world, tier, type);
                break;
            case RangedPirate:
                entity = new EntityRangedPirate(world, type, tier);
                break;
            case Pirate:
                entity = new EntityPirate(world, EnumMonster.Pirate, tier);
                break;
            case MayelPirate:
                entity = new EntityPirate(world, EnumMonster.MayelPirate, tier);
                break;
            case FireImp:
                entity = new EntityFireImp(world, tier, type);
                break;
            case Troll1:
            case Troll:
                entity = new BasicMeleeMonster(world, EnumMonster.Troll, tier);
                break;
            case Goblin:
                entity = new BasicMeleeMonster(world, EnumMonster.Goblin, tier);
                break;
            case Mage:
                entity = new BasicMageMonster(world, EnumMonster.Mage, tier);
                break;
            case Spider:
            case Spider1:
            case Spider2:
                entity = new DRSpider(world, EnumMonster.Spider, tier);
                break;
            case Golem:
                entity = new EntityGolem(world, tier, type);
                break;
            case Naga:
            	if(new Random().nextBoolean())
            		entity = new BasicMageMonster(world, EnumMonster.Naga, tier);
            	else
            		entity = new BasicMeleeMonster(world, EnumMonster.Naga, tier);
                break;
            case Tripoli1:
            case Tripoli:
                entity = new BasicMeleeMonster(world, EnumMonster.Tripoli, tier);
                break;
            case Blaze:
                entity = new BasicEntityBlaze(world, EnumMonster.Blaze, tier);
                break;
            case Skeleton1:
            case Skeleton:
                entity = new BasicEntitySkeleton(world, tier);
                break;
            case Wither:
                entity = new DRWitherSkeleton(world, EnumMonster.Wither, tier);
                break;
            case MagmaCube:
                entity = new DRMagma(world, EnumMonster.MagmaCube, tier);
                break;
            case Daemon:
                entity = new DRPigman(world, EnumMonster.Daemon, tier);
                break;
            case Silverfish:
                entity = new DRSilverfish(world, EnumMonster.Silverfish, tier);
                break;
            case SpawnOfInferno:
                entity = new DRMagma(world, EnumMonster.SpawnOfInferno, tier);
                ((DRMagma) entity).setSize(4);
                break;
            case GreaterAbyssalDemon:
                entity = new DRSilverfish(world, EnumMonster.GreaterAbyssalDemon, tier);
                break;
            case Monk:
            	entity = new BasicMeleeMonster(world, EnumMonster.Monk, tier);
            	break;
            case Lizardman:
            	entity = new BasicMeleeMonster(world, EnumMonster.Lizardman, tier);
            	break;
            case Zombie:
            	entity = new BasicMeleeMonster(world, EnumMonster.Zombie, tier);
            	break;
            default:
                Utils.log.info("[SPAWNING] Tried to create " + monsEnum.idName + " but it has failed.");
                return null;
        }
        return entity;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
        loadSpawners();
    }

    @Override
    public void stopInvocation() {
        killAll();
        Bukkit.getWorlds().get(0).getEntities().forEach(entity -> {
           ((CraftWorld)entity.getWorld()).getHandle().removeEntity(((CraftEntity) entity).getHandle());
            entity.remove();
        });
        Bukkit.getWorlds().get(0).getLivingEntities().forEach(entity -> {
            ((CraftWorld)entity.getWorld()).getHandle().removeEntity(((CraftEntity) entity).getHandle());
            entity.remove();
        });
    }

    /**
     * @return
     */
    public static SpawningMechanics getInstance() {
        if (instance == null)
            instance = new SpawningMechanics();
        return instance;
    }
}
