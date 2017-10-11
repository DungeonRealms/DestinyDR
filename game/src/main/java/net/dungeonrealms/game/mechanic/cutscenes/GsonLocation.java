package net.dungeonrealms.game.mechanic.cutscenes;

import lombok.Getter;
import org.bukkit.Location;

/*
  So gson can nicely translate this for us.
 */
@Getter
public class GsonLocation {

    private String worldName;
    private double x, y, z;
    float yaw, pitch;

    public GsonLocation(Location loc) {
        this.worldName = loc.getWorld().getName();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
    }
}
