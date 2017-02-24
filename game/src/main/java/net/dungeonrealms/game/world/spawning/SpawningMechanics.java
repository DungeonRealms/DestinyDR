package net.dungeonrealms.game.world.spawning;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.command.CommandSpawner;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.base.*;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.InfernalLordsGuard;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.*;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.RangedSkeleton;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.RangedWitherSkeleton;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.RangedZombie;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff.BasicEntityBlaze;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff.StaffSkeleton;
import net.dungeonrealms.game.world.item.*;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.modifiers.WeaponModifiers;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
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

    // CUSTOM_MOB_NAME, LIST OF DROP TABLE RULES (loot mechanics)
    public static Map<String, List<String>> customMobLootTables = new HashMap<>();

    private static void initAllSpawners() {
        ALLSPAWNERS.forEach(BaseMobSpawner::init);
        ELITESPAWNERS.forEach(EliteMobSpawner::init);
    }

    private static void killAll() {
        ALLSPAWNERS.forEach(mobSpawner -> {
            mobSpawner.kill();
            mobSpawner.getArmorstand().getBukkitEntity().remove();
            mobSpawner.getArmorstand().getWorld().removeEntity(mobSpawner.getArmorstand());
        });
        ELITESPAWNERS.forEach(eliteMobSpawner -> {
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

            //coords=type*(name):tier;amount<high/low (lvl range)>@SpawnTime#rangeMin-rangMax$
            //x,y,z=type*(Name):4;1-@400#1-1$
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
            String spawnRange = String.valueOf(line.charAt(line.indexOf("@") - 1));
            int spawnDelay = Integer.parseInt(line.substring(line.indexOf("@") + 1, line.indexOf("#")));
            int initialDelay = spawnDelay;
            if (spawnDelay < 25) {
                if (!isElite) {
                    switch (tier) {
                        case 1:
                            spawnDelay = 40;
                            break;
                        case 2:
                            spawnDelay = 80;
                            break;
                        case 3:
                            spawnDelay = 105;
                            break;
                        case 4:
                            spawnDelay = 145;
                            break;
                        case 5:
                            spawnDelay = 200;
                            break;
                        default:
                            spawnDelay = 70;
                            break;
                    }
                } else {
                    switch (tier) {
                        case 1:
                            spawnDelay = 300;
                            break;
                        case 2:
                            spawnDelay = 500;
                            break;
                        case 3:
                            spawnDelay = 750;
                            break;
                        case 4:
                            spawnDelay = 1000;
                            break;
                        case 5:
                            spawnDelay = 1500;
                            break;
                        default:
                            spawnDelay = 500;
                            break;
                    }
                }
            }
            spawnDelay += spawnDelay / 10;
            String locationRange[] = line.substring(line.indexOf("#") + 1, line.lastIndexOf("$")).split("-");

            int minXZ = Integer.parseInt(locationRange[0]);
            int maxXZ = Integer.parseInt(locationRange[1]);

            MobSpawner spawner;
            if (!isElite) {
                if (spawnRange.equalsIgnoreCase("+")) {
                    spawner = new BaseMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, ALLSPAWNERS.size(), "high", spawnDelay, minXZ, maxXZ);
                } else {
                    spawner = new BaseMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount, ALLSPAWNERS.size(), "low", spawnDelay, minXZ, maxXZ);
                }
                ALLSPAWNERS.add((BaseMobSpawner) spawner);
            } else {
                if (spawnRange.equalsIgnoreCase("+")) {
                    spawner = new EliteMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, ELITESPAWNERS.size(), "high", spawnDelay, minXZ, maxXZ);
                } else {
                    spawner = new EliteMobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, ELITESPAWNERS.size(), "low", spawnDelay, minXZ, maxXZ);
                }
                ELITESPAWNERS.add((EliteMobSpawner) spawner);
            }

            //Delay to save instead of the randomized delay.
            spawner.setInitialRespawnDelay(initialDelay);
            //Theres some more data at the end.
            String extraData = !line.endsWith("$") ? line.substring(line.indexOf("$") + 1) : "";

            if (extraData.length() > 1) {
                //Get extra data here..
                if (extraData.contains("@WEP@")) {
                    String weaponData = extraData.substring(extraData.indexOf("@WEP@") + 5, extraData.lastIndexOf("@WEP@"));
                    Bukkit.getLogger().info("Weapon data: " + weaponData);

                    Item.ItemType itemType = Item.ItemType.getByName(weaponData);

                    if (itemType == null) {
                        Bukkit.getLogger().info("Invalid weapon type: " + weaponData);
                    } else {
                        spawner.setWeaponType(itemType.name());
                    }

                    extraData = !extraData.endsWith("@WEP@") ? extraData.substring(extraData.lastIndexOf("@WEP@") + 4) : extraData;
                }

                if (extraData.contains("@ELEM@")) {
                    String elemental = extraData.substring(extraData.indexOf("@ELEM@") + 6, extraData.lastIndexOf("@ELEM@"));


                    double chance = 100;
                    if (elemental.contains("%")) {
                        String[] args = elemental.split("%");
                        chance = Double.parseDouble(args[1]);
                        elemental = args[0];
                    }
                    if (elemental == null) {
                        Bukkit.getLogger().info("Invalid element type: " + elemental);
                    } else {
                        spawner.setElementalDamage(elemental);
                        spawner.setElementChance(chance);
                    }
                }


            }
        }
        SpawningMechanics.initAllSpawners();
        Bukkit.getWorlds().get(0).getEntities().forEach(entity -> {
            ((CraftEntity) entity).getHandle().damageEntity(DamageSource.GENERIC, 20f);
            entity.remove();
        });
        Bukkit.getWorlds().get(0).getLivingEntities().forEach(entity -> ((CraftEntity) entity).getHandle().damageEntity(DamageSource.GENERIC, 20f));
    }

    public void loadCustomMobDrops() {
        int count = 0;
        try {
            Utils.log.info("LOADING ALL DUNGEON REALMS CUSTOM MOB DROPS...");
            for (File f : new File("plugins/DungeonRealms/custom_mobs").listFiles()) {
                List<String> lmsg_template = new ArrayList<>();
                String tn = f.getName();
                if (tn.endsWith(".loot")) {
                    BufferedReader reader = new BufferedReader(new FileReader(f));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        lmsg_template.add(line);
                    }
                    reader.close();
                }
                Utils.log.info(tn + " -> " + lmsg_template);
                customMobLootTables.put(tn.replaceAll(".loot", ""), lmsg_template);
                count++;
            }

            Utils.log.info(count + " CUSTOM MOB DROP profiles have been LOADED.");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Deprecated
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
        if (spawnDelay < 25) {
            switch (tier) {
                case 1:
                    spawnDelay = 35;
                    break;
                case 2:
                    spawnDelay = 75;
                    break;
                case 3:
                    spawnDelay = 105;
                    break;
                case 4:
                    spawnDelay = 145;
                    break;
                case 5:
                    spawnDelay = 200;
                    break;
                default:
                    spawnDelay = 70;
                    break;
            }
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

    public static void remove(MobSpawner mobSpawner) {
        if (mobSpawner instanceof BaseMobSpawner)
            ALLSPAWNERS.remove(mobSpawner);
        else if (mobSpawner instanceof EliteMobSpawner) {
            ELITESPAWNERS.remove(mobSpawner);
        }

        if (mobSpawner.getEditHologram() != null)
            mobSpawner.getEditHologram().delete();

        CommandSpawner.shownMobSpawners.remove(mobSpawner.getLoc());
    }

    public static Entity getEliteMob(World world, int tier, EnumNamedElite elite) {
        EnumEntityType type = EnumEntityType.HOSTILE_MOB;
        Entity entity;
        switch (elite) {
            case MITSUKI:
            case COPJAK:
            case GREEDKING:
                entity = new MeleeZombie(world, EnumMonster.Bandit, tier);
                break;
            case MOTHEROFDOOM:
                entity = new LargeSpider(world, tier, EnumMonster.Spider1);
                break;
            case LORD_TAYLOR:
                entity = new MeleeWitherSkeleton(world, tier, EnumMonster.Undead, type);
                break;
            case IMPATHEIMPALER:
                entity = new MeleeWitherSkeleton(world, tier, EnumMonster.Goblin, type);
                break;
            case KILATAN:
                entity = new StaffSkeleton(world, EnumMonster.Daemon, tier);
                break;
            case DURANOR:
            case ZION:
                entity = new MeleeSkeleton(world, EnumMonster.Monk, type, tier);
                break;
            case BLAYSHAN:
                entity = new MeleeZombie(world, EnumMonster.Naga, tier);
                break;
            case ACERON:
                entity = new RangedWitherSkeleton(world, EnumMonster.Undead, type, tier);
                break;
            default:
                entity = null;
                break;
        }
        return entity;
    }

    /**
     * @param monsEnum
     * @return
     */
    public static Entity getMob(World world, int tier, EnumMonster monsEnum) {
        EnumEntityType type = EnumEntityType.HOSTILE_MOB;
        Entity entity;
        switch (monsEnum) {
            case InfernalEndermen:
                entity = new InfernalEndermen(world, tier, false);
                break;
            case LordsGuard:
                entity = new InfernalLordsGuard(world, tier);
                break;
            case Bandit:
            case Bandit1:
                switch (new Random().nextInt(4)) {
                    case 0:
                        entity = new RangedSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    case 1:
                        entity = new MeleeZombie(world, monsEnum, tier);
                        break;
                    case 2:
                        entity = new MeleeSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    case 3:
                        entity = new MeleeZombie(world, monsEnum, tier);
                        break;
                    case 4:
                        entity = new MeleeSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    default:
                        entity = new MeleeSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                }
                break;
            case RangedPirate:
                entity = new RangedSkeleton(world, monsEnum, type, tier);
                break;
            case Pirate:
                switch (new Random().nextInt(2)) {
                    case 0:
                        entity = new RangedSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    case 1:
                        entity = new MeleeSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    default:
                        entity = new MeleeSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                }
                break;
            case MayelPirate:
                switch (new Random().nextInt(3)) {
                    case 0:
                        entity = new StaffSkeleton(world, monsEnum, tier);
                        break;
                    case 1:
                        entity = new MeleeSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
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
                        entity = new MeleeSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
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
                entity = new StaffSkeleton(world, monsEnum, tier);
                break;
            case Troll1:
            case Troll:
                entity = new MeleeZombie(world, EnumMonster.Troll, tier);
                break;
            case Goblin:
                entity = new MeleeZombie(world, EnumMonster.Goblin, tier);
                break;
            case Mage:
                entity = new StaffSkeleton(world, EnumMonster.Mage, tier);
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
                        entity = new StaffSkeleton(world, EnumMonster.Naga, tier);
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
                        entity = new RangedWitherSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    default:
                        entity = new RangedWitherSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                }
                break;
            case Skeleton1:
            case Skeleton:
                switch (new Random().nextInt(3)) {
                    case 0:
                        entity = new RangedSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    case 1:
                        entity = new MeleeSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    case 2:
                        entity = new MeleeSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    case 3:
                        entity = new RangedSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                    default:
                        entity = new MeleeSkeleton(world, monsEnum, EnumEntityType.HOSTILE_MOB, tier);
                        break;
                }
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
                entity = new StaffSkeleton(world, EnumMonster.Daemon2, tier);
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
            case Enderman:
                entity = new MeleeEnderman(world, tier);
                break;
            case Ocelot:
                entity = new EntityOcelot(world);
                break;
            default:
                Utils.log.info("[SPAWNING] Tried to create " + monsEnum.idName + " but it has failed.");
                return null;
        }
        return entity;
    }

    public static void rollElement(Entity ent, EnumMonster enumMonster) {
        if (enumMonster.elementalChance > 0) {
            if (new Random().nextInt(100) < enumMonster.elementalChance) {
                String element = enumMonster.getRandomElement();
                GameAPI.setMobElement(ent, element);
            }
        }
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
        loadBaseSpawners();
        loadCustomMobDrops();
    }

    @Override
    public void stopInvocation() {
        killAll();
        Bukkit.getWorlds().get(0).getEntities().forEach(entity -> {
            ((CraftWorld) entity.getWorld()).getHandle().removeEntity(((CraftEntity) entity).getHandle());
            entity.remove();
        });
        Bukkit.getWorlds().get(0).getLivingEntities().forEach(entity -> {
            ((CraftWorld) entity.getWorld()).getHandle().removeEntity(((CraftEntity) entity).getHandle());
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

    public static List<MobSpawner> getAllSpawners() {
        List<MobSpawner> spawners = Lists.newArrayList();
        ALLSPAWNERS.forEach(spawners::add);
        ELITESPAWNERS.forEach(spawners::add);
        return spawners;
    }

    public static ArrayList<BaseMobSpawner> getALLSPAWNERS() {
        return ALLSPAWNERS;
    }

    public static ArrayList<EliteMobSpawner> getELITESPAWNERS() {
        return ELITESPAWNERS;
    }
}
