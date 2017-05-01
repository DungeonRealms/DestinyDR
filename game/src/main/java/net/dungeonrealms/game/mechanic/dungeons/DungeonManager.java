package net.dungeonrealms.game.mechanic.dungeons;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;
import net.dungeonrealms.game.listener.world.DungeonListener;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.dungeonrealms.game.world.spawning.MobSpawner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * DungeonManager - Manages Dungeons.
 * 
 * Redone on April 28th, 2017.
 * @author Kneesnap
 */
public class DungeonManager implements GenericMechanic {

	@Getter private static DungeonManager instance = new DungeonManager();
	@Getter private static List<Dungeon> dungeons = new CopyOnWriteArrayList<>();
	@Getter private static Map<DungeonType, List<MobSpawner>> dungeonSpawns = new ConcurrentHashMap<>();
	
    private static int MAX_DUNGEON_TIME = 120; // 2 Hours
    private static int EXTRA_DELAY = 10; // Give them an extra 10 minutes.
    private static int[] REMINDERS = new int[] {30, 60, 90};

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.ARCHBISHOPS;
    }

    @Override
    public void startInitialization() {
        Utils.log.info("[DUNGEONS] Loading Dungeon Mechanics ... STARTING");

        // Remove any leftover realms.
        Utils.removeFiles(GameAPI.getRoot(), file -> file.getName().contains("DUNGEON"));
        GameAPI.mkDir("dungeons");
        
        // Register dungeon listeners, and load the mob spawns.
        for (DungeonType dungeonType : DungeonType.values())
        	dungeonType.register();
        
        // Update mobs
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), () -> 
        	getDungeons().forEach(d -> d.getAliveMonsters().forEach(d::updateMob)), 200L, 10L);
        
        // Spawns dungeon mobs every few half a second.
    	Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), () -> {
    		for (Dungeon d : getDungeons()) {
    			
    			// Spawns in normal mob spawns.
    			for (MobSpawner spawner : d.getSpawns()) {
    				spawner.getLocation().setWorld(d.getWorld());
    				if (!GameAPI.arePlayersNearby(spawner.getLocation(), 50))
    					continue;
    				spawner.spawnIn();
    				spawner.getSpawnedMonsters().forEach(e -> {
    					d.getTrackedMonsters().put(e, spawner.getLocation());
    					((LivingEntity)e).setRemoveWhenFarAway(false);
    				});
    				d.getSpawns().remove(spawner);
    			}
    			
    			// Automatically spawns in bosses, if needed.
    			// Don't automatically spawn final bosses or special bosses.
    			// Since they're have spawn triggers.
    			BossType.getFor(d.getType()).stream().filter(b -> !b.isFinalBoss() && !b.isSpecial() && !d.hasSpawned(b)
    					&& GameAPI.arePlayersNearby(b.getLocation(d.getWorld()), 50)).forEach(d::spawnBoss);
    		}
    	}, 0L, 10L);

        
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), () -> getDungeons().forEach(d -> {
        	d.increaseTimer();
        	if (d.getTime() <= 10)
        		return;
        	
        	if (d.getWorld() == null) {
        		getDungeons().remove(d);
        		return;
        	}
        	
        	if (d.getAllPlayers().isEmpty()) {
        		d.remove();
        		return;
        	}
        	
        	d.attemptTaunt();
        	
        	// Give reminders.
        	for (int r : REMINDERS)
        		if (d.getTime() == r * 20 * 60)
        			d.announce(ChatColor.WHITE + "[" + ChatColor.GOLD + d.getType().getBoss().getName() + ChatColor.WHITE + "] "
        					+ ChatColor.RED + "This dungeon has reached the " + r + " minute marker!");
        	
        	int max = MAX_DUNGEON_TIME * 20 * 60;
        	if (d.getTime() == max)
        		d.announce(ChatColor.GOLD + "This dungeon has reached its time limit. It will terminate in "
        				+ ChatColor.RED + EXTRA_DELAY + ChatColor.GOLD + " minutes.");
        	
        	if (d.getTime() == max + (20 * 60 * EXTRA_DELAY))
        		d.remove();
        	
        	updateActionBar(d);
        }), 0L, 20L);
        
        Bukkit.getPluginManager().registerEvents(new DungeonListener(), DungeonRealms.getInstance());
    }

    @Override
    public void stopInvocation() {

    }
    
    public static List<MobSpawner> getSpawns(DungeonType type) {
    	return new ArrayList<>(getDungeonSpawns().get(type));
    }
    
    /**
     * Get all the dungeons of a certain type.
     */
    public static List<Dungeon> getDungeons(DungeonType type) {
    	return getDungeons().stream().filter(d -> d.getType() == type).collect(Collectors.toList());
    }
    
    public static boolean isDungeon(Entity entity) {
    	return isDungeon(entity.getWorld());
    }
    
    public static boolean isDungeon(Location l) {
    	return isDungeon(l.getWorld());
    }
    
    /**
     * Is this world a dungeon?
     */
    public static boolean isDungeon(World w) {
    	return w.getName().contains("DUNGEON") && getDungeon(w) != null;
    }
    
    /**
     * Is this world a dungeon of the specified type?
     */
    public static boolean isDungeon(World w, DungeonType type) {
    	return isDungeon(w) && getDungeon(w).getType() == type;
    }
    
    /**
     * Get the dungeon for this world.
     */
    public static Dungeon getDungeon(World world) {
        for (Dungeon dungeon : getDungeons())
            if (world.equals(dungeon.getWorld()))
                return dungeon;
        return null;
    }
    
    public static void removeDungeonItems(Player player) {
    	for (ItemStack item : player.getInventory().getContents())
    		if (ItemManager.isDungeonItem(item))
    			player.getInventory().remove(item);
    }
    
    private void updateActionBar(Dungeon dungeon) {
    	for (Player p : dungeon.getAllPlayers()) {
    		TitleAPI.sendActionBar(p, ChatColor.AQUA + "Time: " + ChatColor.GOLD
    				+ (dungeon.getTime() / 1200) + "/" + MAX_DUNGEON_TIME + " " + ChatColor.AQUA
    				+ "Alive: " + ChatColor.RED + dungeon.getKillCount() + ChatColor.GRAY + "/" + ChatColor.GRAY + dungeon.getMaxMobCount());
    	}
    }
    
    public static Dungeon createDungeon(DungeonType type, List<Player> players) {
    	if (type == null) {
    		players.forEach(p -> p.sendMessage(ChatColor.RED + "Invalid dungeon type."));
    		Utils.printTrace();
        	Bukkit.getLogger().warning("Attempted to start a dungeon without a dungeontype!");
        	return null;
        }
    	
    	if (DungeonRealms.getInstance().isEventShard) {
    		players.forEach(p -> p.sendMessage(ChatColor.RED + "You cannot enter a dungeon on this shard."));
    		return null;
    	}
    	
    	if (getDungeons().size() >= 3) {
    		players.forEach(p -> p.sendMessage(ChatColor.RED + "This shard has the max amount of dungeons open."));
    		return null;
    	}
    	
    	if (DungeonRealms.getInstance().isAlmostRestarting()) {
    		players.forEach(p -> p.sendMessage(ChatColor.RED + "This shard is restarting soon, you may not start a dungeon."));
    		return null;
    	}
    	
    	players.forEach(MountUtils::removeMount);
    	players.forEach(PetUtils::removePet);
    	
    	// This is the player who is triggered the dungeon start.
    	Player player = players.get(0);
    	
    	if (Affair.isInParty(player)) {
    		Party party = Affair.getParty(player);
    		if (party.isDungeon()) {
    			player.sendMessage(ChatColor.RED + "Your party is already inside a " + ChatColor.UNDERLINE + "different" + ChatColor.RED + " instanced dungeon.");
                player.sendMessage(ChatColor.GRAY + "You'll need to either leave your current party or wait for them to finish their run.");
                return null;
    		}
    		
    		if (!party.isOwner(player)) {
    			player.sendMessage(ChatColor.RED + "You are " + ChatColor.UNDERLINE + "NOT" + ChatColor.RED + " the party leader.");
                player.sendMessage(ChatColor.GRAY + "Only the party leader can start a new dungeon instance.");
    			return null;
    		}
    	} else {
    		Affair.createParty(player);
    	}
    	
    	players.forEach(p -> p.sendMessage(ChatColor.GRAY + "Loading Instance: '" + ChatColor.UNDERLINE + type.getDisplayName() + ChatColor.GRAY + "' -- Please wait..."));
    	Dungeon dungeon = type.createDungeon();
    	getDungeons().add(dungeon);
    	dungeon.startDungeon(players);
    	return dungeon;
    }
}
