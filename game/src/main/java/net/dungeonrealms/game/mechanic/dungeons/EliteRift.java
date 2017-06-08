package net.dungeonrealms.game.mechanic.dungeons;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.spawning.MobSpawner;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.lingala.zip4j.core.ZipFile;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 6/7/2017.
 */
public class EliteRift extends Dungeon {


    private MapData map;
    public EliteRift(List<Player> players) {
        super(DungeonType.T1_ELITE_RIFT, players);
        map = MapData.values()[ThreadLocalRandom.current().nextInt(MapData.values().length)];
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

    @Getter
    private enum MapData {
        VARENGLADE("varenglade", new Location(null,-363,59,16)),
        INFERNAL("infernalAbyss", new Location(null,-55,157,670));

        private String worldName;
        private Location spawnLocation;
        MapData(String worldName, Location spawnLocation) {
            this.worldName = worldName;
            this.spawnLocation = spawnLocation;
        }
    }

    @Override
    public void giveShards() {
        //No shards for these rifts.
    }

    @Override
    public boolean canBossSpawn() {
        return false;
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
}
