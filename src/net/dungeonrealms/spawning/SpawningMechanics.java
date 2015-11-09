package net.dungeonrealms.spawning;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.*;
import net.dungeonrealms.entities.types.monsters.base.*;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;

import java.util.ArrayList;

/**
 * Created by Chase on Sep 28, 2015
 */
public class SpawningMechanics implements GenericMechanic {

    private static ArrayList<MobSpawner> ALLSPAWNERS = new ArrayList<>();
    public static ArrayList<String> SPAWNER_CONFIG = new ArrayList<>();
    private static SpawningMechanics instance;


    public static void initSpawners() {
        ALLSPAWNERS.forEach(MobSpawner::init);
    }

    public static ArrayList<MobSpawner> getSpawners() {
        return ALLSPAWNERS;
    }

    public static void add(MobSpawner spawner) {
        ALLSPAWNERS.add(spawner);
    }

    public static void killAll() {
        ALLSPAWNERS.stream().forEach(mobSpawner -> {
            mobSpawner.kill();
            mobSpawner.armorstand.getBukkitEntity().remove();
            mobSpawner.armorstand.getWorld().removeEntity(mobSpawner.armorstand);
        });
    }

    public static void loadSpawners() {
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
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), SpawningMechanics::initSpawners, 0, 4 * 20L);
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
        add(spawner);
        spawner.init();
    }

    /**
     * @param i
     */
    public static void remove(int i) {
        ALLSPAWNERS.remove(i);
    }

    /**
     * @param mobSpawner
     */
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
            case FireImp:
                entity = new EntityFireImp(world, tier, type);
                break;
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
                entity = new DRSpider(world, EnumMonster.Spider, tier);
                break;
            case Golem:
                entity = new EntityGolem(world, tier, type);
                break;
            case Naga:
                entity = new BasicMageMonster(world, EnumMonster.Naga, tier);
                break;
            case Tripoli:
                entity = new BasicMeleeMonster(world, EnumMonster.Tripoli, tier);
                break;
            case Blaze:
                entity = new BasicEntityBlaze(world, EnumMonster.Blaze, tier);
                break;
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
            case SpawnOfInferno:
                entity = new DRMagma(world, EnumMonster.SpawnOfInferno, tier);
                ((DRMagma) entity).setSize(4);
                break;
            case GreaterAbyssalDemon:
                entity = new DRSilverfish(world, EnumMonster.GreaterAbyssalDemon, tier);
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
