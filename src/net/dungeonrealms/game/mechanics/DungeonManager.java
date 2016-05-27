package net.dungeonrealms.game.mechanics;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.world.spawning.MobSpawner;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.minecraft.server.v1_8_R3.Entity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Nick on 10/19/2015.
 */
public class DungeonManager implements GenericMechanic {

    static DungeonManager instance = null;

    public static DungeonManager getInstance() {
        if (instance == null) {
            instance = new DungeonManager();
        }
        return instance;
    }

    public CopyOnWriteArrayList<DungeonObject> Dungeons = new CopyOnWriteArrayList<>();

    public DungeonObject getDungeon(World world) {
        for (DungeonObject dungeon : Dungeons) {
            if (world.getName().equalsIgnoreCase(dungeon.getWorldName()))
                return dungeon;
        }
        return null;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.ARCHBISHOPS;
    }

    @Override
    public void startInitialization() {
        Utils.log.info("[DUNGEONS] Loading Dungeon Mechanics ... STARTING");
        try {
            FileUtils.forceMkdir(new File(DungeonRealms.getInstance().getDataFolder() + File.separator + "/dungeons/"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () -> Dungeons.stream().forEach(dungeon -> dungeon.aliveMonsters.stream().forEach(mob -> {
            if (mob != null) {
                if (!mob.isAlive() || mob.dead) {
                    dungeon.aliveMonsters.remove(mob);
                }
            }
        })), 0, 10);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> Dungeons.stream().forEach(dungeonObject -> {
            int time = dungeonObject.getTime();
            int monstersAlive = dungeonObject.aliveMonsters.size();
            int maxAlive = dungeonObject.maxAlive;
            int NinetyPercent = (int) (maxAlive - (maxAlive * 1.9));
            if (!dungeonObject.canSpawnBoss && maxAlive > 0 && monstersAlive > 0)
                if ((maxAlive - monstersAlive) <= (maxAlive - NinetyPercent)) {
                    dungeonObject.canSpawnBoss = true;
                    dungeonObject.getPlayerList().stream().forEach(player -> player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD
                            + dungeonObject.type.getBossName() + ChatColor.WHITE + "]" + " " + ChatColor.YELLOW + "You really want to fight me?"));
                }
            if (dungeonObject.getPlayerList().size() <= 0 || Bukkit.getWorld(dungeonObject.worldName).getPlayers().size() <= 0) {
                removeInstance(dungeonObject);
                return;
            }
            switch (time) {
                // 46 minutes
                case 2760:
                    removeInstance(dungeonObject);
                    break;
                // 45 minutes
                case 2700:
                    dungeonObject.getPlayerList().stream().forEach(player -> player.sendMessage(ChatColor.WHITE
                            + "[" + ChatColor.GOLD + dungeonObject.type.getBossName() + ChatColor.WHITE + "]" + " "
                            + ChatColor.RED
                            + "This instance has reached it's max threshold, it will now terminate in (1) minute."));
                    break;
                // 35 minutes
                case 2100:
                    dungeonObject.getPlayerList().stream()
                            .forEach(player -> player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD
                                    + dungeonObject.type.getBossName() + ChatColor.WHITE + "]" + " " + ChatColor.RED
                                    + "This instance has reached (35) minute marker!"));
                    break;
                // 15 minutes
                case 900:
                    dungeonObject.getPlayerList().stream()
                            .forEach(player -> player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD
                                    + dungeonObject.type.getBossName() + ChatColor.WHITE + "]" + " " + ChatColor.RED
                                    + "This instance has reached (15) minute marker!"));
                    break;
            }
            dungeonObject.modifyTime(1);
            updateDungeonBoard(dungeonObject);
        }), 0, 20L);
        Utils.log.info("[DUNGEONS] Finished Loading Dungeon Mechanics ... OKAY");
    }

    @Override
    public void stopInvocation() {

    }

    /**
     * Update the dungeon scoreboard.
     *
     * @param dungeonObject
     * @since 1.0
     */
    public void updateDungeonBoard(DungeonObject dungeonObject) {
        dungeonObject.getPlayerList().forEach(player -> BountifulAPI.sendActionBar(player, ChatColor.AQUA + "Time: " + ChatColor.WHITE + ChatColor.GOLD
                + String.valueOf(dungeonObject.getTime() / 60) + "/45" + " " + ChatColor.AQUA + "Alive: " + ChatColor.WHITE + dungeonObject.aliveMonsters.size() + ChatColor.GRAY
                + "/" + ChatColor.RED + dungeonObject.maxAlive));
    }

    /**
     * Removes the instance dungeon from EVERYTHING.
     *
     * @param dungeonObject The dungeon object.
     * @since 1.0
     */
    public void removeInstance(DungeonObject dungeonObject) {
        dungeonObject.getPlayerList().forEach(player -> {
            if (player != null) {
                if (Bukkit.getPlayer(player.getUniqueId()) != null) {
                    if (API.getGamePlayer(player).isInDungeon()) {
                        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD + dungeonObject.type.getBossName() + ChatColor.WHITE + "]" + " " + ChatColor.RED + "This instance is will close!");
                        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                    }
                }
            }
        });
        Bukkit.getWorlds().remove(Bukkit.getWorld(dungeonObject.getWorldName()));
        Utils.log.info("[DUNGEONS] Removing world: " + dungeonObject.getWorldName() + " from worldList().");
        Bukkit.unloadWorld(dungeonObject.getWorldName(), false);
        Utils.log.info("[DUNGEONS] Unloading world: " + dungeonObject.getWorldName() + " in preparation for deletion!");
        Dungeons.remove(dungeonObject);
        try {
            FileUtils.deleteDirectory(new File(dungeonObject.worldName));
            Utils.log.info("[DUNGEONS] Deleted world: " + dungeonObject.getWorldName() + " final stage.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param type       DungeonType
     * @param playerList List of players to enter!
     * @since 1.0
     */
    public void createNewInstance(DungeonType type, List<Player> playerList) {
        DungeonObject dungeonObject = new DungeonObject(type, 0, playerList, "DUNGEON_" + String.valueOf(System.currentTimeMillis() / 1000L));
        Dungeons.add(dungeonObject);
        dungeonObject.load();
    }

    public boolean canCreateInstance() {
        return Dungeons.size() < 6;
    }

    /**
     * Will extract a players realm .zip to the correct folder.
     *
     * @param zipFile
     * @since 1.0
     */
    public void unZip(ZipFile zipFile, DungeonObject dungeonObject) {
        Utils.log.info("[DUNGEON] Unzipping instance for " + dungeonObject.getWorldName());
        new File(dungeonObject.getWorldName()).mkdir();
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(dungeonObject.getWorldName(), entry.getName());
                if (entry.isDirectory())
                    entryDestination.mkdirs();
                else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    out.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class DungeonObject {

        private DungeonType type;
        private Integer time;
        private List<Player> playerList;
        private String worldName;
        public CopyOnWriteArrayList<Entity> aliveMonsters = new CopyOnWriteArrayList<>();
        public boolean canSpawnBoss = false;
        public int tier;
        public int maxAlive = 0;

        public DungeonObject(DungeonType type, Integer time, List<Player> playerList, String worldName) {
            this.type = type;
            this.time = time;
            this.playerList = playerList;
            this.worldName = worldName;
            switch (type) {
                case BANDIT_TROVE:
                    tier = 1;
                    break;
                case VARENGLADE:
                    tier = 3;
                    break;
                case THE_INFERNAL_ABYSS:
                    tier = 4;
                    break;
                default:
                    break;
            }
        }

        public DungeonType getType() {
            return type;
        }

        public Integer getTime() {
            return time;
        }

        public List<Player> getPlayerList() {
            return playerList;
        }

        public String getWorldName() {
            return worldName;
        }

        public void modifyTime(int second) {
            time += second;
        }

        public void load() {
            try {
                unZip(new ZipFile(DungeonRealms.getInstance().getDataFolder() + type.getLocation()), this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            loadInWorld(getWorldName(), getPlayerList(), getType());
        }

        /**
         *
         */
        public void teleportPlayersOut() {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> getPlayerList().stream().filter(
                    p -> p != null && p.isOnline()).forEach(p -> p.teleport(Teleportation.Cyrennica)), 20 * 15);
            getPlayerList().stream().filter(p -> p != null && p.isOnline()).forEach(p -> p.sendMessage(ChatColor.YELLOW + "You will be teleported out in 15 seconds..."));
        }

        /**
         *
         */
        public void giveShards() {
            int shardsToGive = 100;

            switch (tier) {
                case 1:
                    shardsToGive = 750 + new Random().nextInt(150);
                    break;
                case 2:
                    shardsToGive = 900 + new Random().nextInt(300);
                    break;
                case 3:
                    shardsToGive = 1000 + new Random().nextInt(500);
                    break;
                case 4:
                    shardsToGive = 1200 + new Random().nextInt(750);
                    break;
                case 5:
                    shardsToGive = 1500 + new Random().nextInt(1000);
                    break;
            }

            for (Player p : Bukkit.getWorld(worldName).getPlayers()) {
                p.sendMessage(API.getTierColor(tier) + "You have gained " + ChatColor.UNDERLINE + shardsToGive
                        + " Portal Shards" + API.getTierColor(tier) + " for completing this Dungeon.");
                switch (tier) {
                    case 1:
                        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$INC, EnumData.PORTAL_SHARDS_T1,
                                shardsToGive, true);
                        break;
                    case 2:
                        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$INC, EnumData.PORTAL_SHARDS_T2,
                                shardsToGive, true);
                        break;
                    case 3:
                        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$INC, EnumData.PORTAL_SHARDS_T3,
                                shardsToGive, true);
                        break;
                    case 4:
                        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$INC, EnumData.PORTAL_SHARDS_T4,
                                shardsToGive, true);
                        break;
                    case 5:
                        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$INC, EnumData.PORTAL_SHARDS_T5,
                                shardsToGive, true);
                        break;
                    default:
                        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$INC, EnumData.PORTAL_SHARDS_T1,
                                shardsToGive, true);
                        break;

                }
            }
        }
    }

    /**
     * Loads the nonExistent world and teleports all players to the
     * spawnLocation of that world.
     *
     * @param worldName  Name of the Dungeon world(DUNGEON_unixTime)
     * @param playerList List of players going to Dungeon.
     * @since 1.0
     */
    public void loadInWorld(String worldName, List<Player> playerList, DungeonType type) {
        /*
         * Only creates a world if the contents of a world don't already exist.
		 * This method loadInWorld() is called in the actual object load().
		 */
        World w = Bukkit.getServer().createWorld(new WorldCreator(worldName));
        w.setKeepSpawnInMemory(false);
        w.setAutoSave(false);
        w.setPVP(false);
        w.setStorm(false);
        w.setMonsterSpawnLimit(300);
        w.setGameRuleValue("doFireTick", "false");
        w.setGameRuleValue("randomTickSpeed", "0");
        Bukkit.getWorlds().add(w);

        playerList.stream().forEach(player -> {
            player.teleport(w.getSpawnLocation());
            player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD + type.getBossName() + ChatColor.WHITE + "] "
                    + ChatColor.GREEN + "You have invoked a[n] Instance Dungeon. This Instance Dungeon is on "
                    + "a timer of 45 minutes!");
        });
        if (type.equals(DungeonType.BANDIT_TROVE)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                DungeonObject object = this.getDungeon(w);
                for (MobSpawner spawner : SpawningMechanics.BanditTroveSpawns) {
                    Location loc = spawner.loc;
                    loc.setWorld(w);
                    spawner.loc = loc;
                    spawner.setDungeonSpawner(true);
                    spawner.dungeonSpawn(object);
                }
                object.maxAlive = object.aliveMonsters.size();

            }, 40);
        }
    }

    /**
     * Enum type of dungeons includes, zip locations & bossNames
     *
     * @since 1.0
     */
    public enum DungeonType {
        BANDIT_TROVE("Mayel the Cruel", "/dungeons/banditTrove.zip"), VARENGLADE("Burick The Fanatic",
                "/dungeons/varenglade.zip"), THE_INFERNAL_ABYSS("The Infernal Abyss", "/dungeons/theInfernalAbyss.zip");

        /**
         * WIP Dungeons, including The Depths of Aceron and The Crimson
         * Monastery.
         */

        private String bossName;
        private String location;

        DungeonType(String bossName, String location) {
            this.bossName = bossName;
            this.location = location;
        }

        public String getBossName() {
            return bossName;
        }

        public String getLocation() {
            return location;
        }
    }

}
