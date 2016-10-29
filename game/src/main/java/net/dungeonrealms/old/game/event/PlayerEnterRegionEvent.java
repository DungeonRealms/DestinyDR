package net.dungeonrealms.old.game.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Nick on 10/24/2015.
 */
public class PlayerEnterRegionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private String region;

    public PlayerEnterRegionEvent(Player player, String region) {
        this.player = player;
        this.region = region;
    }

    public PlayerEnterRegionEvent(boolean isAsync, Player player, String region) {
        super(isAsync);
        this.player = player;
        this.region = region;
    }

    public Player getPlayer() {
        return player;
    }

    public String getRegion() {
        return region;
    }

    @Override
	public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
