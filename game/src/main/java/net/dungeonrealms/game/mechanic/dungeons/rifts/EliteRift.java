package net.dungeonrealms.game.mechanic.dungeons.rifts;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.dungeons.*;
import net.dungeonrealms.game.world.entity.type.monster.boss.RiftEliteBoss;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.spawning.MobSpawner;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.lingala.zip4j.core.ZipFile;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.material.MaterialData;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
    private MapData map;
    @Setter
    private int ourTier = 1;
    @Getter
    private EliteBossType bossType;
    private int taskID = -1;
    @Getter
    private long startTime;
    public EliteRift(List<Player> players) {
        super(DungeonType.ELITE_RIFT, players);
        map = MapData.values()[ThreadLocalRandom.current().nextInt(MapData.values().length)];
        bossType = EliteBossType.values()[ThreadLocalRandom.current().nextInt(EliteBossType.values().length)];
        bossType = EliteBossType.CLEAR_FLOOR;
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
        if(tier < 1 || tier > 5) throw new IllegalArgumentException("Tier is out of range!");
    }

    public void tick() {
        if(getWorld() == null) return;
        if(bossType.equals(EliteBossType.CLEAR_FLOOR)) {
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

            if(System.currentTimeMillis() - startTime > 10000) {
                for (Player player : this.allowedPlayers) {
                    if (player.getWorld() != getWorld()) continue;
                    Long lastMovement = lastMovements.get(player);
                    if (lastMovement != null && (System.currentTimeMillis() - lastMovement < 3000)) continue;
                    Block bl = player.getLocation().subtract(0, 1, 0).getBlock();
                    if(bl == null || bl.getType().equals(Material.STATIONARY_LAVA) || bl.getType().equals(Material.AIR) || bl.getType().equals(Material.LAVA)) continue;
                    MaterialData data = new MaterialData(bl.getType(), bl.getData());
                    this.getBlockTypes().put(bl.getLocation(), new Tuple<>(data, System.currentTimeMillis()));
                    bl.setType(Material.AIR);
                }
            }
        }
    }

    @Override
    public void startDungeon() {
        Location spawnLoc = map.getSpawnLocation();
        for (Player player : this.allowedPlayers) {
            PlayerWrapper pw = PlayerWrapper.getWrapper(player);
            pw.setStoredLocation(TeleportLocation.CYRENNICA.getLocation());
            GameAPI.teleport(player, new Location(getWorld(), spawnLoc.getX(),spawnLoc.getY(),spawnLoc.getZ()));
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
        if(bossType.equals(EliteBossType.CLEAR_FLOOR)) {
            taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                if(getWorld() == null) return;
                tick();
            },5,5);
        }
        return spawnBoss(type, new Location(getWorld(),loc.getX(),loc.getY(),loc.getZ()));
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
        if(taskID > -1)Bukkit.getScheduler().cancelTask(taskID);
    }
}
