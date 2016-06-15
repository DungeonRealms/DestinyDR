package net.dungeonrealms.game.world.spawning;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.BowMobs.RangedSkeleton;
import net.dungeonrealms.game.world.entities.types.monsters.BowMobs.RangedWitherSkeleton;
import net.dungeonrealms.game.world.entities.types.monsters.BowMobs.RangedZombie;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.MeleeMobs.*;
import net.dungeonrealms.game.world.entities.types.monsters.StaffMobs.BasicEntityBlaze;
import net.dungeonrealms.game.world.entities.types.monsters.StaffMobs.StaffSkeleton;
import net.dungeonrealms.game.world.entities.types.monsters.StaffMobs.StaffZombie;
import net.dungeonrealms.game.world.entities.types.monsters.base.*;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by Chase on Sep 28, 2015
 */
public class SpawningMechanics implements GenericMechanic {

    private static ArrayList<BaseMobSpawner> ALLSPAWNERS = new ArrayList<>();
    private static ArrayList<EliteMobSpawner> ELITESPAWNERS = new ArrayList<>();
    public static ArrayList<String> SPAWNER_CONFIG = new ArrayList<>();
    public static ArrayList<BaseMobSpawner> BanditTroveSpawns = new ArrayList<>();
    private static SpawningMechanics instance;


    private static void initAllSpawners() {
        ALLSPAWNERS.forEach(BaseMobSpawner::init);
        ELITESPAWNERS.forEach(EliteMobSpawner::init);
    }

    private static void killAll() {
        ALLSPAWNERS.stream().forEach(mobSpawner -> {
            mobSpawner.kill();
            mobSpawner.getArmorstand().getBukkitEntity().remove();
            mobSpawner.getArmorstand().getWorld().removeEntity(mobSpawner.getArmorstand());
        });
        ELITESPAWNERS.stream().forEach(eliteMobSpawner -> {
            eliteMobSpawner.kill();
            eliteMobSpawner.getArmorstand().getBukkitEntity().remove();
            eliteMobSpawner.getArmorstand().getWorld().removeEntity(eliteMobSpawner.getArmorstand());
        });
    }

    private static void loadBaseSpawners() {
    	Utils.log.info("LOADING ALL DUNGEON REALMS MONSTERS...");
        SPAWNER_CONFIG = (ArrayList<String>) DungeonRealms.getInstance().getConfig().getStringList("spawners");
        for (String line : SPAWNER_CONFIG) {
            if (line == null || line.equalsIgnoreCase("null")) {
                continue;
            }
            boolean isElite = false;
            if (line.contains("*")) {
                isElite = true;
            }
            String[] coords = line.split("=")[0].split(",");
            double x, y, z;
            x = Double.parseDouble(coords[0]);
            y = Double.parseDouble(coords[1]);
            z = Double.parseDouble(coords[2]);
            String tierString = line.substring(line.indexOf(":"), line.indexOf(";"));
            tierString = tierString.substring(1);
            int tier = Integer.parseInt(tierString);
            Character strAmount = line.charAt(line.indexOf(";") + 1);
            int spawnAmount = Integer.parseInt(String.valueOf(strAmount));
            String monster = line.split("=")[1].split(":")[0];
            String spawnRange = String.valueOf(line.charAt(line.lastIndexOf("@") - 1));
            int spawnDelay = Integer.parseInt(line.substring(line.lastIndexOf("@") + 1, line.indexOf("#")));
            if (spawnDelay < 20) {
                if (!isElite) {
                    spawnDelay = 20;
                } else {
                    spawnDelay = 60;
                }
            }
            String locationRange[] = line.substring(line.indexOf("#") + 1, line.lastIndexOf("$")).split("-");
            int minXZ = Integer.parseInt(locationRange[0]);
            int maxXZ = Integer.parseInt(locationRange[1]);
            if (!isElite) {
                BaseMobSpawner spawner;
                if (spawnRange.equalsIgnoreCase("+")) {
                    spawner = new BaseMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, ALLSPAWNERS.size(), "high", spawnDelay, minXZ, maxXZ);
                } else {
                    spawner = new BaseMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, ALLSPAWNERS.size(), "low", spawnDelay, minXZ, maxXZ);
                }
                ALLSPAWNERS.add(spawner);
            } else {
                //TODO: Dangerous code!!! REMOVE BEFORE RELEASE!!!
                spawnDelay = 60;
                EliteMobSpawner spawner;
                if (spawnRange.equalsIgnoreCase("+")) {
                    spawner = new EliteMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, ELITESPAWNERS.size(), "high", spawnDelay, minXZ, maxXZ);
                } else {
                    spawner = new EliteMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, ELITESPAWNERS.size(), "low", spawnDelay, minXZ, maxXZ);
                }
                ELITESPAWNERS.add(spawner);
            }
        }
        SpawningMechanics.initAllSpawners();
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
        Character strAmount = line.charAt(line.indexOf(";") + 1);
        int spawnAmount = Integer.parseInt(String.valueOf(strAmount));
        String monster = line.split("=")[1].split(":")[0];
        String spawnRange = String.valueOf(line.charAt(line.lastIndexOf("@") - 1));
        BaseMobSpawner spawner;
        int spawnDelay = Integer.parseInt(line.substring(line.lastIndexOf("@") + 1, line.indexOf("#")));
        if (spawnDelay < 20) {
            spawnDelay = 20;
        }
        String locationRange[] = line.substring(line.indexOf("#") + 1, line.indexOf("$")).split("-");
        int minXZ = Integer.parseInt(locationRange[0]);
        int maxXZ = Integer.parseInt(locationRange[1]);
        if (spawnRange.equalsIgnoreCase("+")) {
            spawner = new BaseMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, ALLSPAWNERS.size(), "high", spawnDelay, minXZ, maxXZ);
        } else {
            spawner = new BaseMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, ALLSPAWNERS.size(), "low", spawnDelay, minXZ, maxXZ);
        }
        ALLSPAWNERS.add(spawner);
        spawner.init();
    }
    
    public static void remove(BaseMobSpawner mobSpawner) {
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
            case Bandit1:
                switch (new Random().nextInt(3)) {
                    case 0:
                        entity = new StaffSkeleton(world, monsEnum, tier);
                        break;
                    case 1:
                        entity = new MeleeZombie(world, monsEnum, tier);
                        break;
                    case 2:
                        entity = new RangedSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    default:
                        entity = new RangedSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                }
                break;
            case RangedPirate:
                entity = new RangedSkeleton(world, monsEnum, type, tier);
                break;
            case Pirate:
                switch (new Random().nextInt(3)) {
                    case 0:
                        entity = new StaffSkeleton(world, monsEnum, tier);
                        break;
                    case 1:
                        entity = new MeleeZombie(world, monsEnum, tier);
                        break;
                    case 2:
                        entity = new RangedSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    default:
                        entity = new RangedSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                }
                break;
            case MayelPirate:
                switch (new Random().nextInt(3)) {
                    case 0:
                        entity = new StaffSkeleton(world, monsEnum, tier);
                        break;
                    case 1:
                        entity = new MeleeZombie(world, monsEnum, tier);
                        break;
                    case 2:
                        entity = new RangedSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    default:
                        entity = new RangedSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                }
                break;
            case Acolyte:
                switch (new Random().nextInt(3)) {
                    case 0:
                        entity = new StaffSkeleton(world, monsEnum, tier);
                        break;
                    case 1:
                        entity = new MeleeZombie(world, monsEnum, tier);
                        break;
                    case 2:
                        entity = new RangedSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    default:
                        entity = new RangedSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                }
                break;
            case FireImp:
                entity = new StaffZombie(world, monsEnum, tier);
                break;
            case Troll1:
            case Troll:
                entity = new MeleeZombie(world, EnumMonster.Troll, tier);
                break;
            case Goblin:
                entity = new MeleeZombie(world, EnumMonster.Goblin, tier);
                break;
            case Mage:
                entity = new StaffZombie(world, EnumMonster.Mage, tier);
                break;
            case Spider1:
                entity = new LargeSpider(world, tier, EnumMonster.Spider1);
                break;
            case Spider2:
                entity = new SmallSpider(world, tier, EnumMonster.Spider2);
                break;
            case Golem:
                entity = new MeleeGolem(world, tier, type);
                break;
            case Naga:
                switch (new Random().nextInt(3)) {
                    case 0:
                        entity = new StaffZombie(world, EnumMonster.Naga, tier);
                        break;
                    case 1:
                        entity = new MeleeZombie(world, EnumMonster.Naga, tier);
                        break;
                    case 2:
                        entity = new RangedZombie(world, EnumMonster.Naga, tier);
                        break;
                    default:
                        entity = new RangedZombie(world, EnumMonster.Naga, tier);
                        break;
                }
                break;
            case Tripoli1:
            case Tripoli:
                entity = new MeleeZombie(world, EnumMonster.Tripoli, tier);
                break;
            case Blaze:
                entity = new BasicEntityBlaze(world, EnumMonster.Blaze, tier);
                break;
            case Skeleton2:
            case FrozenSkeleton:
                switch (new Random().nextInt(3)) {
                    case 0:
                        entity = new RangedWitherSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    case 1:
                        entity = new MeleeWitherSkeleton(world, tier, monsEnum, EnumEntityType.HOSTILE_MOB);
                        break;
                    case 2:
                        entity = null;
                        //TODO: Staff Wither
                        break;
                    default:
                        Utils.log.info("[SPAWNING] Tried to create " + monsEnum.idName + " but it has failed.");
                        return null;
                }
                break;
            case Skeleton1:
            case Skeleton:
                entity = new RangedSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                break;
            case Wither:
                entity = new MeleeWitherSkeleton(world, tier, monsEnum, EnumEntityType.HOSTILE_MOB);
                break;
            case MagmaCube:
                entity = new DRMagma(world, EnumMonster.MagmaCube, tier);
                break;
            case Daemon:
                entity = new DRPigman(world, EnumMonster.Daemon, tier);
                break;
            case Daemon2:
                entity = new StaffZombie(world, EnumMonster.Daemon2, tier);
                break;
            case Silverfish:
                entity = new DRSilverfish(world, EnumMonster.Silverfish, tier);
                break;
            case SpawnOfInferno:
                entity = new DRMagma(world, EnumMonster.SpawnOfInferno, tier);
                break;
            case GreaterAbyssalDemon:
                entity = new DRSilverfish(world, EnumMonster.GreaterAbyssalDemon, tier);
                break;
            case Monk:
            	entity = new MeleeZombie(world, EnumMonster.Monk, tier);
            	break;
            case Lizardman:
            	entity = new MeleeZombie(world, EnumMonster.Lizardman, tier);
            	break;
            case Zombie:
            	entity = new MeleeZombie(world, EnumMonster.Zombie, tier);
            	break;
            case Wolf:
                entity = new DRWolf(world, EnumMonster.Wolf, tier);
                break;
            case Undead:
                entity = new MeleeZombie(world, EnumMonster.Undead, tier);
                break;
            case Witch:
                entity = new DRWitch(world, EnumMonster.Witch, tier);
                break;
            case Pig:
                entity = new EntityPig(world);
                break;
            case Bat:
                entity = new EntityBat(world);
                break;
            case Cow:
                entity = new EntityCow(world);
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
        loadBaseSpawners();
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
        if (instance == null) {
            instance = new SpawningMechanics();
        }
        return instance;
    }

    public List<BaseMobSpawner> getChunkMobBaseSpawners(Chunk chunk) {
        return ALLSPAWNERS.stream().filter(mobSpawner -> mobSpawner.getLoc().getChunk().equals(chunk)).collect(Collectors.toList());
    }

    public List<EliteMobSpawner> getChunkEliteMobSpawners(Chunk chunk) {
        return ELITESPAWNERS.stream().filter(mobSpawner -> mobSpawner.getLocation().getChunk().equals(chunk)).collect(Collectors.toList());
    }


    public static ArrayList<BaseMobSpawner> getALLSPAWNERS() {
        return ALLSPAWNERS;
    }

    public static ArrayList<EliteMobSpawner> getELITESPAWNERS() {
        return ELITESPAWNERS;
    }
}
