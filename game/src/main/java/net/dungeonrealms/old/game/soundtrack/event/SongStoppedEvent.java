package net.dungeonrealms.old.game.soundtrack.event;

import net.dungeonrealms.old.game.soundtrack.SongPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/2/2016
 */

public class SongStoppedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private SongPlayer song;

    public SongStoppedEvent(SongPlayer song) {
        this.song = song;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public SongPlayer getSongPlayer() {
        return song;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
