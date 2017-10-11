package net.dungeonrealms.game.quests;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class TrackedTarget {
    private final Location location;
    private final TrackType trackType;
    private Entity target;

    public TrackedTarget(final Entity e) {
        this.target = e;
        this.location = e.getLocation();
        this.trackType = TrackType.DYNAMIC;
    }

    public TrackedTarget(final Location l) {
        this.location = l;
        this.trackType = TrackType.STATIC;
    }

    public Entity getTarget() {
        return this.target;
    }

    public Location getLocation() {
        if (this.trackType == TrackType.DYNAMIC) {
            return this.target.getLocation();
        }
        return this.location;
    }

    public TrackType getType() {
        return this.trackType;
    }

    public enum TrackType {
        DYNAMIC,
        STATIC;
    }
}