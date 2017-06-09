package net.dungeonrealms.game.mechanic.dungeons.rifts;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.dungeons.*;
import net.dungeonrealms.game.mechanic.rifts.RiftPortal;
import net.dungeonrealms.game.world.entity.type.monster.boss.RiftEliteBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.lingala.zip4j.core.ZipFile;
import net.minecraft.server.v1_9_R2.EntityAreaEffectCloud;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftAreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 6/7/2017.
 */
public class EliteRift extends Dungeon {


    @Getter
    private ConcurrentHashMap<Location, Tuple<MaterialData, Long>> blockTypes = new ConcurrentHashMap<>();
    @Getter
    private ConcurrentHashMap<Player, Long> lastMovements = new ConcurrentHashMap<>();
    @Getter
    private CopyOnWriteArrayList<Entity> minions = new CopyOnWriteArrayList<>();
    @Getter
    private MapData map;
    @Setter
    private int ourTier = 1;
    @Getter
    private EliteBossType bossType;
    private int taskID = -1;
    private long lastMinionSpawn;
    @Getter
    private long startTime;
    private int totalSpawnedMinions = 0;

    private CraftAreaEffectCloud cloud;

    public EliteRift(List<Player> players) {
        super(DungeonType.ELITE_RIFT, players);
        map = MapData.values()[ThreadLocalRandom.current().nextInt(MapData.values().length)];
        //bossType = EliteBossType.values()[ThreadLocalRandom.current().nextInt(EliteBossType.values().length)];
        bossType = EliteBossType.ZONE_ONLY;
    }

    @Override
    protected void createWorld() {
        final String worldName = "DUNGEON_" + System.currentTimeMillis() + "/";
        Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
            Bukkit.getLogger().info("[Dungeons] Creating world '" + worldName + "'.");

            try {
                new ZipFile(getZipFile(map.getWorldName())).extractAll(worldName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Utils.removeFile(new File(worldName + "/uid.dat"));
            Utils.removeFile(new File(worldName + "/players"));

            Bukkit.getLogger().info("[Dungeons] Successfully created " + getType().getName() + ".");
            Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
                setupWorld(worldName);
                startDungeon();
            });
        });
    }


    public File getZipFile(String name) {
        return new File(GameAPI.getDataFolder() + "/dungeons/" + name + ".zip");
    }

    public void setTier(int tier) {
        if (tier < 1 || tier > 5) throw new IllegalArgumentException("Tier is out of range!");
    }

    private boolean canSpawnAMinion() {
        return System.currentTimeMillis() - lastMinionSpawn > 5000 && getNumberOfSpawnedMinions() < 10 && totalSpawnedMinions < 50;
    }

    public int getNumberOfSpawnedMinions() {
        int toReturn = 0;
        for (int k = 0; k < minions.size(); k++) {
            Entity ent = minions.get(k);
            if (ent == null || ent.isDead()) {
                minions.remove(k);
                continue;
            }
            toReturn++;
        }

        return toReturn;
    }

    public void spawnAMinion() {
        if (!canSpawnAMinion()) return;

        Location loc = getRandomMinionLocation();
        EnumMonster monsterType = EnumMonster.RiftMinion;
        LivingEntity le = (LivingEntity) EntityAPI.spawnCustomMonster(loc, monsterType, Utils.randInt(25, 50), ourTier, null, "Rift Minion");
        le.setRemoveWhenFarAway(false);
        le.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(60);
        minions.add(le);
        lastMinionSpawn = System.currentTimeMillis();
        totalSpawnedMinions++;
    }

    private Location getRandomMinionLocation() {
        Location center = getMap().getCenterLocation().clone();
        center.setWorld(getWorld());
        center.add(ThreadLocalRandom.current().nextInt(getMap().getMapRadius()), 0, ThreadLocalRandom.current().nextInt(getMap().getMapRadius()));
        return center;
    }

    public void tick() {
        if (getWorld() == null) return;
        spawnAMinion();
        if (bossType.equals(EliteBossType.CLEAR_FLOOR)) {
            for (Map.Entry<Location, Tuple<MaterialData, Long>> entry : blockTypes.entrySet()) {
                Location blockLoc = entry.getKey();
                Tuple<MaterialData, Long> values = entry.getValue();
                MaterialData data = values.a();
                Long time = values.b();
                if (System.currentTimeMillis() - time > 20000) {
                    Block b = getWorld().getBlockAt(blockLoc);
                    b.setType(data.getItemType());
                    b.setData(data.getData());
                    blockTypes.remove(blockLoc);
                }
            }

            if (!isFinished() && System.currentTimeMillis() - startTime > 10000) {
                RiftEliteBoss boss = (RiftEliteBoss) getBoss();
                Player target = boss.getTarget();
                if (target != null) {
                    if (target.getWorld() == getWorld()) {
                        Long lastMovement = lastMovements.get(target);
                        if (lastMovement != null && (System.currentTimeMillis() - lastMovement < 3000)) return;
                        Block bl = target.getLocation().subtract(0, 1, 0).getBlock();
                        for (int x = bl.getX() - 2; x < bl.getX() + 2; x++) {
                            for (int z = bl.getZ() - 2; z < bl.getZ() + 2; z++) {
                                Location newBlockLoc = new Location(getWorld(), x, bl.getY(), z);
                                Block newBlock = newBlockLoc.getBlock();
                                if (newBlock == null || newBlock.getType().equals(Material.STATIONARY_LAVA) || newBlock.getType().equals(Material.AIR) || newBlock.getType().equals(Material.LAVA))
                                    continue;
                                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                    MaterialData data = new MaterialData(newBlock.getType(), newBlock.getData());
                                    this.getBlockTypes().put(newBlock.getLocation(), new Tuple<>(data, System.currentTimeMillis()));
                                    newBlock.setType(Material.STATIONARY_LAVA);
                                }, 5);
                            }
                        }
                    }
                }
            }
        } else if (bossType.equals(EliteBossType.ZONE_ONLY)) {
            boolean canTeleport;
            if(cloud == null) canTeleport = true;
            else if(!cloud.hasMetadata("lastTeleport")) canTeleport = true;
            else {
                Long lastTeleport = cloud.getMetadata("lastTeleport").get(0).asLong();
                canTeleport = System.currentTimeMillis() - lastTeleport > 10000;
            }

            if(canTeleport) setCurrentAOELocation(getRandomMinionLocation());
        }
    }

    @Override
    public void addPlayer(Player player) {
        if (!this.allowedPlayers.contains(player))
            this.allowedPlayers.add(player);

        Location location = map.getSpawnLocation();
        location.setWorld(getWorld());
        PlayerWrapper pw = PlayerWrapper.getWrapper(player);
        pw.setStoredLocation(player.getLocation());
        GameAPI.teleport(player, location);
        player.setFallDistance(0F);
    }

    @Override
    public void startDungeon() {
        Location spawnLoc = map.getSpawnLocation();
        for (Player player : this.allowedPlayers) {
            PlayerWrapper pw = PlayerWrapper.getWrapper(player);
            pw.setStoredLocation(TeleportLocation.CYRENNICA.getLocation());
            GameAPI.teleport(player, new Location(getWorld(), spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ()));
            player.setFallDistance(0F);
            player.sendMessage(ChatColor.RED + getType().getBoss().getName() + "> " + ChatColor.WHITE + "How dare you enter my domain!");
        }
    }


    @Override
    public void giveShards() {
        //No shards for these rifts.
    }

    @Override
    public boolean canBossSpawn() {
        return true;
    }

    @Override
    protected void setupWorld(String worldName) {
        // Create world.
        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.generateStructures(false);
        World w = Bukkit.getServer().createWorld(worldCreator);
        w.setStorm(false);
        w.setAutoSave(false);
        w.setKeepSpawnInMemory(false);
        w.setPVP(false);
        w.setGameRuleValue("randomTickSpeed", "0");
        Bukkit.getWorlds().add(w);
        this.world = w;

        // Load spawns.
        //this.spawns = DungeonManager.getSpawns(getWorld(), getType());

        //for (MobSpawner spawn : spawns)
        //  maxMobCount += spawn.getSpawnAmount();
    }

    @Override
    public ItemWeapon getGeneralMobWeapon() {
        return (ItemWeapon) new ItemWeapon().setTier(ourTier).setRarity(Item.ItemRarity.UNIQUE);
    }

    @Override
    public void giveMount() {
        //No mount
    }

    @Override
    public ItemArmor getGeneralMobArmorSet() {
        return (ItemArmor) new ItemArmor().setTier(ourTier).setMaxRarity(Item.ItemRarity.UNIQUE, 3);
    }

    @Override
    public DungeonBoss spawnBoss(BossType type) {
        Location loc = map.getBossLocation();
        //if (bossType.equals(EliteBossType.CLEAR_FLOOR)) {
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            if (getWorld() == null) return;
            tick();
        }, 5, 5);
        //}
        return spawnBoss(type, new Location(getWorld(), loc.getX(), loc.getY(), loc.getZ()));
    }

    @Override
    public DungeonBoss spawnBoss(BossType type, Location loc) {
        // We've already spawned this boss.
        if (hasSpawned(type))
            return null;

        RiftEliteBoss boss = null;

        try {
            net.minecraft.server.v1_9_R2.World w = ((CraftWorld) getWorld()).getHandle();
            net.minecraft.server.v1_9_R2.EntityInsentient e = type.getMonster().getClazz().getDeclaredConstructor(net.minecraft.server.v1_9_R2.World.class).newInstance(w);
            e.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
            w.addEntity(e, CreatureSpawnEvent.SpawnReason.CUSTOM);
            Entity ent = e.getBukkitEntity();
            ent.teleport(loc);
            boss = (RiftEliteBoss) e;
            boss.setOurTier(ourTier);
            boss.createEntity(100);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("Failed to construct " + type.getName() + ".");
            return null;
        }

        getSpawnedBosses().add(type);
        if (type.getSound() != null)
            getWorld().playSound(loc, type.getSound(), type.getVolume(), type.getPitch());

        if (type.isFinalBoss())
            this.boss = boss;

        this.startTime = System.currentTimeMillis();
        for (Player player : getAllowedPlayers())
            player.sendMessage(ChatColor.RED + getType().getBoss().getName() + "> " + ChatColor.WHITE + "You traveled all this way just to die adventurer?!");

        return boss;
    }

    @Override
    public void removePlayers(boolean success) {
        if (!success)
            announce(ChatColor.RED + getType().getBoss().getName() + "> " + ChatColor.RESET + "You have failed, Adventurers.");
        for (Player p : getAllPlayers()) {
            GameAPI.teleport(p, TeleportLocation.CYRENNICA.getLocation());

            DungeonManager.removeDungeonItems(p);
        }

        RiftPortal associate = RiftPortal.getPortalFromDungeon(this);
        if (associate != null) {
            associate.removePortals();
        }
        if (taskID > -1) Bukkit.getScheduler().cancelTask(taskID);
    }

    @Override
    public void completeDungeon() {
        if (finished) return;
        finished = true;
        announce(ChatColor.YELLOW + "You will be teleported out in 30 seconds...");
        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> removePlayers(true), 600);
        giveShards();
        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), this::giveDrops, 5L);

        for (Map.Entry<Location, Tuple<MaterialData, Long>> entry : blockTypes.entrySet()) {
            Location blockLoc = entry.getKey();
            Tuple<MaterialData, Long> values = entry.getValue();
            MaterialData data = values.a();
            Block b = getWorld().getBlockAt(blockLoc);
            b.setType(data.getItemType());
            b.setData(data.getData());
            blockTypes.remove(blockLoc);
        }
    }


    public void setCurrentAOELocation(Location loc) {
        if (!this.bossType.equals(EliteBossType.ZONE_ONLY))
            throw new IllegalStateException("Illegal boss type for AOE!");
        if (this.cloud == null) {
            EntityAreaEffectCloud entity = new EntityAreaEffectCloud(((CraftWorld) getWorld()).getHandle());
            this.cloud = new CraftAreaEffectCloud((CraftServer) Bukkit.getServer(), entity);
            this.cloud.setDuration(Integer.MAX_VALUE);
            this.cloud.setColor(Color.RED);
            this.cloud.setRadius(5000);
            this.cloud.setParticle(Particle.REDSTONE);
            ((CraftWorld) getWorld()).getHandle().addEntity(this.cloud.getHandle(), CreatureSpawnEvent.SpawnReason.CUSTOM);
            entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            this.cloud.addCustomEffect(new PotionEffect(PotionEffectType.GLOWING,0,0),true);
            this.cloud.setRadiusPerTick(1);
            this.cloud.setRadiusOnUse(1);
            this.cloud.setBasePotionData(new PotionData(PotionType.NIGHT_VISION));
        }
        this.cloud.teleport(loc);
        this.cloud.setMetadata("lastTeleport", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
    }
}
