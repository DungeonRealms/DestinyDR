package net.dungeonrealms.game.listener.world;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.event.PlayerEnterRegionEvent;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.mechanic.dungeons.DungeonType;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DungeonListener - Listens for basic dungeon events.
 * 
 * Redone on April 28th, 2017.
 * @author Kneesnap
 */
public class DungeonListener implements Listener {
	
	//TODO: Mounts -> Database
	//TODO: Mob nametag stuff.
	// Lootchest particles better.
	//TODO: Metadata
	//TODO: Write a .item converter.
	//TODO: Prevent using items while sharding.
	//TODO: Integrate new drops to dungeons.
	//TODO: Finish support tools
	//TODO: Convert all toggles to toggles.
	
	
    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeaveDungeon(PlayerEnterRegionEvent evt) {
    	if (!DungeonManager.isDungeon(evt.getPlayer()) || !evt.getNewRegion().toLowerCase().startsWith("exit_instance"))
    		return;
    	
    	Player player = evt.getPlayer();
    	player.teleport(TeleportLocation.CYRENNICA.getLocation());
    	
    	Affair.getParty(player).announce(player.getName() + " has left the dungeon.");
    }
    
    /**
     * Handles a player entering a dungeon.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerAttemptDungeonEnter(PlayerEnterRegionEvent event) {
    	if (!GameAPI.isMainWorld(event.getPlayer()) || !event.getNewRegion().toLowerCase().startsWith("instance_"))
            return;
        Player player = event.getPlayer();
        List<Player> players = Affair.isInParty(player) ? Affair.getParty(player).getAllMembers().stream().filter(p -> p.getLocation().distanceSquared(player.getLocation()) <= 200).collect(Collectors.toList())
        		: Arrays.asList(player);
        DungeonManager.createDungeon(DungeonType.getInternal(event.getNewRegion().split("_")[1]), players);
    }

    /**
     * Prevents opening hoppers.
     */
    @EventHandler
    public void onHopperInteract(PlayerInteractEvent evt) {
    	if (!evt.hasBlock() || evt.getClickedBlock().getType() != Material.HOPPER || !DungeonManager.isDungeon(evt.getClickedBlock().getLocation()))
    		return;
    	if (!Rank.isGM(evt.getPlayer()))
    		evt.setCancelled(true);
    }
}
