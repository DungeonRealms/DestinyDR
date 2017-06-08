package net.dungeonrealms.game.mechanic.dungeons;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.item.Item;
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
    private int tier = 1;
    public EliteRift(List<Player> players) {
        super(DungeonType.ELITE_RIFT, players);
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

    public void setTier(int tier) {
        if(tier < 1 || tier > 5) throw new IllegalArgumentException("Tier is out of range!");
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
        VARENGLADE("varenglade", new Location(null,-363,59,16),new Location(null,-363,59,-12)),
        INFERNAL("infernalAbyss", new Location(null,-55,157,670),new Location(null,-55,157,647));

        private String worldName;
        private Location spawnLocation;
        private Location bossLocation;
        MapData(String worldName, Location spawnLocation, Location bossLocation) {
            this.worldName = worldName;
            this.spawnLocation = spawnLocation;
            this.bossLocation = bossLocation;
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
        return (ItemWeapon) new ItemWeapon().setTier(tier).setRarity(Item.ItemRarity.UNIQUE);
    }

    @Override
    public void giveMount() {
        //No mount
    }

    @Override
    public ItemArmor getGeneralMobArmorSet() {
        return (ItemArmor) new ItemArmor().setTier(tier).setMaxRarity(Item.ItemRarity.UNIQUE, 3);
    }

    @Override
    public DungeonBoss spawnBoss(BossType type) {
        Location loc = map.getBossLocation();
        return spawnBoss(type, new Location(getWorld(),loc.getX(),loc.getY(),loc.getZ()));
    }

    @Override
    public void removePlayers(boolean success) {
        if (!success)
            announce(ChatColor.RED + getType().getBoss().getName() + "> " + ChatColor.RESET + "You have failed, Adventurers.");
        for (Player p : getAllPlayers()) {
            GameAPI.teleport(p, TeleportLocation.CYRENNICA.getLocation());

            DungeonManager.removeDungeonItems(p);
        }
    }
}
