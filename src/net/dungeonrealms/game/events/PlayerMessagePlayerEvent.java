package net.dungeonrealms.game.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Nick on 11/20/2015.
 */
public class PlayerMessagePlayerEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Player sender;
    private Player receiver;
    private String message;

    public PlayerMessagePlayerEvent(Player sender, Player receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public PlayerMessagePlayerEvent(boolean isAsync, Player sender, Player receiver, String message) {
        super(isAsync);
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public Player getSender() {
        return sender;
    }

    public Player getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
