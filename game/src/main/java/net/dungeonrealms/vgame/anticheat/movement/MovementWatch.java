package net.dungeonrealms.vgame.anticheat.movement;

import net.dungeonrealms.vgame.anticheat.Watch;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Copyright © 2016 Matthew E Development - All Rights Reserved
 * You may NOT use, distribute and modify this code.
 * <p>
 * Created by Matthew E on 10/30/2016 at 11:58 AM.
 */
public abstract class MovementWatch extends Watch implements Listener {

    private MovementBase movementBase;

    public MovementWatch(MovementBase movementBase) {
        this.movementBase = movementBase;
    }

    public abstract void onPlayerMove(PlayerMoveEvent event);

    public MovementBase getMovementBase() {
        return movementBase;
    }
}
