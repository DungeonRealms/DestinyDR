package net.dungeonrealms.game.world.realms;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.listener.world.RealmListener;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.realms.RealmTier;


/**
 * Realm Mechanics.
 * 
 * Recoded March 24th, 2017.
 * @author Kneesnap
 */
public class Realms implements GenericMechanic {

	@Getter
    private static Realms instance = new Realms();

	//List of blocks that need to be set. (Upgrading Realms)
    private Map<UUID, List<Location>> processingBlocks = new ConcurrentHashMap<>();
    
    //Realm map.
    private Map<UUID, Realm> realms = new ConcurrentHashMap<>();
    
    //Max blocks upgraded per update.
    public static final int SERVER_BLOCK_BUFFER = 1024;
    
    //The Y position of grass.
    public static final int GRASS_POSITION = 128;
    
    //List of materials that the realm upgrader can override.
    public static final List<Material> REPLACEABLE_BLOCKS = Arrays.asList(Material.AIR, Material.BEDROCK);
    
    @Override
	public void startInitialization() {
    	Utils.log.info("[REALMS] - Initializing");

        // INITIALIZE WORK FOLDERS
        File pluginFolder = DungeonRealms.getInstance().getDataFolder();
        File rootFolder = new File(System.getProperty("user.dir"));
        File uploadingFolder = new File(pluginFolder, "/realms/uploading");
        try {
            FileUtils.forceMkdir(new File(pluginFolder, "/realms/downloaded"));
            FileUtils.forceMkdir(uploadingFolder);
        } catch (IOException e) {
            e.printStackTrace();
            Utils.log.info("Failed to create realm directories!");
        }

        Utils.log.info("[REALMS] - Fixing cached realms.");

        Arrays.stream(rootFolder.listFiles()).filter(file -> GameAPI.isUUID(file.getName())).forEach(f -> {
            try {
                FileUtils.forceDelete(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
            Utils.log.info("[REALMS] - Uploading " + uploadingFolder.listFiles().length + " cached realms.");
            
            Arrays.stream(uploadingFolder.listFiles()).filter(file -> GameAPI.isUUID(file.getName().split(".zip")[0])).forEach(f -> {
                UUID uuid = UUID.fromString(f.getName().split(".zip")[0]);
                if ((boolean) DatabaseAPI.getInstance().getData(EnumData.REALM_UPLOAD, uuid)) {
                    //Upload realm.
                	Realm.uploadZippedRealm(f, uuid.toString());
                    //Fix player data.
                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.REALM_UPLOAD, false, false);
                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.REALM_UPGRADE, false, false);
                    GameAPI.updatePlayerData(uuid);
                }
                //Remove the zip file.
                try {
                    FileUtils.forceDelete(f);
                } catch (Exception e) {
                	e.printStackTrace();
                }
            });
            
            Utils.log.info("[REALMS] - Finished uploading cached realms.");
        });

        Bukkit.getPluginManager().registerEvents(new RealmListener(), DungeonRealms.getInstance());
	}
    
    /**
     * Handles a player logout.
     */
    public void handleLogout(Player player) {
    	if(!hasRealm(player))
    		return;
    	
    	Realm realm = getRealm(player);
    	
    	//Don't do anything while this is upgrading, it's not ready yet.
    	if(realm.getState() == RealmState.UPGRADING)
    		return;

        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> realm.removePortal(ChatColor.RED + "The owner of this realm has LOGGED OUT."));
    	
    	realm.setState(RealmState.REMOVING);
    	
    	//Must run sync.
    	Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> realm.removeRealm(true), 5);
    }
    
    /**
     * Get if any realms are currently upgrading.
     */
    public boolean areRealmsUpgrading() {
    	return getRealms().stream().filter(realm -> realm.getState() == RealmState.UPGRADING).findFirst().isPresent();
    }
    
    /**
     * Saves all realms, does not remove them.
     */
    public void saveAllRealms() {
    	getRealms().forEach(realm ->
    		Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> realm.uploadRealm(false)));
    }
    
    /**
     * Removes all realms and uploads them.
     */
    public void removeAllRealms(boolean runAsync) {
       	getRealms().forEach(realm -> realm.removeRealm(runAsync));
    }
    
    /**
     * Get all realms mapped by UUIDs.
     * @return
     */
    public Map<UUID, Realm> getRealmMap() {
    	return this.realms;
    }
    
    /**
     * Gets a realm, or if it isn't cached, construct one.
     */
    public Realm getOrCreateRealm(Player player) {
    	return getOrCreateRealm(player.getName(), player.getUniqueId());
    }
    
    /**
     * Gets a realm, or if it isn't cached, construct one.
     */
    public Realm getOrCreateRealm(UUID uuid) {
    	Realm realm = getRealm(uuid);
    	return (realm != null) ? realm : getOrCreateRealm((String)DatabaseAPI.getInstance().getData(EnumData.USERNAME, uuid), uuid);
    }
    
    /**
     * Gets a realm, or if it isn't cached, construct one.
     */
    public Realm getOrCreateRealm(String name, UUID uuid) {
    	Realm realm = getRealm(uuid);
    	if(realm != null)
    		return realm;
    	
    	//Create a new realm object.
    	realm = new Realm(uuid, name);
    	getRealmMap().put(uuid, realm);
    	return realm;
    }
    /**
     * Gets a realm for a player.
     */
    public Realm getRealm(Player player) {
    	return getRealm(player.getUniqueId());
    }
    
    /**
     * Gets the realm for a player.
     */
    public Realm getRealm(UUID uuid) {
    	return getRealmMap().get(uuid);
    }
    
    /**
     * Gets a realm within two blocks of the supplied location.
     */
    public Realm getRealm(Location location) {
    	if(!GameAPI.isMainWorld(location)) {
    		GameAPI.sendDevMessage(ChatColor.DARK_RED + "[ERROR] " + ChatColor.WHITE + "Tried to find a realm in world " + location.getWorld().getName() + " on {SERVER}.");
    		Utils.log.info("Tried to load realm from " + location.getWorld().getName() + "?");
    		Utils.printTrace();
    		return null;
    	}
    	
    	for(Realm realm : getRealms())
    		if(realm.isOpen() && GameAPI.isMainWorld(realm.getPortalLocation()))
    			if(realm.getPortalLocation().distanceSquared(location) <= 2)
    				return realm;
    	return null;
    			
    }
    
    /**
     * Upgrades a realm, queues all the blocks to be updated.
     * @param realm
     * @param newTier
     */
    public void upgradeRealmBlocks(Realm realm, RealmTier newTier) {
        // Init
    	List<Location> blockList = new ArrayList<>();
        RealmTier oldTier = RealmTier.getByTier(newTier.getTier() - 1);
        int size = newTier.getDimensions();
        int oldSize = oldTier.getDimensions();
        World w = realm.getWorld();
        
        //+16 Skips chunk 0
        int limX = size + 16;
        int limZ = size + 16;

        int x, y, z;
        int limY = GRASS_POSITION - size + 1;
        int oldY = GRASS_POSITION - oldSize - 1; // Subtract an extra 1 for bedrock border area.

        // BEDROCK
        for (x = 16; x < limX; x++)
            for (z = 16; z < limZ; z++)
            	blockList.add(new Location(w, x, limY, z));

        // DIRT
        for (x = 16; x < limX; x++)
            for (y = GRASS_POSITION - 1; y > limY; y--)
                for (z = 16; z < limZ; z++) {
                    Block b = w.getBlockAt(new Location(w, x, y, z));
                    
                    // If the user placed a block here, don't override it.
                    if (!REPLACEABLE_BLOCKS.contains(b.getType()))
                    	continue;
                    
                    //If this is bedrock (Removes old bedrock layer), it's under the old Y size, and it's not in the old region area, add it to the queue.
                    if (b.getType() == Material.BEDROCK || y - 1 <= oldY || x >= oldSize || z >= oldSize)
                    	blockList.add(new Location(w, x, y, z));
                }

        // GRASS
        for (x = 16; x < limX; x++)
            for (z = 16; z < limZ; z++)
            	blockList.add(new Location(w, x, GRASS_POSITION, z));
        
        processingBlocks.put(realm.getOwner(), blockList);
    }
    
    /**
     * Does this player have a realm stored?
     */
    public boolean hasRealm(Player player) {
    	return getRealm(player) != null;
    }
    
    /**
     * Get a list of all realms.
     */
    public Collection<Realm> getRealms() {
    	return this.realms.values();
    }
    
    /**
     * Get a map of all blocks that need to be placed for realm upgrades.
     */
    public Map<UUID, List<Location>> getProcessingBlocks() {
        return this.processingBlocks;
    }
    
	@Override
	public void stopInvocation() {
		Utils.log.info("[REALM] Uploading all realms.");
        removeAllRealms(false);
        Utils.log.info("[REALM] All realms uploaded.");
	}
	
	@Override
	public EnumPriority startPriority() {
		return EnumPriority.BISHOPS;
	}
	
	/**
	 * Returns the player's realm tier. May be called by something such as ItemManager#createPortalRune, which calls before a realm object is loaded.
	 * @param uuid
	 */
	public static RealmTier getRealmTier(UUID uuid) {
		return RealmTier.getByTier((Integer)DatabaseAPI.getInstance().getData(EnumData.REALM_TIER, uuid));
	}
	
	/**
	 * Get the realm this world belongs to.
	 */
	public Realm getRealm(World world) {
		//The main world is not a realm.
		if(GameAPI.isMainWorld(world))
			return null;
		
		for(Realm realm : getRealms())
			if(world.equals(realm.getWorld()))
				return realm;
		return null;
	}
	
	/**
	 * Is a player in a realm?
	 */
	public boolean isInRealm(Player player) {
		return isRealm(player.getWorld());
	}
	
	public boolean isRealm(World world) {
		return getRealm(world) != null;
	}
	
	public boolean isRealm(Location loc) {
		return isRealm(loc.getWorld());
	}
}