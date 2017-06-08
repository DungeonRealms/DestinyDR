package net.dungeonrealms.game.mechanic.dungeons;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.ShardTier;
import net.dungeonrealms.game.mechanic.dungeons.InfernalAbyss.InfernalListener;
import net.dungeonrealms.game.mechanic.dungeons.Varenglade.VarengladeListener;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.spawning.MobSpawner;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DungeonType - Contains data for each dungeon.
 * <p>
 * Redone on April 28th, 2017.
 *
 * @author Kneesnap
 */
@AllArgsConstructor @Getter
public enum DungeonType {
    BANDIT_TROVE("Bandit Trove", "banditTrove", "t1dungeon", StatColumn.BOSS_KILLS_MAYEL,
            BanditTrove.class, null, EnumMounts.WOLF,
            1, 100, 250, 100, 250, 5000, EnumAchievements.BANDIT_TROVE,
            l(BossType.Mayel, BossType.Pyromancer)),

    VARENGLADE("Varenglade", "varenglade", "dodungeon", StatColumn.BOSS_KILLS_BURICK,
            Varenglade.class, VarengladeListener.class, EnumMounts.SLIME,
            3, 100, 375, 1000, 2500, 25000, EnumAchievements.VARENGLADE,
            l(BossType.Burick, BossType.BurickPriest)),

    THE_INFERNAL_ABYSS("Infernal Abyss", "infernalAbyss", "fireydungeon", StatColumn.BOSS_KILLS_INFERNALABYSS,
            InfernalAbyss.class, InfernalListener.class, EnumMounts.SPIDER,
            4, 150, 250, 10000, 12000, 50000, EnumAchievements.INFERNAL_ABYSS,
            l(BossType.InfernalAbyss, BossType.InfernalGhast, BossType.InfernalGuard)),

    ELITE_RIFT("Elite Rift", "eliteRift", "riftdungeon", StatColumn.T1_MOB_KILLS, EliteRift.class, null, null,1,0,0,0,0,1000,null,l(BossType.RiftEliteBoss));

    private String name;
    private String internalName;
    private String legacyName;
    private StatColumn stat;
    private Class<? extends Dungeon> dungeonClass;
    private Class<? extends Listener> listenerClass;
    private EnumMounts mount;
    private int tier;
    private int minShards;
    private int maxShards;
    private int minGems;
    private int maxGems;
    private int XP;
    private EnumAchievements achievement;
    private BossType[] bosses;
    
    /**
     * Get the final boss for this dungeon.
     */
    public BossType getBoss() {
    	return Arrays.stream(getBosses()).filter(BossType::isFinalBoss).findAny().orElse(null);
    }

    /**
     * Gets a random number of gems in the allowed range.
     */
    public int getGems() {
        return Utils.randInt(minGems, maxGems);
    }

    /**
     * Returns the name of the dungeon with color applied.
     */
    public String getDisplayName() {
        return GameAPI.getTierColor(getTier()).toString() + ChatColor.BOLD + getName();
    }

    public ShardTier getShardTier() {
        return ShardTier.getByTier(getTier());
    }

    /**
     * Register the listener for this dungeon, if any.
     * Should only be called on startup.
     */
    public void register() {
        loadSpawnData();
        // Register Bukkit Listener, if any.
        if (getListenerClass() == null)
            return;

        try {
            Bukkit.getPluginManager().registerEvents(getListenerClass().getDeclaredConstructor().newInstance(), DungeonRealms.getInstance());
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("Failed to create Dungeon Listener for " + getName());
        }
    }

    public File getZipFile() {
        return new File(GameAPI.getDataFolder() + "/dungeons/" + getInternalName() + ".zip");
    }

    public Dungeon createDungeon(List<Player> players) {
        try {
            return getDungeonClass().getDeclaredConstructor(List.class).newInstance(players);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("Failed to initialize " + getName() + "!");
        }

        return null;
    }

    private void loadSpawnData() {
        if(this.equals(DungeonType.T1_ELITE_RIFT) || this.equals(DungeonType.T2_ELITE_RIFT) || this.equals(DungeonType.T3_ELITE_RIFT) || this.equals(DungeonType.T4_ELITE_RIFT) || this.equals(DungeonType.T5_ELITE_RIFT)) return;
        // Load mob spawns.
        File f = new File(GameAPI.getDataFolder() + "/dungeonSpawns/" + getInternalName() + ".dat");
        if (!f.exists()) {
            Bukkit.getLogger().warning("[Dungeons] " + getInternalName() + ".dat does not exist!");
            return;
        }


        List<MobSpawner> spawns = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            for (String line; (line = br.readLine()) != null; ) {
                if (!line.contains("="))
                    continue;
                MobSpawner s = SpawningMechanics.loadSpawner(line);
                s.setDungeon(true);
                spawns.add(s);
            }
            br.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            Bukkit.getLogger().warning("Failed to parse mob spawns for " + f.getName() + ".");
        }
        Bukkit.getLogger().warning("[Dungeons] Loaded spawns for " + getInternalName() + ".dat!");

        DungeonManager.getDungeonSpawns().put(this, spawns);
    }

    public static DungeonType getInternal(String internalName) {
        for (DungeonType d : values())
            if (d.getInternalName().equalsIgnoreCase(internalName) || d.getLegacyName().equalsIgnoreCase(internalName))
                return d;
        return null;
    }

    private static BossType[] l(BossType... a) {
    	return a;
    }
}
