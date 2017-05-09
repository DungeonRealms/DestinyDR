package net.dungeonrealms.game.event;

import lombok.Getter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.KarmaHandler.WorldZoneType;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Nick on 10/24/2015.
 */
public class PlayerEnterRegionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter private Player player;
    private Location oldLoc;
    private Location newLoc;

    public PlayerEnterRegionEvent(Player player, Location oldLoc, Location newLoc) {
        this.player = player;
        this.oldLoc = oldLoc;
        this.newLoc = newLoc;
    }
    
    public String getOldRegion() {
    	return GameAPI.getRegionName(oldLoc);
    }

    public String getNewRegion() {
        return GameAPI.getRegionName(newLoc);
    }
    
    public WorldZoneType getOldZone() {
    	return GameAPI.getZone(oldLoc);
    }
    
    public WorldZoneType getNewZone() {
    	return GameAPI.getZone(newLoc);
    }

    @Override
	public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
