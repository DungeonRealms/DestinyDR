package net.dungeonrealms.vgame.anticheat;

import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.vgame.anticheat.movement.MovementBase;
import net.dungeonrealms.vgame.anticheat.movement.watch.SpeedWatch;

/**
 * Created by Matthew E on 10/29/2016 at 10:41 AM.
 */
public class AntiCheatHandler implements SuperHandler.Handler {

    private static MovementBase movementBase;

    public static MovementBase getMovementBase() {
        return movementBase;
    }

    @Override
    public void prepare() {
        this.movementBase = new MovementBase();
        movementBase.registerMovementCheck(new SpeedWatch(movementBase));
    }
}
