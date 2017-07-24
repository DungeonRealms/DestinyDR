package net.dungeonrealms.game.miscellaneous;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerShardEvent extends Event {
    private static HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Getter
    private Player player;
    public PlayerShardEvent(Player player){
        this.player = player;
    }
}
