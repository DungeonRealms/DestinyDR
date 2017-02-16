package net.dungeonrealms.game.mechanic;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.util.AsyncUtils;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityStats;
import net.dungeonrealms.game.world.realms.instance.RealmInstance;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.dungeonrealms.game.world.spawning.dungeons.DungeonMobCreator;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.minecraft.server.v1_9_R2.Entity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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

    private CopyOnWriteArrayList<DungeonObject> Dungeons = new CopyOnWriteArrayList<>();
    public static volatile ConcurrentHashMap<String, HashMap<Location, String>> instance_mob_spawns = new ConcurrentHashMap<>();

    public Map<UUID, Location> TRACKED_SPAWNS = new WeakHashMap<>();

    @Getter
    private ConcurrentHashMap<String, Integer> players_Entering_Dungeon = new ConcurrentHashMap<>();
    @Getter
    private ConcurrentHashMap<String, Integer> dungeon_Wither_Effect = new ConcurrentHashMap<>();
    @Getter
    private Set<Entity> fireUnderEntity = new CopyOnWriteArraySet<>();

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

    public static void sendStaffAlert(World world, String msg){
        for(Player player : world.getPlayers()){
            if(Rank.isTrialGM(player)){
                player.sendMessage(msg);
            }
        }
        Bukkit.getLogger().info("Dungeon Message: " + msg);

    }

    @Override
    public void startInitialization() {
        Utils.log.info("[DUNGEONS] Loading Dungeon Mechanics ... STARTING");

        File rootFolder = new File(System.getProperty("user.dir"));
        Arrays.stream(rootFolder.listFiles())
                .filter(file -> file.getName().contains("DUNGEON")).forEach(f -> {
            try {
                FileUtils.forceDelete(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        try {
            FileUtils.forceMkdir(new File(DungeonRealms.getInstance().getDataFolder() + File.separator + "/dungeons/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (Map.Entry<String, Integer> entry : players_Entering_Dungeon.entrySet())
                if (entry.getValue() > 1) players_Entering_Dungeon.put(entry.getKey(), (entry.getValue() - 1));
                else players_Entering_Dungeon.remove(entry.getKey());
        }, 100L, 20L);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> Dungeons.forEach(dungeon -> dungeon.aliveMonsters.forEach(mob -> {
            if (mob != null) {
                if (mob.locY < 90 && dungeon.getType().equals(DungeonType.THE_INFERNAL_ABYSS))

                    if (TRACKED_SPAWNS.containsKey(mob.getUniqueID())) {
                        Location location = TRACKED_SPAWNS.get(mob.getUniqueID());
                        mob.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    }

                if (!mob.isAlive() || mob.dead) {
                    dungeon.aliveMonsters.remove(mob);
                    dungeon.killed = dungeon.killed + 1;
                }
            }
        })), 200L, 10L);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (Entity entity : fireUnderEntity) {
                if (!entity.isAlive()) {
                    fireUnderEntity.remove(entity);
                    return;
                }
                if (!entity.getBukkitEntity().isOnGround()) {
                    return;
                }
                Location location = entity.getBukkitEntity().getLocation();
                if (location.getBlock().getType() == Material.AIR) {
                    location.getBlock().setType(Material.FIRE);
                }
                if (new Random().nextInt(20) == 0) {
                    net.minecraft.server.v1_9_R2.World world = entity.getWorld();
                    net.minecraft.server.v1_9_R2.Entity toSpawn = SpawningMechanics.getMob(world, 3, EnumMonster.MagmaCube);
                    int level = Utils.getRandomFromTier(3, "low");
                    String newLevelName = ChatColor.AQUA.toString() + "[Lvl. " + level + "] ";
                    EntityStats.createDungeonMob(toSpawn, level, 3);
                    SpawningMechanics.rollElement(toSpawn, EnumMonster.MagmaCube);
                    if (toSpawn == null) {
                        return; //WTF?? UH OH BOYS WE GOT ISSUES
                    }
                    toSpawn.setCustomName(newLevelName + GameAPI.getTierColor(3).toString() + "Spawn of Inferno");
                    toSpawn.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(3).toString() + "Spawn of Inferno"));
                    Location toSpawnLoc = new Location(world.getWorld(), location.getX(), location.getY() + 2, location.getZ());
                    entity.setLocation(toSpawnLoc.getX(), toSpawnLoc.getY(), toSpawnLoc.getZ(), 1, 1);
                    world.addEntity(toSpawn, CreatureSpawnEvent.SpawnReason.CUSTOM);
                    entity.setLocation(toSpawnLoc.getX(), toSpawnLoc.getY(), toSpawnLoc.getZ(), 1, 1);
                }
            }
        }, 0L, 5L);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> Dungeons.forEach(dungeonObject -> {
            if (dungeonObject.getTime() > 10) {
                if (dungeonObject.getType() == DungeonType.THE_INFERNAL_ABYSS) {
                    Bukkit.getWorld(dungeonObject.worldName).getPlayers().stream().filter(player -> player.hasPotionEffect(PotionEffectType.WITHER)).forEach(player -> {
                        player.getActivePotionEffects().stream().filter(potionEffect -> potionEffect.getType().getName().equals(PotionEffectType.WITHER.getName())).filter(potionEffect ->
                                !(dungeon_Wither_Effect.containsKey(player.getWorld().getName()))).forEach(potionEffect -> {
                                    dungeon_Wither_Effect.put(player.getWorld().getName(), (potionEffect.getDuration() / 20) - 1);
                        });
                    });
                }
            }

            for (Map.Entry<String, Integer> entry : dungeon_Wither_Effect.entrySet()) {
                String worldName = entry.getKey();
                if (Bukkit.getServer().getWorld(worldName) == null) {
                    dungeon_Wither_Effect.remove(worldName);
                    continue;
                }
                int secondsLeft = entry.getValue();

                secondsLeft--;

                if (secondsLeft == 30) {
                    for (Player pl : Bukkit.getServer().getWorld(worldName).getPlayers()) {
                        pl.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + ">> " + ChatColor.RED + "You have " + ChatColor.UNDERLINE
                                + secondsLeft + "s" + ChatColor.RED + " left until the inferno consumes you.");
                    }
                } else if (secondsLeft <= 1) {
                    for (Player pl : Bukkit.getServer().getWorld(worldName).getPlayers()) {
                        pl.setHealth(1);
                        HealthHandler.getInstance().setPlayerHPLive(pl, 1);
                        pl.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "You have been drained of nearly all your life by the power of the inferno.");
                        pl.playSound(pl.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 2, 1.3F);
                        pl.removePotionEffect(PotionEffectType.WITHER);
                    }
                    dungeon_Wither_Effect.remove(worldName);
                    continue;
                }

                dungeon_Wither_Effect.put(worldName, secondsLeft);
            }
            //Needs to be once a second..
        }), 200L, 20);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> Dungeons.forEach(dungeonObject -> {
            int time = dungeonObject.getTime();
            dungeonObject.modifyTime(1);
            if (time < 10) {
                return;
            }
            if (Bukkit.getWorld(dungeonObject.getWorldName()) == null) {
                Dungeons.remove(dungeonObject);
                return;
            }

            if (Bukkit.getWorld(dungeonObject.worldName).getPlayers().size() <= 0) {
                removeInstance(dungeonObject);
                return;
            }

            if (dungeonObject.triedTeleportingOut) {
                dungeonObject.teleportPlayersOut(true);
                return;
            }

            int monstersAlive = dungeonObject.maxAlive - dungeonObject.killed;
            int maxAlive = dungeonObject.maxAlive;
            if (!dungeonObject.canSpawnBoss && maxAlive > 0 && monstersAlive > 0) {
                if (monstersAlive <= (maxAlive * 0.2)) {
                    dungeonObject.canSpawnBoss = true;
                    Bukkit.getWorld(dungeonObject.getWorldName()).getPlayers().forEach(player -> {
                        if (player != null) {
                            if (GameAPI.getGamePlayer(player).isInDungeon()) {
                                player.sendMessage(ChatColor.RED.toString() + dungeonObject.type.getBossName() + ChatColor.RESET + ": Do you really wish to fight me?");
                            }
                        }
                    });
                }
            }
            switch (time) {
                // 2h 10 minutes
                case 7500:
                    removeInstance(dungeonObject);
                    break;
                // 2h
                case 7200:
                    Bukkit.getWorld(dungeonObject.getWorldName()).getPlayers().forEach(player -> {
                        if (player != null) {
                            if (GameAPI.getGamePlayer(player).isInDungeon()) {
                                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD
                                        + dungeonObject.type.getBossName() + ChatColor.WHITE + "]" + " " + ChatColor.RED
                                        + "This instance has reached it's max threshold, it will now terminate in (10) minutes.");
                            }
                        }
                    });
                    break;
                // 1h30 minutes
                case 5400:
                    Bukkit.getWorld(dungeonObject.getWorldName()).getPlayers().forEach(player -> {
                        if (player != null) {
                            if (GameAPI.getGamePlayer(player).isInDungeon()) {
                                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD
                                        + dungeonObject.type.getBossName() + ChatColor.WHITE + "]" + " " + ChatColor.RED
                                        + "This instance has reached (90) minute marker!");
                            }
                        }
                    });
                    break;
                // 1h
                case 3600:
                    Bukkit.getWorld(dungeonObject.getWorldName()).getPlayers().forEach(player -> {
                        if (player != null) {
                            if (GameAPI.getGamePlayer(player).isInDungeon()) {
                                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD
                                        + dungeonObject.type.getBossName() + ChatColor.WHITE + "]" + " " + ChatColor.RED
                                        + "This instance has reached (60) minute marker!");
                            }
                        }
                    });
                    break;
                // 30 minutes
                case 1800:
                    Bukkit.getWorld(dungeonObject.getWorldName()).getPlayers().forEach(player -> {
                        if (player != null) {
                            if (GameAPI.getGamePlayer(player).isInDungeon()) {
                                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD
                                        + dungeonObject.type.getBossName() + ChatColor.WHITE + "]" + " " + ChatColor.RED
                                        + "This instance has reached (30) minute marker!");
                            }
                        }
                    });
                    break;
                // 15 minutes
                case 900:
                    Bukkit.getWorld(dungeonObject.getWorldName()).getPlayers().forEach(player -> {
                        if (player != null) {
                            if (GameAPI.getGamePlayer(player).isInDungeon()) {
                                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD
                                        + dungeonObject.type.getBossName() + ChatColor.WHITE + "]" + " " + ChatColor.RED
                                        + "This instance has reached (15) minute marker!");
                            }
                        }
                    });
                    break;
            }
            updateDungeonBoard(dungeonObject);
        }), 0L, 20L);
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
    private void updateDungeonBoard(DungeonObject dungeonObject) {
        Bukkit.getWorld(dungeonObject.getWorldName()).getPlayers().forEach(player -> {
            if (player != null) {
                if (GameAPI.getGamePlayer(player) != null) {
                    if (GameAPI.getGamePlayer(player).isInDungeon()) {
                        TitleAPI.sendActionBar(player, ChatColor.AQUA + "Time: " + ChatColor.WHITE + ChatColor.GOLD
                                + String.valueOf(dungeonObject.getTime() / 60) + "/120" + " " + ChatColor.AQUA + "Alive: " + ChatColor.WHITE + (dungeonObject.maxAlive - dungeonObject.killed) + ChatColor.GRAY
                                + "/" + ChatColor.RED + dungeonObject.maxAlive);
                    }
                }
            }
        });
    }

    /**
     * Removes the instance dungeon from EVERYTHING.
     *
     * @param dungeonObject The dungeon object.
     * @since 1.0
     */
    public void removeInstance(DungeonObject dungeonObject) {
        if (CrashDetector.crashDetected) return;

        World dungeon = Bukkit.getWorld(dungeonObject.getWorldName());
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            dungeon.getPlayers().forEach(player -> {
                if (player != null) if (Bukkit.getPlayer(player.getUniqueId()) != null)
                    if (GameAPI.getGamePlayer(player) != null) if (GameAPI.getGamePlayer(player).isInDungeon()) {
                        DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 1800);
                        player.sendMessage(ChatColor.RED.toString() + dungeonObject.type.getBossName() + ChatColor.RESET + ": You have failed, Adventurers.");
                        player.teleport(Teleportation.Cyrennica);
                        for (ItemStack stack : player.getInventory().getContents())
                            if (stack != null && stack.getType() != Material.AIR) if (isDungeonItem(stack))
                                player.getInventory().remove(stack);
                    }
            });
            Bukkit.getWorlds().remove(Bukkit.getWorld(dungeonObject.getWorldName()));
            Utils.log.info("[DUNGEONS] Removing world: " + dungeonObject.getWorldName() + " from worldList().");

            if (dungeonObject.isEditMode()) {
                //Remove entities so they are not saved.
                for (org.bukkit.entity.Entity ent : dungeon.getEntities()) {
                    if (!(ent instanceof Player)) {
                        ent.remove();
                    }
                }

                for(Chunk loaded : dungeon.getLoadedChunks()){
                    loaded.unload(true);
                }

                Bukkit.unloadWorld(dungeonObject.getWorldName(), true);
                try {
                    Bukkit.getLogger().info("Saving dungeon " + dungeonObject.instanceName + " from " + dungeonObject.worldName + " Exempt: " + dungeonObject.worldName);
                    RealmInstance.getInstance().zip(dungeonObject.worldName + "/", "plugins/DungeonRealms" + dungeonObject.getType().getLocation());
                    Bukkit.getLogger().info("Dungeon saved.");
                } catch (Exception e) {
                    Bukkit.getLogger().info("Error saving dungeon to zip file: " + dungeonObject.instanceName + " World name: " + dungeonObject.worldName);
                    e.printStackTrace();
                }
            } else {
                Bukkit.unloadWorld(dungeonObject.getWorldName(), false);
            }
            Utils.log.info("[DUNGEONS] Unloading world: " + dungeonObject.getWorldName() + " in preparation for deletion!");
            Bukkit.getScheduler().cancelTask(dungeonObject.spawningTaskID);

            GameAPI.submitAsyncCallback(() -> {
                deleteFolder(new File(dungeonObject.worldName));
                deleteFolder(new File("plugins/WorldGuard/worlds/" + dungeonObject.worldName));
                return true;
            }, consumer -> {
                if (Dungeons.contains(dungeonObject)) {
                    dungeonObject.cleanup();
                    Dungeons.remove(dungeonObject);
                }

                Utils.log.info("[DUNGEONS] Deleted world: " + dungeonObject.getWorldName() + " final stage.");
            });
        });
    }

    public boolean isAllOppedPlayers(World world) {
        boolean allOps = false;
        for (Player pl : world.getPlayers()) {
            if (!pl.isOp()) {
                return false;
            }
            allOps = true;
        }
        return allOps;
    }

    /**
     * @param type       DungeonType
     * @param playerList List of players to enter!
     * @since 1.0
     */
    public DungeonObject createNewInstance(DungeonType type, Map<Player, Boolean> playerList, String instanceName) {
        if (!DungeonRealms.getInstance().isAlmostRestarting()) {
            if (!instance_mob_spawns.containsKey(instanceName)) {
                loadDungeonMobSpawns(instanceName);
            }
            DungeonObject dungeonObject = new DungeonObject(type, 0, playerList, "DUNGEON_" + String.valueOf(System.currentTimeMillis() / 1000L), instanceName);
            Dungeons.add(dungeonObject);
            dungeonObject.load();
            return dungeonObject;
        } else {
            for (Player player : playerList.keySet()) {
                player.sendMessage(ChatColor.RED + "You can't enter a dungeon if the shard is almost restarting");
            }
        }
        return null;
    }

    public boolean canCreateInstance() {
        //TODO: Increase on non US-1 shards.
        return Dungeons.size() < 3;
    }

    /**
     * Will extract a players realm .zip to the correct folder.
     *
     * @param zipFile
     * @since 1.0
     */
    private void unZip(ZipFile zipFile, DungeonObject dungeonObject) {
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
        private Map<Player, Boolean> playerList;
        private String worldName;
        public CopyOnWriteArrayList<Entity> aliveMonsters = new CopyOnWriteArrayList<>();
        public boolean canSpawnBoss = false;
        public int tier;
        public int maxAlive = 0;
        public int killed = 0;
        private ConcurrentHashMap<Entity, Location> toSpawn = new ConcurrentHashMap<>();
        String instanceName;
        int spawningTaskID;
        public boolean beingRemoved;
        public boolean hasBossSpawned;
        public int keysDropped;
        public boolean triedTeleportingOut;

        @Getter
        @Setter
        public boolean editMode = false;

        DungeonObject(DungeonType type, Integer time, Map<Player, Boolean> playerList, String worldName, String instanceName) {
            this.type = type;
            this.time = time;
            this.playerList = playerList;
            this.worldName = worldName;
            this.instanceName = instanceName;
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

        public Map<Player, Boolean> getPlayerList() {
            return playerList;
        }

        public String getWorldName() {
            return worldName;
        }

        public void modifyTime(int second) {
            time += second;
        }

        void load() {
            AsyncUtils.pool.submit(() -> {
                try {
                    unZip(new ZipFile(DungeonRealms.getInstance().getDataFolder() + type.getLocation()), this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                loadInWorld(getWorldName(), getPlayerList(), getType());
            }, 20L);
        }

        void cleanup() {
            playerList.clear();
            aliveMonsters.clear();
            toSpawn.clear();
        }

        /**
         *
         */
        public void teleportPlayersOut(boolean secondTry) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                if (Bukkit.getWorld(worldName) == null) return;
                Bukkit.getWorld(worldName).getPlayers().stream().filter(p -> p != null && p.isOnline()).forEach(player -> {
                    if (GameAPI.getGamePlayer(player) != null && GameAPI.getGamePlayer(player).isInDungeon()) {
                        switch (getType()) {
                            case BANDIT_TROVE:
                                Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.BANDIT_TROVE);
                                break;
                            case VARENGLADE:
                                Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.VARENGLADE);
                                break;
                            case THE_INFERNAL_ABYSS:
                                Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.INFERNAL_ABYSS);
                                break;
                            default:
                                break;
                        }
                        //No dungeons for next 30mins
                        DungeonManager.getInstance().getPlayers_Entering_Dungeon().put(player.getName(), 1800);
                        player.teleport(Teleportation.Cyrennica);
                        triedTeleportingOut = true;
                        for (ItemStack stack : player.getInventory().getContents()) {
                            if (stack != null && stack.getType() != Material.AIR) {
                                if (isDungeonItem(stack)) {
                                    player.getInventory().remove(stack);
                                }
                            }
                        }
                    }
                });
            }, 30 * 20L);
            if (!secondTry)
                Bukkit.getWorld(worldName).getPlayers().stream().filter(p -> p != null && p.isOnline()).forEach(p -> p.sendMessage(ChatColor.YELLOW + "You will be teleported out in 30 seconds..."));
        }

        /**
         *
         */
        public void giveShards() {
            int shardsToGive = 100;

            switch (tier) {
                case 1:
                    shardsToGive = 100 + new Random().nextInt(150);
                    break;
                case 2:
                    shardsToGive = 900 + new Random().nextInt(300);
                    break;
                case 3:
                    shardsToGive = 100 + new Random().nextInt(275);
                    break;
                case 4:
                    //150 - 200 points.
                    shardsToGive = 150 + new Random().nextInt(100);
                    break;
                case 5:
                    shardsToGive = 1500 + new Random().nextInt(1000);
                    break;
            }

            for (Player p : Bukkit.getWorld(worldName).getPlayers()) {
                p.sendMessage(GameAPI.getTierColor(tier) + "You have gained " + ChatColor.UNDERLINE + shardsToGive
                        + " Portal Shards" + GameAPI.getTierColor(tier) + " for completing this Dungeon.");
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
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                teleportPlayersOut(true);
                if (Dungeons.contains(DungeonManager.getInstance().getDungeon(Bukkit.getWorld(worldName)))) {
                    DungeonManager.getInstance().getDungeon(Bukkit.getWorld(worldName)).cleanup();
                    Dungeons.remove(DungeonManager.getInstance().getDungeon(Bukkit.getWorld(worldName)));
                }
            }, 1500L);
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
    private void loadInWorld(String worldName, Map<Player, Boolean> playerList, DungeonType type) {
        /*
         * Only creates a world if the contents of a world don't already exist.
		 * This type loadInWorld() is called in the actual object load().
		 */
        AsyncUtils.pool.submit(() -> {
            if (new File(worldName + "/" + "uid.dat").exists()) {
                // Delete that shit.
                new File(worldName + "/" + "uid.dat").delete();
            }
            deleteFolder(new File(worldName + "/players"));
            try {
                FileUtils.copyDirectory(new File("plugins/WorldGuard/worlds/" + type.getWorldGuardName()), new File("plugins/WorldGuard/worlds/" + worldName));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Utils.log.info("Completed setup of Dungeon: " + worldName);
        });

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            WorldCreator worldCreator = new WorldCreator(worldName);
            worldCreator.generateStructures(false);
            World w = Bukkit.getServer().createWorld(worldCreator);
            w.setStorm(false);
            w.setAutoSave(false);
            w.setKeepSpawnInMemory(false);
            w.setPVP(false);
            w.setGameRuleValue("randomTickSpeed", "0");
            Bukkit.getWorlds().add(w);

            if (!instance_mob_spawns.containsKey(this.getDungeon(w).instanceName)) {
                loadDungeonMobSpawns(this.getDungeon(w).instanceName);
            }
            DungeonObject object = this.getDungeon(w);
            object.spawningTaskID = Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                net.minecraft.server.v1_9_R2.World world = ((CraftWorld) w).getHandle();
                object.toSpawn = DungeonMobCreator.getEntitiesToSpawn(object.instanceName, w);
                object.maxAlive = object.toSpawn.size();
                Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                    for (Map.Entry<Entity, Location> entry : object.toSpawn.entrySet()) {
                        Location location = entry.getValue();
                        location.setWorld(w);
                        if (GameAPI.arePlayersNearby(location, 50)) {
                            final Entity entity = entry.getKey();
                            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                            world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                            TRACKED_SPAWNS.put(entity.getUniqueID(), location);
                            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                            entity.setCustomNameVisible(true);
                            if (entity.isAlive()) {
                                object.aliveMonsters.add(entity);
                                object.toSpawn.remove(entity);
                            }
                        }
                    }
                }, 0L, 10L);
            }, 60L);

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> playerList.keySet().forEach(player -> {
                if (playerList.get(player)) {
                    String locationAsString = "-367,86,390,0,0"; // Cyrennica
                    if (player.getWorld().equals(Bukkit.getWorlds().get(0))) {
                        locationAsString = player.getLocation().getX() + "," + (player.getLocation().getY() + 0.5) + "," + player.getLocation().getZ() + "," + player.getLocation().getYaw() + "," + player.getLocation().getPitch();
                    }
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.CURRENT_LOCATION, locationAsString, true);
                    player.teleport(w.getSpawnLocation());
                    player.setFallDistance(0F);
                    if (object.getType() == DungeonType.THE_INFERNAL_ABYSS) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            sendWorldEnvironment(player, World.Environment.NETHER);
                        }, 5L);
                    }
                    player.sendMessage(ChatColor.RED.toString() + object.type.getBossName() + ChatColor.RESET + ": How dare you enter my domain!");
                } else {
                    player.sendMessage(ChatColor.LIGHT_PURPLE.toString() + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + ">" + ChatColor.GRAY + " "
                            + "Your party has started the " + ChatColor.LIGHT_PURPLE + ChatColor.UNDERLINE + object.getType().name().replaceAll("_", " ") + ChatColor.RESET + ChatColor.GRAY + " Dungeon.");
                    if (GameAPI.isInSafeRegion(player.getLocation())) {
                        player.sendMessage(ChatColor.GRAY + "Due to your location, you can join them instantly via" + ChatColor.GREEN + ChatColor.UNDERLINE + "/djoin");
                    }
                    //Player isn't nearby so shouldn't be teleported (BUT) can still enter the dungeon at a later date.
                }
            }), 150L);
        }, 20L);
    }

    private void loadDungeonMobSpawns(String instanceName) {
        for (File file : new File("plugins/DungeonRealms/dungeonSpawns/").listFiles()) {
            String fileName = file.getName().replaceAll(".dat", "");
            if (fileName.equalsIgnoreCase(instanceName)) {
                DungeonRealms.getInstance().getLogger().info("Found Dungeon Spawn Template for " + instanceName);
                HashMap<Location, String> dungeonMobData = new HashMap<>();
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    for (String line; (line = br.readLine()) != null; ) {
                        if (line.equalsIgnoreCase("null")) {
                            continue;
                        }
                        if (line.contains("=")) {
                            String[] coordinates = line.split("=")[0].split(",");
                            Location location = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]),
                                    Double.parseDouble(coordinates[2]));
                            String spawnData = line.split("=")[1];
                            dungeonMobData.put(location, spawnData);
                        }
                    }
                    br.close();
                    instance_mob_spawns.put(instanceName, dungeonMobData);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    public void sendWorldEnvironment(Player player, World.Environment environment) {
        return;
        //TODO: Fix.
        /*CraftPlayer craftPlayer = (CraftPlayer) player;
        CraftWorld world = (CraftWorld) player.getWorld();
        Location location = player.getLocation();

        PacketPlayOutRespawn packet = new PacketPlayOutRespawn(environment.getId(), EnumDifficulty.getById(world.getDifficulty().getValue()), net.minecraft.server.v1_9_R2.WorldType.NORMAL, WorldSettings.EnumGamemode.getById(player.getGameMode().getValue()));

        craftPlayer.getHandle().playerConnection.sendPacket(packet);

        int viewDistance = 8;

        int xMin = location.getChunk().getX() - viewDistance;
        int xMax = location.getChunk().getX() + viewDistance;
        int zMin = location.getChunk().getZ() - viewDistance;
        int zMax = location.getChunk().getZ() + viewDistance;

        for (int x = xMin; x < xMax; ++x) {
            for (int z = zMin; z < zMax; ++z) {
                world.refreshChunk(x, z);
            }
        }

        player.updateInventory();*/
    }


    private void deleteFolder(File folder) {
        try {
            if (folder == null || !folder.exists()) return;
            FileUtils.forceDelete(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isDungeonItem(ItemStack stack) {
        if (stack != null && stack.getType() != Material.AIR && stack.hasItemMeta() && stack.getItemMeta().hasLore()) {
            List<String> itemLore = stack.getItemMeta().getLore();
            for (String string : itemLore) {
                if (string.toLowerCase().contains("dungeon item")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Enum type of dungeons includes, zip locations & bossNames
     *
     * @since 1.0
     */
    public enum DungeonType {
        BANDIT_TROVE("Mayel the Cruel", "/dungeons/banditTrove.zip", "banditTrove"),
        VARENGLADE("Burick The Fanatic", "/dungeons/varenglade.zip", "varenglade"),
        THE_INFERNAL_ABYSS("The Infernal Abyss", "/dungeons/theInfernalAbyss.zip", "infernalAbyss");

        /**
         * WIP Dungeons, including The Depths of Aceron and The Crimson
         * Monastery.
         */

        private String bossName;
        private String location;
        private String worldGuardName;

        DungeonType(String bossName, String location, String worldGuardName) {
            this.bossName = bossName;
            this.location = location;
            this.worldGuardName = worldGuardName;
        }

        public String getBossName() {
            return bossName;
        }

        public String getLocation() {
            return location;
        }

        public String getWorldGuardName() {
            return worldGuardName;
        }
    }

}
