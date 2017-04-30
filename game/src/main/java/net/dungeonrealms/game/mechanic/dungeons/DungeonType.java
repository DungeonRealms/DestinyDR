package net.dungeonrealms.game.mechanic.dungeons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
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
import org.bukkit.event.Listener;

/**
 * DungeonType - Contains data for each dungeon.
 * 
 * Redone on April 28th, 2017.
 * @author Kneesnap
 */
@AllArgsConstructor @Getter
public enum DungeonType {
    BANDIT_TROVE("Bandit Trove", "banditTrove",
    		BanditTrove.class, null, BossType.Mayel,
    		"banditTrove", "T1Dungeon", EnumMounts.WOLF,
    		1, 100, 250, 100, 250, 5000, EnumAchievements.BANDIT_TROVE),
    
    		
    VARENGLADE("Varenglade", "varenglade",
    		Varenglade.class, VarengladeListener.class, BossType.Burick,
    		"varenglade", "DODungeon", EnumMounts.SLIME,
    		3, 100, 375, 1000, 2500, 25000, EnumAchievements.VARENGLADE),
    
    THE_INFERNAL_ABYSS("Infernal Abyss", "theInfernalAbyss",
    		InfernalAbyss.class,  InfernalListener.class, BossType.InfernalAbyss,
    		"infernalAbyss", "fireydungeon", EnumMounts.SPIDER,
    		4, 150, 250, 10000, 12000, 50000, EnumAchievements.INFERNAL_ABYSS);

    private String name;
    private String path;
    private Class<? extends Dungeon> dungeonClass;
    private Class<? extends Listener> listenerClass;
    private BossType boss;
    private String worldGuardName;
    private String internalName;
    private EnumMounts mount;
    private int tier;
    private int minShards;
    private int maxShards;
    private int minGems;
    private int maxGems;
    private int XP;
    private EnumAchievements achievement;
    
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
    	return GameAPI.getTierColor(getTier()) + "" + ChatColor.BOLD + getName();
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
    	return new File(GameAPI.getDataFolder() + "/dungeons/" + getPath() + ".zip");
    }
    
    public Dungeon createDungeon() {
    	try {
    		return getDungeonClass().getDeclaredConstructor().newInstance();
    	} catch (Exception e) {
    		e.printStackTrace();
    		Bukkit.getLogger().warning("Failed to initialize " + getName() + "!");
    	}
    	
    	return null;
    }
    
    private void loadSpawnData() {
    	// Load mob spawns.
    	File f = new File(GameAPI.getDataFolder() + "/dungeonSpawns/" + getInternalName() + ".dat");
    	if (!f.exists()) {
    		Bukkit.getLogger().warning("[Dungeons] " + getInternalName() + ".dat does not exist!");
    		return;
    	}
    	
    	List<MobSpawner> spawns = new ArrayList<>();
    	try (BufferedReader br = new BufferedReader(new FileReader(f))) {
    		for (String line; (line = br.readLine()) != null;) {
    			if (!line.equalsIgnoreCase("null") && line.contains("=")) {
    				MobSpawner s = SpawningMechanics.loadSpawner(line);
    				s.setDungeon(true);
    				spawns.add(s);
    			}
    		}		
    		br.close();
    	} catch (Exception exception) {
    		exception.printStackTrace();
    	}
    	DungeonManager.getDungeonSpawns().put(this, spawns);
    }
}
